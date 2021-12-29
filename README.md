# APEX

"I just want to jump into the code!" See: [Quickstart](#Quickstart)

Language: Scala 2.13

Runtime: Java 11

Web Framework: Play 2.8.X

Containerization: Docker

## Technologies Used

### Scala/sbt/Play

You must have a Java JDK installed on your machine. See [quickstart](#Quickstart) for instruction.

An installation of `sbt` is packaged with the repo so that build runners and docker images have access to a
distribution. But you can also rely on this as a developer by running commands like:

```shell
# clean the target folders, compile all subprojects, run all tests
./bin/sbt clean compile test

# run
./bin/sbt run

# the above commands can also be run separately
./bin/sbt clean
./bin/sbt compile
./bin/sbt test
```

You can also [install sbt](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Mac.html) locally on your machine, but
this is not required.

For help, you can take a look
at [Scala's official getting started docs](https://docs.scala-lang.org/getting-started/index.html).

After you're all setup, be sure to check out [Twitter's Scala School](https://twitter.github.io/scala_school/) if you've
never written Scala before.

### Docker

#### Intro

We use Docker to produce containers that Kubernetes can orchestrate. For this explained in an image,
see [containers](./documentation/k8s.svg).

#### Setup

[Get started with Docker](https://docs.docker.com/get-started/)

#### Base Image Selection Explained

Several base images were considered:

- graalvm
- corretto
- corretto-alpine

GraalVM is huge. We don't need most of what it does. This should be reconsidered if we want to consider `native-image`.

Corretto is nice, but requires we go to their servers for package updates (to get bash, unzip, etc). This is blocked by
Axon firewalls.

Corretto-alpine was selected for its simplicity and because it's based on alpine, package installation (bash, unzip,
etc) come from an offical linux mirror, which is not blocked by firewalls.

The docker image build is done in several layers and all layers are pushed to image storage. This should make us
resilient to dependency unavailability, since we would be able to rely on these "cached" images.

#### Useful Commands

```bash
# to inspect the filesytem of an image at rest 
docker run --rm -it --entrypoint=/bin/bash streaming_service_converter_3

# build from the repo root
docker build . --file ./src/docker/Dockerfile --tag streaming_service_converter_3:latest --build-arg SERVICE_VERSION="0.1.0-SNAPSHOT"
# add `--target build0` to stop at the build0 stage

# run, mapping 8080 on your machine to 9000 on the container
docker run -p 8080:9000 streaming_service_converter_3

# cleanup your system
docker system prune -a -f --volumes
```

### Kubernetes (k8s) and Kustomize

#### Intro

For a professional explanation, see the
official "[Kubernetes Explained](https://kubernetes.io/docs/concepts/overview/what-is-kubernetes/)."

We use an Ingress to let traffic into our cluster and find our Service, which load balances the traffic across all the
pods in our Deployment.

#### Key Kubernetes Concepts

- [Deployments](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)
- [Services](https://kubernetes.io/docs/concepts/services-networking/service/)
- [Ingresses](https://kubernetes.io/docs/concepts/services-networking/ingress/)
- [Config Maps](https://kubernetes.io/docs/concepts/configuration/configmap/)
- [Secrets](https://kubernetes.io/docs/concepts/configuration/secret/)

#### Kustomize

Kustomize offers basic class-based inheritance and overrides for k8s yaml files. Check out `./apex-service/k8s` to see
it in action.

- [Kustomize](https://kubectl.docs.kubernetes.io/references/kustomize/kustomization/)

#### Skaffold

Skaffold allows developers to turn their laptops into CI/CD machines. It speeds up the local development cycle. It can
also deploy to remote servers, but we have TeamCity, `k8s-deploy`, and `argo-apps` for that.

- [Docs](https://skaffold.dev/docs/)
- [Quickstart](https://skaffold.dev/docs/quickstart/)

#### Useful Links

You'll need to have this cheat sheet open when learning your way around `kubectl`!

- [Kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)

# Quickstart

## Steps

1) Install [Git](https://git-scm.com/downloads)
2) Then clone this repository
3) Install [Homebrew](https://brew.sh/)
   ```shell
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"
   ```
   OR ensure brew is up-to-date
   ```shell
   brew upgrade
   ```
4) Then install a JDK
   ```shell
   brew tap AdoptOpenJDK/openjdk
   brew install --cask adoptopenjdk11
   java -version
   # confirm the installed version is what you intended
   ```
   OR [install GraalVM on Mac](https://www.graalvm.org/docs/getting-started/macos/)
5) Install Docker Desktop
   1) [Intel Mac](https://desktop.docker.com/mac/main/amd64/Docker.dmg)
   2) [M Mac](https://desktop.docker.com/mac/main/arm64/Docker.dmg)
6) Ensure Docker Desktop is totally up to date! This may take several updates.
   1) Enable Kubernetes in Docker Desktop
   2) Verify Kubernetes version v1.21.5
7) Run the following:
   ```shell
   /bin/bash setup-k8s-for-mac.sh
   
   /bin/bash setup-docker-desktop-k8s.sh
   
   skaffold run
   ```
8) Or if you prefer to do the setup manually, see [that readme](./documentation/README_LOCAL_K8S.md)

## Scripts and Commands

#### Local Computer

```shell
# with docker desktop kubernetes running,
# setup all the namespaces and required resources
# then build and deploy the app!
/bin/bash setup-docker-desktop-k8s.sh

# setup your mac with the tools necessary for k8s
/bin/bash setup-k8s-for-mac.sh
```

#### Skaffold Commands

```shell
# stop at the build
skaffold build

# like a ci/cd pipeline would, build and deploy the app one time
skaffold run
   
# or to tail the app logs after deployment 
skaffold run --tail

# build and deploy your app every time your code saves/changes
skaffold dev
```

## IntelliJ Tips

If you are seeing errors in build.sbt files regarding trailing commas, manually enable trailing commas for the project
in `Preferences | Languages & Frameworks | Scala | Misc | Trailing commas`

You will need to restart Intellij for this change to take effect
