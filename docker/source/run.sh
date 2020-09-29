#!/bin/bash

cd /collection-api
java -cp ".:./config:./collection-api.jar" -Dloader.path=file://`pwd`/collection-api.jar,./lib/,. -jar collection-api.jar

