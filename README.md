# Collection API

![Build Status](https://img.shields.io/travis/kit-data-manager/collection-api.svg)
![Code Coverage](https://img.shields.io/coveralls/github/kit-data-manager/collection-api.svg)
![License](https://img.shields.io/github/license/kit-data-manager/collection-api.svg)
![Docker Cloud Build Status](https://img.shields.io/docker/cloud/build/kitdm/collection-api)
![Docker Image Version (latest semver)](https://img.shields.io/docker/v/kitdm/collection-api)

This project provides an implementation of the Collection API as it was proposed by the RDA Recommendation on Research Data Collections [doi: 10.15497/RDA00022](https://zenodo.org/record/2428145#.XTbIMZMzbbA). 
The Collection API is implemented as Spring Boot-based Microservice and can be used for building collections of digital objects in a generic way independent
from any repository managing the digital objects. The implementation is complete with regard to the recommendations supporting pagination, collection sorting, and listing of sub-collections.

## Differences and Improvements

As some aspects of the Collection API are not clearly defined by the recommendation, this implementation contains some fixes [FIX], additions [ADD] and 
restrictions [RES]
marked with the according tag. In all other cases the implementation is expected to behave as recommended.

* [FIX] Return type inconsistencies have been fixed, e.g. in /collections/{id}/members/{mid}
* [FIX] Delete operations return status 204 (NO_CONTENT) according to the HTTP specification
* [FIX] Delete operations are realized idempotent following the HTTP specification. This means, that DELETE can be issued multiple times to a resource and returns HTTP 204 in all cases.
* [FIX] Collection operations allow navigation the same way all other operations do, e.g. via prev and next links.
* [RES] Listing a collection recursively does not consider the sorting of child elements.
* [RES] A recursive listing of a collection will also contain member items of expanded collections. The recommendation document was not clear on this.
* [RES] There is currently no build-in PID support. If no PID are provided with a collection or member, a UUID is assigned.
* [ADD] Integrated ETag support in order to avoid concurrent modifications.
* [ADD] Navigation through a result set is realized using default Spring pagination, e.g. supporting page and size query parameters. The cursors (next and prev) of a result set are pointing to the next/prev page link.

## How to build

In order to build the Collection API you'll need:

* Java SE Development Kit 8 or higher

After obtaining the sources change to the folder where the sources are located perform the following steps:

```
user@localhost:/home/user/collection-api$ ./gradlew -Pclean-release build
> Configure project :
Using release profile for building collection-api
<-------------> 0% EXECUTING [0s]
[...]
user@localhost:/home/user/collection-api$
```

The Gradle wrapper will now take care of downloading the configured version of Gradle, checking out all required libraries, build these
libraries and finally build the collection-api microservice itself. As a result, a fat jar containing the entire service is created at 'build/jars/collection-api.jar'.

## How to start

### Prerequisites

* PostgreSQL 9.1 or higher

### Setup
Before you are able to start the microservice, you have to modify the file 'application.properties' according to your local setup. 
Therefor, copy the file 'conf/application.properties' to your project folder and customize it. For the Collection API you just have to adapt the properties of 
spring.datasource and you may change the server.port property. All other properties can be ignored for the time being.

As soon as you finished modifying 'application.properties', you may start the collection-api microservice by executing the following command inside the project folder, 
e.g. where the service has been built before:

```
user@localhost:/home/user/collection-api$ ./build/libs/collection-registry.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.5.RELEASE)
[...]
1970-01-01 00:00:00.000  INFO 56918 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8070 (http) with context path ''

```

As soon as the microservice is started, you can browse to 

http://localhost:8070/swagger-ui.html

in order to see available RESTful endpoints and their documentation. You may have to adapt the port according to your local settings.
Furthermore, you can use this Web interface to test single API calls in order to get familiar with the service. 

The Collection API offers a graphical web frontend in order to visualize managed collections, collection items and relationships between them as well as associated metadata. 
In addition it allows a simple search for elements in order to visualize linked nodes. To access the web frontend, open the following link in your browser:

http://localhost:8070/static/overview.html

## More Information

* [RDA Recommendation on Research Data Collections (doi: 10.15497/RDA00022)](https://zenodo.org/record/2428145#.XTbIMZMzbbA)
* [Getting Started & Documentation](https://kit-dm-documentation.readthedocs.io/en/latest/)

## License

The KIT Data Manager is licensed under the Apache License, Version 2.0.
