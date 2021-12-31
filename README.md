# Streaming Service Converter

"I just want to jump into the code!" See: [Quickstart](#Quickstart)

Language: Scala 2.13

Runtime: Java 11

Web Framework: Play 2.8.X

Containerization: Docker

Deployment: Google Cloud Run

## Technologies Used

### Scala/sbt/Play

You must have a Java JDK installed on your machine. See [quickstart](#Quickstart) for instruction.

An installation of `sbt` is packaged with the repo so that build runners and docker images have access to a
distribution. But you can also rely on this as a developer by running commands like:

```shell
# clean the target folders, format all files, compile all subprojects, run all tests
./bin/sbt clean fmt compile test

# run
./bin/sbt run

# the above commands can also be run separately
./bin/sbt clean
./bin/sbt fmt
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

#### Useful Commands

```bash
# to inspect the filesytem of an image at rest 
docker run --rm -it --entrypoint=/bin/bash streaming_service_converter_3

# build from the repo root
docker build . --file ./src/docker/Dockerfile --tag streaming_service_converter_3:latest --build-arg SERVICE_VERSION="0.1.0-SNAPSHOT"
# add `--target build0` to stop at the build0 stage

# run
# setting a memory limit (to behave like it will on Google Cloud Run)
# setting environment variables
# you must have these environment variables set appropriately on the host machine
# setting 8080 on your machine mapped to 9000 on the container
docker run -m 256m -e PLAY_APPLICATION_SECRET -e SPOTIFY_CLIENT_SECRET -p 8080:9000 streaming_service_converter_3

# cleanup your system
docker system prune -a -f --volumes
```

### Skaffold

Skaffold allows developers to turn their laptops into CI/CD machines. It speeds up the local development cycle. It can
also deploy to remote servers, but we have TeamCity, `k8s-deploy`, and `argo-apps` for that.

- [Docs](https://skaffold.dev/docs/)
- [Quickstart](https://skaffold.dev/docs/quickstart/)

```shell
# stop at the build and push
skaffold build

# like a ci/cd pipeline would, build and deploy the app one time
skaffold run
```

### Google Cloud Run

Cloud Run can run arbitrary containers.

The app is deployed [here](https://console.cloud.google.com/run/detail/us-west1/streaming-service-converter-3/revisions?project=four-track-friday-2) (access is required to view this page).

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
7) Run the following:
   ```shell
   skaffold build
   
   docker run -m 256m -e PLAY_APPLICATION_SECRET -e SPOTIFY_CLIENT_SECRET -p 8080:9000 streaming_service_converter_3
   ```
8) Or if you prefer to do the setup manually, see [that readme](./documentation/README_LOCAL_K8S.md)

## IntelliJ Tips

If you are seeing errors in build.sbt files regarding trailing commas, manually enable trailing commas for the project
in `Preferences | Languages & Frameworks | Scala | Misc | Trailing commas`

You will need to restart Intellij for this change to take effect
