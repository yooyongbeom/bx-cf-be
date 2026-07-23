package com.bwg.channel.backend.productsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.PaginationReqDto;
import com.bwg.channel.backend.common.util.PageUtil;
import com.bwg.channel.backend.productsvc.constants.ProductErrorCode;
import com.bwg.channel.backend.productsvc.exception.BwgProductException;
import com.bwg.channel.backend.productsvc.domain.dto.ProductReqDto;
import com.bwg.channel.backend.productsvc.domain.dto.ProductResDto;
import com.bwg.channel.backend.productsvc.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 상품 저장소 전략을 선택해 상품 조회를 처리하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final Map<String, ProductRepository> productRepositoryMap;
    private final ProductRepository defaultProductRepository;

    /**
     * 요청한 저장소 전략으로 상품 목록을 조회하고 선택적 페이지 메타데이터를 생성한다.
     *
     * @param paramDto 상품 조회 조건과 선택적 페이지 요청
     * @param type 사용할 상품 저장소 유형
     * @return 상품 목록과 요청된 경우 페이지 정보가 포함된 응답
     * @throws BwgProductException 저장소 유형에 해당하는 구현체가 없는 경우
     */
    @Override
    public ApiResponse<List<ProductResDto>> getProductList(ApiRequest<ProductReqDto> paramDto, String type) {
        // 저장소 전략 선택
        ProductRepository repository = getRepository(type);
        // 선택된 저장소 기준 상품 목록 조회
        List<ProductResDto> result = repository.findAll(paramDto);
        PaginationReqDto requestPagination = paramDto == null ? null : paramDto.getPagination();
        // 페이지 요청이 있을 때만 동일 검색 조건의 전체 건수를 추가 조회한다.
        long totalCount = requestPagination == null ? 0L : repository.count(paramDto);
        return ApiResponse.success(result, PageUtil.of(requestPagination, totalCount));
    }

    /**
     * 요청한 저장소 전략으로 상품 ID에 해당하는 상품 한 건을 조회한다.
     *
     * @param productId 조회할 상품 ID
     * @param type 사용할 상품 저장소 유형
     * @return 저장소가 반환한 상품이 포함된 응답
     * @throws BwgProductException 저장소 유형에 해당하는 구현체가 없거나 상품이 존재하지 않는 경우
     */
    @Override
    public ApiResponse<ProductResDto> getProduct(Long productId, String type) {
        // 저장소 전략 선택
        ProductRepository repository = getRepository(type);
        // 선택된 저장소 기준 상품 단건 조회
        ProductResDto result = repository.findById(productId);
        return ApiResponse.success(result);
    }

    /**
     * 요청 type 값으로 상품 저장소 구현체를 선택한다.
     *
     * <p>type이 없거나 빈 문자열이면 기본 저장소를 사용하고, 값이 있으면 등록된
     * 저장소 맵에서 해당 구현체를 조회한다.</p>
     *
     * @param type 선택할 상품 저장소 유형
     * @return 기본 저장소 또는 type으로 등록된 상품 저장소
     * @throws BwgProductException type에 해당하는 저장소가 등록되어 있지 않은 경우
     */
    private ProductRepository getRepository(String type) {
        ProductRepository repository = (type == null || type.isEmpty())
                ? defaultProductRepository
                : productRepositoryMap.get(type);

        if (repository == null) {
            throw new BwgProductException.Builder()
                    .code(ProductErrorCode.REQUIRED_VALUE_MISSING)
                    .message("Invalid Product repository type : " + type)
                    .build();
        }

        return repository;
    }

}
