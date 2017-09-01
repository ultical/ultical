[![Build Status](https://travis-ci.org/ultical/ultical.svg?branch=master)](https://travis-ci.org/ultical/ultical)
# UltiCal

UltiCal is being developed as a free and open source team and tournament management platform. We are backed by the German Discsport Federation (DFV). Their live version can be found at https://dfv-turniere.de.

# Install

## Requirements

* Java 1.8
* Maven
* MySQL
* Bower
* a webserver to serve the files

## Config

Create a copy and remove the `.dist` of the following files and change the values according to your needs:

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

## License management

### Backend license

License headers and a list of all used libraries are created automatically with Maven. A list of all used libraries and their license can be found in `backend/NOTICE`.

To ensure a valid license configuration, the build breaks after adding a new library, which results in a change to the generated `NOTICE` file. Please check the change carefully and then update the `NOTICE` file with

```bash
cd backend
mvn notice:generate
```

To check the `NOTICE` file manually, run `mvn notice:check`.

Headers for code files are inserted automatically at every build. To manually trigger a header check, run `mvn license:check`, to update the headers run `mvn license:format`.

# Licenses

This project is published as Free and Open Source Software. The licenses for the different components are as follows:

* Backend: [GNU AGPL v3](http://www.gnu.org/licenses/agpl-3.0.html)
* Frontend: [GPL v3](http://www.gnu.org/licenses/gpl.html)

Complete license texts are provided in LICENSE files in the respective software modules.
