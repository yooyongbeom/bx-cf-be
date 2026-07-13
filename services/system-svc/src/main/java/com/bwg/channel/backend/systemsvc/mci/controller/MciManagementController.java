package com.bwg.channel.backend.systemsvc.mci.controller;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.mci.dto.MciTransactionReqDto;
import com.bwg.channel.backend.systemsvc.mci.dto.MciTransactionResDto;
import com.bwg.channel.backend.systemsvc.mci.service.MciManagementService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MCI 거래 관리 API.
 * <p>
 * 관리자 UI는 이 API를 기준으로 거래 목록/상세/저장/수정 화면을 붙인다. 현 단계에서는 YAML 기반 설계 골격이며,
 * DB 기반 관리로 넘어갈 때 service 구현을 실제 저장소와 연결한다.
 */
@Hidden
@Tag(name = "기준정보-MCI")
@RestController
@RequiredArgsConstructor
@RequestMapping("/mci/transactions")
public class MciManagementController {

    /** MCI 거래 정의 관리 서비스. */
    private final MciManagementService mciManagementService;

    /** MCI 거래 정의 목록을 조회한다. */
    @Operation(summary = "MCI 거래 목록 조회")
    @PostMapping("/list")
    public ApiResponse<List<MciTransactionResDto>> getMciTransactions(@RequestBody ApiRequest<MciTransactionReqDto> req) {
        return mciManagementService.getMciTransactions(req);
    }

    /** MCI 거래 정의 상세를 조회한다. */
    @Operation(summary = "MCI 거래 상세 조회")
    @PostMapping("/{transactionCode}/detail")
    public ApiResponse<MciTransactionResDto> getMciTransaction(
            @PathVariable String transactionCode,
            @RequestBody ApiRequest<MciTransactionReqDto> req
    ) {
        return mciManagementService.getMciTransaction(transactionCode, req);
    }

    /** MCI 거래 정의를 저장한다. */
    @Operation(summary = "MCI 거래 등록")
    @PostMapping("/create")
    public ApiResponse<Void> saveMciTransaction(@RequestBody ApiRequest<MciTransactionReqDto> req) {
        return mciManagementService.saveMciTransaction(req);
    }

    /** MCI 거래 정의를 수정한다. */
    @Operation(summary = "MCI 거래 수정")
    @PostMapping("/{transactionCode}/update")
    public ApiResponse<Void> updateMciTransaction(
            @PathVariable String transactionCode,
            @RequestBody ApiRequest<MciTransactionReqDto> req
    ) {
        return mciManagementService.updateMciTransaction(transactionCode, req);
    }
}
