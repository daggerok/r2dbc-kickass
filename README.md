# r2dbc-kickass [![Build Status](https://travis-ci.org/daggerok/r2dbc-kickass.svg?branch=master)](https://travis-ci.org/daggerok/r2dbc-kickass)
Reactive Spring WebFlux R2DBC Postgres Kickass project!

## getting started

```bash
zsh
setjdk11
mvn -v

docker run --rm -it -p 5432:5432 postgres:alpine

./mvnw spring-boot:run

http :8080 name=ololo
http :8080 name=trololo
http :8080
http delete :8080
http :8080
```

NOTE: _This project has been based on [GitHub: daggerok/main-starter](https://github.com/daggerok/main-starter)_

<!--

```bash
./mvnw versions:display-property-updates
```

-->
