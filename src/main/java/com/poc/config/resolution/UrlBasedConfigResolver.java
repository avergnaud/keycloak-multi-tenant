package com.poc.config.resolution;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;

public class UrlBasedConfigResolver implements KeycloakConfigResolver {

    private static final Pattern p = Pattern.compile("\\/tenant\\/(.*?)\\/.*");

    private final ConcurrentHashMap<String, KeycloakDeployment> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unused")
    private static AdapterConfig adapterConfig;

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {

        String realm = getRealm(request.getRelativePath());

        if (!cache.containsKey(realm)) {
            InputStream is = getClass().getResourceAsStream("/" + realm + "-keycloak.json");
            cache.put(realm, KeycloakDeploymentBuilder.build(is));
        }
        return cache.get(realm);
    }

    /**
     * méthode utils
     * @return
     */
    public static String getRealm(String path) {
        Matcher m = p.matcher(path) ;
        if(!m.matches()) {
            throw new IllegalStateException("Cannot resolve realm from " + path);
        }
        return m.group(1);
    }

    static void setAdapterConfig(AdapterConfig adapterConfig) {
        UrlBasedConfigResolver.adapterConfig = adapterConfig;
    }

}