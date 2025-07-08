#!/bin/bash
# Script: 01-ca.sh
# Description: Crée l'Autorité de Certification (CA) racine.

echo "--- Étape 1 : Création de l'Autorité de Certification (CA) ---"

# Crée le dossier pour la CA
mkdir -p certs/ca
cd certs/ca

echo "Génération de la clé privée de la CA (ca.key)..."
openssl genrsa -out ca.key 2048

echo "Génération du certificat de la CA (ca.crt) - auto-signé..."
openssl req -x509 -new -nodes -key ca.key -sha256 -days 3650 -out ca.crt -subj "/C=FR/ST=IleDeFrance/L=Nanteuil-les-Meaux/O=RewriteApp/OU=CA/CN=RewriteRootCA"

openssl x509 -in ca.crt -out ca.pem -outform PEM

# Crée un fichier de série pour la CA (nécessaire pour signer les certificats)
echo 01 > ca.srl

echo "CA créée avec succès dans certs/ca/"

# Retour au répertoire de base du projet
cd ../..