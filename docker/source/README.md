![Docker Cloud Build Status](https://img.shields.io/docker/cloud/build/kitdm/collection-api)

# Docker Configuration - collection-api

This repository contains a the docker configuration files for the collection-api service of KIT DM 2.0 repository platform. It is build and hosted at [DockerHub](https://hub.docker.com/) and can be found under the namespace ***kitdm***. 

## Prerequisites

* docker (tested with 18.09.2)

## Building and Startup

Typically, there is no need for locally building images as all version are accessible via [DockerHub](https://hub.docker.com/).

Running for example a collection-api instance can be achieved as follows:

```
user@localhost:/home/user/$ docker run -p 8080:8080 kitdm/collection-api
[...]
user@localhost:/home/user/$
```

In some cases, you may want to change the configuration of the service instance. All service-specific configuration is located in each image at

```/collection-api/conf/application.properties```

You can easily overwrite this file by creating an own Dockerfile, which looks as follows in case of the collection-api service:

```
FROM kitdm/collection-api:latest

COPY application.properties /collection-api/config/application.properties
```

Afterwards, you have to build the modified image locally by calling:

```
user@localhost:/home/user/my-collection-api/$ docker build .
[...]
user@localhost:/home/user/my-collection-api/$
```

Now, you can start the container using your modified configuration.

## License

The KIT Data Manager is licensed under the Apache License, Version 2.0.
