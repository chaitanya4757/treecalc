# TreeCalc

A Spring Boot REST API that stores algebraic equations as postfix expression trees, returns them in infix form, and evaluates them with variable values.

## Tech
Java 21, Spring Boot 3.3, springdoc-openapi (Swagger), JUnit 5

## Run locally
```
mvn spring-boot:run
```
Swagger UI: http://localhost:8080/swagger-ui.html

## Endpoints
| Method | Path | Purpose |
|---|---|---|
| POST | /api/equations/store | Store an equation, e.g. `{"equation":"3x + 2y - z"}` |
| GET | /api/equations | List equations in infix form |
| POST | /api/equations/{id}/evaluate | Evaluate with `{"variables":{"x":2,"y":3,"z":1}}` |