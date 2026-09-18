package com.SmartJobFinder;

import java.sql.Connection;
import java.sql.DriverManager;

public class PwdTest {
    public static void main(String[] args) {
        String[] list = {
            "root", "Root", "ROOT", "root123", "Root123", "Root@123", "root@123", "root1234", "Root1234", "Root@1234",
            "admin", "Admin", "ADMIN", "admin123", "Admin123", "Admin@123", "admin@123",
            "password", "Password", "Password123", "Password@123", "password@123", "pass", "Pass@123",
            "1234", "12345", "123456", "1234567", "12345678", "123456789", "1234567890", "0000", "00000000",
            "mysql", "MySQL", "mysql80", "MySQL80", "mysql123", "MySQL@123",
            "system", "System", "tiger", "scott", "oracle", "manager",
            ""
        };

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.out.println("Driver error");
            return;
        }

        for (String p : list) { 
            try {
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", p);
                System.out.println(">>> SUCCESS_FOUND_PASSWORD: [" + p + "]");
                conn.close();
                return;
            } catch (Exception ignore) {}
        }
        System.out.println(">>> NO_PASSWORD_MATCHED_IN_LIST");
    }
}