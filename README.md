# UltiCal

UltiCal is being developed as a free and open source team and tournament management platform. We are backed by the German Discsport Federation (DFV). Their live version can be found at http://dfv-turniere.de

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

### Update license information

To ensure that no incompatible license is added to the code, the build fails after new dependencies are added. To check the license run `mvn notice:check`, to update the notice file run `mvn notice:generate`.

Headers for code files are inserted automatically at every build. To manually trigger a header check, run `mvn license:check`, to update the headers run `mvn license:format`.

## Frontend

Inside the `web/`directory install dependencies with bower:

`bower install`

Change the document root of your web server to the web directory


# License

This software is published as Open Source. The licenses for the different components are as follows:

* Backend: [GNU AGPL v3](http://www.gnu.org/licenses/agpl-3.0.html)
* Frontend: [GPL v3](http://www.gnu.org/licenses/gpl.html)

Complete license texts are provided in LICENSE files in the respective software modules.
