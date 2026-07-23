package com.bwg.channel.backend.productsvc.service;

import com.bwg.channel.backend.common.domain.dto.ApiRequest;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.bwg.channel.backend.common.domain.dto.PaginationReqDto;
import com.bwg.channel.backend.common.domain.dto.SortReqDto;
import com.bwg.channel.backend.productsvc.domain.dto.ProductReqDto;
import com.bwg.channel.backend.productsvc.domain.dto.ProductResDto;
import com.bwg.channel.backend.productsvc.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceResponseMetaTests {

    @Test
    void productListResponseContainsPaginationMetadata() {
        ProductRepository repository = mock(ProductRepository.class);
        ProductServiceImpl productService = new ProductServiceImpl(
                Map.of("mybatisProduct", repository),
                repository
        );
        ApiRequest<ProductReqDto> request = productListRequest();
        ProductResDto firstProduct = new ProductResDto();
        ProductResDto secondProduct = new ProductResDto();
        when(repository.findAll(request)).thenReturn(List.of(firstProduct, secondProduct));
        when(repository.count(request)).thenReturn(42L);

        ApiResponse<List<ProductResDto>> response = productService.getProductList(request, "mybatisProduct");

        assertThat(response.getPayload()).containsExactly(firstProduct, secondProduct);
        assertThat(response.getPagination().getPage()).isEqualTo(1);
        assertThat(response.getPagination().getSize()).isEqualTo(20);
        assertThat(response.getPagination().getTotalCount()).isEqualTo(42L);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(3);
        verify(repository).count(request);
    }

    @Test
    void productListWithoutPaginationDoesNotRunCountQuery() {
        ProductRepository repository = mock(ProductRepository.class);
        ProductServiceImpl productService = new ProductServiceImpl(
                Map.of("mybatisProduct", repository),
                repository
        );
        ApiRequest<ProductReqDto> request = new ApiRequest<>();
        request.setData(new ProductReqDto());
        ProductResDto product = new ProductResDto();
        when(repository.findAll(request)).thenReturn(List.of(product));

        ApiResponse<List<ProductResDto>> response = productService.getProductList(request, "mybatisProduct");

        assertThat(response.getPayload()).containsExactly(product);
        assertThat(response.getPagination()).isNull();
        verify(repository, never()).count(any());
    }

    private ApiRequest<ProductReqDto> productListRequest() {
        PaginationReqDto pagination = new PaginationReqDto();
        pagination.setPage(1);
        pagination.setSize(20);

        SortReqDto sort = new SortReqDto();
        sort.setSort("createdAt,desc");

        ApiRequest<ProductReqDto> request = new ApiRequest<>();
        request.setPagination(pagination);
        request.setSort(sort);
        request.setData(new ProductReqDto());
        return request;
    }
}
