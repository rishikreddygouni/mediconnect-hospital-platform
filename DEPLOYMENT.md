# MediConnect deployment checklist

## Railway (GitHub connected deployment)

Railway deploys each Dockerfile as its own service; it does not run this Compose file as one deployment. Use a Railway MySQL service rather than the Compose database for a hosted deployment. See [Railway's Compose migration guide](https://docs.railway.com/guides/docker-compose).

1. Push this repository to GitHub. In Railway, create an empty project and add a MySQL database.
2. Add a GitHub service for this repository for the backend. Set its root directory to `/backend`, then name the service `backend`.
3. Add another GitHub service for the same repository for the frontend. Set its root directory to `/frontend`, then name the service `frontend`.
4. In the backend Variables, set:

   ```text
   DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   DB_USERNAME=${{MySQL.MYSQLUSER}}
   DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   JWT_SECRET=<generate a unique random secret of at least 32 bytes>
   DDL_AUTO=update
   SEED_DEMO_DATA=false
   PLATFORM_ADMIN_EMAIL=<your platform admin email>
   PLATFORM_ADMIN_PASSWORD=<unique password, 12–72 characters>
   CLIENT_URL=https://<frontend-domain>
   ```

   `MySQL` above must match the exact Railway MySQL service name. Keep its private networking enabled and do not expose the database publicly.
5. In the frontend Variables, set `BACKEND_HOST=backend.railway.internal`. The backend service can remain private because Nginx proxies `/api` requests through Railway's private network.
6. Generate a public domain for the frontend service in Settings → Networking. Set the backend's `CLIENT_URL` to that exact `https://` origin, then redeploy the backend.
7. Open the frontend domain and verify the app and `/api/health`. Review deployment logs for database or startup errors before using the service.

The host name `backend.railway.internal` is based on Railway's private-network naming rules; the frontend service is configured to proxy API traffic through it. Railway database variable references are documented in [Railway's MySQL guide](https://docs.railway.com/databases/mysql). Railway only assigns a public domain after you generate one in service networking settings; see [Railway domains](https://docs.railway.com/networking/domains/working-with-domains).

**Deployment status:** this setup has not yet been deployed to Railway. The backend test suite and Docker image have not been verified against Railway's managed MySQL environment. Do not use this as a production healthcare deployment until migrations, tenant isolation, backups, access control and privacy requirements have been validated.

## Docker deployment
1. Install Docker Engine/Desktop and Docker Compose.
2. Copy `.env.example` to `.env`.
3. Replace `DB_PASSWORD` and `JWT_SECRET` with unique production secrets.
4. Set `SEED_DEMO_DATA=false` for a real deployment, or change all seeded demo credentials.
5. Run `docker compose up --build -d`.
6. Check `docker compose ps` and `docker compose logs -f backend`.
7. Open `http://YOUR_SERVER_IP/`.
8. Verify `http://YOUR_SERVER_IP/api/health` reports `ok: true` and `database: UP`.

## Production recommendations
- Put HTTPS/TLS in front of the frontend using a managed load balancer or reverse proxy.
- Use a managed MySQL service or a durable encrypted volume and configure backups.
- Set `DDL_AUTO=validate` after the initial schema has been created.
- Use unique admin credentials and disable demo seeding.
- Rotate JWT/database credentials according to your hosting policy.
- Restrict MySQL so it is not exposed publicly.
- Configure the allowed frontend origin with `CLIENT_URL` if the API is hosted separately.

## Tenant migration from a single-hospital installation
1. Take and verify a database backup before deploying the tenant-aware backend.
2. During a maintenance window, start once with `DDL_AUTO=update` so Hibernate adds the hospital associations to the existing schema and the startup backfill assigns existing records to the `default` hospital.
3. Run `database/migrations/V2__hospital_tenant_scoping.sql` against the `mediconnect` database. It is additive and preserves existing data; it creates the hospital foreign keys and tenant indexes.
4. Verify the migrated data and hospital login flows, then set `DDL_AUTO=validate` for later production starts.

This repository currently carries the migration as a reviewed SQL script rather than an automatically applied Flyway migration. Do not skip the backup or apply the script to a database whose schema is different from this project.

To bootstrap the first platform administrator, set `PLATFORM_ADMIN_EMAIL` and `PLATFORM_ADMIN_PASSWORD` before the backend's first startup. The platform role can onboard hospitals and hospital admins but is blocked from clinical APIs. Hospital admins are created through the platform hospital-admin endpoint.
