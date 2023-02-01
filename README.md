# pcc-api Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Prerequisites
-   Java 11+
-   Apache Maven 3.8.1+
-   Docker (for dev mode)

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

## Running the application in dev mode

Execute the script:
```shell
run-local-db-env.sh
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.


After the initial set up, you can run the quarkus application
on its own without the script.

In order to reset the local-db, you can issue the command:

```shell
docker-compose down --volumes
```

## HRLS Service Communication
In order to communicate with the HRLS service, pcc-api
needs two environmental variables that dictate the username
and password for basic auth.

```shell
$ export HRLS_USERNAME = user
``` 
```shell
$ export HRLS_PASSWORD = pass
``` 
