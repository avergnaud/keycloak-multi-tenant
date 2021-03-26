package com.poc.controller;

import com.poc.data.Customer;
import com.poc.data.CustomerRepository;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.IDToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProtectedResourceController {

    @Autowired
    CustomerRepository repository;

    /*
    @GetMapping(value={
            "/tenant/realm-1/protected-resource",
            "/tenant/realm-2/protected-resource"})
    */
    @GetMapping("/protected-resource")
    public List<Customer> listCatalogBranch1() {

        Authentication authentication = SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        KeycloakAuthenticationToken keycloakAuthentication = (KeycloakAuthenticationToken) authentication;

        IDToken iDToken = keycloakAuthentication.getAccount().getKeycloakSecurityContext().getIdToken();

        iDToken.getIssuer();// http://localhost:8081/auth/realms/realm-1
        String realm = "realm-1";
        if(iDToken.getIssuer().endsWith("realm-2")) {
            realm = "realm-2";
        }

        return repository.findByRealm(realm);
    }
}
