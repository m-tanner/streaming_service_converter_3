#!/bin/bash
set -xeuo

kubectl config use-context docker-desktop
# this only works with k8s v1.21.5!
# for 1.22 and above use 1.0.X!
# for more info, see https://kubernetes.io/blog/2021/07/26/update-with-ingress-nginx/
# for newest version, see https://github.com/kubernetes/ingress-nginx/releases
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.49.3/deploy/static/provider/cloud/deploy.yaml
kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=120s

kubectl apply -f apex-service/k8s/dev/namespace.yml
kubectl config set-context --current --namespace=development

kubectl apply -f apex-service/k8s/dev/secret.yml
