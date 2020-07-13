#!/bin/bash

cd /kitdm20/collection-registry/release
java -cp ".:./config:./collection-registry.jar" -Dloader.path=file://`pwd`/collection-registry.jar,./lib/,. -jar collection-registry.jar
