####################################################
# START GLOBAL DECLARATION
####################################################
ARG REPO_NAME_DEFAULT=base-repo
ARG REPO_PORT_DEFAULT=8080
ARG SERVICE_ROOT_DIRECTORY_DEFAULT=/spring
####################################################
# END GLOBAL DECLARATION
####################################################

####################################################
# Building environment (java & git)
####################################################
FROM openjdk:16-bullseye AS build-env-java
LABEL maintainer=webmaster@datamanager.kit.edu
LABEL stage=build-env

# Install git as additional requirement
RUN apt-get -y update && \
    apt-get -y upgrade  && \
    apt-get install -y --no-install-recommends git bash && \
    apt-get clean \
    && rm -rf /var/lib/apt/lists/*

####################################################
# Building service
####################################################
FROM build-env-java AS build-service-base-repo
LABEL maintainer=webmaster@datamanager.kit.edu
LABEL stage=build-contains-sources

# Fetch arguments from above
ARG REPO_NAME_DEFAULT
ARG SERVICE_ROOT_DIRECTORY_DEFAULT

# Declare environment variables
ENV REPO_NAME=${REPO_NAME_DEFAULT}
ENV SERVICE_DIRECTORY=${SERVICE_ROOT_DIRECTORY_DEFAULT}/${REPO_NAME_DEFAULT}

# Create directory for repo
RUN mkdir -p /git/base-repo/
WORKDIR /git/base-repo/
COPY . .
RUN cp config/application-docker.properties settings/application-default.properties
# Build service in given directory
RUN bash ./build.sh $SERVICE_DIRECTORY

####################################################
# Runtime environment 4 base-repo
####################################################
FROM openjdk:16-bullseye AS run-service-base-repo
LABEL maintainer=webmaster@datamanager.kit.edu
LABEL stage=run

# Fetch arguments from above
ARG REPO_NAME_DEFAULT
ARG REPO_PORT_DEFAULT
ARG SERVICE_ROOT_DIRECTORY_DEFAULT

# Declare environment variables
ENV REPO_NAME=${REPO_NAME_DEFAULT}
ENV SERVICE_DIRECTORY=${SERVICE_ROOT_DIRECTORY_DEFAULT}/${REPO_NAME}
ENV REPO_PORT=${REPO_PORT_DEFAULT}

# Install bash as additional requirement
RUN apt-get -y update && \
    apt-get -y upgrade  && \
    apt-get install -y --no-install-recommends bash && \
    apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy service from build container
RUN mkdir -p ${SERVICE_DIRECTORY}
WORKDIR ${SERVICE_DIRECTORY}
COPY --from=build-service-base-repo ${SERVICE_DIRECTORY} ./

# Define repo port 
EXPOSE ${REPO_PORT}
ENTRYPOINT ["bash", "./run.sh"]
