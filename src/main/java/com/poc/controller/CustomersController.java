package com.poc.controller;

import com.poc.config.resolution.HeaderBasedConfigResolver;
import com.poc.data.Customer;
import com.poc.data.CustomerRepository;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.IDToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class CustomersController {

    @Autowired
    CustomerRepository repository;

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getCustomers(HttpServletRequest request) {

        String hostRealm = HeaderBasedConfigResolver.getRealm(request.getHeader("Host"));

        KeycloakAuthenticationToken keycloakAuthentication = (KeycloakAuthenticationToken) SecurityContextHolder
                .getContext()
                .getAuthentication();

        IDToken iDToken = keycloakAuthentication.getAccount().getKeycloakSecurityContext().getIdToken();
        String realm = "realm-1";
        if(iDToken.getIssuer().endsWith("realm-2")) {
            realm = "realm-2";
        }

        if(!hostRealm.equals(realm)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        List<Customer> retour = repository.findByRealm(realm);

        return ResponseEntity.status(HttpStatus.OK).body(retour);
    }
}
