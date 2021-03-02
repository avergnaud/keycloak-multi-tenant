package com.poc;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import com.poc.config.PathBasedConfigResolver;

@SpringBootApplication
public class KeycloakMultiTenantApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeycloakMultiTenantApplication.class, args);
    }

    @Bean
    @ConditionalOnMissingBean(PathBasedConfigResolver.class)
    public KeycloakConfigResolver keycloakConfigResolver() {
        return new PathBasedConfigResolver();
    }
}
