# Rewrite server

## Prerequisites

### Java

You need to have Java 21:

- [JDK 21](https://openjdk.java.net/projects/jdk/21/)

### Node.js and NPM

Before you can build this project, you must install and configure the following dependencies on your machine:

[Node.js](https://nodejs.org/): We use Node to run a development web server and build the project.
Depending on your system, you can install Node either from source or as a pre-packaged bundle.

After installing Node, you should be able to run the following command to install development tools.
You will only need to run this command when dependencies change in [package.json](package.json).

```
npm install
```

## Local environment

- [Local server](http://localhost:8080)

<!-- jhipster-needle-localEnvironment -->

## generate certificat

cd src/main/resources

# Nettoyage initial (optionnel, supprime le dossier certs existant)

rm -rf certs/

# 1. Créer la CA

./scripts/01-ca.sh

# 2. Créer le certificat du serveur

./scripts/02-server.sh

# 3. Créer les certificats des clients (répétez pour chaque client)

./scripts/03-clients.sh client1 rewrite-cli-client-1
./scripts/03-clients.sh client2 rewrite-cli-client-2

# ... autres clients ...

# 4. Convertir en formats Java

./scripts/04-convert-to-java.sh

echo "Processus de génération de certificats terminé."

pour configurer le client:

```bash
mkdir -p ~/.rewrite/certs/client1
mkdir -p ~/.rewrite/certs/ca
cp src/main/resources/certs/clients/client1/client1.p12 ~/.rewrite/certs/client1/
cp src/main/resources/certs/clients/client1/client1_truststore.jks ~/.rewrite/certs/client1/
cp src/main/resources/certs/ca/ca.crt ~/.rewrite/certs/ca

nano ~/.rewrite/config.yaml
```

```yaml
apiVersion: v1
currentContext: my-secure-context # Nom du contexte par défaut
clusters:
  - name: my-secure-cluster
    cluster:
      server: https://localhost:8443/api/rewrite # URL de votre serveur HTTPS
      # Utilisez l'une des options suivantes pour le certificat CA du serveur
      certificateAuthorityFile: ~/.rewrite/certs/ca/ca.crt # Chemin vers le certificat de la CA racine pour valider le serveur
      # Ou, si vous l'encodez en base64 dans le fichier de config (moins recommandé pour la lisibilité)
      # certificateAuthorityData: <Base64 encodé de certs/ca/ca.crt>
      insecureSkipTlsVerify: false # Mettez à 'true' UNIQUEMENT pour le DEV/TEST, JAMAIS en PROD !
users:
  - name: my-secure-user
    user:
      username: votre_nom_utilisateur_git # Utilisé pour le commit Git
      gitPatForGit: votre_pat_git_pour_repo # Votre PAT Git pour cloner/pousser le dépôt
      gitPatForApi: votre_pat_api_pour_pr_mr # Votre PAT pour créer des PR/MR (peut être le même que gitPatForGit)
      # Utilisez l'une des options suivantes pour le certificat client
      clientKeystorePath: ~/.rewrite/certs/clients/client1/client1.p12 # Chemin vers votre keystore client
      clientKeystorePassword: changeit # Mot de passe de votre keystore client
      clientTruststorePath: ~/.rewrite/certs/clients/client1/client1_truststore.jks # Chemin vers votre truststore client (pour valider le serveur)
      clientTruststorePassword: changeit # Mot de passe de votre truststore client
contexts:
  - name: my-secure-context
    context:
      cluster: my-secure-cluster
      user: my-secure-user
```

compiler le client avec :

mvn clean package

puis executer le client avec la commande de base suivante

java -jar rewrite-cli-client/target/rewrite-cli-client-1.0.0-SNAPSHOT-jar-with-dependencies.jar --help

java -jar target/rewrite-cli-client-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
 --repo-url https://github.com/openrewrite/spring-petclinic-migration \
 --recipe org.openrewrite.java.format.AutoFormat \
 --git-pat votre_pat_pour_git \
 --api-pat votre_pat_pour_api \
 --platform github

## Start up

```bash
./mvnw
```

```bash
docker compose -f src/main/docker/redis.yml up -d
```

```bash
docker compose -f src/main/docker/postgresql.yml up -d
```

<!-- jhipster-needle-startupCommand -->

## Documentation

- [Hexagonal architecture](documentation/hexagonal-architecture.md)
- [Package types](documentation/package-types.md)
- [Assertions](documentation/assertions.md)
- [sonar](documentation/sonar.md)
- [Redis](documentation/redis.md)
- [Logs Spy](documentation/logs-spy.md)
- [CORS configuration](documentation/cors-configuration.md)
- [PostgreSQL](documentation/postgresql.md)

<!-- jhipster-needle-documentation -->
