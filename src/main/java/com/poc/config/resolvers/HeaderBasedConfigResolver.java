package com.poc.config.resolvers;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

public class HeaderBasedConfigResolver implements KeycloakConfigResolver {

    private final ConcurrentHashMap<String, KeycloakDeployment> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unused")
    private static AdapterConfig adapterConfig;

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {

        String host = request.getHeader("Host");
        String app = "app1", realm = "realm-1";
        if(host.startsWith("app2")) {
            app = "app2";
            realm = "realm-2";
        }

        if (!cache.containsKey(realm)) {
            InputStream is = getClass().getResourceAsStream("/" + realm + "-keycloak.json");
            cache.put(app, KeycloakDeploymentBuilder.build(is));
        }
        return cache.get(app);
    }

    static void setAdapterConfig(AdapterConfig adapterConfig) {
        HeaderBasedConfigResolver.adapterConfig = adapterConfig;
    }

}