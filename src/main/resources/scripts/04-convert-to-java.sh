#!/bin/bash
# Script: 04-convert-to-java.sh
# Description: Convertit les certificats et clés en formats Java (PKCS12 et JKS).
# Ce script doit être exécuté après 01-ca.sh, 02-server.sh et 03-clients.sh.

echo "--- Étape 4 : Conversion des Certificats en Formats Java Keystore/Truststore ---"

# Stocker le chemin absolu du répertoire de base du projet (où ce script est exécuté)
PROJECT_ROOT=$(pwd)

# Chemin absolu du certificat de la CA
CA_CRT_ABSOLUTE_PATH="$PROJECT_ROOT/certs/ca/ca.crt"

# Vérifier si le certificat de la CA existe avant de commencer
if [ ! -f "$CA_CRT_ABSOLUTE_PATH" ]; then
    echo "Erreur : Le certificat de la CA n'a pas été trouvé à $CA_CRT_ABSOLUTE_PATH."
    echo "Assurez-vous d'avoir exécuté 01-ca.sh en premier."
    exit 1
fi

# ----- Pour le Serveur -----
echo "Conversion des certificats serveur..."
# Naviguer dans le répertoire du serveur
cd "$PROJECT_ROOT/certs/server" || { echo "Erreur: Impossible de naviguer vers certs/server. Arrêt du script."; exit 1; }

# Vérifier l'existence des fichiers nécessaires
if [ ! -f "server.crt" ] || [ ! -f "server.key" ]; then
    echo "Erreur: server.crt ou server.key manquant(s) dans certs/server/. Assurez-vous d'avoir exécuté 02-server.sh."
    cd "$PROJECT_ROOT"
    exit 1
fi

# Créer le keystore du serveur (server.p12)
openssl pkcs12 -export -in server.crt -inkey server.key -certfile "$CA_CRT_ABSOLUTE_PATH" -name "server_cert" -out server.p12 -passout pass:changeit || { echo "Erreur OpenSSL lors de la création de server.p12. Arrêt du script."; cd "$PROJECT_ROOT"; exit 1; }

# Créer le truststore du serveur (server_truststore.jks)
keytool -import -trustcacerts -alias ca -file "$CA_CRT_ABSOLUTE_PATH" -keystore server_truststore.jks -storepass changeit -noprompt || { echo "Erreur Keytool lors de la création de server_truststore.jks. Arrêt du script."; cd "$PROJECT_ROOT"; exit 1; }

echo "Certificats serveur convertis avec succès."

# Retour au répertoire racine du projet
cd "$PROJECT_ROOT"

# ----- Pour les Clients (trouve tous les dossiers clients et les traite) -----
echo "Conversion des certificats clients..."

# Parcourir tous les sous-répertoires dans certs/clients/
for client_dir_path in "$PROJECT_ROOT"/certs/clients/*/; do
    # Extraire le nom du dossier du client (ex: client1, client2)
    CLIENT_FOLDER_NAME=$(basename "$client_dir_path")
    echo "Traitement du client : $CLIENT_FOLDER_NAME"

    # Naviguer dans le répertoire spécifique du client
    cd "$client_dir_path" || { echo "Erreur: Impossible de naviguer vers $client_dir_path. Skipping ce client."; continue; }

    # Vérifier l'existence des fichiers du client
    if [ ! -f "$CLIENT_FOLDER_NAME.crt" ] || [ ! -f "$CLIENT_FOLDER_NAME.key" ]; then
        echo "Avertissement : Certificat ($CLIENT_FOLDER_NAME.crt) ou clé privée ($CLIENT_FOLDER_NAME.key) du client $CLIENT_FOLDER_NAME manquants. Skipping."
        cd "$PROJECT_ROOT" # Retour au répertoire racine avant de continuer
        continue
    fi

    # Créer le keystore du client (clientN.p12)
    # Notez que .crt et .key sont relatifs au répertoire client actuel,
    # mais $CA_CRT_ABSOLUTE_PATH est absolu, donc toujours correct.
    openssl pkcs12 -export -in "$CLIENT_FOLDER_NAME".crt -inkey "$CLIENT_FOLDER_NAME".key -certfile "$CA_CRT_ABSOLUTE_PATH" -name "${CLIENT_FOLDER_NAME}_cert" -out "$CLIENT_FOLDER_NAME".p12 -passout pass:changeit || { echo "Erreur OpenSSL lors de la création de ${CLIENT_FOLDER_NAME}.p12. Skipping ce client."; cd "$PROJECT_ROOT"; continue; }

    # Créer le truststore du client (clientN_truststore.jks)
    keytool -import -trustcacerts -alias ca -file "$CA_CRT_ABSOLUTE_PATH" -keystore "${CLIENT_FOLDER_NAME}_truststore.jks" -storepass changeit -noprompt || { echo "Erreur Keytool lors de la création de ${CLIENT_FOLDER_NAME}_truststore.jks. Skipping ce client."; cd "$PROJECT_ROOT"; continue; }

    echo "Certificats client '$CLIENT_FOLDER_NAME' convertis avec succès."

    # Retour au répertoire racine du projet avant la prochaine itération
    cd "$PROJECT_ROOT"
done

echo "Conversion terminée. Fichiers Java KeyStore (.p12) et TrustStore (.jks) générés."
echo "N'oubliez pas de copier les fichiers nécessaires vers leurs emplacements respectifs et de mettre à jour vos configurations."