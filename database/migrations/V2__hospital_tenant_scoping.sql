-- Upgrade an existing MediConnect schema after the current Hibernate schema has been created.
-- This migration preserves existing rows and places legacy data in the default hospital.
CREATE TABLE IF NOT EXISTS hospitals (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    subscription_status VARCHAR(30) NOT NULL DEFAULT 'TRIAL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO hospitals (name, slug, active, subscription_status)
VALUES ('MediConnect Demo Hospital', 'default', TRUE, 'TRIAL')
ON DUPLICATE KEY UPDATE slug = VALUES(slug);

CREATE TABLE IF NOT EXISTS support_tickets (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    hospital_id BIGINT NOT NULL,
    created_by_id BIGINT NOT NULL,
    subject VARCHAR(180) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ticket_hospital_created (hospital_id, created_at),
    INDEX idx_ticket_creator (created_by_id),
    CONSTRAINT fk_ticket_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_ticket_creator FOREIGN KEY (created_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS appointment_reschedules (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    hospital_id BIGINT NOT NULL,
    appointment_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    old_date DATE NOT NULL,
    old_time TIME NOT NULL,
    new_date DATE NOT NULL,
    new_time TIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_reschedule_appt (appointment_id, created_at),
    INDEX idx_reschedule_hospital (hospital_id),
    CONSTRAINT fk_reschedule_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_reschedule_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    CONSTRAINT fk_reschedule_actor FOREIGN KEY (actor_id) REFERENCES users(id)
);

DELIMITER $$
DROP PROCEDURE IF EXISTS mediconnect_add_hospital_column$$
DROP PROCEDURE IF EXISTS mediconnect_add_hospital_index$$
DROP PROCEDURE IF EXISTS mediconnect_add_hospital_fk$$
CREATE PROCEDURE mediconnect_add_hospital_column(IN p_table VARCHAR(64))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = 'hospital_id') THEN
        SET @migration_sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN hospital_id BIGINT NULL');
        PREPARE migration_stmt FROM @migration_sql;
        EXECUTE migration_stmt;
        DEALLOCATE PREPARE migration_stmt;
    END IF;
END$$

CREATE PROCEDURE mediconnect_add_hospital_index(IN p_table VARCHAR(64))
BEGIN
    SET @index_name = CONCAT('idx_', p_table, '_hospital');
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics
                   WHERE table_schema = DATABASE() AND table_name = p_table AND index_name = @index_name) THEN
        SET @migration_sql = CONCAT('CREATE INDEX `', @index_name, '` ON `', p_table, '` (hospital_id)');
        PREPARE migration_stmt FROM @migration_sql;
        EXECUTE migration_stmt;
        DEALLOCATE PREPARE migration_stmt;
    END IF;
END$$

CREATE PROCEDURE mediconnect_add_hospital_fk(IN p_table VARCHAR(64))
BEGIN
    SET @constraint_name = CONCAT('fk_', p_table, '_hospital');
    IF NOT EXISTS (SELECT 1 FROM information_schema.key_column_usage
                   WHERE constraint_schema = DATABASE() AND table_name = p_table AND column_name = 'hospital_id'
                     AND referenced_table_name = 'hospitals') THEN
        SET @migration_sql = CONCAT('ALTER TABLE `', p_table, '` ADD CONSTRAINT `', @constraint_name,
                                    '` FOREIGN KEY (hospital_id) REFERENCES hospitals(id)');
        PREPARE migration_stmt FROM @migration_sql;
        EXECUTE migration_stmt;
        DEALLOCATE PREPARE migration_stmt;
    END IF;
END$$
DELIMITER ;

CALL mediconnect_add_hospital_column('users');
CALL mediconnect_add_hospital_column('departments');
CALL mediconnect_add_hospital_column('availabilities');
CALL mediconnect_add_hospital_column('appointments');
CALL mediconnect_add_hospital_column('medical_records');
CALL mediconnect_add_hospital_column('prescriptions');
CALL mediconnect_add_hospital_column('reviews');
CALL mediconnect_add_hospital_column('audit_logs');
CALL mediconnect_add_hospital_column('notifications');

SET @default_hospital_id = (SELECT id FROM hospitals WHERE slug = 'default');
UPDATE users SET hospital_id = @default_hospital_id WHERE hospital_id IS NULL AND role <> 'PLATFORM_SUPER_ADMIN';
UPDATE departments SET hospital_id = @default_hospital_id WHERE hospital_id IS NULL;
UPDATE availabilities SET hospital_id = @default_hospital_id WHERE hospital_id IS NULL;
UPDATE appointments SET hospital_id = @default_hospital_id WHERE hospital_id IS NULL;
UPDATE medical_records SET hospital_id = @default_hospital_id WHERE hospital_id IS NULL;
UPDATE prescriptions SET hospital_id = @default_hospital_id WHERE hospital_id IS NULL;
UPDATE reviews SET hospital_id = @default_hospital_id WHERE hospital_id IS NULL;
UPDATE audit_logs SET hospital_id = @default_hospital_id WHERE hospital_id IS NULL;
UPDATE notifications n JOIN users u ON u.id = n.user_id
    SET n.hospital_id = u.hospital_id WHERE n.hospital_id IS NULL;
UPDATE notifications SET hospital_id = @default_hospital_id WHERE hospital_id IS NULL;

CALL mediconnect_add_hospital_index('users');
CALL mediconnect_add_hospital_index('departments');
CALL mediconnect_add_hospital_index('availabilities');
CALL mediconnect_add_hospital_index('appointments');
CALL mediconnect_add_hospital_index('medical_records');
CALL mediconnect_add_hospital_index('prescriptions');
CALL mediconnect_add_hospital_index('reviews');
CALL mediconnect_add_hospital_index('audit_logs');
CALL mediconnect_add_hospital_index('notifications');

CALL mediconnect_add_hospital_fk('users');
CALL mediconnect_add_hospital_fk('departments');
CALL mediconnect_add_hospital_fk('availabilities');
CALL mediconnect_add_hospital_fk('appointments');
CALL mediconnect_add_hospital_fk('medical_records');
CALL mediconnect_add_hospital_fk('prescriptions');
CALL mediconnect_add_hospital_fk('reviews');
CALL mediconnect_add_hospital_fk('audit_logs');
CALL mediconnect_add_hospital_fk('notifications');

DROP PROCEDURE mediconnect_add_hospital_column;
DROP PROCEDURE mediconnect_add_hospital_index;
DROP PROCEDURE mediconnect_add_hospital_fk;

-- Remove the legacy global department-name uniqueness and enforce uniqueness inside each tenant.
SET @legacy_department_unique = (
    SELECT MAX(index_name) FROM (
        SELECT index_name FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'departments' AND non_unique = 0 AND index_name <> 'PRIMARY'
        GROUP BY index_name HAVING COUNT(*) = 1 AND SUM(column_name = 'name') = 1
    ) AS legacy_indexes
);
SET @migration_sql = IF(@legacy_department_unique IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE departments DROP INDEX `', @legacy_department_unique, '`'));
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;
SET @has_tenant_department_unique = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'departments' AND index_name = 'uk_department_hospital_name'
);
SET @migration_sql = IF(@has_tenant_department_unique > 0, 'SELECT 1',
    'ALTER TABLE departments ADD CONSTRAINT uk_department_hospital_name UNIQUE (hospital_id, name)');
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;
