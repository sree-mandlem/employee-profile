# Employee Profile HR Application

A small but production-style Employee Profile (HR) application built as a React SPA (single-page application) with a Spring Boot backend.

## Overview

The application allows:

- **Managers and profile owners** to view and edit full employee profile data (including sensitive fields).
- **Co-workers** to view only non-sensitive profile data and leave feedback.
- **Employees** to request absences which managers can approve or reject.

## High-Level Architecture

- **Frontend:** React SPA (single-page application) with React Router and a simple state management approach.
- **Backend:** Single Spring Boot service exposing a REST API.
- **Security:** Spring Security with JWT (JSON Web Token) based authentication and role-based access control.
- **Persistence:** Spring Data JPA (Java Persistence API) with PostgreSQL as relational database.
- **Migrations:** Flyway for database schema evolution.
- **Mapping:** MapStruct for DTO (Data Transfer Object) ↔ entity mapping.
- **Serialization:** Jackson for JSON (JavaScript Object Notation) serialization and deserialization, with custom serializers where needed.
- **Testing:** JUnit 5 with Testcontainers for PostgreSQL integration tests.
- **Build:** Maven for dependency management and build lifecycle.

# Architecture Decisions and Patterns

## Modular Monolith
This project is intentionally kept as a **single deployable application** while still reflecting patterns used in production systems.
This is ideal for this exercise but still organized into clear modules/packages (employee, feedback, absence, auth, common) to keep boundaries explicit and prepare for possible future extraction.

## Authentication using JWT
JWT is chosen over server-side sessions for
- **Stateless** backend (scales horizontally easily).
- Simpler integration with other clients or services in the future.
- Tokens have an expiration time and can be refreshed via a dedicated endpoint or re-login.

## Authorization with Role-Based Access Control
The application supports these roles - ROLE_MANAGER, ROLE_EMPLOYEE. 
- Method-level security using annotations like `@PreAuthorize`.
- Further finer access control using field level visibility using DTOs.

## Testing Strategy
- **Testcontainers** is chosen to keep tests close to production behavior (same database engine, real migrations via Flyway).
- Focus on testing critical flows with unit tests and integration tests:
    - Authentication and authorization.
    - Profile access rules (sensitive vs non-sensitive).
    - Absence request lifecycle.

# Local Development and Environments

- **Local:**
    - Run PostgreSQL via Docker Compose and/or Testcontainers.
    - Start Spring Boot application and React development server separately.
      - The application needs the following properties defined as ENV variables to run,
        ```
        DB_PASSWORD=???
        DB_URL=jdbc:postgresql://localhost:5432/postgres
        DB_USERNAME=???
        JWT_SECRET=???
        ```

# Next?
* **Docker**ize the application
* Provide **search** functionality with pagination for co-workers to search for employees to provide feedback. 
* Implement feedback **polish** service backed by integration with ai models like **Huggingface**.

