package com.poc.controller;

import java.security.Principal;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedResourceController {

    @GetMapping(value={
            "/tenant/realm-1/protected-resource",
            "/tenant/realm-2/protected-resource"})
    public AccessToken listCatalogBranch1() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        KeycloakAuthenticationToken keycloakAuthentication = (KeycloakAuthenticationToken) authentication;

        /*
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        System.out.println(oauthToken.getPrincipal().getAttributes());
         */

        final Principal principal = (Principal) keycloakAuthentication.getPrincipal();
        AccessToken accessToken = null;
        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal<KeycloakSecurityContext> kPrincipal = (KeycloakPrincipal<KeycloakSecurityContext>) principal;
            KeycloakSecurityContext ksc = kPrincipal.getKeycloakSecurityContext();
            accessToken = kPrincipal.getKeycloakSecurityContext().getToken();
        }
        return accessToken;
    }
}
