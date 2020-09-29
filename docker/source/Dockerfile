FROM openjdk:11-stretch AS build-env-java
MAINTAINER webmaster@datamanager.kit.edu
LABEL stage=build-env

RUN apt-get update && \
    apt-get upgrade --assume-yes && \
    apt-get install --assume-yes git && \
    mkdir -p /collection-api/

FROM build-env-java AS build-collection-api
MAINTAINER webmaster@datamanager.kit.edu
LABEL stage=build-contains-sources

COPY build.sh /collection-api/

RUN sh /collection-api/build.sh

FROM openjdk:11-stretch AS run-collection-api
MAINTAINER webmaster@datamanager.kit.edu
LABEL stage=run

RUN mkdir -p /collection-api/config/
COPY --from=build-collection-api /collection-api/collection-api/build/libs/collection-api.jar /collection-api/collection-api.jar
COPY --from=build-collection-api /collection-api/collection-api/config/application-default.properties /collection-api/application.properties
COPY run.sh /collection-api/

EXPOSE 8080
ENTRYPOINT ["sh", "/collection-api/run.sh"]
