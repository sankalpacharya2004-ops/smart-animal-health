# Smart Animal Health Assistant

A modern, high-performance web application designed to streamline clinic workflows, automate triage for pet owners, and empower veterinarians with decision-support tools. Built using a robust Java Servlet backend, a MySQL database, and a premium glassmorphic dark-slate user interface.

---

## 🚀 Key Features

### 1. Caretaker Portal (Animal Owners)
- **Animal Registry**: Log and maintain records of pets, farm animals, or stray animals (including species, breed, age, and weight).
- **Symptom Logger**: Report physical and behavioral observations (fever, reduced appetite, vomiting, low activity, limping, and custom symptoms).
- **Rules-Based Triage**: Powered by a custom **Inference Engine** that immediately evaluates symptom logs to estimate risk levels (Low, Medium, High) and provide recommended actions.

### 2. Veterinary Portal
- **Vet Overwatch Dashboard**: Real-time queue for checking triage alerts.
- **Clinical Override**: Review automated assessments, override triage status with final professional diagnoses, and prescribe medications.
- **Vaccination Tracker**: Track and schedule immunizations, and log administered dates with batch notes.

### 3. Administrator Portal
- **Caretaker Directory**: Full user account control panel. Dynamically promote or demote users to `User`, `Doctor`, or `Admin` roles.
- **System Guardrails**: Integrated UI and API-level checks to prevent self-demotion or self-deletion for the active administrator.
- **System Audit Logs**: Trace and audit all key system-wide clinic operations and modifications.

---

## 🛠️ Technology Stack

- **Backend**: Java Servlets (Servlet API 4.0), JDBC
- **Database**: MySQL 8.0+
- **Data Exchange**: Google Gson (JSON serialization/deserialization)
- **Frontend**: Semantic HTML5, Vanilla JavaScript (ES6+), Vanilla CSS3 (Custom-tailored HSL colors, responsive flex/grid layouts, ambient animations, and glassmorphism)
- **Web Server**: Apache Tomcat 9.0.x
- **Build & Execution**: PowerShell Automation Scripts

---

## 📂 Project Directory Structure

```text
d:\MCA Project Main\
├── src/
│   ├── main/
│   │   ├── java/com/smartanimal/
│   │   │   ├── dao/             # Database Access Objects (CRUD)
│   │   │   ├── model/           # Entity Model Classes
│   │   │   ├── servlet/         # HTTP Controllers & APIs
│   │   │   └── util/            # Inference Engine logic
│   │   └── webapp/
│   │       ├── css/             # CSS styling stylesheets
│   │       ├── js/              # Application scripts (auth.js, main.js)
│   │       └── WEB-INF/         # Configuration & deployment descriptor (web.xml)
├── compile_and_package.ps1      # Compiles Java code and generates WAR package
├── run_project.ps1              # Downloads Tomcat (if missing) and launches app
├── schema.sql                   # MySQL database creation and seed data script
└── README.md                    # This document
```

---

## 💾 Database Schema

The database consists of 5 main tables matching the application models:
1. **`users`**: Manages user accounts, credentials, and roles (`User`, `Doctor`, `Admin`).
2. **`animals`**: Holds animal registry details.
3. **`symptoms`**: Records observation logs linked to animals.
4. **`health_assessments`**: Stores automated triage results, doctor diagnoses, and prescriptions.
5. **`vaccinations`**: Logs scheduled and completed immunizations.

The setup script is located in [schema.sql](file:///d:/MCA%20Project%20Main/schema.sql).

---

## ⚙️ Quick Start Guide

### Prerequisites
- Java Development Kit (JDK 8 or higher) installed and set in your `PATH`.
- MySQL Server installed and running on `localhost:3306`.
- PowerShell execution policy enabled to run scripts locally.

### Setup and Running
1. **Initialize Database**:
   Import [schema.sql](file:///d:/MCA%20Project%20Main/schema.sql) into MySQL to create `animal_health_db` and insert test accounts:
   ```sql
   source d:/MCA Project Main/schema.sql;
   ```
   *Note: Default users included for testing:*
   - **Admin**: Username `admin` / Password `admin123`
   - **Doctor**: Username `doctor` / Password `password123`
   - **Caretaker**: Username `john_doe` / Password `password123`

2. **Configure Credentials**:
   Adjust the MySQL connection details inside [web.xml](file:///d:/MCA%20Project%20Main/src/main/webapp/WEB-INF/web.xml) (such as URL, username, and password) if necessary:
   ```xml
   <context-param>
       <param-name>db.password</param-name>
       <param-value>YourMySQLPassword</param-value>
   </context-param>
   ```

3. **Build the Application**:
   Compile the Java sources and package them into a `.war` file:
   ```powershell
   .\compile_and_package.ps1
   ```

4. **Run Server**:
   Start Apache Tomcat and deploy the WAR archive to `ROOT` context automatically:
   ```powershell
   .\run_project.ps1
   ```
   Open [http://localhost:8080/](http://localhost:8080/) in your browser.
