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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("register.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {

            String err = URLEncoder.encode("All fields are required.", StandardCharsets.UTF_8);
            response.sendRedirect("register.html?error=" + err);
            return;
        }

        try {
            Connection connection = DBConnection.getConnection();

            if (connection == null) {
                String err = URLEncoder.encode("Database connection failed. Please ensure MySQL is running.", StandardCharsets.UTF_8);
                response.sendRedirect("register.html?error=" + err);
                return;
            }

            // Check if email already exists
            String checkSql = "SELECT id FROM users WHERE email = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setString(1, email.trim());
            ResultSet checkResult = checkStmt.executeQuery();

            if (checkResult.next()) {
                checkResult.close();
                checkStmt.close();
                connection.close();

                String err = URLEncoder.encode("An account with this email already exists. Please log in.", StandardCharsets.UTF_8);
                response.sendRedirect("login.html?error=" + err);
                return;
            }

            checkResult.close();
            checkStmt.close();

            // Insert new user
            String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, name.trim());
            statement.setString(2, email.trim());
            statement.setString(3, password);

            int result = statement.executeUpdate();

            statement.close();
            connection.close();

            if (result > 0) {
                String msg = URLEncoder.encode("Account registered successfully! Please sign in.", StandardCharsets.UTF_8);
                response.sendRedirect("login.html?message=" + msg);
            } else {
                String err = URLEncoder.encode("Registration failed. Please try again.", StandardCharsets.UTF_8);
                response.sendRedirect("register.html?error=" + err);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String err = URLEncoder.encode("Registration error: " + e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("register.html?error=" + err);
        }
    }
}