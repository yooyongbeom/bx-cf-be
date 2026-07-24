package com.bwg.channel.backend.authsvc.authentication.repository;

import com.bwg.channel.backend.authsvc.authentication.repository.api.ApiLoginRepositoryAdapter;
import com.bwg.channel.backend.authsvc.authentication.repository.jpa.JpaLoginRepositoryAdapter;
import com.bwg.channel.backend.authsvc.authentication.repository.mybatis.MybatisLoginRepositoryAdapter;
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
