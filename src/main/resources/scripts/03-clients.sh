#!/bin/bash
# Script: 03-clients.sh
# Description: Crée le certificat et la clé privée pour un client spécifique.
# Usage: bash 03-clients.sh <client_name> <common_name>

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Usage: bash 03-clients.sh <client_folder_name> <common_name_for_cert>"
    echo "Example: bash 03-clients.sh client1 rewrite-cli-client-1"
    exit 1
fi

CLIENT_FOLDER_NAME="$1"
CLIENT_COMMON_NAME="$2"

echo "--- Étape 3 : Création du Certificat pour le client '$CLIENT_COMMON_NAME' ---"

# Crée le dossier pour le client
mkdir -p certs/clients/"$CLIENT_FOLDER_NAME"
cd certs/clients/"$CLIENT_FOLDER_NAME"

echo "Génération de la clé privée du client ($CLIENT_FOLDER_NAME.key)..."
openssl genrsa -out "$CLIENT_FOLDER_NAME".key 2048

echo "Génération de la demande de signature de certificat (CSR) pour le client ($CLIENT_FOLDER_NAME.csr)..."
openssl req -new -key "$CLIENT_FOLDER_NAME".key -out "$CLIENT_FOLDER_NAME".csr -subj "/C=FR/ST=IleDeFrance/L=Nanteuil-les-Meaux/O=RewriteApp/OU=Client/CN=$CLIENT_COMMON_NAME"

echo "Signature du CSR du client avec la CA ($CLIENT_FOLDER_NAME.crt)..."
# La ligne ci-dessous utilise la substitution de processus <() qui nécessite Bash
openssl x509 -req -in "$CLIENT_FOLDER_NAME".csr -CA ../../ca/ca.crt -CAkey ../../ca/ca.key -CAcreateserial -out "$CLIENT_FOLDER_NAME".crt -days 365 -sha256 -extfile <(printf "extendedKeyUsage=clientAuth")

echo "Certificat du client '$CLIENT_COMMON_NAME' créé avec succès dans certs/clients/$CLIENT_FOLDER_NAME/"

# Retour au répertoire de base du projet
cd ../../..