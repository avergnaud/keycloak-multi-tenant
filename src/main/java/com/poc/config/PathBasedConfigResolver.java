package com.poc.config;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;

public class PathBasedConfigResolver implements KeycloakConfigResolver {

    private final ConcurrentHashMap<String, KeycloakDeployment> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unused")
    private static AdapterConfig adapterConfig;

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {

        String path = request.getURI();
        int index = path.indexOf("tenant/");
        if (index == -1) {
            throw new IllegalStateException("Tenant not found");
        }
        String realm = path.substring(path.indexOf("tenant/")).split("/")[1];
        if (realm.contains("?")) {
            realm = realm.split("\\?")[0];
        }
        if (!cache.containsKey(realm)) {
            InputStream is = getClass().getResourceAsStream("/" + realm + "-keycloak.json");
            cache.put(realm, KeycloakDeploymentBuilder.build(is));
        }
        return cache.get(realm);
    }

    static void setAdapterConfig(AdapterConfig adapterConfig) {
        PathBasedConfigResolver.adapterConfig = adapterConfig;
    }

}