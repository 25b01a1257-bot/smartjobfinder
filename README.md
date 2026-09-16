# ⚡ Smart Job Finder - AI-Powered Job Matching System

**Smart Job Finder** is a full-stack Java web application built on **Jakarta Servlet 6.0**, **JDBC**, **MySQL**, and modern **Vanilla HTML5/CSS3/JS**. It provides intelligent career opportunity matching by calculating multi-criteria compatibility scores (0–100%) based on technical skills, desired job roles, experience levels, and expected compensation.

---

## 🌟 Key Features

1. **Intelligent Match Scoring Engine (0–100%)**:
   - Calculates real-time compatibility based on skill overlap, role title alignment, experience bracket, and salary competitiveness.
   - Dynamically sorts and highlights top-ranked companies first.
2. **Direct HTTPS Application Links**:
   - Direct links to official corporate recruitment portals for Google, Microsoft, Amazon, Deloitte, Apple, Meta, TCS, Infosys, and more.
3. **Robust Connection & Resource Management**:
   - Clean, leak-free database operations using Java `try-with-resources`.
   - Automatic multi-credential fallback (`lahari0405`, `root`, blank password, or `DB_PASSWORD` environment variable).
4. **Session Management & Authentication**:
   - User registration and login with session persistence (`userName`, `userEmail`, `userId`).
   - Clean session invalidation with dedicated `/logout` handler.
5. **Modern Premium UI/UX**:
   - Sleek responsive glassmorphic cards, custom typography (*Plus Jakarta Sans* & *Outfit*), dynamic match badges, interactive quick-add skill chips, and live LPA compensation preview.

---

## 📁 Project Structure

```text
smartjobfinder/
├── schema.sql                         # MySQL database schema & seed SQL script
├── README.md                          # Project documentation and setup guide
├── lib/                               # Core project libraries
│   ├── jakarta.servlet-api.jar        # Jakarta Servlet 6.0 API
│   └── mysql-connector-j-26.7.0.jar   # MySQL JDBC Driver
├── src/
│   └── com/
│       └── SmartJobFinder/
│           ├── DBConnection.java      # Centralized database connection manager
│           ├── DBTest.java            # Diagnostic test tool for MySQL connection
│           ├── DatabaseSetup.java     # Automated database & seed data initializer
│           ├── LoginServlet.java      # Authentication servlet (/login)
│           ├── RegisterServlet.java   # Registration servlet (/register)
│           ├── LogoutServlet.java     # Session logout servlet (/logout)
│           └── FindJobsServlet.java   # Matching & recommendation servlet (/findJobs)
└── web/
    ├── index.html                     # Landing page with hero & feature showcase
    ├── login.html                     # Sign In page with demo credentials auto-fill
    ├── register.html                  # Account registration page
    ├── job-search.html                # Interactive job search & filter form
    ├── results.html                   # Results fallback placeholder page
    ├── css/
    │   └── style.css                  # Modern responsive design system
    ├── js/
    │   └── script.js                  # Client utilities, chip sync & alert banners
    ├── images/                        # Vector SVG company logos
    │   ├── google.svg
    │   ├── microsoft.svg
    │   ├── amazon.svg
    │   ├── apple.svg
    │   ├── meta.svg
    │   ├── deloitte.svg
    │   ├── accenture.svg
    │   ├── ibm.svg
    │   ├── infosys.svg
    │   ├── oracle.svg
    │   ├── tcs.svg
    │   └── default-company.svg
    └── WEB-INF/
        ├── web.xml                    # Servlet mappings & deployment descriptor
        ├── classes/                   # Compiled .class files for deployment
        └── lib/                       # Container deployment libraries
            ├── jakarta.servlet-api.jar
            └── mysql-connector-j-26.7.0.jar
```

---

## 🛠️ Prerequisites

- **Java JDK**: Version 17 or higher
- **Servlet Container**: Apache Tomcat 10+ (supporting Jakarta EE 10 / Servlet 6.0)
- **Database**: MySQL 8.0+ running on `localhost:3306`

---

## 🚀 Quick Setup & Execution Guide

### Step 1: Initialize the MySQL Database
You can initialize the database in **one step** using either of the following methods:

