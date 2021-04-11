# De OAuht 2.0 à OpenID Connect multi tenant

**Documentation** et **code source** pour
 * Solution "URL based" : [branche url-based](https://github.com/avergnaud/keycloak-multi-tenant/tree/url-based)
 * Solution "Header based' : [branche header-based](https://github.com/avergnaud/keycloak-multi-tenant/tree/header-based)

**Objectif**

![keycloak-mt-principe](./doc/keycloak-mt-principe.png?raw=true)

**Données**

Table Customer

| firstName | lastName | realm |
| ------------- | ------------- | ------------- |
| Jack | Bauer | realm-1 |
| David | Palmer | realm-1 |
| ... | ... | ... |
| Adrien | Vergnaud | realm-2 |
| ... | ... | ... |



# Authentification et authorization

Quand je vais à l'hôtel, je me présente à l'accueil lors de mon arrivée.
Je m'authentifie avec ma carte d'identité, éventuellement un numéro de réservation, une empreinte Carte Bancaire... Je prouve que je suis bien qui je prétend être. L'hôtel sait que "monsieur Untel est présent,
que c'est un client régulier, qu'il habite à telle adresse, avec tel n° de téléphone, tel email".
L'hôtel sait que je suis présent. L'hôtel sait comment je me suis authentifié en arrivant.
L'hôtel me délivre un badge

J'ai réservé une chambre, une place de parking, un accès au self, un vélo (?).
Lorsque je rentre dans ma chambre, je ne réitère pas le process d'authentification...
Je passe mon badge est la porte s'ouvre. Mais rien ne prouve que c'est moi. J'ai peut-être prêté mon badge
à quelqu'un. Idem pour l'accès au self, au golf, au parking, au sauna etc.
Le badge est un moyen de déléguer l'accès à tous ces services (au porteur du badge).
Le badge est comme une clé. Mais la clé ne dit rien sur le propiétaire de la clé.

Avec cet usage du badge, l'hôtel a décorrélé l'authentification de l'authorization.
Je ne m'authentifie pas à chaque fois que j'accède à ma chambre, au self, au parking.
Si je prête mon badge à mon conjoint pour accéder au self, 
* j'aurai délégué l'accès au self à mon conjoint
* j'aurai autorisé mon conjoint à accéder au self
* la "cible" du badge reste le self

Si je prête mon badge à quelqu'un pour qu'il accède à ma voiture dans le parking,
* j'autorise cette personne à accéder au parking "en mon nom"

On ne peut pas vraiment pousser l'analogie plus loin...

# Avant et après OAuth 2.0

On considère un utilisateur, _resource-owner_ de ses contacts Google.
Ses contacts sont sur le _resource server_ contacts.google.com.
On considère une application tierce, le _client_ qui veut accéder à ses contacts Google.
Dans un modèle client-serveur classique, le _client_ va requêter le _resource server_ en utilisant les identifiants du _resource owner_.
Autrement dit, pour autoriser l'application tierce à accéder à ses contacts, l'utilisateur doit partager ses identifiants avec l'application tierce.
Cela pose [plusieurs problèmes](https://tools.ietf.org/html/rfc6749#page-4), notamment :
* En tant qu'utilisateur, je ne fais confiance qu'à Google pour gérer mon authentification Google. Il se peut qu'une application tierce gère mal mes identifiants par exemple en persistant le password.
* Je suis prêt à faire confiance à une application tierce pour accéder en lecture à mes contacts. Mais avec mes identifiants Google, cette application pourrait aussi accéder à mes emails.
* Je veux pouvoir révoquer l'accès à mes contacts pour cette application...

Avant OAuth 2.0, dés lors que l'application tierce _client_ possède les identifiants de l'utilisateur _resource owner_, il n'y a pas de distinction possible entre les deux.

## Avant :

![avant-oauth2](./doc/avant_oauth20.png?raw=true)

## Après :

![avec_auth20](./doc/avec_auth20.png?raw=true)

Rappel des rôles OAuth 2.0...
* resource owner : Utilisateur qui délègue l'accès à une ressource protégée
* resource server : Le serveur qui héberge la ressource protégée. 
* client : Application qui requête la ressource protégée au nom de l'utilisateur final et avec son autorisation.
* authorization server : Le serveur qui fournit les access-token à l'application client après avoir obtenu le consentement de l'utilisateur

# OAuth 2.0

[https://tools.ietf.org/](https://tools.ietf.org/)

OAuth 2.0 introduit une couche d'**autorisation** qui distingue les rôles de _client_ et de _resource owner_. Avec OAuth 2.0, l'application cliente demande un accès aux ressources contrôlées par le _resource owner_ qui sont servies par le _resource server_. Pour accéder à ces ressources privées, l'application cliente n'utilise plus les identifiants de l'utilisateur _resource owner_.

Le framework OAuth 2.0 permet à des applications tierces d'accéder à des ressources privées d'un utilisateur en son nom.

OAuth 2.0 n'est pas un protocole d'authentification [source](https://oauth.net/articles/authentication/).

L'authentification dit à une application :
* qui est l'utilisateur (avec un identifiant, une adresse mail, un username, etc)
* est-ce qu'il est présent
* comment il s'authentifie

OAuth 2.0 ne donne pas ces informations à une application cliente. OAuth 2.0 dit à une application :
* comment obtenir accès (avec des droits) à une ressource privée de l'utilisateur

## Le access-token

L'utilisateur (_resource owner_) délègue l'accès à sa _resource_ à l'application, avec un access-token.
Le access-tooken est comme un badge d'hôtel, ou une clé qui ouvre votre appartement.
Le access-token ne fournit pas à l'application les information d'authentification.

Au lieu d'utiliser les identifiants de l'utilisateur, le _client_ obtient un access-token. Ce access-token définit les _scopes_ i.e. le périmètre d'accès consenti par l'utilisateur. Le access-token a une durée de vie courte (minutes). Les access-token sont fournis par le _authorization server_ (_token issuer_) avec l'approbation de l'utilisateur final.

## OAuth découple authentification et autorisation

[https://oauth.net/articles/authentication/](https://oauth.net/articles/authentication/)

Dans OAuth, le access-token n'est en fait pas destiné à l'application cliente. 
**L'application cliente est authorisée à présenter le access-token** au _resource server_.

> "The client is not the intended _audience_ of the OAuth access token. Instead, it is 
> the authorized presenter of that token, and the _audience_ is in fact the protected resource. The protected resource is not 
> generally going to be in a position to tell if the user is still present by the token alone, since by the very nature and 
> design of the OAuth protocol the user will not be available on the connection between the client and protected resource"

C'est le principe de OAuth : l'authentification est découplée de l'autorisation. Au moment où l'application accède à
la _resource_, l'utilisateur propriétaire de la ressource n'intervient pas. Il a déjà préalablement donné son accord.

## Authorization code flow


Les access-token sont fournis par le _authorization server_ (_token issuer_) avec l'approbation de l'utilisateur final. Comment a lieu ce mécanisme d'approbation / consentement ?

**Attention**

[https://tools.ietf.org/html/rfc6749#section-1.3.1](https://tools.ietf.org/html/rfc6749#section-1.3.1)
> Before directing the resource owner back to the client with the authorization code, the authorization server authenticates the resource owner and obtains authorization.  Because the resource owner only authenticates with the authorization server, the resource owner's credentials are never shared with the client.

Le process OAuth 2.0 inclut une étape d'authentification : le _resource owner_ s'authentifie auprès du _authorization server_

[https://oauth.net/articles/authentication/](https://oauth.net/articles/authentication/)
> As an additional confounder to our topic, an OAuth process does usually include several kinds of authentication in its process: the resource owner authenticates to the authorization server in the authorization step, the client authenticates to the authorization server in the token endpoint, and there may be others. The existence of these authentication events within the OAuth protocol does not translate to the Oauth protocol itself being able to reliably convey authentication.

![oauth2_authorization_code_grant](./doc/oauth2_authorization_code_grant.png?raw=true)

## PKCE enhanced authorization code flow

https://auth0.com/docs/flows/authorization-code-flow-with-proof-key-for-code-exchange-pkce#how-it-works

## Les endpoints OAuth 2.0

[https://tools.ietf.org/html/rfc6749#section-3](https://tools.ietf.org/html/rfc6749#section-3)

Exposés par le _authorization server_ :
* Authorization endpoint - Appelé par le _client_ pour obtenir un authorization code de la part du _resource owner_ via redirection
* Token endpoint - Appelé par le _client_ pour échanger un authorization code contre un acess-token

Exposé par le _client_ !
* Redirection endpoint - Utilisé par le _authorization server_ pour transmettre le authorization code au _client_ via redirection

## Pour aller plus loin

[https://developer.okta.com/blog/2017/06/21/what-the-heck-is-oauth](https://developer.okta.com/blog/2017/06/21/what-the-heck-is-oauth)

## De OAuth 2.0 à OpenID Connect

[https://openid.net/connect/](https://openid.net/connect/)

On peut construire un protocole **d'authentification et d'identité** sur le protocole OAuth 2.0 **d'autorisation et de délégation** [source](https://oauth.net/articles/authentication/)

Une première idée est que l'utlisateur va déléguer l'accès à une ressource particulière : son identité. L'application client va alors accéder à la _resource_ identité (une API) pour ainsi découvrir **qui** en a autorisé l'accès à l'orgine.

> As it turns out, though, there are a handful of things that can be used along with OAuth to create an authentication and identity protocol on top of this delegation and authorization protocol. In nearly all of these cases, the core functionality of OAuth remains intact, and what's happening is that the user is delegating access to their identity to the application they're trying to log in to. The client application then becomes a consumer of the identity API, thereby finding out who authorized the client in the first place. One major benefit of building authentication on top of authorization in this way is that it allows for management of end-user consent, which is very important in cross-domain identity federation at internet scale. Another important benefit is that the user can delegate access to other protected APIs along side their identity at the same time, making it much simpler for both application developers and end users to manage. With one call, an application can find out if a user is logged in, what the app should call the user, download photos for printing, and post updates to their message stream. 

Cette _resource_ est fournie par le _identity provider_ (_authorization server_)

Une deuxième idée est de transmettre un nouveau token en plus du access-token, avec les informations d'identité.

> "OpenID Connect's ID Token provides a secondary token along side the access token that communicates the authentication information directly to the client."

**Quel est l'apport de OIDC ?**
On a besoin de OIDC parce-que, même si OAuth fournit la mécanique d'autorisations, il ne fournit pas la mécanique d'authentification. Avec OAuth, le user s'est authentifié et a prouvé qu'il était présent auprès du _authorization server_ mais le seul objectif de cette authentification était de générer un access-token pour l'application cliente. L'utilisateur ne s'authentifie pas directement auprès de l'application cliente. OAuth ne fournit pas l'information de quand, où et comment a eu lieu l'authentification.
OIDC étend OAuth 2.0 pour permettre à l'application cliente d'obtenir les information d'identité, de récupére les détails relatifs à l'événement d'authentification

# OpenID Connect

OpenID Connect est une extension / une surcouche de OAuth 2.0 [source](https://www.keycloak.org/docs/latest/securing_apps/index.html#overview).
OpenID Connect permet à une application cliente de vérifier l'identité d'un utilisateur, à partir de son authentification sur un _authorization server_

Sources [1](https://www.youtube.com/watch?v=6DxRTJN1Ffo) et [2](https://www.youtube.com/watch?v=WVCzv50BslE)
 Avec OpenID Connect, les applications peuvent :
* obtenir les informations d'identité
* récupérer le détail de quand, où et comment a eu lieu l'événement d'authentification
* utiliser du SSO fédéré.

Le _End User_ OpenID Connect est le _resource owner_ OAuth 2.0. C'est l'utilisateur authentifié. Et une des ressources qu'il possède est sa propre identité.

Un _Relying Party_ OpenID Connect est un _client_ OAuth 2.0 qui dépend de l'_Identity Provider_ pour 
* authentifier les utilisateurs
* requêter des _claims_ à propos de cet utlisateur

Le _Identity Provider_ est un OAuth 2.0 _Authorization Server_ qui fournit de l'_Authentication As A Service_
* garantit que l'utilisateur est authentifié
* fournit les _claims_ à propos de cet utilisateur
* fournit les information de l'événement d'authentification au _Relying Party_

## ID Token

Comment est-ce que le _identity provider_ fournit au _relying party_ les informations du _end user_ ?
Le _Identity Provider_ fournit toutes ces informations dans un ID Token.
Si le access-token est un badge d'hôtel ou une clé d'appartement, le id-token est plutôt une carte d'identité ou un passeport.
Le ID Token contient un ensemble d'attributs relatifs à l'utilsateur : les _claims_.

[https://developer.okta.com/docs/concepts/oauth-openid/](https://developer.okta.com/docs/concepts/oauth-openid/)
> A "claim" is a piece of information about the end user.

_Claims_ requis :
* Subject (identifiant unique de l'utilisateur pour l'IDP)
* Issuing Authority (le IDP)
* Audience (le _relying party_ qui peut utiliser ce token)
* Issue date
* Expiration Date

_Claims_ optionnels :
* email
* name
* family_name
* ...

## Scopes

Les scopes sont utilisés pour réclamer certains sous-ensembles de claims. Ces scopes sont des scopes OAuth. Ils sont définis par le standart OIDC.
Lors de l'authentification initiale, l'application cliente passe les scopes auxquels elle veut avoir accès. Si l'utilisateur final consent à déléguer l'accès à ces scopes, alors le id-token contiendra les claims correspondants.

_Scope_ requis :
* openid. La présence de ce scope va déclencher l'envoi d'un id-token, et autoriser l'accès à la ressource /userinfo avec le access-token. Le id-token est retourné avec le access-token

_Scopes_ optionnels :
* profile : réclame l'accès aux _claims_ {name, family_name, given_name, middle_name, nickname, email, address, phone, gender, birthdate, profile, picture, website, zoneinfo, locale, updated_at}
* email
* address
* phone

[https://oauth.net/articles/authentication/](https://oauth.net/articles/authentication/)
> After all, it's preferable to say "Good Morning, Jane Doe" instead of "Good Morning, 9XE3-JI34-00132A". OpenID Connect defines a set of standardized OAuth scopes that map to subsets of these attributes: profile, email, phone, and address, allowing plain OAuth authorization requests to carry the necessary information for a request.

## UserInfo Endpoint

OIDC permet aussi à l'application d'obtenir les informations de l'utilisateur à travers une API REST, plus précisément avec la ressource /userinfo.
Ce userinfo endpoint est une ressource protégée OAuth 2.0.
Ce endpoint retourne les _claims_ ou "informations utilisateur" que l'utilisateur a consenti à partager / dont l'utilisateur a délégué l'accès au _relying party_

> It should be noted that clients are not required to use the access token, since the ID Token contains all the necessary information for processing the authentication event. However, in order to provide compatibility with OAuth and match the general tendency for authorizing identity and other API access in parallel, OpenID Connect always issues the ID token along side an OAuth access token. In addition to the claims in the ID Token, OpenID Connect defines a standard protected resource that contains claims about the current user. 

# OpenID Connect Multi Tenancy

## Realms

[https://www.keycloak.org/docs/latest/server_admin/](https://www.keycloak.org/docs/latest/server_admin/)
> A realm manages a set of users, credentials, roles, and groups. A user belongs to and logs into a realm. Realms are isolated from one another and can only manage and authenticate the users that they control.

Un realm Keycloak est un ensemble de comptes utilisateurs avec leurs identifiants, rôles et groupes. Chaque realm est isolé des autres et gère l'authentification de ses propres utilisateurs. On peut définir plusieurs realms très facilement dans Keycloak.

## Tenants

Le multi-tenancy est un concept issu du cloud. Un _tenant_ est un client d'une solution SaaS (locataire). 

Chaque _tenant_ a sa propre population d'utilisateurs.

* Une solution _single tenant_ est un programme qui sert un seul client. 
* Une solution _multi tenant_ est un programme qui sert plusieurs clients, de façon isolée.

Les _realms_ Keycloak sont une solution pour répondre à un besoin d'authentification _multi tenant_.

[https://quarkus.io/guides/security-openid-connect-multitenancy](https://quarkus.io/guides/security-openid-connect-multitenancy)
> When serving multiple customers from the same application (e.g.: SaaS), each customer is a tenant. By enabling multi-tenancy support to your applications you are allowed to also support distinct authentication policies for each tenant even though if that means authenticating against different OpenID Providers, such as Keycloak and Google.

L'accès à une ressource protégée, servie par une webapp, va être sécurisé à travers différents royaumes Keycloak.

## Exemple d'utilisation OIDC Multi tenant

En fonction du tenant de l'utilisateur qui fait la requête, l'application _secured app_ va retourner des ressources différentes.

![keycloak-mt-principe](./doc/keycloak-mt-principe.png?raw=true)

### Données

Table Customer

| firstName | lastName | realm |
| ------------- | ------------- | ------------- |
| Jack | Bauer | realm-1 |
| David | Palmer | realm-1 |
| Chloe | O'Brian | realm-1 |
| Kim | Bauer | realm-1 |
| Michelle | Dessler | realm-1 |
| Jules | Muller | realm-2 |
| Benoit | Laffitte | realm-2 |
| Adrien | Vergnaud | realm-2 |
| Germain | Vivion | realm-2 |
| Gary | Morisson | realm-2 |

### Démo

**Documentation** et **code source** pour
 * Solution "URL based" : [branche url-based](https://github.com/avergnaud/keycloak-multi-tenant/tree/url-based)
 * Solution "Header based' : [branche header-based](https://github.com/avergnaud/keycloak-multi-tenant/tree/header-based)

## Distinct OpenID Providers (Identity providers)

[https://quarkus.io/guides/security-openid-connect-multitenancy](https://quarkus.io/guides/security-openid-connect-multitenancy)
> Tenants can be distinct realms or security domains within the same OpenID Provider or even distinct OpenID Providers

## Notes Keycloak

Un adapter est une "librairie ++" pour un type d'application. Par exemple :
* adapter OpenID Connect pour Spring Boot [source](https://www.keycloak.org/docs/latest/securing_apps/index.html#_spring_boot_adapter)
* adapter OpenID Connect pour Spring Security [source](https://www.keycloak.org/docs/latest/securing_apps/index.html#_spring_security_adapter)
