meta-server
=========

Micronauts web application that serves meta-information about Terasology.
It is available for in-game use in JSON format and in HTML format for web browsers.

The server is available at 
**http://meta.terasology.org**

Features
-------------
* Save and provide information about Terasology modules
* Receive and provide information about Terasology servers (Looking for game)
* Using https://db-ip.com/ for providing additional info about servers.
* Use PostgresDB via Jooq for persistence 
* Use Micronauts for core Frameworks

Run locally
-------------
Gradle-way:
1. Clone
2. Go to Project Dir
3. Set environment variable `MICRONAUT_ENVIRONMENTS` to `dev` (local h2 database)
4. `./gradlew run` at Linux/Macos or `gradlew run` at Windows
5. found at logs entry like `[main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 624ms. Server Running: http://localhost:39195` 
6. Go To url described at logs with your browser

Docker-way:
1. Clone
2. Go To Project Dir
3. Run `docker build . -t test-meta-server`
4. Run `docker run --env "MICRONAUT_ENVIRONMENTS=dev" test-meta-server` 

Deployment
-------------
Docker: 
1. Clone repository
2. Go To project directory
3. `docker build . -t <tag>`
4. `docker push` image whatever you want

You can setup what you want with [Micronaut's configs](https://docs.micronaut.io/latest/guide/index.html#config):
Common (ENVIRONMENT_VARIABLES):

     MICRONAUT_SERVER_PORT - port (example - 80)
     DATASOURCES_DEFAULT_URL - url to PG database (example - postgres://name:pw@host:port/database
     META_SERVER_DBIP_API_KEY=<get one from db-ip.com>
     META_SERVER_EDIT_SECRET=<a password only known for admins with write access>

Geo-Location
-------------

The lookup service by [DB-IP.com](https://db-ip.com/) is used to retrieve additional information on the servers.


Flag Icons
-------------

The flag icons are from [famfamfam](http://www.famfamfam.com/lab/icons/flags/).

License
-------------

This software is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
