package com.bwg.channel.backend.systemsvc.controller;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeDto;
import com.bwg.channel.backend.systemsvc.domain.dto.CommonCodeGroupDto;
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
 * 공통코드 그룹과 코드 관리 API를 제공하는 컨트롤러.
 */
@Tag(name = "기준정보-공통코드")
@RestController
@RequiredArgsConstructor
@RequestMapping("/common-codes")
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    /**
     * 등록된 공통코드 그룹 목록을 조회한다.
     */
    @Operation(summary = "공통코드 그룹 목록 조회")
    @PostMapping("/groups/list")
    public ApiResponse<List<CommonCodeGroupDto>> getCommonCodeGroups() {
        return commonCodeService.getCommonCodeGroups();
    }

    /**
     * 신규 공통코드 그룹을 등록한다.
     */
    @Operation(summary = "공통코드 그룹 등록")
    @PostMapping("/groups/create")
    public ApiResponse<Void> createCommonCodeGroup(@RequestBody CommonCodeGroupDto paramDto) {
        return commonCodeService.createCommonCodeGroup(paramDto);
    }

    /**
     * 경로의 그룹 코드를 기준으로 공통코드 그룹 정보를 수정한다.
     */
    @Operation(summary = "공통코드 그룹 수정")
    @PostMapping("/groups/{groupCd}/update")
    public ApiResponse<Void> updateCommonCodeGroup(
            @PathVariable String groupCd,
            @RequestBody CommonCodeGroupDto paramDto
    ) {
        return commonCodeService.updateCommonCodeGroup(groupCd, paramDto);
    }

    /**
     * 특정 그룹에 속한 공통코드 목록을 조회한다.
     */
    @Operation(summary = "공통코드 목록 조회")
    @PostMapping("/groups/{groupCd}/codes/list")
    public ApiResponse<List<CommonCodeDto>> getCommonCodes(@PathVariable String groupCd) {
        return commonCodeService.getCommonCodes(groupCd);
    }

    /**
     * 특정 그룹에 새 공통코드를 등록한다.
     */
    @Operation(summary = "공통코드 등록")
    @PostMapping("/groups/{groupCd}/codes/create")
    public ApiResponse<Void> createCommonCode(
            @PathVariable String groupCd,
            @RequestBody CommonCodeDto paramDto
    ) {
        return commonCodeService.createCommonCode(groupCd, paramDto);
    }

    /**
     * 특정 그룹의 공통코드 정보를 수정한다.
     */
    @Operation(summary = "공통코드 수정")
    @PostMapping("/groups/{groupCd}/codes/{code}/update")
    public ApiResponse<Void> updateCommonCode(
            @PathVariable String groupCd,
            @PathVariable String code,
            @RequestBody CommonCodeDto paramDto
    ) {
        return commonCodeService.updateCommonCode(groupCd, code, paramDto);
    }
}
