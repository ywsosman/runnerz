# Runnerz

A small REST API for logging runs, built with **Spring Boot 4** and **Java 21+** while working through a Spring Boot course.

It stores runs in a database, validates what you send it, loads sample data on startup, and calls an external API to fetch users. Every layer has tests.

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Requirements](#requirements)
- [Getting started](#getting-started)
- [Project structure](#project-structure)
- [Data model](#data-model)
- [API reference](#api-reference)
- [Validation and errors](#validation-and-errors)
- [Database](#database)
- [Sample data](#sample-data)
- [Users API (external)](#users-api-external)
- [Testing](#testing)
- [Trying the API](#trying-the-api)
- [Troubleshooting](#troubleshooting)
- [Next steps](#next-steps)

---

## Features

- Full CRUD (create, read, update, delete) for runs at `/api/runs`
- Filter runs by location (`INDOOR` / `OUTDOOR`)
- Request validation with clear `400 Bad Request` responses
- `404 Not Found` for missing runs on read, update and delete
- SQL persistence with Spring's `JdbcClient` and an H2 database that runs inside the app
- Sample runs loaded from a JSON file when the database is empty
- Read-only proxy to [JSONPlaceholder](https://jsonplaceholder.typicode.com) users using Spring's `RestClient`
- Tests for the repository, the controller, the REST client and the full application context

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 21+ (records, text blocks) |
| Framework | Spring Boot 4.1.1 (Spring MVC) |
| Database | H2 (in-memory), accessed with `JdbcClient` |
| Validation | Jakarta Bean Validation (Hibernate Validator) |
| JSON | Jackson 3 (`tools.jackson.databind`) |
| HTTP client | Spring `RestClient` |
| Build | Maven (wrapper included: `mvnw` / `mvnw.cmd`) |
| Testing | JUnit 5, AssertJ, Mockito, MockMvc, MockRestServiceServer |

## Requirements

- **JDK 21 or newer.** The project sets `java.version` to 21, and any newer JDK also works.
- You **don't** need Maven installed. Use the wrapper that comes with the project.
- You **don't** need to install a database. H2 runs inside the application.

> **Windows note:** If `java -version` prints `1.8`, your PATH points at an old Java 8. Set `JAVA_HOME` to a JDK 21+ before using the Maven wrapper, or run everything from IntelliJ with the project SDK set to 21+ (**File → Project Structure → Project → SDK**).

## Getting started

### From IntelliJ IDEA

1. Open the project folder. IntelliJ detects `pom.xml` and imports it as a Maven project.
2. If you changed `pom.xml`, click the **Maven reload** icon.
3. Run `RunnerzApplication` (the green arrow next to `main`).
4. The app starts on **http://localhost:8080**.

### From the command line

PowerShell (Windows):

```powershell
./mvnw.cmd spring-boot:run
```

Git Bash, macOS or Linux:

```bash
./mvnw spring-boot:run
```

When the app starts you should see log lines like these:

```
Loaded 5 runs from data/runs.json
Run: Run[id=1, title=First Run, ...]
```

The second line comes from the demo `CommandLineRunner` in `RunnerzApplication`. It only logs a run and doesn't save anything.

### Build a runnable jar

```bash
./mvnw clean package
java -jar target/runnerz-0.0.1-SNAPSHOT.jar
```

## Project structure

```
runnerz/
├── api/
│   └── runnerz.http                  # Ready-made requests for IntelliJ's HTTP client
├── src/main/java/com/example/runnerz/
│   ├── RunnerzApplication.java       # Entry point + demo CommandLineRunner
│   ├── run/
│   │   ├── Run.java                  # Record: the run data + validation rules
│   │   ├── Location.java             # Enum: INDOOR, OUTDOOR
│   │   ├── RunController.java        # REST endpoints under /api/runs
│   │   ├── RunRepository.java        # SQL queries using JdbcClient
│   │   ├── RunNotFoundException.java # Turns into a 404 response
│   │   └── RunDataLoader.java        # Loads data/runs.json on startup
│   └── user/
│       ├── User.java                 # Record: user fields we keep
│       ├── UserRestClient.java       # Calls jsonplaceholder.typicode.com
│       └── UserController.java       # REST endpoints under /api/users
├── src/main/resources/
│   ├── application.properties        # Datasource + SQL init settings
│   ├── schema.sql                    # Creates the `run` table
│   └── data/runs.json                # Sample runs
└── src/test/java/com/example/runnerz/
    ├── RunnerzApplicationTests.java  # Full context starts
    ├── run/RunRepositoryTest.java    # Database layer (@JdbcTest)
    ├── run/RunControllerTest.java    # Web layer (@WebMvcTest)
    └── user/UserRestClientTest.java  # HTTP client (@RestClientTest)
```

### How a request flows

```
HTTP request
    │
    ▼
RunController      ← validates the body (@Valid), maps URLs to methods
    │
    ▼
RunRepository      ← runs SQL with JdbcClient
    │
    ▼
H2 database (table: run)
```

## Data model

### `Run`

| Field | Type | Rules |
|---|---|---|
| `id` | `Integer` | Primary key. You supply it when creating a run. |
| `title` | `String` | Required, can't be blank |
| `startedOn` | `LocalDateTime` | Required, ISO format e.g. `2026-09-27T08:00:00` |
| `completedOn` | `LocalDateTime` | Required, must be **after** `startedOn` |
| `miles` | `Integer` | Must be positive |
| `location` | `Location` | Required: `INDOOR` or `OUTDOOR` |

Example JSON:

```json
{
  "id": 10,
  "title": "Sunday Hill Repeats",
  "startedOn": "2026-09-27T08:00:00",
  "completedOn": "2026-09-27T08:40:00",
  "miles": 4,
  "location": "OUTDOOR"
}
```

### `User`

| Field | Type |
|---|---|
| `id` | `Integer` |
| `name` | `String` |
| `username` | `String` |
| `email` | `String` |
| `phone` | `String` |
| `website` | `String` |

The external API returns more fields (address, company…), and they're ignored.

## API reference

Base URL: `http://localhost:8080`

### Runs

| Method | Path | Body | Success | Errors |
|---|---|---|---|---|
| `GET` | `/api/runs` | none | `200` list of runs | — |
| `GET` | `/api/runs/{id}` | none | `200` one run | `404` if not found |
| `GET` | `/api/runs/location/{location}` | none | `200` runs at `INDOOR` or `OUTDOOR` | `400` if not a valid location |
| `POST` | `/api/runs` | Run JSON | `201 Created` | `400` if invalid |
| `PUT` | `/api/runs/{id}` | Run JSON | `204 No Content` | `400` if invalid, `404` if not found |
| `DELETE` | `/api/runs/{id}` | none | `204 No Content` | `404` if not found |

> For `PUT`, the `id` in the **URL** decides which run gets updated.

### Users

| Method | Path | Success |
|---|---|---|
| `GET` | `/api/users` | `200` list of users from JSONPlaceholder |
| `GET` | `/api/users/{id}` | `200` one user |

## Validation and errors

Validation happens in two places:

1. **Bean Validation annotations** on `Run` (`@NotBlank`, `@NotNull`, `@Positive`) are checked when a controller parameter is marked with `@Valid`. If a check fails, Spring returns **`400 Bad Request`**.
2. **The compact constructor** in `Run` throws `IllegalArgumentException` if `completedOn` isn't after `startedOn`. This runs every time a `Run` is created. For a request body, Jackson can't build the object, so the response is also `400`.

Missing runs throw `RunNotFoundException`. It's annotated with `@ResponseStatus(HttpStatus.NOT_FOUND)`, so Spring turns it into a **`404`**.

## Database

Settings in `application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:runnerz
spring.sql.init.mode=always
```

- `jdbc:h2:mem:runnerz` is an **in-memory** database. It's created on startup and **deleted when the app stops**.
- `spring.sql.init.mode=always` tells Spring to run `schema.sql` on every startup.

`schema.sql` creates this table:

```sql
CREATE TABLE IF NOT EXISTS run (
    id           INT          NOT NULL,
    title        VARCHAR(250) NOT NULL,
    started_on   TIMESTAMP    NOT NULL,
    completed_on TIMESTAMP    NOT NULL,
    miles        INT          NOT NULL,
    location     VARCHAR(10)  NOT NULL,
    PRIMARY KEY (id)
);
```

`JdbcClient` maps columns to record fields by name. It converts `snake_case` to `camelCase` automatically, so `started_on` becomes `startedOn`.

### Switching to a real database later

The repository uses plain SQL, so moving to PostgreSQL mostly means:

1. Replacing the `h2` dependency with `org.postgresql:postgresql`.
2. Setting `spring.datasource.url`, `username` and `password` in `application.properties`.

## Sample data

On startup, `RunDataLoader` (a `CommandLineRunner`) checks `runRepository.count()`:

- If the table is **empty**, it reads `src/main/resources/data/runs.json` and inserts 5 sample runs.
- If the table **already has rows**, it skips the load.

Because H2 is in-memory, the table is always empty on startup, so the sample runs come back on every restart.

## Users API (external)

`UserRestClient` builds a `RestClient` with the base URL `https://jsonplaceholder.typicode.com`. JSONPlaceholder is a free fake API for testing. `UserController` exposes the results at `/api/users`.

This part needs an internet connection when you run the app. The tests don't, because they fake the external server.

## Testing

Run all tests:

```bash
./mvnw test
```

| Test class | What it starts | What it checks |
|---|---|---|
| `RunnerzApplicationTests` | The whole application | The Spring context starts, including the schema and the sample data load |
| `RunRepositoryTest` | Only database beans (`@JdbcTest`) | Queries against a real H2 database. Each test is rolled back afterwards. |
| `RunControllerTest` | Only the web layer (`@WebMvcTest`) | Status codes, JSON output, validation. The repository is a Mockito mock (`@MockitoBean`). |
| `UserRestClientTest` | Only the REST client (`@RestClientTest`) | JSON parsing, using `MockRestServiceServer` to fake JSONPlaceholder |

Current result: **17 tests, 0 failures**.

> Spring Boot 4 moved the test annotations into new packages. For example, `WebMvcTest` is now in `org.springframework.boot.webmvc.test.autoconfigure` and `JdbcTest` is in `org.springframework.boot.jdbc.test.autoconfigure`. If code from older tutorials doesn't compile, check the imports.

## Trying the API

### IntelliJ HTTP client (easiest)

Open `api/runnerz.http` and click the green arrow next to any request. It has examples for every endpoint, including the `400` and `404` cases.

### curl

In **PowerShell**, type `curl.exe`. Plain `curl` there runs PowerShell's `Invoke-WebRequest` instead.

```powershell
curl.exe http://localhost:8080/api/runs
curl.exe http://localhost:8080/api/runs/1
curl.exe -i http://localhost:8080/api/runs/99
curl.exe http://localhost:8080/api/runs/location/OUTDOOR
```

Create a run (PowerShell needs the inner quotes escaped):

```powershell
curl.exe -i -X POST http://localhost:8080/api/runs -H "Content-Type: application/json" -d '{\"id\":10,\"title\":\"Evening Run\",\"startedOn\":\"2026-09-27T18:00:00\",\"completedOn\":\"2026-09-27T18:45:00\",\"miles\":4,\"location\":\"OUTDOOR\"}'
```

Update and delete:

```powershell
curl.exe -i -X PUT http://localhost:8080/api/runs/10 -H "Content-Type: application/json" -d '{\"id\":10,\"title\":\"Evening Run (longer)\",\"startedOn\":\"2026-09-27T18:00:00\",\"completedOn\":\"2026-09-27T19:00:00\",\"miles\":6,\"location\":\"OUTDOOR\"}'
curl.exe -i -X DELETE http://localhost:8080/api/runs/10
```

Useful curl options:

| Option | Meaning |
|---|---|
| `-i` | Show the response status and headers |
| `-X METHOD` | Use `POST`, `PUT` or `DELETE` |
| `-H "Name: value"` | Add a request header |
| `-d 'body'` | Send a request body |

## Troubleshooting

| Problem | Cause / fix |
|---|---|
| `unsupported class file major version` or `release version 21 not supported` | Maven is using an old JDK. Point `JAVA_HOME` at JDK 21+, or set the project SDK in IntelliJ. |
| `Port 8080 was already in use` | Another app (or an earlier run) is still running. Stop it, or add `server.port=8081` to `application.properties`. |
| `java: unexpected type, required: variable, found: value` | You used `=` (assignment) where you meant `==` (comparison). |
| Code from a video has `id: 1` in it | That's an IntelliJ **parameter hint**, not code. Type only the value: `new Run(1, ...)`. |
| New dependencies aren't found in IntelliJ | Click the Maven **reload** icon after editing `pom.xml`. |
| `/api/users` returns an error | The app needs internet access to reach jsonplaceholder.typicode.com. |
| Data I created disappeared | Expected: H2 is in-memory and resets on every restart. |

## Next steps

Ideas for extending the project:

- Switch to PostgreSQL (e.g. with Docker Compose) so data survives restarts
- Let the database generate `id` values instead of sending them in the request body
- Return a JSON error body (e.g. with `ProblemDetail`) instead of an empty 400/404
- Add paging and sorting to `GET /api/runs`
- Add an integration test that starts the full app and calls it over HTTP
- Store users locally and link runs to users
