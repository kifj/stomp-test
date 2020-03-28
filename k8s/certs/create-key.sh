#!/bin/sh
NAME=$1

if [ -z "$NAME" ]; then
  echo "Usage $0: service [namespace] - create TLS key"
  exit 1
fi

cat "$NAME-cert.json" | cfssl genkey - | cfssljson -bare "$NAME"
