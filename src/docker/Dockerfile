FROM amazoncorretto:11-alpine3.14-jdk as corretto

FROM corretto as build0

# we need this to run the sbt scripts later
RUN ["apk", "add", "--no-cache", "bash"]

FROM build0 as build1

# we need this to unzip later
RUN ["apk", "add", "--no-cache", "unzip"]

FROM build1 as build2

ARG SERVICE_VERSION

# copy the file from the machine with the source code into the container
COPY . /streaming_service_converter_3
# cd into the "copied to" directory
WORKDIR /streaming_service_converter_3

# create a distribution independent of sbt
RUN ["./bin/sbt", "clean", "dist"]

# cd into the distribution directory
WORKDIR /streaming_service_converter_3/target/universal
# unzip the files for the production build to use below
RUN unzip streaming_service_converter_3-$SERVICE_VERSION.zip
# get rid of the version identifier
RUN mv streaming_service_converter_3-$SERVICE_VERSION streaming_service_converter_3

FROM build0

# Add apex user and group
RUN addgroup -S serviceGroup && adduser -S serviceUser -G serviceGroup
# Change to non-root privilege
USER serviceUser

# copy the unzipped files from above into the production container
COPY --from=build2 /streaming_service_converter_3/target/universal /opt

EXPOSE 9000

# set running the service as the entrypoint
ENTRYPOINT ["/bin/bash", "/opt/streaming_service_converter_3/bin/streaming_service_converter_3"]
# but allow for runtime commands to overwrite the arguments
CMD ["-Dconfig.file=/opt/streaming_service_converter_3/conf/production.conf"]
