package com.poc.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

public class SpringKeycloakSecurityAdapter extends AbstractHttpConfigurer<SpringKeycloakSecurityAdapter, HttpSecurity> {

    @Override
    public void init(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()
                    .antMatchers("/logout", "/", "/unsecured").permitAll()
                    .antMatchers("/customers").authenticated()
                    .anyRequest().denyAll();

    }

}
