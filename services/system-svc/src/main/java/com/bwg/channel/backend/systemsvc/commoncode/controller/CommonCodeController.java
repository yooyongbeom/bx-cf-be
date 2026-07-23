package com.bwg.channel.backend.systemsvc.commoncode.controller;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.securitycommon.constants.InternalAuthHeaders;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeCreateReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupDetailResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeGroupResDto;
import com.bwg.channel.backend.systemsvc.commoncode.dto.CommonCodeReplaceReqDto;
import com.bwg.channel.backend.systemsvc.commoncode.service.CommonCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @Operation(
            summary = "공통코드 그룹 목록 조회",
            description = "상세코드를 제외한 공통코드 그룹 목록을 조회한다."
    )
    @PostMapping("/groups/list")
    public ApiResponse<List<CommonCodeGroupResDto>> getCommonCodeGroups() {
        // 공통코드 그룹 목록 조회를 서비스에 위임
        return commonCodeService.getCommonCodeGroups();
    }

    @Operation(
            summary = "공통코드 그룹 및 상세코드 통합 등록",
            description = "그룹 정보와 codes 상세코드 목록을 함께 등록한다. codes가 빈 배열이면 그룹만 등록한다."
    )
    @PostMapping("/create")
    public ApiResponse<Void> createCommonCodes(
            @RequestBody ApiRequest<CommonCodeCreateReqDto> req,
            @RequestHeader(InternalAuthHeaders.USER) String userId
    ) {
        // 검증된 등록자와 그룹·상세코드 통합 등록 요청을 서비스에 전달한다.
        return commonCodeService.createCommonCodes(req, userId);
    }

    @Operation(
            summary = "전체 공통코드 그룹 및 상세코드 목록 조회",
            description = "요청 본문 없이 전체 그룹과 각 그룹의 상세코드 목록을 조회한다."
    )
    @PostMapping("/list")
    public ApiResponse<List<CommonCodeGroupDetailResDto>> getCommonCodeGroupDetails() {
        // 전체 공통코드 그룹과 상세코드 조회를 서비스에 위임한다.
        return commonCodeService.getCommonCodeGroupDetails();
    }

    @Operation(
            summary = "그룹별 공통코드 및 상세코드 조회",
            description = "경로의 groupCd에 해당하는 그룹과 상세코드 목록을 조회한다."
    )
    @PostMapping("/{groupCd}/detail")
    public ApiResponse<List<CommonCodeGroupDetailResDto>> getCommonCodeGroupDetail(
            @PathVariable String groupCd
    ) {
        // 경로의 그룹 코드에 해당하는 그룹과 상세코드 조회를 서비스에 위임한다.
        return commonCodeService.getCommonCodeGroupDetail(groupCd);
    }

    @Operation(
            summary = "공통코드 그룹 및 상세코드 일괄 교체",
            description = "그룹 정보를 수정하고 기존 상세코드를 삭제한 뒤 codes 목록으로 다시 등록한다. "
                    + "codes가 빈 배열이면 기존 상세코드 전체 삭제로 처리한다."
    )
    @PostMapping("/{groupCd}/replace")
    public ApiResponse<Void> replaceCommonCodes(
            @PathVariable String groupCd,
            @RequestBody ApiRequest<CommonCodeReplaceReqDto> req,
            @RequestHeader(InternalAuthHeaders.USER) String userId
    ) {
        // 경로의 그룹 코드, 교체 목록, 검증된 변경자를 서비스에 함께 전달한다.
        return commonCodeService.replaceCommonCodes(groupCd, req, userId);
    }

    @Operation(
            summary = "공통코드 그룹 및 상세코드 통합 삭제",
            description = "경로의 groupCd에 해당하는 상세코드 전체와 그룹을 함께 삭제한다."
    )
    @PostMapping("/{groupCd}/delete")
    public ApiResponse<Void> deleteCommonCodes(
            @PathVariable String groupCd,
            @RequestHeader(InternalAuthHeaders.USER) String userId
    ) {
        // 경로의 그룹 코드와 검증된 삭제자를 서비스에 전달한다.
        return commonCodeService.deleteCommonCodes(groupCd, userId);
    }
}
