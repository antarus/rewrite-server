#!/bin/bash
# Script: 02-server.sh
# Description: Crée le certificat et la clé privée pour le serveur.

echo "--- Étape 2 : Création du Certificat du Serveur ---"

# Crée le dossier pour le serveur
mkdir -p certs/server
cd certs/server

echo "Génération de la clé privée du serveur (server.key)..."
openssl genrsa -out server.key 2048

echo "Génération de la demande de signature de certificat (CSR) pour le serveur (server.csr)..."
# Le CN (Common Name) doit correspondre au hostname du serveur (ex: localhost)
# subjectAltName est crucial pour les clients modernes et doit inclure 'localhost' et '127.0.0.1'
openssl req -new -key server.key -out server.csr -subj "/C=FR/ST=IleDeFrance/L=Nanteuil-les-Meaux/O=RewriteApp/OU=Server/CN=localhost" -addext "subjectAltName = DNS:localhost,IP:127.0.0.1"

echo "Signature du CSR du serveur avec la CA (server.crt)..."
# La ligne ci-dessous utilise la substitution de processus <() qui nécessite Bash
openssl x509 -req -in server.csr -CA ../ca/ca.crt -CAkey ../ca/ca.key -CAcreateserial -out server.crt -days 365 -sha256 -extfile <(printf "subjectAltName=DNS:localhost,IP:127.0.0.1\nextendedKeyUsage=serverAuth")

echo "Certificat du serveur créé avec succès dans certs/server/"

# Retour au répertoire de base du projet
cd ../..