FROM adoptopenjdk/openjdk16:x86_64-alpine-jdk-16.0.2_7 AS build

# Add current content to image
ADD . /project
WORKDIR /project

# Remove possible temporary build files
RUN rm -f ./local.properties && \
    find . -name build -print0 | xargs -0 rm -rf && \
    rm -rf .gradle && \
    rm -rf ~/.m2 && \
    rm -rf ~/.gradle && \
    ./gradlew shadowJar


FROM adoptopenjdk/openjdk16:x86_64-alpine-jre-16.0.1_9

ARG port
ARG host
ARG hostPath

# Add user
ENV USER filelisting
ENV GROUP dockerbuilder
ARG user_id
ENV USER_ID=${user_id}
ARG group_id
ENV GROUP_ID=${group_id}

WORKDIR /home/${USER}
COPY --from=build /project/FileListing.jar /home/${USER}/FileListing.jar

RUN addgroup --gid ${GROUP_ID} ${GROUP} ; \
    adduser --disabled-password --home /home/${USER} -u ${USER_ID} -G ${GROUP} ${USER} ; \
    echo "java -jar ./FileListing.jar --port ${port} --host ${host} --hostPath ${hostPath} --path /home/${USER}/files" > ./start.sh ; \
    chmod +x ./start.sh ; \
    mkdir -p /home/${USER}/files ; \
    chown -R ${USER}:${GROUP} .

USER ${USER}
CMD [ "/bin/sh", "-c", "./start.sh" ]
