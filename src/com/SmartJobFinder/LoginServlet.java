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

            String sql = "SELECT id, name, email FROM users WHERE email = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email.trim());
            statement.setString(2, password);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String name = result.getString("name");

                // Initialize HTTP Session
                HttpSession session = request.getSession(true);
                session.setAttribute("userEmail", email.trim());
                session.setAttribute("userName", name);

                result.close();
                statement.close();
                connection.close();

                response.sendRedirect("job-search.html");
            } else {
                result.close();
                statement.close();
                connection.close();

                String err = URLEncoder.encode("Invalid email or password. Please try again.", StandardCharsets.UTF_8);
                response.sendRedirect("login.html?error=" + err);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String err = URLEncoder.encode("System error: " + e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("login.html?error=" + err);
        }
    }
}