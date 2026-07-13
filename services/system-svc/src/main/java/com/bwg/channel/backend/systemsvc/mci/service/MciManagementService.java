package com.bwg.channel.backend.systemsvc.mci.service;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.mci.dto.MciTransactionReqDto;
import com.bwg.channel.backend.systemsvc.mci.dto.MciTransactionResDto;
import java.util.List;

/**
 * MCI 거래 관리 서비스 계약.
 * <p>
 * 현재 구현은 YAML 기반 설계 골격이고, 추후 DB 저장/배포 반영/승인 프로세스가 필요하면 이 인터페이스 뒤쪽 구현을
 * 확장한다.
 */
public interface MciManagementService {

    /** MCI 거래 정의 목록을 조회한다. */
    ApiResponse<List<MciTransactionResDto>> getMciTransactions(ApiRequest<MciTransactionReqDto> req);

    /** MCI 거래 정의 상세를 조회한다. */
    ApiResponse<MciTransactionResDto> getMciTransaction(String transactionCode, ApiRequest<MciTransactionReqDto> req);

    /** MCI 거래 정의 저장 요청을 받는다. */
    ApiResponse<Void> saveMciTransaction(ApiRequest<MciTransactionReqDto> req);

    /** MCI 거래 정의 수정 요청을 받는다. */
    ApiResponse<Void> updateMciTransaction(String transactionCode, ApiRequest<MciTransactionReqDto> req);
}
