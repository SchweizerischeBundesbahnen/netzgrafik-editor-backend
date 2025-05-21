# Netzgrafik-Editor Backend API

This repository contains the source for the backend API for the [Netzgrafik-Editor frontend](https://github.com/SchweizerischeBundesbahnen/netzgrafik-editor-frontend).

- [Spring Framework](https://spring.io/projects/spring-framework)
  with [Spring Boot](https://spring.io/projects/spring-boot) is used to implement the REST API.
- Data is stores in the [PostgreSQL](https://www.postgresql.org) relational database.
- [jOOQ](https://www.jooq.org/) is used to access the database through a database-independent
  SQL-Like Domain-Specific-Language. For that the jOOQ framework generates classes for every table,
  row, index and sequence in the database. This allows for type-save declarations of SQL queries.
- [Flyway](https://flywaydb.org/) migrations are used to manage the database schema (see
  src/main/resources/db.migration).
- [Project Lombok](https://projectlombok.org/) enhances the Java code with additional language
  features.
- [H2](https://www.h2database.com/html/main.html) is used as an in-memory database for end-to-end
  integration tests.


## Online Demo

[Extern: nge.flatland.cloud](https://nge.flatland.cloud) - powered by [Flatland Association](https://www.flatland-association.org/)

( [Sample Netzgrafik](https://github.com/SchweizerischeBundesbahnen/netzgrafik-editor-frontend/blob/main/src/app/sample-netzgrafik/Demo_Netzgrafik_Fernverkehr_2024.json) - [How to Import JSON](https://github.com/SchweizerischeBundesbahnen/netzgrafik-editor-frontend/blob/main/documentation/DATA_MODEL_JSON.md) )

## Setup Local Demo Environment with Docker Compose

Use [extern: netzgrafik-editor-docker-compose](https://github.com/flatland-association/netzgrafik-editor-docker-compose) for a one-line setup based
on [Docker Compose](https://docs.docker.com/compose/) - powered by [Flatland Association](https://www.flatland-association.org/).


## Getting-Started

### Setup Development Environment

#### Prerequisites

- Java JDK 21
- Maven >= 3.9.7
- Docker and `docker-compose`


### Command line setup guide

1. Clone the repository:
   ```shell
   git clone https://github.com/SchweizerischeBundesbahnen/netzgrafik-editor-backend.git
   cd netzgrafik-editor-backend
   ```

1. Start the local development environment (PostgreSQL, Keycloak):
   ```shell
   docker compose up -d
   ```

1. Setup environment variables
   ```shell
   export DB_URL=jdbc:postgresql://localhost/netzgrafikeditor
   export DB_USER=netzgrafikeditor
   export DB_PASSWORD=netzgrafikeditor
   export CORS_ALLOWED_ORIGINS=*
   export AUTH_SERVICE_NAME=fc44839c-e95f-4854-a52d-449867a9aa62
   ```

1. Start the app using maven
   ```shell
   # run application with Flyway migration
   mvn spring-boot:run
   ```
   The database schema is managed using [Flyway](https://flywaydb.org/). Migrations are placed
      under `src/main/resources/db/migration` and can be executed using maven. 
   Automatic migrations can be disabled using `-Dspring.flyway.enabled=false` (see e.g. https://www.baeldung.com/database-migrations-with-flyway) 

   Migrations can also be run separately:
   ```shell
   mvn flyway:migrate
   ```

## License

This project is licensed under [Apache 2.0](LICENSE).

## Contributing

This repository includes a [CONTRIBUTING.md](CONTRIBUTING.md) file that outlines how to contribute to the project, including how to submit bug reports, feature requests, and pull requests.

## Coding Standards

This repository includes a [CODING_STANDARDS.md](CODING_STANDARDS.md) file that outlines the coding standards that you should follow when contributing to the project.

## Code of Conduct

To ensure that your project is a welcoming and inclusive environment for all contributors, you should establish a good [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)

## Continuous Integration

This repository uses [release-please](https://github.com/googleapis/release-please) for CHANGELOG generation, the creation of GitHub releases, and version bumps for your projects.
See [CI.md](CONTRIBUTING.md) for details.
