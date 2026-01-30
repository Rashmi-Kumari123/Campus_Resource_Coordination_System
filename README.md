# Campus Resource Coordination System (CRCS)

A microservices-based platform for managing campus resources (rooms, labs, equipment) with real-time availability tracking, booking management, and notifications.

- **End-to-end workflows**: [VISUAL_WORKFLOW.md](VISUAL_WORKFLOW.md) — signup, login, booking lifecycle, notifications.
- **API reference**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md) — endpoints, request/response schemas, and usage.

---

### Project Structure

```
Campus Resource Coordination System/
├── api-gateway/
├── auth-service/
├── user-service/
├── resource-service/
├── booking-service/
├── notification-service/
├── crcs-common/
│   └── enums/
│       └── UserRole.java
├── crcs-kafka-common/
└── frontend
```

---

## Architecture

The platform follows a microservices architecture with the following components:

### Services

- **api-gateway**: API Gateway for routing requests (Port 6000)
- **auth-service**: Authentication and authorization service (Port 6001)
- **user-service**: User profile management service (Port 6002)
- **resource-service**: Resource management service for rooms, labs, and equipment (Port 6003)
- **booking-service**: Booking management service (Port 6004)
- **notification-service**: Notification service for emails and other notifications (Port 6005)

### Infrastructure

- **MySQL**: Database for all services
- **Kafka**: Message broker for asynchronous notifications
- **Zookeeper**: Required for Kafka

### Common Libraries

- **crcs-common**: Common utilities, DTOs, and enums (including UserRole)
- **crcs-kafka-common**: Centralized Kafka configuration and utilities

### Frontend

- **frontend**: React (Vite + TypeScript) UI with role-based access, integrated with the API Gateway. See [frontend/README.md](frontend/README.md).

---

## Features

### Core Features

- **Resource Listing**: Manage rooms, labs, and equipment
- **Real-time Availability Status**: Track resource availability in real-time
- **Ownership & Responsibility Tracking**: Assign owners and responsible persons to resources
- **Booking Management**: Create, update, and cancel bookings
- **Notifications**: Email notifications for booking confirmations and cancellations
- **Role-Based Access Control**: Different user types with appropriate permissions

---

## User Roles

The system supports the following user roles:

### 1. **USER** (Default)

- **Description**: Regular campus user (students, faculty, staff)
- **Permissions**:
  - View available resources
  - Book resources
  - Manage own bookings
  - View own profile

### 2. **RESOURCE_MANAGER**

- **Description**: Manages campus resources (rooms, labs, equipment)
- **Permissions**:
  - All USER permissions
  - Create, update, and delete resources
  - Manage resource status
  - View all bookings

### 3. **FACILITY_MANAGER**

- **Description**: Manages facilities and approves bookings
- **Permissions**:
  - All RESOURCE_MANAGER permissions
  - Approve/reject bookings
  - Manage facility-wide settings

### 4. **ADMIN**

- **Description**: System administrator with full access
- **Permissions**:
  - All permissions
  - Manage users
  - System configuration
  - Access all data

---

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.1**
- **Spring Cloud Gateway** (API Gateway)
- **Spring Data JPA** (Database access)
- **MySQL 8.0** (Database)
- **Apache Kafka** (Message broker)
- **Docker & Docker Compose** (Containerization)

---

## Running the System

### Local Setup — Run the application

#### 1. Requirements

- JDK 21, Maven 3.6+, MySQL Workbench, Docker Desktop

#### 2. Clone and build

1. Clone the repo:
   ```bash
   git clone https://github.com/Rashmi-Kumari123/Campus-Resource-Coordination-System.git
   ```
2. Build the parent pom:
   ```bash
   mvn clean install
   ```
3. Rename `.env.example` to `.env` and paste your own required environment variables.

#### 3. Start Kafka locally

Open Docker Desktop (Docker engine) locally and run the following commands on terminal:

```bash
docker network create kafka-net
docker rm -f kafka zookeeper
docker run -d --name zookeeper --network kafka-net -p 2181:2181 -e ZOOKEEPER_CLIENT_PORT=2181 confluentinc/cp-zookeeper:7.4.0
docker run -d --name kafka --network kafka-net -p 9092:9092 -e KAFKA_BROKER_ID=1 -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 confluentinc/cp-kafka:7.4.0
docker exec -it kafka kafka-topics --create --topic crcs-notification --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
docker ps
```

#### 4. Run backend services

Run each service: api-gateway, notification-service, booking-service, resource-service, user-service, auth-service.

#### 5. Start the frontend locally

1. Navigate to the frontend: `cd frontend`
2. Install dependencies: `npm install`
3. Start dev server: `npm run dev`

---

#### Key Environment Variables

- `DB_URL`: Database connection URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: Secret key for JWT token generation (must be at least 32 characters)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker addresses (default: localhost:9092)
- `SMTP_HOST`: SMTP server host for email notifications (default: smtp.gmail.com)
- `SMTP_PORT`: SMTP server port (default: 587)
- `SMTP_USERNAME`: SMTP username for email sending
- `SMTP_PASSWORD`: SMTP password/app password for email sending
- `RESOURCE_SERVICE_URL`: Resource service URL (default: http://localhost:6003)

---

### Services Endpoints

- **API Gateway**: http://localhost:6000
- **Auth Service**: http://localhost:6001
- **User Service**: http://localhost:6002
- **Resource Service**: http://localhost:6003
- **Booking Service**: http://localhost:6004
- **Notification Service**: http://localhost:6005

---

## Role-Based Access Control

### How It Works

1. **JWT Token**: Contains user role information
2. **API Gateway**: Validates token and extracts role
3. **Role-Based Filter**: Checks if user has required permissions
4. **Service-Level**: Additional role checks in services if needed

### Role Validation

- Roles are validated during signup
- Invalid roles default to "USER"
- Role information is stored in JWT token
- API Gateway enforces role-based access

---

## License

This project is part of the Campus Resource Coordination System.
