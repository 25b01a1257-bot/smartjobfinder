package com.SmartJobFinder;

import java.sql.Connection;

public class DBTest {

    public static void main(String[] args) {

        Connection connection = DBConnection.getConnection();

        if (connection != null) {
            System.out.println("JDBC CONNECTION SUCCESSFUL!");
            try (java.sql.Statement s = connection.createStatement()) {
                try (java.sql.ResultSet rs = s.executeQuery("SELECT count(*) FROM companies")) {
                    if (rs.next()) {
                        System.out.println("Total Companies in Database: " + rs.getInt(1));
                    }
                }
                try (java.sql.ResultSet rs = s.executeQuery("SELECT count(*) FROM users")) {
                    if (rs.next()) {
                        System.out.println("Total Registered Users: " + rs.getInt(1));
                    }
                }
            } catch (Exception e) {
                System.err.println("Query check: " + e.getMessage());
            } finally {
                try { connection.close(); } catch (Exception ignore) {}
            }
        } else {
            System.out.println("JDBC CONNECTION FAILED!");
        }
    }
}