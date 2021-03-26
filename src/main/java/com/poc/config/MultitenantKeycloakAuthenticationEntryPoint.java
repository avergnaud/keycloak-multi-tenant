package com.poc.config;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.poc.config.resolution.UrlBasedConfigResolver;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class MultitenantKeycloakAuthenticationEntryPoint extends KeycloakAuthenticationEntryPoint {

    public MultitenantKeycloakAuthenticationEntryPoint(AdapterDeploymentContext adapterDeploymentContext) {
        super(adapterDeploymentContext);
    }

    public MultitenantKeycloakAuthenticationEntryPoint(AdapterDeploymentContext adapterDeploymentContext, RequestMatcher apiRequestMatcher) {
        super(adapterDeploymentContext, apiRequestMatcher);
    }

    @Override
    protected void commenceLoginRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String realm = UrlBasedConfigResolver.getRealm(request.getServletPath());
        String contextAwareLoginUri = request.getContextPath() + "/tenant/" + realm + DEFAULT_LOGIN_URI;
        response.sendRedirect(contextAwareLoginUri);
    }
}
