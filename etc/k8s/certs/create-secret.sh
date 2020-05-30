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
kubectl --cluster=default-cluster --namespace "$NAMESPACE" delete secret "pki-$NAME-secret"
kubectl --cluster=default-cluster create secret tls "pki-$NAME-secret" --cert=tls.crt --key=tls.key --certificate-authority=/etc/pki/CA/cacert.pem --namespace=$NAMESPACE
rm -f tls.crt tls.key

