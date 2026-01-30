# CRCS API Documentation

Complete API documentation for Campus Resource Coordination System (CRCS).

**Base URL**: `http://localhost:6000` (API Gateway)

**API Version**: v1

---

## Table of Contents

1. [Authentication](#authentication)
2. [User Management](#user-management)
3. [Resource Management](#resource-management)
4. [Booking Management](#booking-management)
5. [Error Handling](#error-handling)
6. [Rate Limiting](#rate-limiting)

---

## Authentication

All endpoints (except authentication endpoints) require a JWT token in the Authorization header.

### Headers
```
Authorization: Bearer <jwt_token>
```

### Sign Up

Create a new user account.

**Endpoint**: `POST /auth/signup`

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe",
  "role": "USER"
}
```

**Role Options**:
- `USER` (default) - Regular campus user
- `RESOURCE_MANAGER` - Can manage resources
- `FACILITY_MANAGER` - Can manage facilities
- `ADMIN` - System administrator

**Response** (201 Created):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 36000000,
  "claims": {
    "role": "USER",
    "userId": "uuid-here"
  },
  "user": {
    "userId": "uuid-here",
    "email": "user@example.com",
    "name": null,
    "role": "USER"
  }
}
```

**Error Responses**:
- `400 Bad Request` - Invalid input data
- `409 Conflict` - User already exists

---

### Login

Authenticate and receive JWT tokens.

**Endpoint**: `POST /auth/login`

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 36000000,
  "claims": {
    "role": "USER",
    "userId": "uuid-here"
  },
  "user": {
    "userId": "uuid-here",
    "email": "user@example.com",
    "name": null,
    "role": "USER"
  }
}
```

**Error Responses**:
- `401 Unauthorized` - Invalid credentials

---

### Refresh Token

Get a new access token using refresh token.

**Endpoint**: `POST /auth/refresh`

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 36000000,
  "claims": {
    "role": "USER",
    "userId": "uuid-here"
  },
  "user": {
    "userId": "uuid-here",
    "email": "user@example.com",
    "name": null,
    "role": "USER"
  }
}
```

**Error Responses**:
- `401 Unauthorized` - Invalid or expired refresh token

---

### Logout

Logout user and invalidate refresh tokens.

**Endpoint**: `POST /auth/logout`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Response** (200 OK):
```json
{
  "message": "Logged out successfully",
  "timestamp": "2025-01-28T12:00:00"
}
```

---

## User Management

### Create User Profile

Create a new user profile. Use this when you need to register a user profile in the system (e.g. after signup via auth-service or for admin-created users).

**Endpoint**: `POST /users`

**Headers**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "userId": "uuid-here",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "USER"
}
```

**Request Body Fields**:
- `userId` (string, required) - Unique user ID (e.g. from auth-service signup)
- `email` (string, required) - Valid email address
- `name` (string, optional) - Display name
- `role` (string, optional, default: `USER`) - User role. Options: `USER`, `RESOURCE_MANAGER`, `FACILITY_MANAGER`, `ADMIN`

**Response** (201 Created):
```json
{
  "userId": "uuid-here",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "USER",
  "profilePicture": null,
  "bio": null,
  "phoneNumber": null,
  "isEmailVerified": false,
  "isPhoneVerified": false,
  "isActive": true,
  "createdAt": "2025-01-28T10:00:00",
  "updatedAt": "2025-01-28T10:00:00"
}
```

**Error Responses**:
- `400 Bad Request` - Invalid input (e.g. invalid email, missing required fields)
- `401 Unauthorized` - Missing or invalid token
- `409 Conflict` - User profile already exists for the given userId

---

### Get User Profile

Retrieve user profile by ID.

**Endpoint**: `GET /users/{userId}`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `userId` (string, required) - User ID

**Response** (200 OK):
```json
{
  "userId": "uuid-here",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "USER",
  "profilePicture": "https://example.com/avatar.jpg",
  "bio": "Software Engineer",
  "phoneNumber": "+1234567890",
  "isEmailVerified": true,
  "isPhoneVerified": false,
  "isActive": true,
  "createdAt": "2025-01-28T10:00:00",
  "updatedAt": "2025-01-28T12:00:00"
}
```

**Error Responses**:
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - User not found

---

### Update User Profile

Update user profile information.

**Endpoint**: `PUT /users/{userId}`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `userId` (string, required) - User ID

**Request Body**:
```json
{
  "name": "John Doe",
  "email": "newemail@example.com",
  "bio": "Updated bio",
  "profilePicture": "https://example.com/new-avatar.jpg",
  "phoneNumber": "+1234567890"
}
```

**Response** (200 OK):
```json
{
  "message": "Profile updated successfully",
  "timestamp": "2025-01-28T12:00:00",
  "data": {
    "userId": "uuid-here",
    "email": "newemail@example.com",
    "name": "John Doe",
    "role": "USER",
    "profilePicture": "https://example.com/new-avatar.jpg",
    "bio": "Updated bio",
    "phoneNumber": "+1234567890",
    "isEmailVerified": true,
    "isPhoneVerified": false,
    "isActive": true,
    "createdAt": "2025-01-28T10:00:00",
    "updatedAt": "2025-01-28T12:00:00"
  }
}
```

---

### Get All Users

Get paginated list of all active users.

**Endpoint**: `GET /users`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Query Parameters**:
- `page` (integer, optional, default: 0) - Page number (0-indexed)
- `size` (integer, optional, default: 20) - Page size

**Response** (200 OK):
```json
{
  "content": [
    {
      "userId": "uuid-1",
      "email": "user1@example.com",
      "name": "User One",
      "role": "USER",
      "isActive": true
    },
    {
      "userId": "uuid-2",
      "email": "user2@example.com",
      "name": "User Two",
      "role": "RESOURCE_MANAGER",
      "isActive": true
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true
}
```

---

### Deactivate User

Deactivate a user account.

**Endpoint**: `POST /users/{userId}/deactivate`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `userId` (string, required) - User ID

**Response** (200 OK):
```json
{
  "message": "Account deactivated successfully",
  "timestamp": "2025-01-28T12:00:00"
}
```

---

### Delete User

Permanently delete a user account.

**Endpoint**: `DELETE /users/{userId}`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `userId` (string, required) - User ID

**Response** (200 OK):
```json
{
  "message": "User deleted successfully",
  "timestamp": "2025-01-28T12:00:00"
}
```

---

## Resource Management

### Create Resource

Create a new resource (room, lab, or equipment).

**Endpoint**: `POST /resources`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Required Role add it in X-User-Role in header **: `RESOURCE_MANAGER`, `FACILITY_MANAGER`, or `ADMIN`

**Request Body**:
```json
{
  "name": "Conference Room A",
  "type": "ROOM",
  "description": "Large conference room with projector and whiteboard",
  "location": "Building 1, Floor 2, Room 201",
  "capacity": 50,
  "ownerId": "user-uuid",
  "responsiblePerson": "John Doe"
}
```

**Resource Types**:
- `ROOM` - Meeting/conference room
- `LAB` - Laboratory
- `EQUIPMENT` - Equipment item

**Response** (201 Created):
```json
{
  "id": "resource-uuid",
  "name": "Conference Room A",
  "type": "ROOM",
  "description": "Large conference room with projector and whiteboard",
  "status": "AVAILABLE",
  "location": "Building 1, Floor 2, Room 201",
  "capacity": 50,
  "ownerId": "user-uuid",
  "responsiblePerson": "John Doe",
  "createdAt": "2025-01-28T10:00:00",
  "updatedAt": "2025-01-28T10:00:00"
}
```

**Error Responses**:
- `400 Bad Request` - Invalid input data
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Insufficient permissions

---

### Get Resource by ID

Retrieve a specific resource by its ID.

**Endpoint**: `GET /resources/{id}`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `id` (string, required) - Resource ID

**Response** (200 OK):
```json
{
  "id": "resource-uuid",
  "name": "Conference Room A",
  "type": "ROOM",
  "description": "Large conference room with projector",
  "status": "AVAILABLE",
  "location": "Building 1, Floor 2",
  "capacity": 50,
  "ownerId": "user-uuid",
  "responsiblePerson": "John Doe",
  "createdAt": "2025-01-28T10:00:00",
  "updatedAt": "2025-01-28T10:00:00"
}
```

**Resource Status Values**:
- `AVAILABLE` - Available for booking
- `BOOKED` - Currently booked
- `MAINTENANCE` - Under maintenance
- `UNAVAILABLE` - Not available

---

### Get All Resources

Retrieve all resources with pagination.

**Endpoint**: `GET /resources`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Query Parameters**:
- `page` (integer, optional, default: 0) - Page number
- `size` (integer, optional, default: 10) - Page size

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "resource-uuid-1",
      "name": "Conference Room A",
      "type": "ROOM",
      "status": "AVAILABLE",
      "location": "Building 1",
      "capacity": 50
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 50,
  "totalPages": 5,
  "last": false
}
```

---

### Get Resources by Type

Retrieve resources filtered by type.

**Endpoint**: `GET /resources/type/{type}`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `type` (enum, required) - Resource type: `ROOM`, `LAB`, or `EQUIPMENT`

**Query Parameters**:
- `page` (integer, optional, default: 0)
- `size` (integer, optional, default: 10)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "resource-uuid",
      "name": "Conference Room A",
      "type": "ROOM",
      "status": "AVAILABLE",
      "location": "Building 1",
      "capacity": 50
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 20,
  "totalPages": 2,
  "last": false
}
```

---

### Get Available Resources

Retrieve all available resources.

**Endpoint**: `GET /resources/available`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Query Parameters**:
- `page` (integer, optional, default: 0)
- `size` (integer, optional, default: 10)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "resource-uuid",
      "name": "Conference Room A",
      "type": "ROOM",
      "status": "AVAILABLE",
      "location": "Building 1",
      "capacity": 50
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 15,
  "totalPages": 2,
  "last": false
}
```

---

### Update Resource

Update an existing resource.

**Endpoint**: `PUT /resources/{id}`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Required Role**: `RESOURCE_MANAGER`, `FACILITY_MANAGER`, or `ADMIN`

**Path Parameters**:
- `id` (string, required) - Resource ID

**Request Body**:
```json
{
  "name": "Updated Conference Room A",
  "description": "Updated description",
  "location": "Building 2, Floor 3",
  "capacity": 60,
  "status": "AVAILABLE"
}
```

**Response** (200 OK):
```json
{
  "id": "resource-uuid",
  "name": "Updated Conference Room A",
  "type": "ROOM",
  "description": "Updated description",
  "status": "AVAILABLE",
  "location": "Building 2, Floor 3",
  "capacity": 60,
  "ownerId": "user-uuid",
  "responsiblePerson": "John Doe",
  "createdAt": "2025-01-28T10:00:00",
  "updatedAt": "2025-01-28T12:00:00"
}
```

---

### Update Resource Status

Update the status of a resource.

**Endpoint**: `PATCH /resources/{id}/status`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Required Role**: `RESOURCE_MANAGER`, `FACILITY_MANAGER`, or `ADMIN`

**Path Parameters**:
- `id` (string, required) - Resource ID

**Query Parameters**:
- `status` (enum, required) - New status: `AVAILABLE`, `BOOKED`, `MAINTENANCE`, `UNAVAILABLE`

**Response** (200 OK):
```json
{
  "message": "Resource status updated successfully",
  "timestamp": "2025-01-28T12:00:00"
}
```

---

### Delete Resource

Delete a resource by ID.

**Endpoint**: `DELETE /resources/{id}`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Required Role**: `RESOURCE_MANAGER`, `FACILITY_MANAGER`, or `ADMIN`

**Path Parameters**:
- `id` (string, required) - Resource ID

**Response** (200 OK):
```json
{
  "message": "Resource deleted successfully",
  "timestamp": "2025-01-28T12:00:00"
}
```

---

### Get Resources by Owner

Retrieve all resources owned by a specific user.

**Endpoint**: `GET /resources/owner/{ownerId}`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `ownerId` (string, required) - Owner user ID

**Response** (200 OK):
```json
[
  {
    "id": "resource-uuid-1",
    "name": "Conference Room A",
    "type": "ROOM",
    "status": "AVAILABLE",
    "location": "Building 1",
    "capacity": 50,
    "ownerId": "owner-uuid",
    "responsiblePerson": "John Doe"
  },
  {
    "id": "resource-uuid-2",
    "name": "Lab B",
    "type": "LAB",
    "status": "AVAILABLE",
    "location": "Building 2",
    "capacity": 30,
    "ownerId": "owner-uuid",
    "responsiblePerson": "Jane Smith"
  }
]
```

---

## Booking Management

### Check Availability

Check if a resource is available for a given time slot.

**Endpoint**: `GET /bookings/availability`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Query Parameters**:
- `resourceId` (string, required) - Resource ID
- `startTime` (datetime, required) - Start time (ISO 8601 format)
- `endTime` (datetime, required) - End time (ISO 8601 format)

**Example**:
```
GET /bookings/availability?resourceId=resource-uuid&startTime=2025-02-01T10:00:00&endTime=2025-02-01T12:00:00
```

**Response** (200 OK):
```json
{
  "available": true,
  "resourceId": "resource-uuid",
  "startTime": "2025-02-01T10:00:00",
  "endTime": "2025-02-01T12:00:00",
  "message": "Resource is available"
}
```

**Response when not available** (200 OK):
```json
{
  "available": false,
  "resourceId": "resource-uuid",
  "startTime": "2025-02-01T10:00:00",
  "endTime": "2025-02-01T12:00:00",
  "message": "Resource is already booked for this time slot"
}
```

---

### Check Availability (POST)

Check if a resource is available for a given time slot using a request body (alternative to GET with query parameters).

**Endpoint**: `POST /bookings/availability`

**Headers**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "resourceId": "resource-uuid",
  "startTime": "2025-02-01T10:00:00",
  "endTime": "2025-02-01T12:00:00"
}
```

**Request Body Fields**:
- `resourceId` (string, required) - Resource ID
- `startTime` (datetime, required) - Start time (ISO 8601 format, e.g. `2025-02-01T10:00:00`)
- `endTime` (datetime, required) - End time (ISO 8601 format)

**Response** (200 OK):
```json
{
  "available": true,
  "resourceId": "resource-uuid",
  "startTime": "2025-02-01T10:00:00",
  "endTime": "2025-02-01T12:00:00",
  "message": "Resource is available"
}
```

**Response when not available** (200 OK):
```json
{
  "available": false,
  "resourceId": "resource-uuid",
  "startTime": "2025-02-01T10:00:00",
  "endTime": "2025-02-01T12:00:00",
  "message": "Resource is already booked for this time slot"
}
```

**Error Responses**:
- `400 Bad Request` - Invalid request body or missing required fields

---

### Create Booking

Create a new booking for a resource.

**Endpoint**: `POST /bookings`

**Headers**:
```
Authorization: Bearer <jwt_token>
X-User-Id: <user-id>
```

**Request Body**:
```json
{
  "resourceId": "resource-uuid",
  "startTime": "2025-02-01T10:00:00",
  "endTime": "2025-02-01T12:00:00",
  "purpose": "Team meeting"
}
```

**Response** (201 Created):
```json
{
  "id": "booking-uuid",
  "userId": "user-uuid",
  "resourceId": "resource-uuid",
  "resourceName": "Conference Room A",
  "startTime": "2025-02-01T10:00:00",
  "endTime": "2025-02-01T12:00:00",
  "status": "PENDING",
  "purpose": "Team meeting",
  "createdAt": "2025-01-28T10:00:00",
  "updatedAt": "2025-01-28T10:00:00"
}
```

New bookings are created with status `PENDING` and require approval by a FACILITY_MANAGER via `POST /bookings/{id}/approve` before the resource is marked as booked.

**Booking Status Values**:
- `PENDING` - Booking is pending approval
- `CONFIRMED` - Booking is confirmed
- `CANCELLED` - Booking is cancelled
- `COMPLETED` - Booking is completed

**Error Responses**:
- `400 Bad Request` - Resource not available or invalid time slot
- `401 Unauthorized` - Missing or invalid token

---

### Get Booking by ID

Retrieve a specific booking by its ID.

**Endpoint**: `GET /bookings/{id}`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `id` (string, required) - Booking ID

**Response** (200 OK):
```json
{
  "id": "booking-uuid",
  "userId": "user-uuid",
  "resourceId": "resource-uuid",
  "resourceName": "Conference Room A",
  "startTime": "2025-02-01T10:00:00",
  "endTime": "2025-02-01T12:00:00",
  "status": "CONFIRMED",
  "purpose": "Team meeting",
  "createdAt": "2025-01-28T10:00:00",
  "updatedAt": "2025-01-28T10:00:00"
}
```

---

### Get User Bookings

Retrieve all bookings for a specific user.

**Endpoint**: `GET /bookings/user/{userId}`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `userId` (string, required) - User ID

**Query Parameters**:
- `page` (integer, optional, default: 0)
- `size` (integer, optional, default: 10)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "booking-uuid-1",
      "userId": "user-uuid",
      "resourceId": "resource-uuid",
      "resourceName": "Conference Room A",
      "startTime": "2025-02-01T10:00:00",
      "endTime": "2025-02-01T12:00:00",
      "status": "CONFIRMED",
      "purpose": "Team meeting"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 5,
  "totalPages": 1,
  "last": true
}
```

---

### Get Resource Bookings

Retrieve all bookings for a specific resource.

**Endpoint**: `GET /bookings/resource/{resourceId}`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `resourceId` (string, required) - Resource ID

**Query Parameters**:
- `page` (integer, optional, default: 0)
- `size` (integer, optional, default: 10)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "booking-uuid",
      "userId": "user-uuid",
      "resourceId": "resource-uuid",
      "resourceName": "Conference Room A",
      "startTime": "2025-02-01T10:00:00",
      "endTime": "2025-02-01T12:00:00",
      "status": "CONFIRMED",
      "purpose": "Team meeting"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 3,
  "totalPages": 1,
  "last": true
}
```

---

### Update Booking Status

Update the status of a booking.

**Endpoint**: `PATCH /bookings/{id}/status`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `id` (string, required) - Booking ID

**Request Body**:
```json
{
  "status": "CANCELLED"
}
```

**Response** (200 OK):
```json
{
  "id": "booking-uuid",
  "userId": "user-uuid",
  "resourceId": "resource-uuid",
  "resourceName": "Conference Room A",
  "startTime": "2025-02-01T10:00:00",
  "endTime": "2025-02-01T12:00:00",
  "status": "CANCELLED",
  "purpose": "Team meeting",
  "createdAt": "2025-01-28T10:00:00",
  "updatedAt": "2025-01-28T12:00:00"
}
```

---

### Approve Booking

Approve a pending booking. Intended for FACILITY_MANAGER (and ADMIN). Sets booking status to `CONFIRMED` and updates the resource status to `BOOKED`.

**Endpoint**: `POST /bookings/{id}/approve`

**Headers**:
```
Authorization: Bearer <jwt_token>
```

**Path Parameters**:
- `id` (string, required) - Booking ID

**Request**: No request body required.

**Response** (200 OK):
```json
{
  "id": "booking-uuid",
  "userId": "user-uuid",
  "resourceId": "resource-uuid",
  "resourceName": "Conference Room A",
  "startTime": "2025-02-01T10:00:00",
  "endTime": "2025-02-01T12:00:00",
  "status": "CONFIRMED",
  "purpose": "Team meeting",
  "createdAt": "2025-01-28T10:00:00",
  "updatedAt": "2025-01-28T12:00:00"
}
```

**Error Responses**:
- `400 Bad Request` - Booking not found or not in PENDING status (e.g. already approved or cancelled)
- `403 Forbidden` - Caller does not have FACILITY_MANAGER or ADMIN role (when enforced at gateway)

---

### Cancel Booking

Cancel a booking by ID.

**Endpoint**: `POST /bookings/{id}/cancel`

**Headers**:
```
Authorization: Bearer <jwt_token>
X-User-Id: <user-id>
```

**Path Parameters**:
- `id` (string, required) - Booking ID

**Response** (200 OK):
```json
{
  "message": "Booking cancelled successfully",
  "timestamp": "2025-01-28T12:00:00"
}
```

**Error Responses**:
- `404 Not Found` - Booking not found or user doesn't have permission

---

## Error Handling

### Error Response Format

All error responses follow this format:

```json
{
  "message": "Error description",
  "timestamp": "2025-01-28T12:00:00",
  "data": null
}
```

### HTTP Status Codes

- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid authentication token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., user already exists)
- `500 Internal Server Error` - Server error

### Common Error Scenarios

#### 401 Unauthorized
```json
{
  "message": "Invalid email or password",
  "timestamp": "2025-01-28T12:00:00"
}
```

#### 403 Forbidden
```json
{
  "message": "Insufficient permissions to manage resources",
  "timestamp": "2025-01-28T12:00:00"
}
```

#### 404 Not Found
```json
{
  "message": "User not found: uuid-here",
  "timestamp": "2025-01-28T12:00:00"
}
```

#### 400 Bad Request
```json
{
  "message": "Resource not available or booking failed",
  "timestamp": "2025-01-28T12:00:00"
}
```

---

## Rate Limiting

Currently, rate limiting is not implemented. In production, consider implementing:
- Rate limiting per user/IP
- Request throttling
- API key management for external integrations

---

## Swagger/OpenAPI Documentation

Interactive API documentation is available at:

- **Auth Service**: http://localhost:6000/api-docs/auth
- **User Service**: http://localhost:6000/api-docs/user
- **Resource Service**: http://localhost:6000/api-docs/resource
- **Booking Service**: http://localhost:6000/api-docs/booking

---

## Authentication Flow

1. **Sign Up or Login** to get JWT token
2. **Include token** in Authorization header for all subsequent requests
3. **Token expires** after 10 hours (use refresh token to get new token)
4. **Refresh token** expires after 30 days

### Token Format
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Role-Based Access Control

### USER Role
- ✅ View resources
- ✅ Create bookings
- ✅ Manage own bookings
- ✅ View own profile
- ❌ Manage resources
- ❌ View all bookings

### RESOURCE_MANAGER Role
- ✅ All USER permissions
- ✅ Create/update/delete resources
- ✅ Update resource status
- ✅ View all bookings for resources

### FACILITY_MANAGER Role
- ✅ All RESOURCE_MANAGER permissions
- ✅ Approve/reject bookings
- ✅ Manage facility-wide settings

### ADMIN Role
- ✅ All permissions
- ✅ Manage users
- ✅ System configuration
- ✅ Access all data

---

## Date/Time Format

All date/time fields use ISO 8601 format:
- Format: `YYYY-MM-DDTHH:mm:ss`
- Example: `2025-02-01T10:00:00`
- Timezone: UTC (or as configured)

---

## Pagination

All list endpoints support pagination:

**Query Parameters**:
- `page` - Page number (0-indexed, default: 0)
- `size` - Page size (default: 10 or 20 depending on endpoint)

**Response Format**:
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10,
  "last": false,
  "first": true
}
```

---

## Best Practices

1. **Always include Authorization header** for protected endpoints
2. **Handle token expiration** - Use refresh token to get new access token
3. **Validate input data** - Check required fields before sending requests
4. **Handle errors gracefully** - Check status codes and error messages
5. **Use pagination** - Don't fetch all data at once for large datasets
6. **Check availability** - Always check resource availability before booking
7. **Cancel unused bookings** - Free up resources for other users

---

## Support

For issues or questions:
- Check Swagger documentation at `/api-docs/{service}`
- Review error messages in responses
- Check service logs for detailed error information

---

**Last Updated**: January 28, 2025
**API Version**: v1
