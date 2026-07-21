package com.bwg.channel.backend.systemsvc.commoncode.controller;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.commoncode.service.CommonCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 공통코드 그룹과 공통코드 관리 API 컨트롤러.
 */
@Tag(name = "기준정보-공통코드")
@RestController
@RequiredArgsConstructor
@RequestMapping("/common-codes")
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @Operation(summary = "공통코드 그룹 목록 조회")
    @PostMapping("/groups/list")
    public ApiResponse<List<CommonCodeGroupResDto>> getCommonCodeGroups() {
        // 공통코드 그룹 목록 조회를 서비스에 위임
        return commonCodeService.getCommonCodeGroups();
    }

    @Operation(summary = "공통코드 그룹 등록")
    @PostMapping("/groups/create")
    public ApiResponse<Void> createCommonCodeGroup(@RequestBody ApiRequest<CommonCodeGroupReqDto> req) {
        // 공통코드 그룹 등록 요청을 서비스에 위임
        return commonCodeService.createCommonCodeGroup(req);
    }

    @Operation(summary = "공통코드 그룹 수정")
    @PostMapping("/groups/{groupCd}/update")
    public ApiResponse<Void> updateCommonCodeGroup(
            @PathVariable String groupCd,
            @RequestBody ApiRequest<CommonCodeGroupReqDto> req
    ) {
        // 경로 변수와 요청 본문을 함께 서비스에 전달
        return commonCodeService.updateCommonCodeGroup(groupCd, req);
    }

    @Operation(summary = "공통코드 목록 조회")
    @PostMapping("/groups/{groupCd}/codes/list")
    public ApiResponse<List<CommonCodeResDto>> getCommonCodes(@PathVariable String groupCd) {
        // 그룹 코드 기준 공통코드 목록 조회를 서비스에 위임
        return commonCodeService.getCommonCodes(groupCd);
    }

    @Operation(
            summary = "공통코드 그룹 상세 및 코드 목록 조회",
            description = "data.groupCd가 ALL이면 전체 그룹과 하위 공통코드 목록을 조회하고, 그 외에는 해당 그룹만 조회한다."
    )
    @PostMapping("/groups/detail")
    public ApiResponse<List<CommonCodeGroupDetailResDto>> getCommonCodeGroupDetails(
            @RequestBody ApiRequest<CommonCodeGroupReqDto> req
    ) {
        // 요청 그룹 코드 기준 공통코드 그룹과 상세 코드 목록 조회를 서비스에 위임
        return commonCodeService.getCommonCodeGroupDetails(req);
    }

    @Operation(summary = "공통코드 등록")
    @PostMapping("/groups/{groupCd}/codes/create")
    public ApiResponse<Void> createCommonCode(
            @PathVariable String groupCd,
            @RequestBody ApiRequest<CommonCodeReqDto> req
    ) {
        // 그룹 코드와 요청 본문을 함께 서비스에 전달
        return commonCodeService.createCommonCode(groupCd, req);
    }

    @Operation(summary = "공통코드 수정")
    @PostMapping("/groups/{groupCd}/codes/{code}/update")
    public ApiResponse<Void> updateCommonCode(
            @PathVariable String groupCd,
            @PathVariable String code,
            @RequestBody ApiRequest<CommonCodeReqDto> req
    ) {
        // 그룹 코드와 공통코드 기준 수정 요청을 서비스에 위임
        return commonCodeService.updateCommonCode(groupCd, code, req);
    }

    @Operation(summary = "공통코드 일괄 교체")
    @PostMapping("/groups/{groupCd}/codes/replace")
    public ApiResponse<Void> replaceCommonCodes(
            @PathVariable String groupCd,
            @RequestBody ApiRequest<CommonCodeReplaceReqDto> req
    ) {
        // 경로의 그룹 코드와 교체할 전체 코드 목록을 서비스에 함께 전달한다.
        return commonCodeService.replaceCommonCodes(groupCd, req);
    }
}
