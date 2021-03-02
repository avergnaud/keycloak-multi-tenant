# Install

## Install & conf keycloak
```
docker container run --name=keycloak -p 8081:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin jboss/keycloak
```
Créer 2 realm dans Keycloak :
* realm-1
* realm-2

![realms](./doc/realms.png?raw=true)

Créer 2 clients dans Keycloak :
* realm-1, web-client-id-realm1
* realm-2, web-client-id-realm2

![client1](./doc/client1.png?raw=true)

![client2](./doc/client2.png?raw=true)

Créer 2 users dans Keycloak :
* realm-1, user adrien
* realm-2, user benoit

![adrien](./doc/adrien.png?raw=true)

![benoit](./doc/benoit.png?raw=true)

# run

## run keycloak
```
docker container start keycloak
```
Validation du fonctionnement multi tenant :
* http://localhost:8080/tenant/realm-1/protected-resource
adrien/adrien
* http://localhost:8080/tenant/realm-2/protected-resource
benoit/benoit

## run this app
```
mvn spring-boot:run
```

# Doc

## Séquence

Ici détailler le flux OAuth...

## Classes

![PathBasedConfigResolver](./doc/keycloak_1.png?raw=true)

![MultitenantKeycloakAuthenticationEntryPoint](./doc/keycloak_2.png?raw=true)

![KeycloakConfigurationAdapter](./doc/keycloak_3.png?raw=true)