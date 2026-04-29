# MediCare — Doctor Appointment System (Backend)

> A feature-sliced Spring Boot REST API for a healthcare appointment management system, serving role-based portals for Patients, Doctors, and Admins.

**Repository:** [docter-appointment-system-backend](https://github.com/sibiraj17-arch-byte/docter-appointment-system-backend)  
**Frontend:** [doctor-appointment-system-frontend](https://github.com/sibiraj17-arch-byte/doctor-appointment-system-frontend)

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Tech Stack](#tech-stack)
3. [Prerequisites](#prerequisites)
4. [Directory Structure](#directory-structure)
5. [Feature-Based Architecture](#feature-based-architecture)
6. [Feature Flow Explanations](#feature-flow-explanations)
7. [Getting Started](#getting-started)
8. [Environment Configuration](#environment-configuration)
9. [API Overview](#api-overview)
10. [Security & Authorization](#security--authorization)

---

## System Overview

MediCare Backend is a Spring Boot application that provides the REST API layer for the doctor appointment system. It handles authentication, authorization, business logic, and data persistence. The React frontend communicates with this service exclusively over HTTP.

| Concern | Handled By |
|---|---|
| Authentication, authorization, data persistence | This Spring Boot backend |
| UI, routing, state management | React frontend |

Three distinct roles are supported: **PATIENT**, **DOCTOR**, and **ADMIN**, each with scoped access to their respective API endpoints.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot |
| Build Tool | Maven (with Maven Wrapper) |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL / PostgreSQL |
| API Style | RESTful JSON |
| Artifact ID | `com.healthcare:healthcare-app 1.0.0` |

---

## Prerequisites

- **Java 17** or above
- **Maven** (or use the included `mvnw` wrapper — no separate Maven install needed)
- **MySQL** or **PostgreSQL** running and accessible
- Frontend service (`doctor-appointment-system-frontend`) for full-stack usage

---

## Directory Structure

```
src/
└── main/
    ├── java/
    │   └── com/healthcare/
    │       │
    │       ├── config/
    │       │   └── SecurityConfig.java           # Spring Security config, JWT filter, CORS
    │       │
    │       ├── entity/                           # JPA entity classes (shared domain model)
    │       │   ├── Appointment.java
    │       │   ├── Prescription.java
    │       │   ├── Review.java
    │       │   └── [other shared entities]
    │       │
    │       └── feature/                          # Feature-sliced business modules
    │           │
    │           ├── admin/
    │           │   ├── controller/
    │           │   │   └── AdminController.java  # Admin REST endpoints
    │           │   └── service/
    │           │       └── AdminService.java     # Admin business logic
    │           │
    │           ├── auth/                         # Authentication & registration
    │           │   ├── controller/
    │           │   ├── dto/
    │           │   └── service/
    │           │
    │           ├── availability/
    │           │   └── controller/
    │           │       └── AvailabilityController.java  # Doctor availability slots
    │           │
    │           ├── billing/
    │           │   └── service/
    │           │       └── PaymentService.java   # Billing and payment logic
    │           │
    │           ├── discovery/
    │           │   ├── controller/
    │           │   │   └── DiscoveryController.java  # Public doctor discovery endpoints
    │           │   └── service/
    │           │       └── DiscoveryService.java
    │           │
    │           ├── medicalReports/
    │           │   ├── dto/
    │           │   │   └── MedicalReportResponseDTO.java
    │           │   └── service/
    │           │       └── MedicalReportService.java
    │           │
    │           ├── notifications/               # In-app notifications
    │           │   ├── controller/
    │           │   └── service/
    │           │
    │           ├── prescriptions/              # Prescription management
    │           │   ├── controller/
    │           │   ├── dto/
    │           │   ├── repository/
    │           │   └── service/
    │           │
    │           ├── professionals/              # Doctor (professional) management
    │           │   ├── dto/
    │           │   │   └── ProfessionalResponseDTO.java
    │           │   └── repository/
    │           │       └── ProfessionalRepository.java
    │           │
    │           ├── profile/                    # User profile management
    │           │   ├── controller/
    │           │   └── service/
    │           │
    │           ├── reviews/
    │           │   ├── controller/
    │           │   │   └── ReviewController.java
    │           │   ├── dto/
    │           │   │   └── ReviewResponseDTO.java
    │           │   ├── repository/
    │           │   │   └── ReviewRepository.java
    │           │   └── service/
    │           │       └── ReviewService.java
    │           │
    │           └── search/
    │               ├── controller/
    │               │   └── SearchController.java  # Doctor/appointment search endpoints
    │               └── service/
    │                   └── SearchService.java
    │
    └── resources/
        └── application.properties               # DB, JWT, server config
```

---

## Feature-Based Architecture

The `feature/` directory follows a feature-slice pattern. Each feature owns its controller (REST layer), service (business logic), repository (data access), and DTOs (data transfer objects). Shared JPA entities live in the top-level `entity/` package.

| Feature | Responsibility | Accessed By |
|---|---|---|
| `auth` | Login, registration, OTP verification, JWT issuance | Public |
| `professionals` | Doctor entity, repository, profile DTOs | Admin, Doctor |
| `discovery` | Public doctor browsing by specialization, location, rating | Public, Patient |
| `search` | Full-text doctor and appointment search | Patient, Doctor, Admin |
| `availability` | Doctor time slot management | Doctor, Patient |
| `admin` | User management, doctor approval/rejection, analytics | Admin |
| `billing` | Payment processing, invoice generation | Patient, Admin |
| `prescriptions` | Prescription creation and retrieval | Doctor, Patient |
| `medicalReports` | Medical report upload, view, download | Doctor, Patient |
| `reviews` | Doctor ratings and patient reviews | Patient |
| `notifications` | In-app notifications | All roles |
| `profile` | User profile view and update | All roles |
| `config` | Spring Security, JWT filter, CORS configuration | System |

---

## Feature Flow Explanations

### 1. Authentication Flow

```
Client → POST /api/auth/register { name, email, password, role }
  ← User created; 201 Created

Client → POST /api/auth/login { email, password }
  ← JWT token + role returned; 200 OK

Client → POST /api/auth/send-otp { mobile/email }
  ← OTP sent to user

Client → POST /api/auth/verify-otp { otp }
  ← Verified; account activated

All subsequent requests:
  → Authorization: Bearer <token> header
  → SecurityConfig validates token and sets SecurityContext
  → Role-based access applied per endpoint
```

---

### 2. Doctor Discovery & Search Flow

```
Public/Patient → GET /api/discovery/doctors
  ← All doctors with profile, specialization, rating

Public/Patient → GET /api/discovery/doctors?specialization=X
  ← Filtered by specialization

Public/Patient → GET /api/discovery/doctors/{id}
  ← Doctor profile with reviews and availability summary

Patient → GET /api/search?q=keyword
  ← Doctors, appointments, prescriptions matching keyword
  ← Uses SearchService → ProfessionalRepository, AppointmentRepository, PrescriptionRepository
```

---

### 3. Appointment Booking Flow

```
Patient → GET /api/availability/{doctorId}?date=YYYY-MM-DD
  ← Available time slots

Patient → POST /api/appointments { doctorId, slotId, date, reason }
  ← Appointment created; booking ID returned; 201 Created
  → Notification triggered for both patient and doctor

Doctor/Patient → GET /api/appointments?userId=X
  ← List of all appointments (upcoming + past)

Patient → DELETE /api/appointments/{id}
  ← Appointment cancelled; 200 OK

Doctor → PUT /api/appointments/{id}/status { status: COMPLETED }
  ← Appointment marked complete
```

---

### 4. Prescriptions Flow

```
Doctor → POST /api/prescriptions
    { appointmentId, patientId, medications: [...], notes }
  ← Prescription created; 201 Created

Patient/Doctor → GET /api/prescriptions?patientId=X
  ← All prescriptions for a patient

Patient/Doctor → GET /api/prescriptions/{id}
  ← Prescription detail with medication list
```

---

### 5. Billing & Payment Flow

```
System → Bill auto-generated on appointment completion
  → PaymentService reads appointment → doctor fee → creates invoice

Patient → GET /api/billing?patientId=X
  ← List of invoices (paid + unpaid)

Patient → POST /api/billing/{id}/pay { paymentMethod, transactionId }
  ← Bill marked as paid; 200 OK

Admin → GET /api/admin/billing/reports
  ← Revenue summary, outstanding payments
```

---

### 6. Medical Reports Flow

```
Doctor/Patient → POST /api/medical-reports
    { appointmentId, patientId, reportFile, description }
  ← Report uploaded and saved

Patient/Doctor → GET /api/medical-reports?patientId=X
  ← All reports for a patient

Patient/Doctor → GET /api/medical-reports/{id}
  ← Full report detail including doctorId and doctorName
```

---

### 7. Reviews Flow

```
Patient → POST /api/reviews
    { appointmentId, doctorId, rating, comment }
  ← Review created; 201 Created

Public/Patient → GET /api/reviews?doctorId=X
  ← All reviews for a doctor with average rating

Admin → DELETE /api/reviews/{id}
  ← Review removed
```

---

### 8. Admin Flow

```
Admin → GET /api/admin/users
  ← All users (patients + doctors)

Admin → PUT /api/admin/users/{id}/status { active: false }
  ← User deactivated

Admin → GET /api/admin/doctors?status=PENDING
  ← Doctors awaiting approval

Admin → PUT /api/admin/doctors/{id}/approve
  ← Doctor account approved; accessible to patients

Admin → PUT /api/admin/doctors/{id}/reject { reason }
  ← Doctor account rejected

Admin → GET /api/admin/analytics
  ← Total appointments, revenue, active users, top doctors
```

---

### 9. Profile Flow

```
Any authenticated user → GET /api/profile
  ← User data: name, email, mobile, role, specialization (if doctor)

Any authenticated user → PUT /api/profile { updatedFields }
  ← Profile updated; 200 OK
```

---

## Getting Started

### 1. Clone

```bash
git clone https://github.com/sibiraj17-arch-byte/docter-appointment-system-backend.git
cd docter-appointment-system-backend
```

### 2. Configure Environment

Edit `src/main/resources/application.properties`:

```properties
# Server
server.port=8088

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/healthcare_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=your_jwt_secret_key
jwt.expiration=86400000
```

### 3. Create the Database

```sql
CREATE DATABASE healthcare_db;
```

### 4. Build the Project

```bash
# Using Maven Wrapper (no Maven install required)
./mvnw clean install        # Linux / macOS
mvnw.cmd clean install      # Windows
```

### 5. Run the Application

```bash
./mvnw spring-boot:run      # Linux / macOS
mvnw.cmd spring-boot:run    # Windows

# Backend runs at http://localhost:8088
```

### 6. Run Tests

```bash
./mvnw test
```

---

## Environment Configuration

| Property | Description | Example |
|---|---|---|
| `server.port` | Port the app listens on | `8088` |
| `spring.datasource.url` | JDBC connection URL | `jdbc:mysql://localhost:3306/healthcare_db` |
| `spring.datasource.username` | DB username | `root` |
| `spring.datasource.password` | DB password | `password` |
| `spring.jpa.hibernate.ddl-auto` | Schema management strategy | `update` |
| `jwt.secret` | Secret key for JWT signing | `your_secret_key` |
| `jwt.expiration` | Token validity in milliseconds | `86400000` (24 hours) |

---

## API Overview

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user |
| `POST` | `/api/auth/login` | Public | Login, receive JWT |
| `POST` | `/api/auth/send-otp` | Public | Send OTP for verification |
| `POST` | `/api/auth/verify-otp` | Public | Verify OTP |
| `GET` | `/api/discovery/doctors` | Public | Browse all doctors |
| `GET` | `/api/discovery/doctors/{id}` | Public | View doctor profile |
| `GET` | `/api/search` | Patient/Doctor | Full-text search |
| `GET` | `/api/availability/{doctorId}` | Patient | Get available slots |
| `PUT` | `/api/availability` | Doctor | Update schedule/availability |
| `POST` | `/api/appointments` | Patient | Book an appointment |
| `GET` | `/api/appointments` | Patient/Doctor | Get appointments |
| `DELETE` | `/api/appointments/{id}` | Patient | Cancel appointment |
| `PUT` | `/api/appointments/{id}/status` | Doctor | Update appointment status |
| `POST` | `/api/prescriptions` | Doctor | Create prescription |
| `GET` | `/api/prescriptions` | Patient/Doctor | Get prescriptions |
| `GET` | `/api/billing` | Patient | Get billing history |
| `POST` | `/api/billing/{id}/pay` | Patient | Pay an invoice |
| `POST` | `/api/medical-reports` | Doctor/Patient | Upload medical report |
| `GET` | `/api/medical-reports` | Doctor/Patient | Get medical reports |
| `POST` | `/api/reviews` | Patient | Submit a review |
| `GET` | `/api/reviews` | Public | Get doctor reviews |
| `GET` | `/api/profile` | All | Get own profile |
| `PUT` | `/api/profile` | All | Update own profile |
| `GET` | `/api/admin/users` | Admin | List all users |
| `PUT` | `/api/admin/doctors/{id}/approve` | Admin | Approve a doctor |
| `PUT` | `/api/admin/doctors/{id}/reject` | Admin | Reject a doctor |
| `GET` | `/api/admin/analytics` | Admin | Dashboard analytics |

All protected endpoints require `Authorization: Bearer <token>`. Unauthenticated requests receive `401 Unauthorized`. Requests with insufficient role receive `403 Forbidden`.

---

## Security & Authorization

Security is configured in `SecurityConfig.java` using Spring Security with a stateless JWT-based session strategy.

| Role | Access Scope |
|---|---|
| `PATIENT` | Book appointments, view prescriptions/billing/reports, submit reviews, manage profile |
| `DOCTOR` | Manage schedule, view patients, create prescriptions/reports, view reviews |
| `ADMIN` | Full access — user management, doctor approval, analytics, billing reports |

The JWT filter intercepts every request, validates the token signature and expiry, and populates the `SecurityContext` with the authenticated user's role. Endpoint-level access control is enforced via `@PreAuthorize` or `HttpSecurity` role matchers in `SecurityConfig`.
