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

    /** 현재는 DB 저장소가 없으므로 빈 목록과 저장소 힌트만 반환한다. */
    @Override
    public ApiResponse<List<MciTransactionResDto>> getMciTransactions(ApiRequest<MciTransactionReqDto> req) {
        // 아직 기준 저장소가 mci-svc YAML이므로 system-svc DB 조회는 수행하지 않는다.
        // 관리자 UI가 붙는 시점에 이 지점이 목록 조회 repository 호출로 교체된다.
        return ApiResponse.success(List.of());
    }

    /** YAML 단계에서는 system-svc가 상세 원천을 갖지 않으므로 요청 거래 코드 중심의 설계 응답만 반환한다. */
    @Override
    public ApiResponse<MciTransactionResDto> getMciTransaction(String transactionCode, ApiRequest<MciTransactionReqDto> req) {
        return ApiResponse.success(MciTransactionResDto.builder()
                .code(transactionCode)
                .source("YAML")
                .build());
    }

    /** 저장 API 계약을 먼저 열어두고, 실제 저장은 DB registry 전환 시 구현한다. */
    @Override
    public ApiResponse<Void> saveMciTransaction(ApiRequest<MciTransactionReqDto> req) {
        // 현재 단계에서는 API 모양을 고정하는 것이 목적이므로 side effect 없이 성공 응답만 반환한다.
        return ApiResponse.success(null);
    }

    /** 수정 API 계약을 먼저 열어두고, 실제 저장은 DB registry 전환 시 구현한다. */
    @Override
    public ApiResponse<Void> updateMciTransaction(String transactionCode, ApiRequest<MciTransactionReqDto> req) {
        // DB 기반 registry로 전환하면 transactionCode 기준 수정, 이력, 승인 상태를 여기에서 처리한다.
        return ApiResponse.success(null);
    }
}
