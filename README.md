[![Build Status](https://travis-ci.org/ultical/ultical.svg?branch=master)](https://travis-ci.org/ultical/ultical)
# UltiCal

UltiCal is being developed as a free and open source team and tournament management platform. We are backed by the German Discsport Federation (DFV). Their live version can be found at dfv-turniere.de

# Install

## Requirements

* Java 1.8
* Maven
* MySQL
* Bower
* a webserver to serve the files

## Config

Remove the .dist of the following files and change the values according to your needs:
* backend/src/main/resources/default.yaml.dist
* backend/src/main/resources/database/liquibase.properties
* backend/src/main/resources/jdbc.properties (for testing)
* web/config/config.js.dist

## Backend

The Java Backend can be build with Maven:

`mvn package`

To update the database:

`mvn liquibase:update`

To run the server:

`java -jar target/backend-0.0.1-SNAPSHOT.jar server src/main/resources/default.yaml`

## Frontend

Inside the `web/`directory install dependencies with bower:

`bower install`

Change the document root of your web server to the web directory
