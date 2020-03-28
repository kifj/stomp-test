#!/bin/sh
NAME=$1
NAMESPACE=$2

if [ -z "$NAME" ]; then
  echo "Usage $0: service [namespace] - create TLS csr"
  exit 1
fi

if [ -z "$NAMESPACE" ]; then
  NAMESPACE=default
fi

cat <<EOF > "$NAME-csr.yaml"
apiVersion: certificates.k8s.io/v1beta1
kind: CertificateSigningRequest
metadata:
  name: $NAME.$NAMESPACE
spec:
  request: $(cat "$NAME".csr | base64 | tr -d '\n')
  usages:
  - digital signature
  - key encipherment
  - server auth
EOF

kubectl apply -f "$NAME-csr.yaml"
kubectl certificate approve $NAME.$NAMESPACE

