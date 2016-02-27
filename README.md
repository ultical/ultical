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

Create a copy without the `.dist` of the following files and change the values according to your needs:
* `backend/src/main/resources/default.yaml.dist`
* `backend/src/main/resources/database/liquibase.properties`
* `backend/src/main/resources/jdbc.properties` (for testing)
* `web/config/config.js.dist`

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

### Update license information

To check if the project license is compatible with the dependencies' licenses, the file `web/NOTICE` file was generated with the following commands and contains a list of all the used libraries and their licenses.

```
npm install -g bower-license
bower-license -e json > NOTICE
```

[Grunt](http://gruntjs.com/) is used to generate header files for different file formats based on a template (see `config/header.*`) and to add the headers to the respective files.

* To execute the template generation run `grunt` in the directory `ultical/web`.
* Manually copy and paste the license header to the source files.

FIXME: We should build a minified/uglified JavaScript file and add the source header automatically to that one.

# License

This software is published as Open Source. The licenses for the different components are as follows:

* Backend: [GNU AGPL v3](http://www.gnu.org/licenses/agpl-3.0.html)
* Frontend: [GPL v3](http://www.gnu.org/licenses/gpl.html)

Complete license texts are provided in LICENSE files in the respective software modules.
