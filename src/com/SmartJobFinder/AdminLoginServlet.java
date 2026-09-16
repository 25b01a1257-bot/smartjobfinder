package com.SmartJobFinder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/adminLogin")
public class AdminLoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            response.sendRedirect("adminDashboard");
            return;
        }
        response.sendRedirect("admin-login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Admin Login Requirements:
        // - The ONLY valid admin username is: admin
        // - The ONLY valid admin password is: 12345
        // - Allow login ONLY when username is exactly 'admin' and password is exactly '12345'
        // - Reject every other username, email, and password combination
        // - Do not accept any email address as an admin username
        // - If username or password is incorrect, display: "Invalid admin username or password."
        if (username == null || password == null || !"admin".equals(username) || !"12345".equals(password)) {
            String err = URLEncoder.encode("Invalid admin username or password.", StandardCharsets.UTF_8);
            response.sendRedirect("admin-login.html?error=" + err);
            return;
        }

        int adminId = 1;
        String displayUsername = "admin";
        String displayEmail = "admin@smartjobfinder.com";

        try {
            Connection connection = DBConnection.getConnection();

            if (connection != null) {
                // Fetch the official admin record
                String sql = "SELECT id, username, email, password FROM admins WHERE username = 'admin'";
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    adminId = rs.getInt("id");
                    displayUsername = rs.getString("username");
                    displayEmail = rs.getString("email");

                    // Ensure password in database is 12345
                    if (!"12345".equals(rs.getString("password"))) {
                        try (PreparedStatement updateStmt = connection.prepareStatement("UPDATE admins SET password = '12345' WHERE id = ?")) {
                            updateStmt.setInt(1, adminId);
                            updateStmt.executeUpdate();
                        }
                    }
                } else {
                    // Seed official admin if missing
                    try (PreparedStatement insertStmt = connection.prepareStatement(
                            "INSERT INTO admins (username, email, password) VALUES ('admin', 'admin@smartjobfinder.com', '12345')",
                            PreparedStatement.RETURN_GENERATED_KEYS)) {
                        insertStmt.executeUpdate();
                        ResultSet genKeys = insertStmt.getGeneratedKeys();
                        if (genKeys.next()) {
                            adminId = genKeys.getInt(1);
                        }
                        genKeys.close();
                    }
                }

                rs.close();
                stmt.close();
                connection.close();
            }
        } catch (Exception e) {
            System.err.println("Admin login notice: " + e.getMessage());
        }

        // Initialize Secure Admin Session
        HttpSession session = request.getSession(true);
        session.setAttribute("adminLoggedIn", true);
        session.setAttribute("adminId", adminId);
        session.setAttribute("adminUsername", displayUsername);
        session.setAttribute("adminEmail", displayEmail);

        response.sendRedirect("adminDashboard");
    }
}

