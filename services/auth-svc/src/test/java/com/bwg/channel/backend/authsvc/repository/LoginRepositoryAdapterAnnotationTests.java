package com.bwg.channel.backend.authsvc.repository;

import com.bwg.channel.backend.authsvc.repository.api.ApiLoginRepositoryAdapter;
import com.bwg.channel.backend.authsvc.repository.jpa.JpaLoginRepositoryAdapter;
import com.bwg.channel.backend.authsvc.repository.mybatis.MybatisLoginRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRepositoryAdapterAnnotationTests {

    @Test
    void loginRepositoryAdaptersUseRepositoryStereotypeWithBeanNames() {
        assertRepositoryBeanName(MybatisLoginRepositoryAdapter.class, "mybatisLogin");
        assertRepositoryBeanName(JpaLoginRepositoryAdapter.class, "jpaLogin");
        assertRepositoryBeanName(ApiLoginRepositoryAdapter.class, "apiLogin");
    }

    private static void assertRepositoryBeanName(Class<?> adapterType, String beanName) {
        Repository repository = adapterType.getAnnotation(Repository.class);

        assertThat(repository)
                .as("%s should use @Repository", adapterType.getSimpleName())
                .isNotNull();
        assertThat(repository.value()).isEqualTo(beanName);
    }
}
