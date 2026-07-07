package com.bwg.channel.backend.systemsvc.referencedata.controller;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionReqDto;
import com.bwg.channel.backend.systemsvc.referencedata.dto.ReferenceDataVersionResDto;
import com.bwg.channel.backend.systemsvc.referencedata.service.ReferenceDataVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 기준정보 최신 버전 조회 API 컨트롤러.
 */
@Tag(name = "기준정보-버전")
@RestController
@RequiredArgsConstructor
@RequestMapping("/reference-data/versions")
public class ReferenceDataVersionController {

    private final ReferenceDataVersionService referenceDataVersionService;

    @Operation(
            summary = "기준정보 최신 버전 조회",
            description = "data.refType이 ALL이면 전체 기준정보 최신 버전을 조회하고, 그 외에는 해당 기준정보 유형만 조회한다."
    )
    @PostMapping("/latest")
    public ApiResponse<List<ReferenceDataVersionResDto>> getLatestReferenceDataVersions(
            @RequestBody ApiRequest<ReferenceDataVersionReqDto> req
    ) {
        // 기준정보 유형 기준 최신 버전 조회를 서비스에 위임
        return referenceDataVersionService.getLatestReferenceDataVersions(req);
    }
}
