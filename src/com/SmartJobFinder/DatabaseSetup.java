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

            // 4. Non-destructive migration for existing tables: check for 'status' column
            try {
                ResultSet rsStatus = stmt.executeQuery("SHOW COLUMNS FROM companies LIKE 'status'");
                if (!rsStatus.next()) {
                    stmt.executeUpdate("ALTER TABLE companies ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'");
                    System.out.println("[AppInitializer] Added 'status' column to companies table.");
                }
                rsStatus.close();
            } catch (Exception ignore) {}

            // Check for 'description' column
            try {
                ResultSet rsDesc = stmt.executeQuery("SHOW COLUMNS FROM companies LIKE 'description'");
                if (!rsDesc.next()) {
                    stmt.executeUpdate("ALTER TABLE companies ADD COLUMN description TEXT");
                    System.out.println("[AppInitializer] Added 'description' column to companies table.");
                }
                rsDesc.close();
            } catch (Exception ignore) {}

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
            stmt.executeUpdate("INSERT IGNORE INTO users (name, email, password) VALUES ('Demo User', 'demo@smartjobfinder.com', 'password123')");

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
                "0000", "lahari0405", "root", "", "admin", "password", 
                "1234", "123456", "12345678", "root123", "mysql"
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
            try {
                ResultSet rsStatus = appStmt.executeQuery("SHOW COLUMNS FROM companies LIKE 'status'");
                if (!rsStatus.next()) {
                    appStmt.executeUpdate("ALTER TABLE companies ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'");
                    System.out.println("✅ Added 'status' column to companies table.");
                }
                rsStatus.close();
            } catch (Exception ignore) {}

            try {
                ResultSet rsDesc = appStmt.executeQuery("SHOW COLUMNS FROM companies LIKE 'description'");
                if (!rsDesc.next()) {
                    appStmt.executeUpdate("ALTER TABLE companies ADD COLUMN description TEXT");
                    System.out.println("✅ Added 'description' column to companies table.");
                }
                rsDesc.close();
            } catch (Exception ignore) {}

            // Seed/Sync companies using CompanySeedData (110+ companies)
            CompanySeedData.seedOrUpdateCompanies(conn);
            System.out.println("✅ Verified and synchronized 110+ companies.");

            // Insert default admin
            appStmt.executeUpdate("DELETE FROM admins WHERE username != 'admin'");
            appStmt.executeUpdate("INSERT INTO admins (username, email, password) VALUES ('admin', 'admin@smartjobfinder.com', '12345') ON DUPLICATE KEY UPDATE password='12345'");
            System.out.println("✅ Admin account ready (admin / 12345)");

            // Insert demo user
            appStmt.executeUpdate("INSERT IGNORE INTO users (name, email, password) VALUES ('Demo User', 'demo@smartjobfinder.com', 'password123')");
            System.out.println("✅ Demo user ready (demo@smartjobfinder.com / password123)");

            appStmt.close();
            conn.close();

            System.out.println("\n🎉 Database setup completed 100% successfully!");

        } catch (Exception e) {
            System.err.println("❌ Database execution error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


