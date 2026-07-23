package com.bwg.channel.backend.systemsvc.mci.service;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.systemsvc.mci.dto.MciTransactionReqDto;
import com.bwg.channel.backend.systemsvc.mci.dto.MciTransactionResDto;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * MCI 거래 관리 서비스 초기 구현.
 * <p>
 * 아직 거래 설정의 기준 저장소는 mci-svc YAML이다. 그래서 system-svc는 관리자 API 계약을 먼저 제공하고,
 * 저장/수정 API는 추후 DB 기반 registry가 생길 때 실제 영속 처리로 교체한다.
 */
@Service
public class MciManagementServiceImpl implements MciManagementService {

    /**
     * MCI 거래 정의 목록을 조회한다.
     *
     * <p>현재 system-svc에는 거래 정의 저장소가 없으므로 빈 목록을 반환한다.</p>
     *
     * @param req 목록 조회 요청
     * @return 빈 MCI 거래 정의 목록 응답
     */
    @Override
    public ApiResponse<List<MciTransactionResDto>> getMciTransactions(ApiRequest<MciTransactionReqDto> req) {
        // 아직 기준 저장소가 mci-svc YAML이므로 system-svc DB 조회는 수행하지 않는다.
        // 관리자 UI가 붙는 시점에 이 지점이 목록 조회 repository 호출로 교체된다.
        return ApiResponse.success(List.of());
    }

    /**
     * 요청한 거래 코드를 포함하는 MCI 거래 정의 골격을 반환한다.
     *
     * <p>현재 상세 원천은 mci-svc의 YAML이므로 system-svc에서는 거래 코드와
     * {@code YAML} 출처만 포함한 설계 응답을 생성한다.</p>
     *
     * @param transactionCode 조회할 MCI 거래 코드
     * @param req 상세 조회 요청
     * @return 거래 코드와 YAML 출처가 포함된 상세 응답
     */
    @Override
    public ApiResponse<MciTransactionResDto> getMciTransaction(String transactionCode, ApiRequest<MciTransactionReqDto> req) {
        return ApiResponse.success(MciTransactionResDto.builder()
                .code(transactionCode)
                .source("YAML")
                .build());
    }

    /**
     * MCI 거래 정의 저장 API 계약에 대한 성공 응답을 반환한다.
     *
     * <p>현재는 DB 기반 registry가 없으므로 요청을 영속화하지 않는다.</p>
     *
     * @param req 저장할 MCI 거래 정의
     * @return 부수효과 없이 생성한 성공 응답
     */
    @Override
    public ApiResponse<Void> saveMciTransaction(ApiRequest<MciTransactionReqDto> req) {
        // 현재 단계에서는 API 모양을 고정하는 것이 목적이므로 side effect 없이 성공 응답만 반환한다.
        return ApiResponse.success(null);
    }

    /**
     * MCI 거래 정의 수정 API 계약에 대한 성공 응답을 반환한다.
     *
     * <p>현재는 DB 기반 registry가 없으므로 요청을 영속화하지 않는다.</p>
     *
     * @param transactionCode 수정할 MCI 거래 코드
     * @param req 수정할 MCI 거래 정의
     * @return 부수효과 없이 생성한 성공 응답
     */
    @Override
    public ApiResponse<Void> updateMciTransaction(String transactionCode, ApiRequest<MciTransactionReqDto> req) {
        // DB 기반 registry로 전환하면 transactionCode 기준 수정, 이력, 승인 상태를 여기에서 처리한다.
        return ApiResponse.success(null);
    }
}
