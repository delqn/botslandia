# botslandia

A barebones Play app, which can easily be deployed to Heroku.

This application support the [Getting Started with Play on Heroku](https://devcenter.heroku.com/articles/getting-started-with-play-on-heroku) article - check it out.

## Running Locally

### Install Play:
 - `brew install typesafe-activator`

Make sure you have Play and sbt installed.  Also, install the [Heroku Toolbelt](https://toolbelt.heroku.com/).

```sh
$ git clone https://github.com/heroku/botslandia.git
$ cd botslandia
$ sbt compile stage
$ <del>foreman start web</del><del>heroku local web</del>activator && ~ run
```

Your app should now be running on [localhost:5000](http://localhost:5000/).

## Deploying to Heroku

```sh
$ heroku create
$ git push heroku master
$ heroku open
```

## Documentation

For more information about using Play and Scala on Heroku, see these Dev Center articles:

- [Play and Scala on Heroku](https://devcenter.heroku.com/categories/language-support#scala-and-play)

foreman start web
sbt compile stage
This is your new Play application
=====================================

This file will be packaged with your application, when using `play dist`.

Play commands:

* Enter the play CLI: `$ play`
* Run the app: `[botslandia] $ run`
* You can create an idea project with  `$ play idea`
* Debug the app with `$ play debug`
* To start with automatic recompile after save (notice the tilde): `$ play debug \~run` or `$ play \~run`
* Manual compilation with: `$ play compile`
* To create a new project: `$ play new <name>`


Heroku commands:

* create a database for the app: `$ heroku addons:create heroku-postgresql`
* connect to the database with `heroku pg:psql`
* add Postgres backups with `$ heroku addons:add pgbackups`
* adding a domain `$ heroku domains:add www.botslandia`

Install postgresql on OS X from http://postgresapp.com

Postgress DB maintenance:

* heroku addons:add pgbackups # To install the addon
* curl -o latest.dump `heroku pgbackups:url` # To download a dump
* pg_restore --data-only --file=out.sql latest.dump
* psql -f ./out.sql


Regarding the integration between IntelliJ and Play:

* It is no longer necessary to create the module from play/activator.
* Delete all existing idea related directories
* Then in IntelliJ IDEA click File -> Open and choose your build.sbt file. That's all.

source: (http://stackoverflow.com/questions/16135716/how-to-use-intellij-with-play-framework-and-scala)

Important structure for the Scala Play App:

The /project directory
All the build configuration is stored in the project directory. This folder contains 3 main files:

build.properties: This is a marker file that describes the sbt version used.
Build.scala: This is the application project build description.
plugins.sbt: SBT plugins used by the project build.

source: (https://www.playframework.com/documentation/2.0/Build)

------

source: https://github.com/heroku/play-getting-started

$ git clone https://github.com/heroku/play-getting-started.git
$ cd play-getting-started
$ sbt compile stage
$ foreman start web
sbt \~compile stage

---

Configure IntelliJ: https://www.jetbrains.com/help/idea/2016.1/getting-started-with-play-2-x.html?origin=old_help#use_code_assistance

Run with a heroku DB connection: `DATABASE_URL=$(heroku config:get DATABASE_URL -a botslandia)"?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory" activator \~ run`

## IntelliJ config
- run IntelliJ with prefix: `DATABASE_URL=$(heroku config:get DATABASE_URL -a botslandia)"?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory"`
- Edit Run/Debug config
- Add new Remote config
  - Host: localhost, Port: 9999
  - Add external tool
    - Program:  /home/de/activator-1.3.9-minimal/bin/activator
    - Parameters: -jvm-debug 9999 "~ run"

## Cleaning
 - `sbt clean`
 - `git clean -f -d -x` -- removes everything not tracked by git
- clean heroku: `heroku config:set SBT_CLEAN=true -a botslandia` # https://stackoverflow.com/questions/25101792/error-pushing-to-heroku-when-route-is-removed-from-play-framework

## Google OAuth2
 - added in /etc/hosts:  127.0.0.1 botslandia.herokuapp.com
 - had to temporarily set-up the heroku instance to run on port 9000 so Google can verify http://botslandia.herokuapp.com:9000/_oauth-callback/ as being my own.


## How to link an existing heroku app to a git repo
 - `heroku git:remote -a botslandia`


## ChatBot
 - "4. Subscribe the App to the Page - Using the Page Access Token generated in the previous step, make the following call. This will subscribe your app to get updates for this specific Page." `curl -ik -X POST "https://graph.facebook.com/v2.6/me/subscribed_apps?access_token=`cat ~/.fb-token`"`


## Heroku instance config
### Environment variables:
 - `DATABASE_URL` - provided by Heroku when postgres is provisioned
 - `GOOGLE_CLIENT_ID`
 - `GOOGLE_CLIENT_SECRET`
 - `GOOGLE_CLIENT_CALLBACK`
