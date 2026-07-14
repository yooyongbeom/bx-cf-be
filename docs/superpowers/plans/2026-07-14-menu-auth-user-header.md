# Menu Auth User Header Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep menu audit actors out of request payload DTOs and obtain them from the Gateway-provided `X-Auth-User` header for every menu write API.

**Architecture:** Add `security-common` as a compile-only dependency to `system-svc` so controllers use `InternalAuthHeaders.USER` without importing security beans at runtime. Controllers pass the header value explicitly through the service to existing repository actor parameters, and the service validates it before any write.

**Tech Stack:** Java 21, Spring Boot 3.3.4, Spring MVC, MyBatis, JUnit 5, AssertJ, Mockito, Gradle

## Global Constraints

- `MenuCreateReqDto`, `MenuUpdateReqDto`, and `RoleMenuSaveReqDto` must not expose `createdBy`.
- `POST /menus/create`, `POST /menus/{menuId}/update`, `POST /menus/{menuId}/delete`, and `POST /menus/roles/{roleId}/save` require `X-Auth-User`.
- Use `InternalAuthHeaders.USER` from `security-common` for the header name.
- The service must reject a blank actor with `REQUIRED_VALUE_MISSING` before repository writes.
- Database and reference-data audit fields use the validated header value.

---

### Task 1: Controller Header Contract

**Files:**
- Modify: `services/system-svc/build.gradle`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/controller/MenuController.java`
- Test: `services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/controller/SystemControllerMappingTests.java`

**Interfaces:**
- Consumes: `InternalAuthHeaders.USER`
- Produces: write controller methods with a `String userId` parameter passed to `MenuService`

- [x] Add mapping tests that require `@RequestHeader(InternalAuthHeaders.USER)` on all four menu write methods.
- [x] Run the focused controller test and verify it fails against the current no-header methods.
- [x] Add compile-only `security-common` dependencies and controller header parameters.
- [x] Run the focused controller test and verify it passes.

### Task 2: Service Actor Flow

**Files:**
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/service/MenuService.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/service/MenuServiceImpl.java`
- Test: `services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/service/MenuServiceTests.java`

**Interfaces:**
- Consumes: `String userId` supplied by each controller write method
- Produces: validated actor passed to repository writes and reference-data version history

- [x] Change service tests to call each write method with `hong.gildong`, verify that exact actor reaches repository/version methods, and verify blank actors fail before writes.
- [x] Run the focused service test and verify it fails against the hardcoded `admin` implementation.
- [x] Remove `DEFAULT_ACTOR`, add actor parameters, validate with `BusinessValidator.requireNonBlank`, and pass the result to repository/version methods.
- [x] Run the focused service test and verify it passes.

### Task 3: Regression Verification

**Files:**
- Modify: `docs/superpowers/specs/2026-07-13-menu-crud-dto-delete-design.md`
- Modify: `docs/superpowers/plans/2026-07-13-menu-crud-dto-delete.md`

**Interfaces:**
- Consumes: final controller and service contracts
- Produces: documentation matching the Gateway header policy

- [x] Update the design notes to state that all menu writes use `X-Auth-User` and payload DTOs have no actor.
- [x] Run `bash gradlew :services:system-svc:test` and require `BUILD SUCCESSFUL`.
- [x] Run `git diff --check` and require exit code 0.
