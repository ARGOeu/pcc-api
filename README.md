# pcc-api Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Prerequisites
-   Java 11+
-   Apache Maven 3.8.1+
-   Docker (for dev mode)
## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/pcc-api-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Codebase Format
```shell
mvn spotless:apply
```

## Local DB Setup

The `local-db` profile gives the ability to run a mysql instance
in a docker container independently of the quarkus application.
This gives the ability to have a stable DB, hold state 
and not re-create it
every time the application restarts.

This requires:
- docker
- docker-compose

Execute the script:
```shell
run-local-db-env.sh
```

After the initial set up, you can run the quarkus application
on its own without the script.

In order to reset the local-db, you can issue the command:

```shell
docker-compose down --volumes
```

**NOTE**

The ability to run the application with a local-db other than
the one provided with the docker-compose file is possible by executing quarkus with the local-db profile.
```shell
mvn clean quarkus:dev -Dquarkus.profile=local-db
```