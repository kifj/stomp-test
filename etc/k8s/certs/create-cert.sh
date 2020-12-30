#!/bin/sh
NAME=$1
NAMESPACE=$2

if [ -z "$NAME" ]; then
  echo "Usage $0: service [namespace] - retrieve TLS certificate from kubernetes"
  exit 1
fi

if [ -z "$NAMESPACE" ]; then
  NAMESPACE=default
fi

kubectl get csr $NAME.$NAMESPACE -o jsonpath='{.status.certificate}' | base64 --decode > "$NAME.crt"
