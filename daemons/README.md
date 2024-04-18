# PID Probe

PID Probe is a command-line tool that aims to scan a B2HANDLE-HRLS database to perform lifecheck on the 
stored handles.

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

## Running the application

First the following environmental variables must be set appropriately:

```shell
export HRLS_DATABASE_IP=localhost
export HRLS_DATABASE_PORT=3307
export HRLS_DATABASE_NAME=hrlsdb
export HRLS_DATABASE_USERNAME=hrls
export HRLS_DATABASE_PASSWORD=hrls
```

Build:

```shell
build
```
Then, execute the script:
```shell
java -jar daemons-1.0.0-SNAPSHOT.jar
```

Use the `--help` argument to see the list of supported arguments.



## Execute PID Probe tool through pcc-api

Copy daemons-1.0.0-SNAPSHOT.jar to a location in the pc and add daemon.jar.path env parameter
 at the api's application.properties file e.g daemon.jar.path=/home/user/daemons-1.0.0-SNAPSHOT.jar
Add pcc.log file to a location in the pc and add daemon.log.path env parameter
 at the api application.properties file, e.g daemon.log.path=/home/cthermolia/pcc.log

