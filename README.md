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
* `web/public/config/config.js.dist`

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

Inside the `web/`directory install dependencies with [npm](https://www.npmjs.com/):

`npm install`

Change the document root of your web server to the web directory to browse the app.

### Build and develop

The following commands/phases are configured using [plain npm](http://blog.keithcirkel.co.uk/how-to-use-npm-as-a-build-tool/) run scripts in the file `package.json`:

* `npm run build` (**main task**) creates the distribution of the application in the directory `../web/dist`. It comprises several steps, which can be executed indepedently if need be.
  * `build:public` copies all files from the directory `public` to the distribution directory
  * `build:jsbundle` minifies the JavaScript files from the directory `components` and all dependencies into a single file (and debugging mapping) in the distribution directory. The plugin browserify-header prepends the header from the file `components/app.js` since it starts with `/*!` to preserve license information.
  * `build:css` concatenates the stylesheets in the directory `css` and minifies them into a single file in the distribution directory
* `npm run lint` runs a JavaScript linter
* `npm run clean` clears the output directory `../web/dist`, automatically run before `build`
* `npm run build:watch` starts watchers for all partial tasks of `build`
* `npm run test` ... not implemented yet

To install additional dependencies, run `npm install <dependecy name> --save`. To check for updates of all dependencies, run `ncu` (after installing it with `npm install -g npm-check-updates`), see [ncu manual](https://www.npmjs.com/package/npm-check-updates).

To install new development dependencies, run `npm install <module name> --save-dev`.

### Update license information

To check if the project license is compatible with the dependencies' licenses, the file `web/NOTICE` file was generated with the following commands and contains a list of all the used libraries and their licenses. This *should* be repeated regularly

```
sudo npm install -g npm-license
npm-license > NOTICE
```

The npm plugin [browserify-header]() is used to transfer the first code header from the input files to the output file during running browserify. When creating a new source file, developers must *manually copy and paste the file header from one of the existing JavaScript files*.

FIXME: We should build a minified/uglified JavaScript file and add the source header automatically to that one.

# License

This software is published as Open Source. The licenses for the different components are as follows:

* Backend: [GNU AGPL v3](http://www.gnu.org/licenses/agpl-3.0.html)
* Frontend: [GPL v3](http://www.gnu.org/licenses/gpl.html)

Complete license texts are provided in LICENSE files in the respective software modules.
