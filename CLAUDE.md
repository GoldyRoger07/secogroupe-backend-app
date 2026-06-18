# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./mvnw spring-boot:run          # start the app (port 8080)
./mvnw clean package -DskipTests # build JAR
./mvnw test                     # run all tests
```

## Stack

- **Spring Boot 4.x**, Java 21, MySQL
- **Spring Security 7** — stateless JWT (access + refresh tokens)
- **JPA/Hibernate 6** with `ddl-auto=update`
- **Lombok** throughout (no manual getters/setters)
- **JJWT 0.11.5** for token generation/validation

## Architecture

```
security/         JwtFilter, JwtService, CustomUserDetailsService, SecurityConfig
config/           DataInitializer (seeds 16 permissions + 2 roles), WebConfig (serves /uploads/**)
entity/           User, Role, Permission, Employee + UserStatus/EmployeeStatus enums
repository/       Spring Data JPA interfaces with search/pagination methods
dto/              Request/Response DTOs, PageResponse<T>
mapper/           Manual mappers (no MapStruct): EmployeeMapper, UserMapper, RoleMapper, PermissionMapper
service/          UserService, EmployeeService, RoleService, PermissionService, PhotoStorageService, EmailService
controller/       AuthController, UserController, EmployeeController, RoleController, PermissionController
exception/        GlobalExceptionHandler (@RestControllerAdvice)
```

## Key Conventions

**URL structure:**
- Auth: `POST/GET /auth/v1/**` (public)
- API: `GET/POST/PUT/DELETE /api/v1/{resource}` (JWT required)
- Photos: `GET /uploads/photos/{filename}` (public)

**Pagination:** all list endpoints accept `page`, `size`, `sortField`, `sortOrder`, `globalFilter` and return `PageResponse<T>`.

**Employee photo upload:** `POST/PUT /api/v1/employees` consumes `multipart/form-data`. The `data` part is a JSON string of `EmployeeRequest`; the optional `photo` part is the image file.

**Authorization:** method-level `@PreAuthorize("hasAuthority('ACTION_RESOURCE')")`. The 16 permissions follow the pattern `{CREATE|READ|UPDATE|DELETE}_{EMPLOYEE|USER|ROLE|PERMISSION}`.

**Email verification (dev mode):** `app.mail.console-mode=true` in `application.properties` logs OTP to console instead of sending SMTP. The OTP and verification link both appear in the Spring Boot log.

## Database Reset (when adding new fields)

After entity changes, if `ddl-auto=update` doesn't pick up enum columns cleanly:
```sql
DROP DATABASE secogroupe_app;
CREATE DATABASE secogroupe_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Then restart the app — `DataInitializer` re-seeds all permissions and roles automatically.

## Frontend

Angular app at `f:/Documents/dev/secogroupe-frontend-app` (port 4200). The backend is already configured with CORS for `http://localhost:4200`.

Tokens are stored in `localStorage` as `access_token` / `refresh_token`. The Angular `AuthInterceptor` adds `Authorization: Bearer <token>` to every API request.
