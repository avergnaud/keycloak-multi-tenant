# Install

## Install & conf keycloak
```
docker container run --name=keycloak-multi -p 8081:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin jboss/keycloak
```
ou
```
./standalone.sh -Djboss.socket.binding.port-offset=1
```

Créer 2 realm dans Keycloak :
* realm-1
* realm-2

![realms](./doc/realms.png?raw=true)

Créer 2 clients dans Keycloak :
* realm-1, web-client-id-realm1
* realm-2, web-client-id-realm2

![client1](./doc/client1.png?raw=true)

valid redirect_uri :
```
http://app1:8080/tenant/realm-1/sso/login
```

![client2](./doc/client2.png?raw=true)

valid redirect_uri :
```
http://app2:8080/tenant/realm-2/sso/login
```

Créer 2 users dans Keycloak :
* realm-1, user adrien
* realm-2, user benoit

![adrien](./doc/adrien.png?raw=true)

![benoit](./doc/benoit.png?raw=true)

# run

## run keycloak
```
docker container start keycloak-multi
```
ou
```
./standalone.sh -Djboss.socket.binding.port-offset=1
```

## run this app
```
mvn spring-boot:run
```

Validation du fonctionnement multi tenant :
* [http://app1:8080/customers](http://app1:8080/customers)
adrien/adrien
* [http://app2:8080/customers](http://app2:8080/customers)
benoit/benoit

# Doc

## Séquence

![header-based-flow_detail](./doc/header-based-flow_detail.png?raw=true)

# Classes

## HeaderBasedConfigResolver

Retourne un KeycloakDeployment en fonction du header Host.

Charge :
* soit realm-1-keycloak.json
* soit realm-2-keycloak.json

![HeaderBasedConfigResolver.png](./doc/HeaderBasedConfigResolver.png?raw=true)

## MultitenantKeycloakAuthenticationEntryPoint

En fonction du header Host, définit une redirection :
* soit /tenant/realm-1/sso/login
* soit /tenant/realm-2/sso/login

![MultitenantKeycloakAuthenticationEntryPoint](./doc/keycloak_2.png?raw=true)

## KeycloakConfigurationAdapter

Configuration sécurité :

![KeycloakConfigurationAdapter](./doc/keycloak_3.png?raw=true)