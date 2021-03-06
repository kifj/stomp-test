#!/bin/sh
NAME=$1
NAMESPACE=$2

if [ -z "$NAME" ]; then
  echo "Usage $0: service [namespace] - create kubernetes TLS secret for service"
  exit 1
fi

if [ -z "$NAMESPACE" ]; then
  NAMESPACE=default
fi

cp "$NAME.crt" tls.crt
cp "$NAME-key.pem" tls.key
kubectl --namespace "$NAMESPACE" delete secret "pki-$NAME-secret"
kubectl create secret tls "pki-$NAME-secret" --cert=tls.crt --key=tls.key --certificate-authority=kind-ca.crt --namespace=$NAMESPACE
rm -f tls.crt tls.key

