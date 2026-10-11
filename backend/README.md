# MediConnect – Full-Stack Java

Hospital Appointment & Patient Management System.

## Stack
- Java 21
- Spring Boot 3.4.2
- Spring Security + JWT
- Spring Data JPA / Hibernate
- MySQL 8
- React + Vite

## Features
- Patient / doctor registration and login
- JWT authentication and role-based authorization
- Doctor directory and approval workflow
- Doctor availability / schedule management
- Department management
- Appointment booking with slot protection
- Appointment cancellation and status updates
- Patient/doctor/admin dashboards
- Profile management
- Admin user management and statistics

## Database setup
Create the database once:

```sql
CREATE DATABASE mediconnect;
```

Configure the environment before starting the backend. Example:

```bash
export DB_USERNAME=root
export DB_PASSWORD='YOUR_MYSQL_PASSWORD'
export JWT_SECRET='replace-with-a-long-random-secret-at-least-32-bytes'
export SEED_DEMO_DATA=false
```

On Windows PowerShell use `$env:DB_PASSWORD='...'` instead.

## Backend

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

API: http://localhost:8080/api
Health: http://localhost:8080/api/health

The backend creates a `default` hospital workspace on first startup. Hospital admins, doctors and patients are associated with a hospital. Existing rows are assigned to the default hospital by the startup backfill and the SQL migration in `database/migrations` adds tenant indexes and foreign keys.

To bootstrap the first platform administrator, set `PLATFORM_ADMIN_EMAIL` and a unique `PLATFORM_ADMIN_PASSWORD` (12–72 characters) before the first startup. This account can onboard hospitals and their admins; it is blocked from clinical APIs.

Demo accounts are not created by default. For a local-only demo database, set `SEED_DEMO_DATA=true` and provide unique `SEED_ADMIN_PASSWORD`, `SEED_DOCTOR_PASSWORD`, and `SEED_PATIENT_PASSWORD` values.

A new patient can register from the UI.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

## Notes
Do not commit real database passwords or JWT secrets. `DB_PASSWORD` and `JWT_SECRET` have no usable defaults; set them before starting the backend.

## Build fix

The Department repository includes `findByNameIgnoreCase(String)` because the startup data seeder uses that method when creating the default Cardiology department.
