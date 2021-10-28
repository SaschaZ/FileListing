FROM openjdk:8-slim-buster


ENV USER filelisting
ENV GROUP dockerbuilder
ENV HOME_PATH /home/${USER}
ENV PROJECT_FOLDER filelisting
ENV PROJECT_PATH ${HOME_PATH}/${PROJECT_FOLDER}
ENV DISTRIBUTION_NAME file_listing
ENV DISTRIBUTION_FOLDER ${DISTRIBUTION_NAME}-0.0.2

ARG port
ARG host
ARG hostPath

WORKDIR /tmp

RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip \
    && apt-get clean

# Add user
ARG user_id
ENV USER_ID=${user_id}
ARG group_id
ENV GROUP_ID=${group_id}

RUN addgroup --gid ${GROUP_ID} ${GROUP}
RUN adduser --disabled-password --home ${HOME_PATH} -gecos '' --uid ${USER_ID} --gid ${GROUP_ID} ${USER}

# Add current content to image
ADD --chown=${USER}:${GROUP} . ${PROJECT_PATH}
WORKDIR ${PROJECT_PATH}

# Remove possible temporary build files
RUN rm -f ./local.properties && \
    find . -name build -print0 | xargs -0 rm -rf && \
    rm -rf .gradle && \
    rm -rf ~/.m2 && \
    rm -rf ~/.gradle

USER ${USER}
RUN ./gradlew shadowJar

RUN echo "java -jar ./FileListing.jar --port $port --host $host --hostPath $hostPath --path ${HOME_PATH}/files" > ./start.sh && \
    chmod +x ./start.sh && \
    mkdir -p ${HOME_PATH}/files

CMD [ "/bin/sh", "-c", "./start.sh" ]
