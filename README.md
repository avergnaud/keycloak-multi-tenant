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
http://localhost:8080/tenant/realm-1/sso/login
```

![client2](./doc/client2.png?raw=true)

valid redirect_uri :
```
http://localhost:8080/tenant/realm-2/sso/login
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

## run this app
```
mvn spring-boot:run
```
Validation du fonctionnement multi tenant :
* http://localhost:8080/tenant/realm-1/protected-resource
adrien/adrien
* http://localhost:8080/tenant/realm-2/protected-resource
benoit/benoit

# Doc

## Séquence

Ici détailler le flux OAuth...

## Classes

Retourne un KeycloakDeployment en fonction du path.

Charge :
* soit realm-1-keycloak.json
* soit realm-2-keycloak.json

![PathBasedConfigResolver](./doc/keycloak_1.png?raw=true)

***

En fonction du path, définit une redirection :
* soit /tenant/realm-1/sso/login
* soit /tenant/realm-2/sso/login

![MultitenantKeycloakAuthenticationEntryPoint](./doc/keycloak_2.png?raw=true)

***

Configuration sécurité :

![KeycloakConfigurationAdapter](./doc/keycloak_3.png?raw=true)

# notes

## OAuth 2.0

OAuth 2.0 n'est pas un protocole d'authentification [source](https://oauth.net/articles/authentication/).

L'authentification dit à une application :
* qui est l'utilisateur (avec un identifiant, une adresse mail, un username, etc)
* est-ce qu'il est présent

OAuth 2.0 ne donne pas ces informatios à une application cliente. OAuth 2.0 dit à une application :
* comment obtenir accès (avec des droits) à une ressource privée de l'utilisateur

L'utilisateur (_resource owner_) délègue l'accès à sa _resource_ à l'application, avec un access-token.
(Le access-tooken est comme une clé qui ouvre votre appartement)
Le access-token ne fournit pas à l'application les information d'authentification.
OAuth 2.0 ne dit pas quand, où ni comment a eu lieu l'authentification.
OAuth 2.0 ne gère pas le SSO.

Dans OAuth, le access-token n'est en fait pas destiné à l'application cliente. 
**L'application cliente est authorisée à présenter le access-token** au _resource server_.

"This problem stems from the fact that the client is not the intended _audience_ of the OAuth access token. Instead, it is 
the authorized presenter of that token, and the _audience_ is in fact the protected resource. The protected resource is not 
generally going to be in a position to tell if the user is still present by the token alone, since by the very nature and 
design of the OAuth protocol the user will not be available on the connection between the client and protected resource"

C'est le principe de OAuth : l'authentification est découplée de l'authorization. Au moment où l'application accède à 
la _resource_, l'utilisateur propriétaire de la ressource n'intervient pas. Il a déjà préalablement donné son accord.

## De OAuth 2.0 à OpenID Connect ?

On peut construire un protocole **d'authentification et d'identité** sur le protocole 
 OAuth 2.0 **d'authorisation et de délégation** [source](https://oauth.net/articles/authentication/)

L'utlisateur va déléguer l'accès à une ressource particulière : son identité. L'application client va alors accéder à 
la _resource_ identité (une API) pour ainsi découvrir **qui** en a autorisé l'accès à l'orgine.

As it turns out, though, there are a handful of things that can be used along with OAuth to create an authentication and identity 
protocol on top of this delegation and authorization protocol. In nearly all of these cases, the core functionality of OAuth 
remains intact, and what's happening is that the user is delegating access to their identity to the application they're trying to log in to. 
The client application then becomes a consumer of the identity API, thereby finding out who authorized the client in the first place. 
One major benefit of building authentication on top of authorization in this way is that it allows for management of end-user consent, 
which is very important in cross-domain identity federation at internet scale. Another important benefit is that the user can 
delegate access to other protected APIs along side their identity at the same time, making it much simpler for both application developers 
and end users to manage. With one call, an application can find out if a user is logged in, what the app should call the user, 
download photos for printing, and post updates to their message stream. 

"OpenID Connect's ID Token provides a secondary token along side the access token that communicates the authentication information directly to the client."

OpenID Connect est une extension de OAuth 2.0 [source](https://www.keycloak.org/docs/latest/securing_apps/index.html#overview)

Sources [1](https://www.youtube.com/watch?v=6DxRTJN1Ffo) et [2](https://www.youtube.com/watch?v=WVCzv50BslE)...
 Avec OpenID Connect, les applications peuvent :
* obtenir les informations d'identité
* récupérer le détail de quand, où et comment a eu lieu l'événement d'authentification
* utiliser du SSO fédéré.

Le _End User_ OpenID Connect est le _resource owner_ OAuth 2.0. C'est l'utilisateur authentifié.

Un _Relying Party_ OpenID Connect est un _client_ OAuth 2.0 qui dépend de l'_Identity Provider_ pour 
* authentifier les utilisateurs
* requêter des _claims_ à propos de cet utlisateur

Le _Identity Provider_ est un OAuth 2.0 _Authorization Server_ qui fournit de l'_Authentication As A Service_
* garantit que l'utilisateur est authentifié
* fournit les _claims_ à propos de cet utilisateur
* fournit les information de l'événement d'authentification
au _Relying Party_

Le _Identity Provider_ fournit toutes ces informations dans un ID Token.
Le ID Token (carte d'identité ou passeport) contient un ensemble d'attributs relatifs à l'utilsateur : les _claims_

Claims :
* Subject
* Issuing Authority
* Audience
* Issue date
* Expiration Date


Un adapter est une "librairie ++" pour un type d'application. Par exemple :
* adapter OpenID Connect pour Spring Boot [source](https://www.keycloak.org/docs/latest/securing_apps/index.html#_spring_boot_adapter)
* adapter OpenID Connect pour Spring Security [source](https://www.keycloak.org/docs/latest/securing_apps/index.html#_spring_security_adapter)

