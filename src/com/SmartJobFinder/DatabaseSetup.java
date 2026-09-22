package com.SmartJobFinder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseSetup {

    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String USER = "root";

    public static void initializeDatabaseSilently() {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.out.println("[AppInitializer] Database not reachable yet, skipping auto-init.");
                return;
            }

            Statement stmt = conn.createStatement();

            // 1. Create users table
            String createUsers = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(150) NOT NULL UNIQUE, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            stmt.executeUpdate(createUsers);

            // 2. Create admins table
            String createAdmins = "CREATE TABLE IF NOT EXISTS admins ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(100) NOT NULL UNIQUE, "
                    + "email VARCHAR(150) NOT NULL UNIQUE, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            stmt.executeUpdate(createAdmins);

            // 3. Create companies table
            String createCompanies = "CREATE TABLE IF NOT EXISTS companies ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "company_name VARCHAR(100) NOT NULL, "
                    + "skills VARCHAR(300) NOT NULL, "
                    + "role VARCHAR(150) NOT NULL, "
                    + "salary VARCHAR(50) NOT NULL, "
                    + "experience VARCHAR(50) DEFAULT '0-2', "
                    + "description TEXT, "
                    + "logo VARCHAR(255) NOT NULL, "
                    + "apply_url VARCHAR(300) NOT NULL, "
                    + "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'"
                    + ")";
            stmt.executeUpdate(createCompanies);

            // 4. Non-destructive migration for existing tables: check for 'status' and 'description'
            addColumnIfNotExists(stmt, "companies", "status", "VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'");
            addColumnIfNotExists(stmt, "companies", "description", "TEXT");
            addColumnIfNotExists(stmt, "companies", "required_degree", "VARCHAR(100) DEFAULT 'B.Tech / B.E., MCA, M.Tech'");
            addColumnIfNotExists(stmt, "companies", "eligible_branches", "VARCHAR(300) DEFAULT 'Computer Science, Information Technology, Electronics & Communication'");
            addColumnIfNotExists(stmt, "companies", "min_qualification", "VARCHAR(100) DEFAULT 'Bachelor'");
            addColumnIfNotExists(stmt, "companies", "location", "VARCHAR(150) DEFAULT 'Pan India / Hybrid'");

            // Migrations for users table
            addColumnIfNotExists(stmt, "users", "degree", "VARCHAR(100) DEFAULT 'B.Tech / B.E.'");
            addColumnIfNotExists(stmt, "users", "branch", "VARCHAR(100) DEFAULT 'Computer Science & Engineering'");
            addColumnIfNotExists(stmt, "users", "graduation_year", "INT DEFAULT 2025");
            addColumnIfNotExists(stmt, "users", "skills", "TEXT");
            addColumnIfNotExists(stmt, "users", "preferred_role", "VARCHAR(150) DEFAULT 'Software Engineer'");
            addColumnIfNotExists(stmt, "users", "experience", "VARCHAR(50) DEFAULT '0-1'");
            addColumnIfNotExists(stmt, "users", "expected_salary", "VARCHAR(50) DEFAULT '800000'");
            addColumnIfNotExists(stmt, "users", "resume_name", "VARCHAR(255)");
            addColumnIfNotExists(stmt, "users", "bio", "TEXT");

            // Create job_applications table for Phase 3 Application Tracker
            String createAppTable = "CREATE TABLE IF NOT EXISTS job_applications ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "company_id INT NOT NULL, "
                    + "company_name VARCHAR(150) NOT NULL, "
                    + "job_title VARCHAR(150) NOT NULL, "
                    + "status VARCHAR(50) NOT NULL DEFAULT 'Applied', "
                    + "applied_date DATE NOT NULL, "
                    + "official_url VARCHAR(500), "
                    + "notes TEXT, "
                    + "follow_up_date DATE, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                    + "INDEX idx_user (user_id)"
                    + ")";
            stmt.executeUpdate(createAppTable);

            // Ensure any NULL status is set to ACTIVE
            try {
                stmt.executeUpdate("UPDATE companies SET status = 'ACTIVE' WHERE status IS NULL OR status = ''");
            } catch (Exception ignore) {}

            // 5. Seed and clean up default admin
            try {
                // Ensure only official admin exists
                stmt.executeUpdate("DELETE FROM admins WHERE username != 'admin'");

                ResultSet rsAdmin = stmt.executeQuery("SELECT COUNT(*) FROM admins WHERE username = 'admin'");
                int adminCount = 0;
                if (rsAdmin.next()) {
                    adminCount = rsAdmin.getInt(1);
                }
                rsAdmin.close();

                if (adminCount == 0) {
                    stmt.executeUpdate("INSERT INTO admins (username, email, password) VALUES ('admin', 'admin@smartjobfinder.com', '12345')");
                    System.out.println("[AppInitializer] ✅ Seeded default admin account (admin / 12345).");
                } else {
                    stmt.executeUpdate("UPDATE admins SET password = '12345' WHERE username = 'admin'");
                }
            } catch (Exception ignore) {}

            // 6. Seed/Sync companies using CompanySeedData (110+ companies)
            CompanySeedData.seedOrUpdateCompanies(conn);
            System.out.println("[AppInitializer] ✅ Verified and synchronized 110+ companies.");

            // Ensure demo user
            stmt.executeUpdate("INSERT IGNORE INTO users (name, email, password, degree, branch, graduation_year, skills, preferred_role) "
                    + "VALUES ('Demo User', 'demo@smartjobfinder.com', 'password123', 'B.Tech / B.E.', 'Computer Science & Engineering', 2025, 'Java, Python, SQL, Spring Boot', 'Software Engineer')");

            stmt.close();
            conn.close();
            System.out.println("[AppInitializer] ✅ Database tables, admin, & demo user initialized successfully.");
        } catch (Exception e) {
            System.err.println("[AppInitializer] Database init note: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     SmartJobFinder - Automated Database Setup    ");
        System.out.println("==================================================");

        String[] potentialPasswords;
        if (args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            potentialPasswords = new String[]{args[0].trim()};
        } else {
            potentialPasswords = new String[]{
                "lahari0405", "root", "", "admin", "password", "1234", "12345", 
                "123456", "12345678", "0000", "root123", "mysql"
            };
        }

        Connection testConn = null;
        String workingPassword = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.err.println("❌ MySQL JDBC Driver not found in classpath.");
            return;
        }

        System.out.println("Authenticating with MySQL server on localhost:3306...");

        for (String pwd : potentialPasswords) {
            try {
                testConn = DriverManager.getConnection(BASE_URL, USER, pwd);
                workingPassword = pwd;
                System.out.println("✅ Successfully authenticated with MySQL root user!");
                break;
            } catch (Exception ignore) {
                // Try next password
            }
        }

        if (testConn == null || workingPassword == null) {
            System.out.println("\n❌ [Access Denied] Could not log in with default passwords.");
            System.out.println("   Please provide your MySQL root password by running:");
            System.out.println("   java -cp \"web\\WEB-INF\\classes;lib\\*\" com.SmartJobFinder.DatabaseSetup YOUR_PASSWORD");
            return;
        }

        try {
            // 1. Create database
            Statement stmt = testConn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS smartjobfinder");
            System.out.println("✅ Database 'smartjobfinder' checked/created successfully.");
            stmt.close();
            testConn.close();

            // 2. Connect to smartjobfinder database
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smartjobfinder", USER, workingPassword);
            Statement appStmt = conn.createStatement();

            // Create users table
            String createUsers = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(150) NOT NULL UNIQUE, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            appStmt.executeUpdate(createUsers);
            System.out.println("✅ Table 'users' verified.");

            // Create admins table
            String createAdmins = "CREATE TABLE IF NOT EXISTS admins ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(100) NOT NULL UNIQUE, "
                    + "email VARCHAR(150) NOT NULL UNIQUE, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            appStmt.executeUpdate(createAdmins);
            System.out.println("✅ Table 'admins' verified.");

            // Create companies table
            String createCompanies = "CREATE TABLE IF NOT EXISTS companies ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "company_name VARCHAR(100) NOT NULL, "
                    + "skills VARCHAR(300) NOT NULL, "
                    + "role VARCHAR(150) NOT NULL, "
                    + "salary VARCHAR(50) NOT NULL, "
                    + "experience VARCHAR(50) DEFAULT '0-2', "
                    + "description TEXT, "
                    + "logo VARCHAR(255) NOT NULL, "
                    + "apply_url VARCHAR(300) NOT NULL, "
                    + "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'"
                    + ")";
            appStmt.executeUpdate(createCompanies);
            System.out.println("✅ Table 'companies' verified.");

            // Check & migrate columns if needed
            addColumnIfNotExists(appStmt, "companies", "status", "VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'");
            addColumnIfNotExists(appStmt, "companies", "description", "TEXT");
            addColumnIfNotExists(appStmt, "companies", "required_degree", "VARCHAR(100) DEFAULT 'B.Tech / B.E., MCA, M.Tech'");
            addColumnIfNotExists(appStmt, "companies", "eligible_branches", "VARCHAR(300) DEFAULT 'Computer Science, Information Technology, Electronics & Communication'");
            addColumnIfNotExists(appStmt, "companies", "min_qualification", "VARCHAR(100) DEFAULT 'Bachelor'");
            addColumnIfNotExists(appStmt, "companies", "location", "VARCHAR(150) DEFAULT 'Pan India / Hybrid'");

            addColumnIfNotExists(appStmt, "users", "degree", "VARCHAR(100) DEFAULT 'B.Tech / B.E.'");
            addColumnIfNotExists(appStmt, "users", "branch", "VARCHAR(100) DEFAULT 'Computer Science & Engineering'");
            addColumnIfNotExists(appStmt, "users", "graduation_year", "INT DEFAULT 2025");
            addColumnIfNotExists(appStmt, "users", "skills", "TEXT");
            addColumnIfNotExists(appStmt, "users", "preferred_role", "VARCHAR(150) DEFAULT 'Software Engineer'");
            addColumnIfNotExists(appStmt, "users", "experience", "VARCHAR(50) DEFAULT '0-1'");
            addColumnIfNotExists(appStmt, "users", "expected_salary", "VARCHAR(50) DEFAULT '800000'");
            addColumnIfNotExists(appStmt, "users", "resume_name", "VARCHAR(255)");
            addColumnIfNotExists(appStmt, "users", "bio", "TEXT");

            // Create job_applications table
            String createAppTable = "CREATE TABLE IF NOT EXISTS job_applications ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "company_id INT NOT NULL, "
                    + "company_name VARCHAR(150) NOT NULL, "
                    + "job_title VARCHAR(150) NOT NULL, "
                    + "status VARCHAR(50) NOT NULL DEFAULT 'Applied', "
                    + "applied_date DATE NOT NULL, "
                    + "official_url VARCHAR(500), "
                    + "notes TEXT, "
                    + "follow_up_date DATE, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                    + "INDEX idx_user (user_id)"
                    + ")";
            appStmt.executeUpdate(createAppTable);
            System.out.println("✅ Table 'job_applications' verified.");

            // Seed/Sync companies using CompanySeedData (110+ companies)
            CompanySeedData.seedOrUpdateCompanies(conn);
            System.out.println("✅ Verified and synchronized 110+ companies.");

            // Insert default admin
            appStmt.executeUpdate("DELETE FROM admins WHERE username != 'admin'");
            appStmt.executeUpdate("INSERT INTO admins (username, email, password) VALUES ('admin', 'admin@smartjobfinder.com', '12345') ON DUPLICATE KEY UPDATE password='12345'");
            System.out.println("✅ Admin account ready (admin / 12345)");

            // Insert demo user
            appStmt.executeUpdate("INSERT IGNORE INTO users (name, email, password, degree, branch, graduation_year, skills, preferred_role) "
                    + "VALUES ('Demo User', 'demo@smartjobfinder.com', 'password123', 'B.Tech / B.E.', 'Computer Science & Engineering', 2025, 'Java, Python, SQL, Spring Boot', 'Software Engineer')");
            System.out.println("✅ Demo user ready (demo@smartjobfinder.com / password123)");

            appStmt.close();
            conn.close();

            System.out.println("\n🎉 Database setup completed 100% successfully!");

        } catch (Exception e) {
            System.err.println("❌ Database execution error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addColumnIfNotExists(Statement stmt, String table, String column, String definition) {
        try {
            ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM " + table + " LIKE '" + column + "'");
            boolean exists = rs.next();
            rs.close();
            if (!exists) {
                stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                System.out.println("✅ Added column '" + column + "' to table '" + table + "'.");
            }
        } catch (Exception e) {
            System.err.println("Note adding column " + column + " to " + table + ": " + e.getMessage());
        }
    }
}


