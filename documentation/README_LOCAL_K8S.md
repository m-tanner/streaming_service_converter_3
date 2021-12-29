## Manual Local Kubernetes Setup Instructions

This can all be done for you by `setup-k8s-for-mac.sh` and `setup-docker-desktop-k8s.sh`, but here are the manual steps
in case you want to walk through it on your own.

1) Brew install necessary software tools. For OS X, this is:
   ```shell
   brew install kubernetes-cli
   brew install kustomize
   brew install skaffold
   ```
2) Install [container-structure-tests](https://github.com/GoogleContainerTools/container-structure-test#installation)
3) Setup `$HOME/.kube/config` file. It will probably look like this automatically. It's here for reference only.
   ```yaml
   apiVersion: v1
   clusters:
   - cluster:
       certificate-authority-data: <REDACTED>
       server: https://kubernetes.docker.internal:6443
     name: docker-desktop
   - cluster:
       insecure-skip-tls-verify: true
       server: https://kubernetes.ecom-b.ag1.taservs.net:443
     name: ecom-b.ag1.taservs.net
   - cluster:
       insecure-skip-tls-verify: true
       server: https://kubernetes.ecom-b.taservs.net:443
     name: ecom-b.taservs.net
   - cluster:
       insecure-skip-tls-verify: true
       server: https://kubernetes.ecom-b.us2.taservs.net:443
     name: ecom-b.us2.taservs.net
   - cluster:
       certificate-authority: /Users/mtanner/.minikube/ca.crt
       extensions:
       - extension:
         last-update: Thu, 08 Apr 2021 14:14:25 PDT
         provider: minikube.sigs.k8s.io
         version: v1.18.1
       name: cluster_info
     server: https://127.0.0.1:63165
     name: minikube
   contexts:
   - context:
       cluster: docker-desktop
       user: docker-desktop
     name: docker-desktop
   - context:
       cluster: ecom-b.ag1.taservs.net
       user: mtanner-ecom-b.ag1.taservs.net
     name: ecom-b.ag1.taservs.net
   - context:
       cluster: ecom-b.taservs.net
       namespace: ecom
       user: mtanner-ecom-b.taservs.net
     name: ecom-b.taservs.net
   - context:
       cluster: ecom-b.us2.taservs.net
       user: mtanner-ecom-b.us2.taservs.net
     name: ecom-b.us2.taservs.net
   - context:
       cluster: minikube
       extensions:
       - extension:
           last-update: Thu, 08 Apr 2021 14:14:25 PDT
           provider: minikube.sigs.k8s.io
           version: v1.18.1
         name: context_info
       namespace: default
       user: minikube
     name: minikube
   current-context: docker-desktop
   kind: Config
   preferences: {}
   users:
   - name: docker-desktop
     user:
       client-certificate-data: <REDACTED>
       client-key-data: <REDACTED>
   - name: minikube
     user:
       client-certificate: /Users/mtanner/.minikube/profiles/minikube/client.crt
       client-key: /Users/mtanner/.minikube/profiles/minikube/client.key
   - name: mtanner-ecom-b.ag1.taservs.net
     user:
       auth-provider:
         config:
           client-id: kube-login
            client-secret: <REDACTED>
            id-token: <REDACTED>
            idp-issuer-url: https://idp.ecom-b.ag1.taservs.net/dex
            refresh-token: <REDACTED>
            name: oidc
    ```

4) Start Kubernetes locally using Docker Desktop.
   1) Ensure that, under Resources > File Sharing, you've mounted $HOME
   2) You must be on k8s 1.21.5 for these instructions to work!
   ```shell
   # should you prefer minikube, use 
   minikube start --driver hyperkit --kubernetes-version v1.21.5 --addons ingress --mount-string "$HOME:$HOME" --mount
   # no promises when using the rest of these instructions, though!
   ```

5) Setup Kubernetes locally
   ```shell
   kubectl config use-context docker-desktop
   
   kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.49.3/deploy/static/provider/cloud/deploy.yaml
   
   # wait until the ingress controller is running
   kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=120s
   
   # ensure that the ingress controller has been assigned an IP address (not <pending>)
   kubectl get services -n ingress-nginx
   # NAME                                 TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)                      AGE
   # ingress-nginx-controller             LoadBalancer   10.107.34.115   localhost     80:32624/TCP,443:31616/TCP   51s
   # ingress-nginx-controller-admission   ClusterIP      10.99.217.232   <none>        443/TCP                      51s
   
   # make the namespace you'll use for the particular environment
   kubectl apply -f apex-service/k8s/dev/namespace.yml
   
   # set kubectl to use that namespace
   kubectl config set-context --current --namespace=development
   
   # create the required secret manually
   # only for local development!
   kubectl apply -f apex-service/k8s/dev/secret.yml
   ```
   1) To generate secret values, which must be encoded in base64
   ```shell
   echo -n "value to convert" | base64
   ```

6) Add `apex.evidence.come` to your `/etc/hosts` file. Don't forget `sudo`!
   ```text
   ##
   # Host Database
   #
   # localhost is used to configure the loopback interface
   # when the system is booting.  Do not change this entry.
   ##
   127.0.0.1	    localhost
   255.255.255.255	broadcasthost
   ::1              localhost
   127.0.0.1        apex.evidence.com
   
   # Added by Docker Desktop
   # To allow the same kube context to work on the host and the container:
   127.0.0.1        kubernetes.docker.internal
   # End of section
   ```

7) Run skaffold
   ```bash
   # like a ci/cd pipeline would, build and deploy the app one time
   skaffold run
   
   # or to tail the app logs after deployment 
   skaffold run --tail
   ```
