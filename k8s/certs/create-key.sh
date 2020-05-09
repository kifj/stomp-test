#!/bin/sh
NAME=$1

if [ -z "$NAME" ]; then
  echo "Usage $0: service - create TLS key"
  exit 1
fi

if [ -f "$NAME-key.pem" ]
then
  echo "Create CSR for $NAME"
  cat "$NAME-cert.json" | cfssl gencsr -key "$NAME-key.pem" - | cfssljson -bare "$NAME"
else
  echo "Create Key and CSR for $NAME"
  cat "$NAME-cert.json" | cfssl genkey - | cfssljson -bare "$NAME"
fi
