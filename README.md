# MediConnect – Hospital Appointment & Patient Management System

MediConnect is a full-stack hospital management platform with three role-based portals: **Patient, Doctor and Administrator**.

## Expanded features

### Administrator
- Complete patient and doctor directory with detailed profiles
- Doctor approval, activation/deactivation and account management
- All appointment history and status management
- Medical records and prescriptions across the hospital
- Department management
- Reviews and ratings
- Security audit logs
- System statistics and operational dashboard
- User search and detailed profile view

### Doctor
- Professional dashboard with today's total, completed and remaining appointments
- Complete appointment history: pending, confirmed, completed, cancelled and no-show
- Patient directory based on assigned appointments
- Custom visiting times and multiple clinic sessions
- Consultation notes
- Medical records
- Prescription creation
- Doctor ratings/reviews
- Notifications
- Profile and professional information management

### Patient
- Professional dashboard and health snapshot
- Doctor discovery and search
- Appointment booking and cancellation
- Upcoming, completed, cancelled and no-show history
- Medical records
- Prescriptions
- Doctor reviews and ratings
- Notifications
- Emergency contact, allergies and blood-group profile information

## Technology
- Backend: Java 21, Spring Boot 3.4.2, Spring Security, JWT, Spring Data JPA/Hibernate
- Database: MySQL 8
- Frontend: React 19, Vite
- Build: Maven / npm

## Run locally

### Database
Create the database:

```sql
CREATE DATABASE mediconnect;
```

### Backend
Set database credentials and a private JWT secret in the environment, then run:

```bash
export DB_USERNAME=root
export DB_PASSWORD='your-local-mysql-password'
export JWT_SECRET="$(openssl rand -base64 48)"
export SEED_DEMO_DATA=false
cd backend
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`.

### Frontend
Create `frontend/.env`:

```env
VITE_API_URL=http://localhost:8080/api
```

Then:

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on the Vite development URL, normally `http://localhost:5173`.

## Demo data
Demo seeding is disabled by default. For an isolated development database, set `SEED_DEMO_DATA=true` and provide unique `SEED_ADMIN_PASSWORD`, `SEED_DOCTOR_PASSWORD`, and `SEED_PATIENT_PASSWORD` values. Never enable demo seeding in a public deployment.

## Security note
Passwords are hashed with BCrypt and protected API routes use JWT authentication and role-based authorization. The application does not expose password hashes through its API responses.

For local development, the frontend uses `http://localhost:8080/api` via `frontend/.env`.
