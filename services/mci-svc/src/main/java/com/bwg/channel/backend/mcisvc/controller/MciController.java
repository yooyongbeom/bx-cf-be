package com.bwg.channel.backend.mcisvc.controller;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.mcicommon.domain.MciRequest;
import com.bwg.channel.backend.mcicommon.domain.MciResponse;
import com.bwg.channel.backend.mcicommon.router.MciRouter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * MCI 거래 실행 API.
 * <p>
 * 채널 backend는 표준 MciRequest를 보내고, mci-svc가 거래 코드 기준으로 고객사 adapter/mapper를 선택한다.
 */
@Tag(name = "MCI")
@RestController
@RequiredArgsConstructor
public class MciController {

    /** 표준 거래 요청을 adapter/mapper 체인으로 실행하는 router. */
    private final MciRouter mciRouter;

    /**
     * MCI 표준 거래를 실행한다.
     *
     * @param request 표준 헤더와 업무 payload를 포함한 요청
     * @return 공통 ApiResponse로 감싼 MCI 표준 응답
     */
    @Operation(summary = "MCI 거래 실행", description = "거래 코드 기준으로 mapper와 adapter를 선택해 고객사 시스템을 호출한다.")
    @PostMapping("/execute")
    public ApiResponse<MciResponse<Object>> execute(@RequestBody MciRequest<Map<String, Object>> request) {
        // 외부 응답 포맷은 기존 서비스와 동일하게 ApiResponse로 감싸고, 내부 payload만 MciResponse 표준을 유지한다.
        return ApiResponse.success(mciRouter.execute(request));
    }
}
