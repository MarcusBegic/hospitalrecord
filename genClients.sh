#!/bin/zsh

NAME=$1
mkdir $NAME
cd $NAME

keytool -import -file ~/hospitalrecord/CA.crt -alias CA -keystore "${NAME}truststore"
keytool -keystore "${NAME}keystore" -genkey -alias clientkeypair
keytool -certreq -alias clientkeypair -keystore "${NAME}keystore" -file client.csr
openssl  x509  -req  -CA ~/hospitalrecord/CA.crt -CAkey ~/hospitalrecord/key.key -in client.csr -out client.cer  -days 365  -CAcreateserial -passin pass:$NAME
keytool -keystore "${NAME}keystore" -alias CARoot -import -file ~/hospitalrecord/CA.crt
keytool -import -keystore "${NAME}keystore" -file client.cer -alias clientkeypair



# keytool -import -file cert.pem -alias CA -keystore "${NAME}truststore"


# keytool -genkey -alias key_pair_id  -keyalg RSA -validity 365 -keystore "${NAME}keystore" -storetype JKS


# keytool -certreq -alias key_pair_id  -file csr -keystore "${NAME}keystore" 

# openssl x509 -req -in csr -CA cert.pem -CAkey key.pem -CAcreateserial -out certificate_chain.pem

# keytool -importcert -alias CA -file cert.pem -keystore "${NAME}keystore" 

# keytool -importcert -alias key_pair_id -file certificate_chain.pem -keystore "${NAME}keystore" 


# # keytool -keystore "${NAME}keystore" -genkey -alias keypair

# keytool -keystore "${NAME}keystore" -certreq -alias keypair -file client.csr

# openssl x509 -req -CA CA.crt -CAkey privkey.pem -in client.csr -out cert-signed-s -days 365 -CAcreateserial -passin pass:$NAME

# keytool -keystore "${NAME}keystore" -alias CARoot -import -file CA.crt

# keytool -import -keystore "${NAME}keystore" -file cert-signed-s -alias keypair

# keytool -import -file CA.crt -alias CA -keystore "${NAME}truststore"





