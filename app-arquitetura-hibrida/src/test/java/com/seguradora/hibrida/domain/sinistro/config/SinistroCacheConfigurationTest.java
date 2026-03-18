package com.seguradora.hibrida.domain.sinistro.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroCacheConfiguration Tests")
class SinistroCacheConfigurationTest {

    @Test
    @DisplayName("SinistroCacheConfiguration deve poder ser instanciada")
    void configurationShouldBeInstantiable() {
        SinistroCacheConfiguration config = new SinistroCacheConfiguration();
        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("classe deve ter anotação @Configuration")
    void configurationShouldHaveConfigurationAnnotation() {
        boolean hasAnnotation = SinistroCacheConfiguration.class
                .isAnnotationPresent(org.springframework.context.annotation.Configuration.class);
        assertThat(hasAnnotation).isTrue();
    }

    @Test
    @DisplayName("classe deve ter anotação @EnableCaching")
    void configurationShouldHaveEnableCachingAnnotation() {
        boolean hasAnnotation = SinistroCacheConfiguration.class
                .isAnnotationPresent(org.springframework.cache.annotation.EnableCaching.class);
        assertThat(hasAnnotation).isTrue();
    }

    @Test
    @DisplayName("classe deve ter anotação @ConditionalOnProperty para redis")
    void configurationShouldHaveConditionalAnnotation() {
        boolean hasAnnotation = SinistroCacheConfiguration.class
                .isAnnotationPresent(org.springframework.boot.autoconfigure.condition.ConditionalOnProperty.class);
        assertThat(hasAnnotation).isTrue();
    }

    @Test
    @DisplayName("método sinistroCacheManager deve existir com @Bean")
    void sinistroCacheManagerShouldExistAsBeanMethod() throws NoSuchMethodException {
        var method = SinistroCacheConfiguration.class.getMethod(
                "sinistroCacheManager",
                org.springframework.data.redis.connection.RedisConnectionFactory.class
        );
        assertThat(method).isNotNull();
        assertThat(method.isAnnotationPresent(org.springframework.context.annotation.Bean.class)).isTrue();
    }
}
