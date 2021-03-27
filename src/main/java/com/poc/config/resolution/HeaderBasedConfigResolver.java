package com.poc.config.resolution;

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

        String realm = getRealm(request.getHeader("Host"));

        if (!cache.containsKey(realm)) {
            InputStream is = getClass().getResourceAsStream("/" + realm + "-keycloak.json");
            cache.put(realm, KeycloakDeploymentBuilder.build(is));
        }
        return cache.get(realm);
    }

    /**
     * utils method
     * @return
     */
    public static String getRealm(String host) {
        if(host.startsWith("app1")) {
            return "realm-1";
        } else if(host.startsWith("app2")) {
            return "realm-2";
        }
        throw new IllegalStateException("Cannot resolve realm from " + host);
    }

    static void setAdapterConfig(AdapterConfig adapterConfig) {
        HeaderBasedConfigResolver.adapterConfig = adapterConfig;
    }

}