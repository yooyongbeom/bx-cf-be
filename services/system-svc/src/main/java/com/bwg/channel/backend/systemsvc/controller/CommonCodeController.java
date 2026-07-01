package com.bwg.channel.backend.systemsvc.controller;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeReqDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeResDto;
import com.bwg.channel.backend.systemsvc.service.CommonCodeService;
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
 * 공통코드 그룹과 코드 관리 API 컨트롤러. (요청 {@code *ReqDto} / 응답 {@code *ResDto}를 그대로 사용)
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
        return commonCodeService.getCommonCodeGroups();
    }

    @Operation(summary = "공통코드 그룹 등록")
    @PostMapping("/groups/create")
    public ApiResponse<Void> createCommonCodeGroup(@RequestBody CommonCodeGroupReqDto req) {
        return commonCodeService.createCommonCodeGroup(req);
    }

    @Operation(summary = "공통코드 그룹 수정")
    @PostMapping("/groups/{groupCd}/update")
    public ApiResponse<Void> updateCommonCodeGroup(
            @PathVariable String groupCd,
            @RequestBody CommonCodeGroupReqDto req
    ) {
        return commonCodeService.updateCommonCodeGroup(groupCd, req);
    }

    @Operation(summary = "공통코드 목록 조회")
    @PostMapping("/groups/{groupCd}/codes/list")
    public ApiResponse<List<CommonCodeResDto>> getCommonCodes(@PathVariable String groupCd) {
        return commonCodeService.getCommonCodes(groupCd);
    }

    @Operation(summary = "공통코드 등록")
    @PostMapping("/groups/{groupCd}/codes/create")
    public ApiResponse<Void> createCommonCode(
            @PathVariable String groupCd,
            @RequestBody CommonCodeReqDto req
    ) {
        return commonCodeService.createCommonCode(groupCd, req);
    }

    @Operation(summary = "공통코드 수정")
    @PostMapping("/groups/{groupCd}/codes/{code}/update")
    public ApiResponse<Void> updateCommonCode(
            @PathVariable String groupCd,
            @PathVariable String code,
            @RequestBody CommonCodeReqDto req
    ) {
        return commonCodeService.updateCommonCode(groupCd, code, req);
    }
}
