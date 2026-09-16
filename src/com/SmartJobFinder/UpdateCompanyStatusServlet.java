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

@WebServlet({"/updateCompanyStatus", "/admin/updateCompanyStatus"})
public class UpdateCompanyStatusServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processStatusUpdate(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processStatusUpdate(request, response);
    }

    private void processStatusUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Session validation: Only logged-in admin can change company status
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            String err = URLEncoder.encode("Access denied. Please log in as administrator.", StandardCharsets.UTF_8);
            response.sendRedirect("admin-login.html?error=" + err);
            return;
        }

        String idStr = request.getParameter("id");
        String targetStatus = request.getParameter("status");

        if (idStr == null || idStr.trim().isEmpty()) {
            String err = URLEncoder.encode("Company ID is required.", StandardCharsets.UTF_8);
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

            // Find current status and company name
            String currentStatus = "ACTIVE";
            String compName = "Company #" + id;
            PreparedStatement selectStmt = connection.prepareStatement("SELECT company_name, status FROM companies WHERE id = ?");
            selectStmt.setInt(1, id);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                compName = rs.getString("company_name");
                String st = rs.getString("status");
                if (st != null && !st.trim().isEmpty()) {
                    currentStatus = st.trim();
                }
            } else {
                rs.close();
                selectStmt.close();
                connection.close();
                String err = URLEncoder.encode("Company not found.", StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?error=" + err);
                return;
            }
            rs.close();
            selectStmt.close();

            // Determine new status
            String newStatus;
            if (targetStatus != null && !targetStatus.trim().isEmpty()) {
                newStatus = targetStatus.equalsIgnoreCase("INACTIVE") ? "INACTIVE" : "ACTIVE";
            } else {
                // Toggle status
                newStatus = currentStatus.equalsIgnoreCase("ACTIVE") ? "INACTIVE" : "ACTIVE";
            }

            PreparedStatement updateStmt = connection.prepareStatement("UPDATE companies SET status = ? WHERE id = ?");
            updateStmt.setString(1, newStatus);
            updateStmt.setInt(2, id);
            int rowsUpdated = updateStmt.executeUpdate();
            updateStmt.close();
            connection.close();

            if (rowsUpdated > 0) {
                String statusLabel = newStatus.equals("ACTIVE") ? "activated (visible to users)" : "deactivated (hidden from users)";
                String msg = URLEncoder.encode("'" + compName + "' has been " + statusLabel + ".", StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?message=" + msg);
            } else {
                String err = URLEncoder.encode("Unable to update status for " + compName, StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?error=" + err);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String err = URLEncoder.encode("Status update error: " + e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("adminDashboard?error=" + err);
        }
    }
}
