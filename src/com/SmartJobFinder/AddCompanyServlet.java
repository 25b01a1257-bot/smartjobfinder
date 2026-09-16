package com.SmartJobFinder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet({"/addCompany", "/admin/addCompany"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 20)
public class AddCompanyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("adminDashboard");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session validation: Only logged-in admin can add companies
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            String err = URLEncoder.encode("Access denied. Please log in as administrator.", StandardCharsets.UTF_8);
            response.sendRedirect("admin-login.html?error=" + err);
            return;
        }

        String companyName = request.getParameter("company_name");
        String role = request.getParameter("role");
        String skills = request.getParameter("skills");
        String experience = request.getParameter("experience");
        String salary = request.getParameter("salary");
        String description = request.getParameter("description");
        String logo = request.getParameter("logo");
        String applyUrl = request.getParameter("apply_url");
        String status = request.getParameter("status");

        if (companyName == null || companyName.trim().isEmpty() ||
            role == null || role.trim().isEmpty() ||
            skills == null || skills.trim().isEmpty() ||
            salary == null || salary.trim().isEmpty()) {

            String err = URLEncoder.encode("Company Name, Job Role, Skills, and Salary are required.", StandardCharsets.UTF_8);
            response.sendRedirect("adminDashboard?error=" + err);
            return;
        }

        if (experience == null || experience.trim().isEmpty()) {
            experience = "0-2";
        }
        if (description == null) {
            description = "";
        }

        // Handle uploaded file or resolve logo
        Part filePart = null;
        try {
            filePart = request.getPart("logo_file");
        } catch (Exception ignore) {}

        String finalLogo = LogoHelper.resolveLogo(filePart, logo, companyName.trim(), getServletContext());

        if (applyUrl == null || applyUrl.trim().isEmpty()) {
            applyUrl = "#";
        }
        if (status == null || (!status.equalsIgnoreCase("ACTIVE") && !status.equalsIgnoreCase("INACTIVE"))) {
            status = "ACTIVE";
        } else {
            status = status.toUpperCase().trim();
        }

        try {
            Connection connection = DBConnection.getConnection();
            if (connection == null) {
                String err = URLEncoder.encode("Database connection failed. Please ensure MySQL is running.", StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?error=" + err);
                return;
            }

            String sql = "INSERT INTO companies (company_name, skills, role, salary, experience, description, logo, apply_url, status) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, companyName.trim());
            stmt.setString(2, skills.trim());
            stmt.setString(3, role.trim());
            stmt.setString(4, salary.trim());
            stmt.setString(5, experience.trim());
            stmt.setString(6, description.trim());
            stmt.setString(7, finalLogo);
            stmt.setString(8, applyUrl.trim());
            stmt.setString(9, status);

            int result = stmt.executeUpdate();
            stmt.close();
            connection.close();

            if (result > 0) {
                String msg = URLEncoder.encode("Company '" + companyName.trim() + "' added successfully!", StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?message=" + msg);
            } else {
                String err = URLEncoder.encode("Failed to add company. Please try again.", StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?error=" + err);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String err = URLEncoder.encode("Error adding company: " + e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("adminDashboard?error=" + err);
        }
    }
}
