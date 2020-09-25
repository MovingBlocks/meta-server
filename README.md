meta-server
=========

A Jetty-based servlet that serves meta-information about Terasology.
It is available for in-game use in JSON format and in HTML format for web browsers.

The server is available at 
**http://meta.terasology.org**

hosted by [@msteiger](http://github.com/msteiger). 
The list of online game servers is hosted on a Amazon EC2 PostgreSQL instance (through Heroku credentials).
The server code is mirrored on two backup instances hosted on Heroku:

https://meta-server.herokuapp.com

https://meta-server-test.herokuapp.com (a debug DB also hosted on Amazon)


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
