# JWT User Management

Ce projet est une application développée avec `Spring Boot` et `Spring Security`, générant des utilisateurs avec des données vraisemblables (nom, adresse, téléphone...) grâce à la bibliothèque `Faker`. Chaque utilisateur reçoit un nom d'utilisateur, une adresse email et un mot de passe pour se connecter et obtenir un `JWT` afin de gérer l'authentification et les autorisations. Les utilisateurs sont assignés aléatoirement à des rôles `admin` ou `user`, chacun disposant de ses propres autorisations spécifiques.

Le projet comprend plusieurs endpoints pour générer des utilisateurs, importer des utilisateurs à partir d'un fichier `JSON`, authentifier les utilisateurs et consulter les profils des utilisateurs.

## Endpoints

### 1. Génération d'utilisateurs

**Méthode :** GET  
**URL :** /api/users/generate  
**Content-Type :** application/json  
**Sécurisé :** Non  
**Paramètres :**
- count: number

Cet endpoint génère un fichier JSON contenant un certain nombre d'utilisateurs avec des données aléatoires.

### 2. Upload du fichier utilisateurs et création des utilisateurs en base de données

**Méthode :** POST  
**URL :** /api/users/batch  
**Content-Type :** multipart/form-data  
**Sécurisé :** Non  
**Paramètres :**
- file: multipart-file

Cet endpoint permet d'importer des utilisateurs à partir d'un fichier JSON. Les utilisateurs sont ensuite enregistrés dans la base de données.

### 3. Connexion utilisateur + génération JWT

**Méthode :** POST  
**URL :** /api/auth  
**Content-Type :** application/json  
**Request-Body :**
- username: string
- password: string

Cet endpoint permet d'authentifier un utilisateur et de générer un token JWT.

### 4. Consultation de mon profil

**Méthode :** GET  
**URL :** /api/users/me  
**Sécurisé :** Oui

Cet endpoint permet à un utilisateur de consulter son propre profil.

### 5. Consultation d'un profil

**Méthode :** GET  
**URL :** /api/users/{username}  
**Sécurisé :** Oui

Cet endpoint permet à un administrateur de consulter le profil de n'importe quel utilisateur.

## Contraintes

- Le projet est réalisé en utilisant Maven, Java17 et la dernière version du framework SpringBoot.
- La base de données utilisée est: MySQL.
- L'application démarre dans le port 9090.
- L'application démarre sans aucune configuration manuelle.
- Le projet expose un endpoint Swagger en utilisant https://springdoc.org/ et tous les endpoints sont testables depuis l'interface Swagger ici: (http://localhost:9090/swagger-ui/index.html).
