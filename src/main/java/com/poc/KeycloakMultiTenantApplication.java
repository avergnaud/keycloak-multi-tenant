package com.poc;

import com.poc.config.resolvers.HeaderBasedConfigResolver;
import com.poc.data.Customer;
import com.poc.data.CustomerRepository;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import com.poc.config.resolvers.UrlBasedConfigResolver;

@SpringBootApplication
public class KeycloakMultiTenantApplication {

    private static final Logger log = LoggerFactory.getLogger(KeycloakMultiTenantApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(KeycloakMultiTenantApplication.class, args);
    }

    /*
        @Bean
        @ConditionalOnMissingBean(UrlBasedConfigResolver.class)
        public KeycloakConfigResolver keycloakConfigResolver() {
            return new UrlBasedConfigResolver();
        }
        */
    @Bean
    @ConditionalOnMissingBean(HeaderBasedConfigResolver.class)
    public KeycloakConfigResolver keycloakConfigResolver() {
        return new HeaderBasedConfigResolver();
    }

    @Bean
    public CommandLineRunner demo(CustomerRepository repository) {
        return (args) -> {
            // save a few customers
            repository.save(new Customer("Jack", "Bauer", "realm-1"));
            repository.save(new Customer("Chloe", "O'Brian", "realm-1"));
            repository.save(new Customer("Kim", "Bauer", "realm-1"));
            repository.save(new Customer("David", "Palmer", "realm-1"));
            repository.save(new Customer("Michelle", "Dessler", "realm-1"));

            repository.save(new Customer("Jules", "Muller", "realm-2"));
            repository.save(new Customer("Benoit", "Laffitte", "realm-2"));
            repository.save(new Customer("Adrien", "Vergnaud", "realm-2"));
            repository.save(new Customer("Germain", "Vivion", "realm-2"));
            repository.save(new Customer("Gary", "Morisson", "realm-2"));
        };
    }
}
