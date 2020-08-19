FROM openjdk:8
RUN mkdir -p /var/repository
RUN mkdir -p /kitdm20/collection-registry/release/
COPY collection-registry.jar /kitdm20/collection-registry/release/
COPY conf/application.properties /kitdm20/collection-registry/release/config/application.properties
COPY run.sh /kitdm20/

EXPOSE 8080
ENTRYPOINT ["bash", "/kitdm20/run.sh"]