# PCC

PCC is a multi-module Java project built with Quarkus and Apache Maven.

## Modules

- `api`
- `connectors`
- `daemons`

---

# Prerequisites

Before running the project, make sure the following tools are installed:

- Java 17+
- Apache Maven 3.8.1+
- Docker & Docker Compose

Verify installation:

```bash
java -version
mvn -version
docker --version
docker compose version
```

---

# Build the Project

Package all modules:

```bash
./mvnw clean package
```

This produces the Quarkus application in:

```text
target/quarkus-app/
```

Run the application:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

---

# Build an Uber JAR

To create a single executable JAR:

```bash
./mvnw clean package -Dquarkus.package.jar.type=uber-jar
```

Run it with:

```bash
java -jar target/*-runner.jar
```

---

# Development Mode

Start the local database environment:

```bash
./run-local-db-env.sh
```

Then run Quarkus in development mode:

```bash
./mvnw quarkus:dev
```

The Quarkus Dev UI is available at:

```text
http://localhost:8080/q/dev
```

---

# Reset Local Database

To remove containers and volumes:

```bash
docker compose down --volumes
```

---

# Native Executable

Build a native executable using GraalVM:

```bash
./mvnw clean package -Pnative
```

Or build inside a container:

```bash
./mvnw clean package -Pnative -Dquarkus.native.container-build=true
```

Run the native executable:

```bash
./target/pcc-api-1.0.0-SNAPSHOT-runner
```

Learn more about Quarkus native builds: https://quarkus.io/guides/maven-tooling

---

# Code Formatting

This project uses:

- Google Java Format
- Spotless Maven Plugin

Format the codebase:

```bash
./mvnw spotless:apply
```

Verify formatting:

```bash
./mvnw spotless:check
```

---

# HRLS Service Configuration

To communicate with the HRLS service, configure the following environment variables:

```bash
export HRLS_USERNAME=user
export HRLS_PASSWORD=pass
```