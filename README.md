master-server
=========

A Jetty-based servlet that runs on Heroku and serves information about Terasology hosters in JSON format.
The data is hosted on a Amazon EC2 PostgreSQL instance.

Data
-----------
A list of available server is served at 

    https://master-server.herokuapp.com/servers/list

Deployment
-------------

Clone the repository and push the content to Heroku. The database connection details are provided by Heroku under the environment variable `DATABASE_URL`.


Geo-Location
-------------

The lookup service by [DB-IP.com](https://db-ip.com/) is used to retrieve additional information on the servers.


License
-------------

This software is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
