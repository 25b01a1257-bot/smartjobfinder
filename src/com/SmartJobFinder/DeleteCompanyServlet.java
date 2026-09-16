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

@WebServlet({"/deleteCompany", "/admin/deleteCompany"})
public class DeleteCompanyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processDelete(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processDelete(request, response);
    }

    private void processDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Session validation: Only logged-in admin can delete companies
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            String err = URLEncoder.encode("Access denied. Please log in as administrator.", StandardCharsets.UTF_8);
            response.sendRedirect("admin-login.html?error=" + err);
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            String err = URLEncoder.encode("Company ID is required for deletion.", StandardCharsets.UTF_8);
            response.sendRedirect("adminDashboard?error=" + err);
            return;
        }

        try {
            int id = Integer.parseInt(idStr.trim());
            Connection connection = DBConnection.getConnection();

            if (connection == null) {
                String err = URLEncoder.encode("Database connection failed. Please ensure MySQL is running.", StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?error=" + err);
                return;
            }

            // Get company name first for friendly message
            String compName = "Company #" + id;
            PreparedStatement nameStmt = connection.prepareStatement("SELECT company_name FROM companies WHERE id = ?");
            nameStmt.setInt(1, id);
            ResultSet rs = nameStmt.executeQuery();
            if (rs.next()) {
                compName = rs.getString("company_name");
            }
            rs.close();
            nameStmt.close();

            // Delete company
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM companies WHERE id = ?");
            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            stmt.close();
            connection.close();

            if (rowsDeleted > 0) {
                String msg = URLEncoder.encode("'" + compName + "' has been permanently removed.", StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?message=" + msg);
            } else {
                String err = URLEncoder.encode("Company with ID " + id + " was not found.", StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?error=" + err);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String err = URLEncoder.encode("Error deleting company: " + e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("adminDashboard?error=" + err);
        }
    }
}
