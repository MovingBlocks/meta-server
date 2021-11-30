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

Deployment
-------------

Heroku: Clone the repository and push the content to Heroku on branch `master`. Some details must be provided through environment variables:

    DATASOURCE_DEFAULT_URL=postgres://name:pw@host:port/database
    SERVER_URL=8080
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
