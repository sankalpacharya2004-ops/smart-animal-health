-- MySQL Database schema for Smart Animal Health Assistant
-- Database Name: animal_health_db

CREATE DATABASE IF NOT EXISTS animal_health_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE animal_health_db;

-- Drop tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS vaccinations;
DROP TABLE IF EXISTS health_assessments;
DROP TABLE IF EXISTS symptoms;
DROP TABLE IF EXISTS animals;
DROP TABLE IF EXISTS users;

-- Table 5: users
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NULL,
    email VARCHAR(100) UNIQUE NULL,
    role ENUM('Admin', 'Doctor', 'User') DEFAULT 'User',
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Table 1: animals
CREATE TABLE animals (
    animal_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NULL,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(100) NULL,
    age INT NULL,
    weight DECIMAL(6,2) NULL,
    animal_type ENUM('pet', 'farm', 'stray') NOT NULL,
    owner_name VARCHAR(100) NULL,
    contact_number VARCHAR(15) NULL,
    registration_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Table 2: symptoms
CREATE TABLE symptoms (
    symptom_id INT PRIMARY KEY AUTO_INCREMENT,
    animal_id INT NOT NULL,
    reduced_appetite BOOLEAN DEFAULT FALSE,
    fever BOOLEAN DEFAULT FALSE,
    vomiting BOOLEAN DEFAULT FALSE,
    low_activity BOOLEAN DEFAULT FALSE,
    limping BOOLEAN DEFAULT FALSE,
    other_symptoms TEXT NULL,
    recorded_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (animal_id) REFERENCES animals(animal_id) ON DELETE CASCADE
);

-- Table 3: health_assessments
CREATE TABLE health_assessments (
    assessment_id INT PRIMARY KEY AUTO_INCREMENT,
    symptom_id INT NOT NULL,
    animal_id INT NOT NULL,
    risk_level ENUM('Low', 'Medium', 'High') NOT NULL,
    possible_condition VARCHAR(200) NULL,
    recommended_action TEXT NULL,
    assessment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    doctor_diagnosis VARCHAR(200) NULL,
    treatment_notes TEXT NULL,
    prescription TEXT NULL,
    doctor_id INT NULL,
    FOREIGN KEY (symptom_id) REFERENCES symptoms(symptom_id) ON DELETE CASCADE,
    FOREIGN KEY (animal_id) REFERENCES animals(animal_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Table 4: vaccinations
CREATE TABLE vaccinations (
    vaccination_id INT PRIMARY KEY AUTO_INCREMENT,
    animal_id INT NOT NULL,
    doctor_id INT NULL,
    vaccine_name VARCHAR(100) NOT NULL,
    scheduled_date DATE NOT NULL,
    administered_date DATE NULL,
    status ENUM('Pending', 'Completed', 'Overdue') DEFAULT 'Pending',
    notes TEXT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (animal_id) REFERENCES animals(animal_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Table 6: appointments
CREATE TABLE appointments (
    appointment_id INT PRIMARY KEY AUTO_INCREMENT,
    animal_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATETIME NOT NULL,
    status ENUM('Pending', 'Scheduled', 'Completed', 'Cancelled') DEFAULT 'Pending',
    reason TEXT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (animal_id) REFERENCES animals(animal_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Insert Demo/Seed Data
-- Passwords are hashed using SHA-256
-- 'admin123' -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- 'password123' -> ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
INSERT INTO users (username, password_hash, full_name, email, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'System Administrator', 'admin@animalhealth.com', 'Admin'),
('doctor', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Dr. Smith', 'smith@animalhealth.com', 'Doctor'),
('john_doe', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'John Doe', 'john@example.com', 'User');

-- Insert animals for John Doe (user_id = 2)
INSERT INTO animals (user_id, name, species, breed, age, weight, animal_type, owner_name, contact_number) VALUES
(2, 'Buddy', 'Dog', 'Golden Retriever', 3, 30.50, 'pet', 'John Doe', '9876543210'),
(2, 'Luna', 'Cat', 'Siamese', 2, 4.20, 'pet', 'John Doe', '9876543210'),
(2, 'Bessie', 'Cow', 'Holstein', 4, 600.00, 'farm', 'John Doe', '9876543210');

-- Insert symptoms for Buddy (animal_id = 1)
INSERT INTO symptoms (animal_id, reduced_appetite, fever, vomiting, low_activity, limping, other_symptoms, recorded_date) VALUES
(1, TRUE, TRUE, TRUE, TRUE, FALSE, 'Vomiting white foam, refusing all water.', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, FALSE, FALSE, FALSE, FALSE, TRUE, 'Limps on left front paw after playing.', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Insert assessments
INSERT INTO health_assessments (symptom_id, animal_id, risk_level, possible_condition, recommended_action, assessment_date) VALUES
(1, 1, 'High', 'Parvovirus / Severe Gastric Infection', 'EMERGENCY: Contact a vet immediately. Isolate the animal and do not feed until instructed.', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 2, 'Medium', 'Musculoskeletal Injury / Sprain', 'VET VISIT: Restrict movement. Avoid stairs or jumping. Consult a vet if no improvement in 24 hours.', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Insert vaccinations
INSERT INTO vaccinations (animal_id, vaccine_name, scheduled_date, administered_date, status, notes) VALUES
(1, 'Rabies', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'Completed', 'Administered at Vet Clinic A'),
(1, 'DHPP Booster', DATE_ADD(CURDATE(), INTERVAL 14 DAY), NULL, 'Pending', 'Schedule morning appointment'),
(2, 'Feline Leukemia', DATE_SUB(CURDATE(), INTERVAL 5 DAY), NULL, 'Overdue', 'Reschedule as soon as possible');
