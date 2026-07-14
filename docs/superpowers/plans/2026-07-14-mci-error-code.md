# MCI Error Code Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace predictable MCI `-9999` responses with typed `-6xxx` error codes while preserving the common `ApiResponse` contract.

**Architecture:** Add an `MciErrorCode` enum implementing the existing `BwgErrorCode` contract, convert `MciException` into a typed `BwgException`, and map each `MciRouter` validation/configuration failure to a specific code. The existing global REST advice remains unchanged and returns the typed code and HTTP status.

**Tech Stack:** Java 21, Spring Boot, Gradle, JUnit 5, AssertJ, Mockito, Spring MockMvc

## Global Constraints

- MCI owns the `-6001` through `-6999` range.
- `-9999` remains reserved for unclassified common server failures.
- Adapter and Mapper implementation names must not be returned in client messages.
- Do not change `GlobalRestExceptionAdvice` or the `ApiResponse` JSON shape.
- Do not classify Adapter call timeout/upstream failures until the SPI exposes a typed failure contract.

---

### Task 1: Define the MCI error-code contract

**Files:**
- Create: `libs/mci-common/src/main/java/com/bwg/channel/backend/mcicommon/constants/MciErrorCode.java`
- Create: `libs/mci-common/src/test/java/com/bwg/channel/backend/mcicommon/constants/MciErrorCodeContractTest.java`

**Interfaces:**
- Consumes: `BwgErrorCode#getCode()`, `getMsg()`, and `getStatus()` from `libs:common`.
- Produces: `MciErrorCode` with `fromCode(String)` for use by `MciException` and `MciRouter`.

- [ ] **Step 1: Write the failing enum contract test**

```java
class MciErrorCodeContractTest {
    @Test
    void mciErrorCodesUseMciRangeAndExpectedStatuses() {
        assertThat(Arrays.stream(MciErrorCode.values()))
                .allSatisfy(errorCode -> assertThat(errorCode.getCode()).startsWith("-6"));
        assertThat(MciErrorCode.REQUEST_HEADER_REQUIRED.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(MciErrorCode.TRANSACTION_NOT_REGISTERED.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(MciErrorCode.TRANSACTION_DISABLED.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(MciErrorCode.CHANNEL_NOT_ALLOWED.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(MciErrorCode.ADAPTER_NOT_REGISTERED.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void mciErrorCodesAreUniqueAndResolvable() {
        long uniqueCount = Arrays.stream(MciErrorCode.values())
                .map(MciErrorCode::getCode)
                .distinct()
                .count();
        assertThat(uniqueCount).isEqualTo(MciErrorCode.values().length);
        assertThat(MciErrorCode.fromCode("-6101")).isEqualTo(MciErrorCode.TRANSACTION_NOT_REGISTERED);
        assertThatThrownBy(() -> MciErrorCode.fromCode("-6998"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```powershell
.\gradlew.bat :libs:mci-common:test --tests "*MciErrorCodeContractTest"
```

Expected: compilation fails because `MciErrorCode` does not exist.

- [ ] **Step 3: Implement the enum**

```java
package com.bwg.channel.backend.mcicommon.constants;

import com.bwg.channel.backend.common.constants.error.BwgErrorCode;
import org.springframework.http.HttpStatus;