**Method A (Via DatabaseSetup.java - Recommended)**:
Run the `com.SmartJobFinder.DatabaseSetup` main class from your IDE or command line. It will automatically connect to MySQL, create the `smartjobfinder` database, create the tables, and seed all company openings and the demo user.

**Method B (Via MySQL CLI / Workbench)**:
Import and run the `schema.sql` script:
```bash
mysql -u root -p < schema.sql
```

---

### Step 2: Test Database Connection
Run `com.SmartJobFinder.DBTest` to verify that your MySQL connection is active and that tables have been populated:
```text
=================================================
  SmartJobFinder - Database Connection Test
=================================================
[✓] SUCCESS: Connected to MySQL database successfully!
[i] Table 'users' verified: 1 record(s) found.
[i] Table 'companies' verified: 11 job opening(s) found.
=================================================
```

---

### Step 3: Run the Web Application

#### In Eclipse / IntelliJ IDEA / VS Code:
1. Open the project folder `smartjobfinder`.
2. Add Tomcat 10+ server runtime.
3. Add `lib/jakarta.servlet-api.jar` and `lib/mysql-connector-j-26.7.0.jar` to the project build path.
4. Deploy the `web` folder to Tomcat and click **Run**.
5. Open your browser at: `http://localhost:8080/smartjobfinder/` (or `http://localhost:8080/web/`).

#### In Apache Tomcat Standalone:
1. Copy the contents of `smartjobfinder/web/` into Tomcat's `webapps/smartjobfinder/` directory.
2. Start Tomcat using `bin/startup.bat` (Windows) or `bin/startup.sh` (Linux/Mac).
3. Navigate to: `http://localhost:8080/smartjobfinder/index.html`.

---

## 🔑 Default Credentials

### 1. Job Seeker (User) Account:
- **Email**: `demo@smartjobfinder.com`
- **Password**: `password123`

### 2. Administrator Account:
- **Username**: `admin`
- **Email**: `admin@smartjobfinder.com`
- **Password**: `admin123`
- **Admin Portal URL**: `http://localhost:8080/smartjobfinder/admin-login.html` (or `/adminLogin`)

---

## 🛡️ Admin Management System Features

The application provides an Administrator Console (`/adminDashboard`) with strict session protection:

1. **Dashboard KPI Overview**: Real-time counters for Total Companies, Active Openings, Inactive/Hidden Listings, and Registered Users.
2. **Add Company**: Create new company openings with Name, Role, Tech Stack, Experience, Salary, Logo, Application Portal URL, Status, and Job Description.
3. **Edit Company**: Update any existing company details with instant modal loading.
4. **Active/Inactive Status Toggle**: Enable or disable company listings with a single click. Inactive companies are automatically hidden from normal job seekers while remaining fully manageable in the Admin Dashboard.
5. **Delete Company**: Remove outdated company records with confirmation modal safety.
6. **Live Search & Filter**: Real-time filtering by company name, role, skills, or visibility status (All, Active, Inactive).
7. **Session Guarding**: Normal users and unauthenticated visitors cannot access the Admin Dashboard directly by typing URLs.

---

## 📝 Summary of Components

| Component | Route / Path | Description | Access Level |
|---|---|---|---|
| **Landing Page** | `/index.html` | Hero, feature overview, navigation | Public |
| **User Sign In** | `/login.html`, `/login` | Job seeker authentication | Public |
| **User Registration** | `/register.html`, `/register` | Account registration | Public |
| **Job Search & Match** | `/job-search.html`, `/findJobs` | Multi-skill & criteria job discovery (ACTIVE only) | User |
| **Admin Sign In** | `/admin-login.html`, `/adminLogin` | Administrator authentication & session creation | Public |
| **Admin Dashboard** | `/adminDashboard` | Management console with KPI metrics & table | Admin Only |
| **Add Company** | `/addCompany`, `/admin/addCompany` | Add new job opening | Admin Only |
| **Edit Company** | `/editCompany`, `/admin/editCompany` | Update existing opening | Admin Only |
| **Delete Company** | `/deleteCompany`, `/admin/deleteCompany` | Permanently remove company opening | Admin Only |
| **Toggle Status** | `/updateCompanyStatus` | Toggle ACTIVE / INACTIVE visibility | Admin Only |
| **Admin Logout** | `/adminLogout` | Invalidate admin session | Admin Only |

