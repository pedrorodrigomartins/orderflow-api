# OrderFlow API

RESTful order management API built with Java 21 and Spring Boot 4, focusing on business rules, transactional consistency, payment processing, and inventory management.

## Technologies

- Java 21
- Spring Boot 4
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Docker and Docker Compose
- Maven
- JUnit 5 and Mockito
- Testcontainers
- Swagger / OpenAPI

## Features

**Customer Management**
- Customer registration and retrieval
- Unique CPF and email constraints

**Product and Category Management**
- Product and category management
- Inventory tracking
- Product activation and deactivation

**Order Management**
- Order creation with multiple items
- Stock validation and reservation
- Order status transitions
- Order cancellation with stock restoration

**Payment Management**
- Payment creation and processing
- Payment expiration handling
- Prevention of duplicate active payments
- Payment confirmation and failure handling
- Payment history by order

## Architecture

The application follows a layered architecture:

```text
controller/
service/
repository/
domain/
  entity/
  enums/
dto/
  request/
  response/
exception/
```

Business rules are implemented in domain entities and application services.

## Running with Docker

### Requirements

- Docker
- Docker Compose

### Setup

Clone the repository:

```bash
git clone https://github.com/pedrorodrigomartins/orderflow-api.git
cd orderflow-api
```

Create the environment file:

```bash
cp .env.example .env
```

Set a local PostgreSQL password in `.env`.

Start the application:

```bash
docker compose up --build -d
```

The API will be available at:

`http://localhost:8080`

PostgreSQL is exposed locally on port `5433`.

Database migrations are automatically applied by Flyway.

## API Documentation

Swagger UI:

`http://localhost:8080/swagger-ui.html`

OpenAPI specification:

`http://localhost:8080/v3/api-docs`

The REST API uses versioned endpoints under `/api/v1`.

## Testing

Run the complete test suite:

```bash
./mvnw clean verify
```

The test suite includes unit tests and integration tests using PostgreSQL with Testcontainers.

Covered scenarios include payment state transitions, order lifecycle rules, concurrent stock reservations, and transaction rollback.

## Transactional Consistency

Order creation and cancellation are transactional operations.

Pessimistic locking is used to coordinate concurrent inventory and payment operations, reducing the risk of overselling and inconsistent payment states.

## Author

**Pedro Rodrigo**

GitHub: https://github.com/pedrorodrigomartins
