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
