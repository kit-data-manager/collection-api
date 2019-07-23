# Collection API

This project provides an implementation of the Collection API as it was proposed by the RDA Recommendation on Research Data Collections [doi: 10.15497/RDA00022](https://zenodo.org/record/2428145#.XTbIMZMzbbA). 
The Collection API is implemented as Spring Boot-based Microservice and can be used for building collections of digital objects in a generic way independent
from any repository managing the digital objects. 

## How to build

In order to build the Collection API you'll need:

* Java SE Development Kit 8 or higher

After obtaining the sources change to the folder where the sources are located perform the following steps:

```
user@localhost:/home/user/collection-api$ ./gradlew build
> Configure project :
Using release profile for building collection-api
<-------------> 0% EXECUTING [0s]
[...]
user@localhost:/home/user/collection-api$
```

The Gradle wrapper will now take care of downloading the configured version of Gradle, checking out all required libraries, build these
libraries and finally build the base-repo microservice itself. As a result, a fat jar containing the entire service is created at 'build/jars/colleciton-api.jar'.

## How to start

### Prerequisites

* PostgreSQL 9.1 or higher

### Setup
Before you are able to start the microservice, you have to modify the file 'application.properties' according to your local setup. 
Therefor, copy the file 'conf/application.properties' to your project folder and customize it. 
For the Collection API you just have to adapt the properties of spring.datasource 
All other properties might be ignored for the time being.

As soon as you finished modifying 'application.properties', you may start the repository microservice by executing the following command inside the project folder, 
e.g. where the service has been built before:

```
user@localhost:/home/user/collection-api$ ./build/libs/collection-api.jar

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

in order to see available RESTful endpoints and their documentation. Furthermore, you can use this Web interface to test single API calls in order to get familiar with the 
service. A small documentation guiding you through the first steps of using the RESTful API you can find at

http://localhost:8090/static/docs/documentation.html

## More Information

* [RDA Recommendation on Research Data Collections (doi: 10.15497/RDA00022)](https://zenodo.org/record/2428145#.XTbIMZMzbbA)

## License

The KIT Data Manager is licensed under the Apache License, Version 2.0.
