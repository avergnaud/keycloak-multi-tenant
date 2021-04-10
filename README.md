# Keycloak multi tenant 

**Documentation** et **code source** pour
 * Solution "URL based" : [branche url-based](https://github.com/avergnaud/keycloak-multi-tenant/tree/url-based)
 * Solution "Header based' : [branche header-based](https://github.com/avergnaud/keycloak-multi-tenant/tree/header-based)

## principe

![keycloak-mt-principe](./doc/keycloak-mt-principe.png?raw=true)

## Données

Table Customer

| firstName | lastName | realm |
| ------------- | ------------- | ------------- |
| Jack | Bauer | realm-1 |
| David | Palmer | realm-1 |
| ... | ... | ... |
| Adrien | Vergnaud | realm-2 |
| ... | ... | ... |

## notes

### Authentification et authorization

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

### Avant OAuth 2.0

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

![avant-oauth2](./doc/avant_oauth20.png?raw=true)

### OAuth 2.0

[https://tools.ietf.org/](https://tools.ietf.org/)

OAuth 2.0 introduit une couche d'**authorisation** qui distingue les rôles de _client_ et de _resource owner_. Avec OAuth 2.0, l'application cliente demande un accès aux ressources contrôlées par le _resource owner_ qui sont servies par le _resource server_. Pour accéder à ces ressources privées, l'application cliente n'utilise plus les identifiants de l'utilisateur _resource owner_.

Le framework OAuth 2.0 permet à des applications tierces d'accéder à des ressources privées d'un utilisateur en son nom.

OAuth 2.0 n'est pas un protocole d'authentification [source](https://oauth.net/articles/authentication/).

L'authentification dit à une application :
* qui est l'utilisateur (avec un identifiant, une adresse mail, un username, etc)
* est-ce qu'il est présent
* comment il s'authentifie

OAuth 2.0 ne donne pas ces informations à une application cliente. OAuth 2.0 dit à une application :
* comment obtenir accès (avec des droits) à une ressource privée de l'utilisateur

#### Le access-token

L'utilisateur (_resource owner_) délègue l'accès à sa _resource_ à l'application, avec un access-token.
Le access-tooken est comme un badge d'hôtel, ou une clé qui ouvre votre appartement.
Le access-token ne fournit pas à l'application les information d'authentification.

Au lieu d'utiliser les identifiants de l'utilisateur, le _client_ obtient un access-token. Ce access-token définit les _scopes_ i.e. le périmètre d'accès consenti par l'utilisateur. Le access-token a une durée de vie courte (minutes). Les access-token sont fournis par le _authorization server_ (_token issuer_) avec l'approbation de l'utilisateur final.

![oauth2_authorization_code_grant](./doc/oauth2_authorization_code_grant.png?raw=true)

#### OAuth découpe authentification et authorization

[https://oauth.net/articles/authentication/](https://oauth.net/articles/authentication/)

Dans OAuth, le access-token n'est en fait pas destiné à l'application cliente. 
**L'application cliente est authorisée à présenter le access-token** au _resource server_.

> "The client is not the intended _audience_ of the OAuth access token. Instead, it is 
> the authorized presenter of that token, and the _audience_ is in fact the protected resource. The protected resource is not 
> generally going to be in a position to tell if the user is still present by the token alone, since by the very nature and 
> design of the OAuth protocol the user will not be available on the connection between the client and protected resource"

C'est le principe de OAuth : l'authentification est découplée de l'authorization. Au moment où l'application accède à 
la _resource_, l'utilisateur propriétaire de la ressource n'intervient pas. Il a déjà préalablement donné son accord.

### De OAuth 2.0 à OpenID Connect ?

On peut construire un protocole **d'authentification et d'identité** sur le protocole 
 OAuth 2.0 **d'authorisation et de délégation** [source](https://oauth.net/articles/authentication/)

L'utlisateur va déléguer l'accès à une ressource particulière : son identité. L'application client va alors accéder à 
la _resource_ identité (une API) pour ainsi découvrir **qui** en a autorisé l'accès à l'orgine.

> As it turns out, though, there are a handful of things that can be used along with OAuth to create an authentication and identity 
> protocol on top of this delegation and authorization protocol. In nearly all of these cases, the core functionality of OAuth 
> remains intact, and what's happening is that the user is delegating access to their identity to the application they're trying to log in to. 
> The client application then becomes a consumer of the identity API, thereby finding out who authorized the client in the first place. 
> One major benefit of building authentication on top of authorization in this way is that it allows for management of end-user consent, 
> which is very important in cross-domain identity federation at internet scale. Another important benefit is that the user can 
> delegate access to other protected APIs along side their identity at the same time, making it much simpler for both application developers 
> and end users to manage. With one call, an application can find out if a user is logged in, what the app should call the user, 
> download photos for printing, and post updates to their message stream. 

Cette _resource_ est fournie par le _identity provider_ (_authorization server_)

> "OpenID Connect's ID Token provides a secondary token along side the access token that communicates the authentication information directly to the client."

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

Exemples de _claims_ :
* Subject
* Issuing Authority
* Audience
* Issue date
* Expiration Date

### Notes Keycloak

Un adapter est une "librairie ++" pour un type d'application. Par exemple :
* adapter OpenID Connect pour Spring Boot [source](https://www.keycloak.org/docs/latest/securing_apps/index.html#_spring_boot_adapter)
* adapter OpenID Connect pour Spring Security [source](https://www.keycloak.org/docs/latest/securing_apps/index.html#_spring_security_adapter)
