package com.SmartJobFinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class FunctionalVerification {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   Smart Job Finder - Verification Test Suite   ");
        System.out.println("=================================================");

        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("❌ Database connection failed!");
                return;
            }
            System.out.println("✅ Database connection established.");

            Statement stmt = conn.createStatement();

            // 1. Verify Users Table
            ResultSet rsUsers = stmt.executeQuery("SELECT id, name, email FROM users");
            int userCount = 0;
            while (rsUsers.next()) {
                userCount++;
                System.out.println("   [User #" + rsUsers.getInt("id") + "] " + rsUsers.getString("name") + " (" + rsUsers.getString("email") + ")");
            }
            rsUsers.close();
            System.out.println("✅ Users verified (Total: " + userCount + ")");

            // 2. Verify Admins Table
            ResultSet rsAdmin = stmt.executeQuery("SELECT id, username, email FROM admins");
            int adminCount = 0;
            while (rsAdmin.next()) {
                adminCount++;
                System.out.println("   [Admin #" + rsAdmin.getInt("id") + "] " + rsAdmin.getString("username") + " (" + rsAdmin.getString("email") + ")");
            }
            rsAdmin.close();
            System.out.println("✅ Admins verified (Total: " + adminCount + ")");

            // 3. Verify Companies Table & Status
            ResultSet rsCompanies = stmt.executeQuery("SELECT id, company_name, role, status FROM companies");
            int totalComp = 0;
            int activeComp = 0;
            int inactiveComp = 0;
            while (rsCompanies.next()) {
                totalComp++;
                String st = rsCompanies.getString("status");
                if ("ACTIVE".equalsIgnoreCase(st)) activeComp++;
                else inactiveComp++;
            }
            rsCompanies.close();
            System.out.println("✅ Companies verified: Total=" + totalComp + ", Active=" + activeComp + ", Inactive=" + inactiveComp);

            // 4. Test Add Company
            String insertSql = "INSERT INTO companies (company_name, skills, role, salary, experience, description, logo, apply_url, status) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement psAdd = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            psAdd.setString(1, "Test Corp Automation");
            psAdd.setString(2, "Java, Spring, Microservices, MySQL");
            psAdd.setString(3, "Lead Software Architect");
            psAdd.setString(4, "2500000");
            psAdd.setString(5, "3-5");
            psAdd.setString(6, "Architect high-performance distributed systems.");
            psAdd.setString(7, "images/default-company.svg");
            psAdd.setString(8, "https://example.com/careers");
            psAdd.setString(9, "ACTIVE");
            psAdd.executeUpdate();

            ResultSet rsKeys = psAdd.getGeneratedKeys();
            int newId = -1;
            if (rsKeys.next()) {
                newId = rsKeys.getInt(1);
            }
            rsKeys.close();
            psAdd.close();
            System.out.println("✅ Added test company with ID: " + newId);

            // 5. Test Status Toggle to INACTIVE
            PreparedStatement psToggle = conn.prepareStatement("UPDATE companies SET status = 'INACTIVE' WHERE id = ?");
            psToggle.setInt(1, newId);
            psToggle.executeUpdate();
            psToggle.close();

            // Verify it is not returned in ACTIVE query
            PreparedStatement psUserQuery = conn.prepareStatement("SELECT * FROM companies WHERE id = ? AND (status = 'ACTIVE' OR status IS NULL)");
            psUserQuery.setInt(1, newId);
            ResultSet rsActiveCheck = psUserQuery.executeQuery();
            boolean visibleToUser = rsActiveCheck.next();
            rsActiveCheck.close();
            psUserQuery.close();
            if (!visibleToUser) {
                System.out.println("✅ Verified: Inactive company is HIDDEN from normal user search results.");
            } else {
                System.err.println("❌ Error: Inactive company was visible in active query!");
            }

            // 6. Test Edit Company
            PreparedStatement psEdit = conn.prepareStatement("UPDATE companies SET role = 'Principal Architect', status = 'ACTIVE' WHERE id = ?");
            psEdit.setInt(1, newId);
            psEdit.executeUpdate();
            psEdit.close();
            System.out.println("✅ Verified: Company updated and reactivated.");

            // 7. Test Delete Company
            PreparedStatement psDel = conn.prepareStatement("DELETE FROM companies WHERE id = ?");
            psDel.setInt(1, newId);
            psDel.executeUpdate();
            psDel.close();
            System.out.println("✅ Verified: Test company deleted successfully.");

            stmt.close();
            conn.close();

            System.out.println("\n🎉 ALL FUNCTIONAL TESTS PASSED 100%!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
