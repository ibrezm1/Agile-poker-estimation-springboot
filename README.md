# Spring Boot Voter Demo

This is a Spring Boot project.

## How to Run locally

To compile and run the application locally, use the provided Gradle wrapper:

```bash
./gradlew bootRun
```

This will download the necessary dependencies and Gradle distribution (if needed), compile your source files, and start the embedded Tomcat server on port `8080`.

### Compilation Only

If you just want to compile the project to verify there are no compilation errors:

```bash
./gradlew compileJava
```

## API Documentation & Swagger

This application includes Swagger UI for API documentation and testing.

Once the application is running, you can access the Swagger UI at:
- **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

The OpenAPI JSON description is available at:
- **[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)**

### Endpoints Overview

The application exposes the following REST endpoints for Agile Poker:

#### `POST /api/polls`
Create a new poll.
- **Body**: `{"name": "My Poll Name"}`
- **Response**: The created `Poll` object with an ID.

#### `GET /api/polls`
List all active polls.
- **Response**: A list of `Poll` objects.

#### `GET /api/polls/{id}`
Get details of a specific poll, including its current votes.
- **Response**: The `Poll` object.

#### `POST /api/polls/{id}/vote`
Cast a vote on a specific poll.
- **Body**: `{"username": "YourName", "vote": "5"}`
- **Response**: The updated `Poll` object.

#### `DELETE /api/polls`
Delete polls that are older than a specific number of hours.
- **Parameters**: `hours` (optional query parameter, default is `1`). For example, `DELETE /api/polls?hours=2` will delete all polls created more than 2 hours ago.
- **Response**: HTTP 204 No Content.

## Memory and Cleanup

- **Storage**: All data is stored in memory using `ConcurrentHashMap`. Data will be lost when the application stops.
- **Cleanup**: A scheduled background task runs every hour and automatically deletes polls that are older than 1 day.

Todo : 
Can you create a product manager view who creates and can view everyones submissions and one view for the members who can only post the vote 