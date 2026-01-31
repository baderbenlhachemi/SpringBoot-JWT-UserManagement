# JWT User Management

Ce projet est une application développée avec `Spring Boot` et `Spring Security`, générant des utilisateurs avec des données vraisemblables (nom, adresse, téléphone...) grâce à la bibliothèque `Faker`. Chaque utilisateur reçoit un nom d'utilisateur, une adresse email et un mot de passe pour se connecter et obtenir un `JWT` afin de gérer l'authentification et les autorisations. Les utilisateurs sont assignés aléatoirement à des rôles `admin` ou `user`, chacun disposant de ses propres autorisations spécifiques.

Le projet comprend plusieurs endpoints pour générer des utilisateurs, importer des utilisateurs à partir d'un fichier `JSON`, authentifier les utilisateurs et consulter les profils des utilisateurs.

## 🚀 Quick Start

### Default Admin Credentials

A default admin user is automatically created when the application starts:

| Field | Value |
|-------|-------|
| **Username** | `admin` |
| **Password** | `admin` |
| **Role** | Administrator |

### Running the Application

1. **Start the Spring Boot backend:**
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

2. **Open the JavaFX client** (see below) or use Swagger UI:
   - Swagger UI: http://localhost:9090/swagger-ui/index.html

3. **Login with:**
   - Username: `admin`
   - Password: `admin`

## 🖥️ JavaFX Client Application

A modern JavaFX desktop client is available in the `javafx-client` folder. It provides a beautiful dark-themed UI to interact with all API endpoints.

### Features
- 🔐 JWT Authentication with animated login screen
- 👤 User profile viewing
- 👥 Fake user generation and download
- 📁 Batch user import from JSON files
- 🔍 Admin user lookup

### Running the JavaFX Client

```bash
cd javafx-client
..\mvnw.cmd javafx:run
```

Or simply run `javafx-client\run.bat` on Windows.

> **Note:** Make sure the Spring Boot backend is running on `localhost:9090` before starting the client.

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