public enum MciErrorCode implements BwgErrorCode {
    REQUEST_HEADER_REQUIRED("-6001", "MCI request header is required", HttpStatus.BAD_REQUEST),
    TRANSACTION_CODE_REQUIRED("-6002", "MCI transaction code is required", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_REGISTERED("-6101", "MCI transaction is not registered", HttpStatus.NOT_FOUND),
    TRANSACTION_DISABLED("-6102", "MCI transaction is disabled", HttpStatus.SERVICE_UNAVAILABLE),
    CHANNEL_NOT_ALLOWED("-6103", "MCI channel is not allowed", HttpStatus.FORBIDDEN),
    ADAPTER_NOT_REGISTERED("-6201", "MCI adapter configuration is invalid", HttpStatus.INTERNAL_SERVER_ERROR),
    MAPPER_NOT_REGISTERED("-6202", "MCI mapper configuration is invalid", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVER_ERROR("-6999", "MCI server internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String msg;
    private final HttpStatus status;

    MciErrorCode(String code, String msg, HttpStatus status) {
        this.code = code;
        this.msg = msg;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    public static MciErrorCode fromCode(String code) {
        for (MciErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Unknown MciErrorCode: " + code);
    }
}
```

- [ ] **Step 4: Run the focused test and verify GREEN**

Run:

```powershell
.\gradlew.bat :libs:mci-common:test --tests "*MciErrorCodeContractTest"
```

Expected: all `MciErrorCodeContractTest` tests pass.

- [ ] **Step 5: Commit the contract**

```powershell
git add libs/mci-common/src/main/java/com/bwg/channel/backend/mcicommon/constants/MciErrorCode.java libs/mci-common/src/test/java/com/bwg/channel/backend/mcicommon/constants/MciErrorCodeContractTest.java
git commit -m "feat: MCI 오류 코드 계약 추가"
```

### Task 2: Propagate typed MCI exceptions through the router

**Files:**
- Modify: `libs/mci-common/src/main/java/com/bwg/channel/backend/mcicommon/exception/MciException.java`
- Modify: `libs/mci-common/src/main/java/com/bwg/channel/backend/mcicommon/router/MciRouter.java`
- Create: `libs/mci-common/src/test/java/com/bwg/channel/backend/mcicommon/exception/MciExceptionTest.java`
- Modify: `libs/mci-common/src/test/java/com/bwg/channel/backend/mcicommon/router/MciRouterTest.java`
- Create: `services/mci-svc/src/test/java/com/bwg/channel/backend/mcisvc/controller/MciControllerExceptionTest.java`

**Interfaces:**
- Consumes: `MciErrorCode` from Task 1 and `BwgException.Builder` from `libs:common`.
- Produces: `MciException.of(MciErrorCode)` and `MciException.of(MciErrorCode, Map<String, Object>)`; router failures expose `MciErrorCode` through `getCode()`.

- [ ] **Step 1: Write and run the failing HTTP response regression test**

Use a real router with an empty registry so the test exercises the original `MciException` path rather than mocking the desired exception:

```java
package com.bwg.channel.backend.mcisvc.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bwg.channel.backend.common.aop.GlobalRestExceptionAdvice;
import com.bwg.channel.backend.mcicommon.router.MciRouter;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

class MciControllerExceptionTest {
    private final MciRouter mciRouter = new MciRouter(
            code -> Optional.empty(),
            List.of(),
            List.of()
    );
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new MciController(mciRouter))
            .setControllerAdvice(new GlobalRestExceptionAdvice())
            .build();

    @Test
    void returnsTypedMciCodeAndStatusForUnknownTransaction() throws Exception {
        mockMvc.perform(post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"header\":{\"transactionCode\":\"UNKNOWN\",\"channelCode\":\"WEB\"},\"data\":{}}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("-6101"))
                .andExpect(jsonPath("$.msg").value("MCI transaction is not registered"));
    }
}
```

Run:

```powershell
.\gradlew.bat :services:mci-svc:test --tests "*MciControllerExceptionTest"
```

Expected: assertion failure because the current response is HTTP 500 with code `-9999`.

- [ ] **Step 2: Write the failing typed-exception test**

```java
package com.bwg.channel.backend.mcicommon.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.bwg.channel.backend.mcicommon.constants.MciErrorCode;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MciExceptionTest {
    @Test
    void keepsTypedCodeSafeMessageAndDetails() {
        MciException exception = MciException.of(
                MciErrorCode.ADAPTER_NOT_REGISTERED,
                Collections.singletonMap("adapterName", "internalAdapter")
        );

        assertThat(exception.getCode()).isEqualTo(MciErrorCode.ADAPTER_NOT_REGISTERED);
        assertThat(exception.getMessage()).isEqualTo(MciErrorCode.ADAPTER_NOT_REGISTERED.getMsg());
        assertThat(exception.getMessage()).doesNotContain("internalAdapter");
        assertThat(exception.getDetails()).containsEntry("adapterName", "internalAdapter");
    }
}
```

- [ ] **Step 3: Add router regression tests for all seven existing failure paths**

Add focused assertions using the real router:

```java
assertMciCode(() -> emptyRouter().execute(null), MciErrorCode.REQUEST_HEADER_REQUIRED);
assertMciCode(() -> emptyRouter().execute(request("", "WEB")), MciErrorCode.TRANSACTION_CODE_REQUIRED);
assertMciCode(() -> emptyRouter().execute(request("UNKNOWN", "WEB")), MciErrorCode.TRANSACTION_NOT_REGISTERED);
assertMciCode(() -> router(disabledDefinition(), List.of(), List.of()).execute(request("CIF001", "WEB")), MciErrorCode.TRANSACTION_DISABLED);
assertMciCode(() -> router(enabledDefinition(), List.of(), List.of()).execute(request("CIF001", "BATCH")), MciErrorCode.CHANNEL_NOT_ALLOWED);
assertMciCode(() -> router(enabledDefinition(), List.of(), List.of(new PassThroughMapper())).execute(request("CIF001", "WEB")), MciErrorCode.ADAPTER_NOT_REGISTERED);
assertMciCode(() -> router(enabledDefinition(), List.of(new SampleAdapter()), List.of()).execute(request("CIF001", "WEB")), MciErrorCode.MAPPER_NOT_REGISTERED);
```

Use this shared helper to assert both exception type and exact code:

```java
private void assertMciCode(ThrowingCallable invocation, MciErrorCode expectedCode) {
    assertThatThrownBy(invocation)
            .isInstanceOf(MciException.class)
            .satisfies(error -> assertThat(((MciException) error).getCode()).isEqualTo(expectedCode));
}
```

- [ ] **Step 4: Run the focused tests and verify RED**

Run:

```powershell
.\gradlew.bat :libs:mci-common:test --tests "*MciExceptionTest" --tests "*MciRouterTest"
```

Expected: compilation fails because the typed factory and `MciErrorCode` router mappings are not implemented.

- [ ] **Step 5: Convert `MciException` to a typed `BwgException`**

```java
package com.bwg.channel.backend.mcicommon.exception;

import com.bwg.channel.backend.common.exception.BwgException;
import com.bwg.channel.backend.mcicommon.constants.MciErrorCode;
import java.util.Collections;
import java.util.Map;

public class MciException extends BwgException {
    private static final long serialVersionUID = 1L;

    private MciException(Builder builder) {
        super(builder);
    }

    public static MciException of(MciErrorCode code) {
        return of(code, Collections.emptyMap());
    }

    public static MciException of(MciErrorCode code, Map<String, Object> details) {
        return new Builder().code(code).message(code.getMsg()).details(details).build();
    }

    @Override
    public MciErrorCode getCode() {
        return (MciErrorCode) super.getCode();
    }

    public static class Builder extends BwgException.Builder<Builder> {
        public Builder code(MciErrorCode code) {
            super.code(code);
            return this;
        }

        @Override
        public MciException build() {
            return new MciException(this);
        }
    }
}
```

- [ ] **Step 6: Map every router failure to its exact code**

Replace the seven throw sites with these exact mappings:

```java
private MciHeader validateAndGetHeader(MciRequest<?> request) {
    if (request == null || request.getHeader() == null) {
        throw MciException.of(MciErrorCode.REQUEST_HEADER_REQUIRED);
    }
    MciHeader header = request.getHeader();
    if (!hasText(header.getTransactionCode())) {
        throw MciException.of(MciErrorCode.TRANSACTION_CODE_REQUIRED);
    }
    return header;
}

private TransactionDefinition findEnabledDefinition(MciHeader header) {
    TransactionDefinition definition = transactionRegistry.findByCode(header.getTransactionCode())
            .orElseThrow(() -> MciException.of(
                    MciErrorCode.TRANSACTION_NOT_REGISTERED,
                    Collections.singletonMap("transactionCode", header.getTransactionCode())
            ));
    if (!definition.isEnabled()) {
        throw MciException.of(
                MciErrorCode.TRANSACTION_DISABLED,
                Collections.singletonMap("transactionCode", header.getTransactionCode())
        );
    }
    return definition;
}

private void validateChannel(MciHeader header, TransactionDefinition definition) {
    if (definition.getChannels() == null || definition.getChannels().isEmpty()) {
        return;
    }
    if (!definition.getChannels().contains(header.getChannelCode())) {
        throw MciException.of(
                MciErrorCode.CHANNEL_NOT_ALLOWED,
                Collections.singletonMap("channelCode", header.getChannelCode())
        );
    }
}

private MciAdapter findAdapter(String adapterName) {
    if (!hasText(adapterName) || !adapters.containsKey(adapterName)) {
        throw MciException.of(
                MciErrorCode.ADAPTER_NOT_REGISTERED,
                Collections.singletonMap("adapterName", adapterName)
        );
    }
    return adapters.get(adapterName);
}

private MciMapper findMapper(String mapperName, String role) {
    if (!hasText(mapperName) || !mappers.containsKey(mapperName)) {
        throw MciException.of(
                MciErrorCode.MAPPER_NOT_REGISTERED,
                Map.of("mapperName", String.valueOf(mapperName), "role", role)
        );
    }
    return mappers.get(mapperName);
}
```

Add `MciErrorCode`, `Collections`, and `Map` imports as required. Keep Adapter/Mapper names only in `details`, never in `message`.

- [ ] **Step 7: Run the focused tests and verify GREEN**

Run:

```powershell
.\gradlew.bat :libs:mci-common:test --tests "*MciExceptionTest" --tests "*MciRouterTest"
.\gradlew.bat :services:mci-svc:test --tests "*MciControllerExceptionTest"
```

Expected: all typed exception and router tests pass.

- [ ] **Step 8: Commit typed exception propagation**

```powershell
git add libs/mci-common/src/main/java/com/bwg/channel/backend/mcicommon/exception/MciException.java libs/mci-common/src/main/java/com/bwg/channel/backend/mcicommon/router/MciRouter.java libs/mci-common/src/test/java/com/bwg/channel/backend/mcicommon/exception/MciExceptionTest.java libs/mci-common/src/test/java/com/bwg/channel/backend/mcicommon/router/MciRouterTest.java services/mci-svc/src/test/java/com/bwg/channel/backend/mcisvc/controller/MciControllerExceptionTest.java
git commit -m "fix: MCI 라우팅 예외 코드 세분화"
```

### Task 3: Document the range and run full verification

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: the MCI error-code contract and response regression tests from Tasks 1 and 2.
- Produces: documented code ownership and fresh module/service verification evidence.

- [ ] **Step 1: Add the MCI range to README**

Add this row to the error-code table:

```markdown
| MCI | `-6001` ~ `-6999` | 요청 검증, 거래 라우팅, Adapter/Mapper 구성 오류 |
```

- [ ] **Step 2: Run module and service verification**

Run:

```powershell
.\gradlew.bat :libs:mci-common:test :services:mci-svc:test
```

Expected: both Gradle tasks complete successfully with zero failed tests.

- [ ] **Step 3: Run compile/build verification for dependent modules**

Run:

```powershell
.\gradlew.bat :libs:mci-common:build :services:mci-svc:build
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit documentation**

```powershell
git add README.md
git commit -m "docs: MCI 오류 코드 대역 추가"
```
