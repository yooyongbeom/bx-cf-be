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

    /**
     * MCI 거래 정의 목록을 조회한다.
     *
     * @param req 목록 조회 요청
     * @return MCI 거래 정의 목록 응답
     */
    ApiResponse<List<MciTransactionResDto>> getMciTransactions(ApiRequest<MciTransactionReqDto> req);

    /**
     * 거래 코드에 해당하는 MCI 거래 정의 상세를 조회한다.
     *
     * @param transactionCode 조회할 MCI 거래 코드
     * @param req 상세 조회 요청
     * @return MCI 거래 정의 상세 응답
     */
    ApiResponse<MciTransactionResDto> getMciTransaction(String transactionCode, ApiRequest<MciTransactionReqDto> req);

    /**
     * MCI 거래 정의 저장 요청을 받는다.
     *
     * <p>현재 구현은 요청을 영속화하지 않고 성공 응답만 반환한다.</p>
     *
     * @param req 저장할 MCI 거래 정의
     * @return 영속 처리 없이 생성한 성공 응답
     */
    ApiResponse<Void> saveMciTransaction(ApiRequest<MciTransactionReqDto> req);

    /**
     * 거래 코드에 해당하는 MCI 거래 정의 수정 요청을 받는다.
     *
     * <p>현재 구현은 요청을 영속화하지 않고 성공 응답만 반환한다.</p>
     *
     * @param transactionCode 수정할 MCI 거래 코드
     * @param req 수정할 MCI 거래 정의
     * @return 영속 처리 없이 생성한 성공 응답
     */
    ApiResponse<Void> updateMciTransaction(String transactionCode, ApiRequest<MciTransactionReqDto> req);
}
