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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            String err = URLEncoder.encode("Please provide both email and password.", StandardCharsets.UTF_8);
            response.sendRedirect("login.html?error=" + err);
            return;
        }

        try {
            Connection connection = DBConnection.getConnection();

            if (connection == null) {
                String err = URLEncoder.encode("Database connection failed. Please ensure MySQL is running.", StandardCharsets.UTF_8);
                response.sendRedirect("login.html?error=" + err);
                return;
            }

            String sql = "SELECT id, name, email, password, degree, branch, graduation_year, skills, preferred_role FROM users WHERE LOWER(email) = LOWER(?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email.trim());

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                int id = result.getInt("id");
                String name = result.getString("name");
                String storedPassword = result.getString("password");
                String degree = result.getString("degree");
                String branch = result.getString("branch");
                String skills = result.getString("skills");
                String role = result.getString("preferred_role");

                if (SecurityUtil.verifyPassword(password, storedPassword)) {
                    // Initialize HTTP Session
                    HttpSession session = request.getSession(true);
                    session.setAttribute("userId", id);
                    session.setAttribute("userEmail", email.trim());
                    session.setAttribute("userName", name);
                    session.setAttribute("userDegree", degree != null ? degree : "B.Tech / B.E.");
                    session.setAttribute("userBranch", branch != null ? branch : "Computer Science & Engineering");
                    session.setAttribute("userSkills", skills != null ? skills : "");
                    session.setAttribute("userRole", role != null ? role : "Software Engineer");

                    // Transparent upgrade of legacy plain-text password to SHA-256
                    if (!storedPassword.equals(SecurityUtil.hashPassword(password.trim()))) {
                        try (PreparedStatement upStmt = connection.prepareStatement("UPDATE users SET password = ? WHERE id = ?")) {
                            upStmt.setString(1, SecurityUtil.hashPassword(password.trim()));
                            upStmt.setInt(2, id);
                            upStmt.executeUpdate();
                        } catch (Exception ignore) {}
                    }

                    result.close();
                    statement.close();
                    connection.close();

                    response.sendRedirect("job-search.html");
                    return;
                }
            }

            result.close();
            statement.close();
            connection.close();

            String err = URLEncoder.encode("Invalid email or password. Please try again.", StandardCharsets.UTF_8);
            response.sendRedirect("login.html?error=" + err);

        } catch (Exception e) {
            e.printStackTrace();
            String err = URLEncoder.encode("System error: " + e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("login.html?error=" + err);
        }
    }
}