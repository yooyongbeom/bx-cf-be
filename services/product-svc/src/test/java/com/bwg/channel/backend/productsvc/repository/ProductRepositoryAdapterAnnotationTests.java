package com.bwg.channel.backend.productsvc.repository;

import com.bwg.channel.backend.productsvc.repository.jpa.JpaProductRepositoryAdapter;
import com.bwg.channel.backend.productsvc.repository.mybatis.MybatisProductRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryAdapterAnnotationTests {

    @Test
    void productRepositoryAdaptersUseRepositoryStereotypeWithBeanNames() {
        assertRepositoryBeanName(MybatisProductRepositoryAdapter.class, "mybatisProduct");
        assertRepositoryBeanName(JpaProductRepositoryAdapter.class, "jpaProduct");
    }

    private static void assertRepositoryBeanName(Class<?> adapterType, String beanName) {
        Repository repository = adapterType.getAnnotation(Repository.class);

        assertThat(repository)
                .as("%s should use @Repository", adapterType.getSimpleName())
                .isNotNull();
        assertThat(repository.value()).isEqualTo(beanName);
    }
}
