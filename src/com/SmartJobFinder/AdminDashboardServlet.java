package com.SmartJobFinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet({"/adminDashboard", "/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Helper data structure for companies
    public static class CompanyItem {
        public int id;
        public String companyName;
        public String skills;
        public String role;
        public String salary;
        public String experience;
        public String description;
        public String logo;
        public String applyUrl;
        public String status;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session validation: Only logged-in admin can access dashboard
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            response.sendRedirect("admin-login.html?error=" +
                java.net.URLEncoder.encode("Please log in with administrator credentials to access the dashboard.", java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        String adminUsername = (String) session.getAttribute("adminUsername");
        if (adminUsername == null || adminUsername.trim().isEmpty()) {
            adminUsername = "Admin";
        }

        // Handle flash message and error parameters
        String message = request.getParameter("message");
        String error = request.getParameter("error");

        // Fetch Metrics & Company records from Database
        int totalCompanies = 0;
        int activeCompanies = 0;
        int inactiveCompanies = 0;
        int totalUsers = 0;
        List<CompanyItem> companies = new ArrayList<>();
        String dbError = null;

        try {
            Connection connection = DBConnection.getConnection();

            if (connection == null) {
                dbError = "Could not establish database connection. Please check MySQL service.";
            } else {
                // 1. Total Registered Users Count
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM users")) {
                    if (rs.next()) {
                        totalUsers = rs.getInt("total");
                    }
                } catch (Exception ignore) {}

                // 2. Fetch all companies
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM companies ORDER BY id DESC")) {
                    while (rs.next()) {
                        CompanyItem item = new CompanyItem();
                        item.id = rs.getInt("id");
                        item.companyName = rs.getString("company_name");
                        item.skills = rs.getString("skills");
                        item.role = rs.getString("role");
                        item.salary = rs.getString("salary");
                        item.experience = rs.getString("experience");
                        try {
                            item.description = rs.getString("description");
                        } catch (Exception ignore) {
                            item.description = "";
                        }
                        item.logo = rs.getString("logo");
                        if (item.logo == null || item.logo.trim().isEmpty()) {
                            item.logo = "images/default-company.svg";
                        } else {
                            item.logo = item.logo.trim();
                            if (!item.logo.startsWith("images/") && !item.logo.startsWith("image/") && !item.logo.startsWith("http://") && !item.logo.startsWith("https://")) {
                                item.logo = "images/" + item.logo;
                            }
                        }
                        item.applyUrl = rs.getString("apply_url");
                        if (item.applyUrl == null || item.applyUrl.trim().isEmpty()) {
                            item.applyUrl = "#";
                        }
                        try {
                            item.status = rs.getString("status");
                        } catch (Exception ignore) {
                            item.status = "ACTIVE";
                        }
                        if (item.status == null || item.status.trim().isEmpty()) {
                            item.status = "ACTIVE";
                        }

                        companies.add(item);
                        totalCompanies++;
                        if ("ACTIVE".equalsIgnoreCase(item.status)) {
                            activeCompanies++;
                        } else {
                            inactiveCompanies++;
                        }
                    }
                }

                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            dbError = "Error loading dashboard data: " + e.getMessage();
        }

        // Render HTML Output
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("    <meta charset='UTF-8'>");
        out.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("    <title>Admin Dashboard - Smart Job Finder</title>");
        out.println("    <link rel='stylesheet' href='css/style.css'>");
        out.println("    <style>");
        out.println("        .table-logo { width: 44px !important; height: 44px !important; min-width: 44px !important; max-width: 44px !important; max-height: 44px !important; object-fit: contain; border-radius: 8px; background: #ffffff; border: 1px solid #e2e8f0; padding: 4px; display: block; }");
        out.println("        .admin-table { width: 100%; border-collapse: collapse; text-align: left; font-size: 14px; }");
        out.println("        .admin-table th { padding: 14px 18px; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.6px; color: #475569; background: #f8fafc; border-bottom: 2px solid #e2e8f0; }");
        out.println("        .admin-table td { padding: 14px 18px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }");
        out.println("        .admin-table tr:hover { background-color: #f8fafc; }");
        out.println("        .company-table-cell { display: flex; align-items: center; gap: 12px; min-width: 170px; }");
        out.println("        .table-company-name { font-weight: 700; color: #0f172a; display: block; font-size: 15px; }");
        out.println("        .table-apply-link { font-size: 12px; color: #2563eb; text-decoration: none; font-weight: 600; }");
        out.println("        .badge-status { display: inline-flex; align-items: center; gap: 6px; padding: 4px 12px; border-radius: 9999px; font-size: 12px; font-weight: 700; }");
        out.println("        .badge-status-active { background: #ecfdf5; color: #047857; border: 1px solid #a7f3d0; }");
        out.println("        .badge-status-inactive { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; }");
        out.println("        .action-btn-group { display: flex; align-items: center; gap: 8px; flex-wrap: nowrap; }");
        out.println("        .btn-action { display: inline-flex; align-items: center; justify-content: center; padding: 6px 12px; border-radius: 6px; font-size: 13px; font-weight: 600; text-decoration: none; border: 1px solid transparent; cursor: pointer; }");
        out.println("        .btn-toggle-active { background: #ecfdf5; color: #047857; border-color: #a7f3d0; }");
        out.println("        .btn-toggle-inactive { background: #f8fafc; color: #475569; border-color: #cbd5e1; }");
        out.println("        .btn-edit { background: #eff6ff; color: #1d4ed8; border-color: #bfdbfe; }");
        out.println("        .btn-delete { background: #fef2f2; color: #b91c1c; border-color: #fecaca; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body class='admin-body'>");

        // Header Navigation
        out.println("    <header class='app-header admin-header'>");
        out.println("        <div class='header-content'>");
        out.println("            <div class='logo-title'>");
        out.println("                <span class='logo-icon admin-icon'>🛡️</span>");
        out.println("                <div>");
        out.println("                    <h1>Smart Job Finder</h1>");
        out.println("                    <span class='badge-admin-tag'>Administrator Console</span>");
        out.println("                </div>");
        out.println("            </div>");
        out.println("            <div class='header-actions'>");
        out.println("                <div class='admin-user-pill'>");
        out.println("                    <span class='admin-avatar'>👤</span>");
        out.println("                    <span class='admin-name'>" + escapeHtml(adminUsername) + "</span>");
        out.println("                </div>");
        out.println("                <a href='job-search.html' target='_blank' class='btn btn-secondary btn-sm'>👁️ View User Portal</a>");
        out.println("                <a href='adminLogout' class='btn btn-outline btn-sm btn-logout'>Sign Out</a>");
        out.println("            </div>");
        out.println("        </div>");
        out.println("    </header>");

        out.println("    <main class='main-container admin-main'>");

        // Notification Alerts
        if (message != null && !message.trim().isEmpty()) {
            out.println("        <div class='alert-banner success' style='display: block; margin-bottom: 24px;'>");
            out.println("            ✅ " + escapeHtml(message));
            out.println("        </div>");
        }
        if (error != null && !error.trim().isEmpty()) {
            out.println("        <div class='alert-banner error' style='display: block; margin-bottom: 24px;'>");
            out.println("            ⚠️ " + escapeHtml(error));
            out.println("        </div>");
        }
        if (dbError != null) {
            out.println("        <div class='alert-banner error' style='display: block; margin-bottom: 24px;'>");
            out.println("            ❌ " + escapeHtml(dbError));
            out.println("        </div>");
        }

        // Top KPI Stats Cards
        out.println("        <section class='admin-stats-grid'>");
        out.println("            <div class='stat-card'>");
        out.println("                <div class='stat-icon stat-icon-blue'>🏢</div>");
        out.println("                <div class='stat-details'>");
        out.println("                    <span class='stat-label'>Total Companies</span>");
        out.println("                    <span class='stat-value'>" + totalCompanies + "</span>");
        out.println("                </div>");
        out.println("            </div>");

        out.println("            <div class='stat-card'>");
        out.println("                <div class='stat-icon stat-icon-green'>🟢</div>");
        out.println("                <div class='stat-details'>");
        out.println("                    <span class='stat-label'>Active Openings</span>");
        out.println("                    <span class='stat-value'>" + activeCompanies + "</span>");
        out.println("                    <span class='stat-sub'>Visible to Job Seekers</span>");
        out.println("                </div>");
        out.println("            </div>");

        out.println("            <div class='stat-card'>");
        out.println("                <div class='stat-icon stat-icon-amber'>⏸️</div>");
        out.println("                <div class='stat-details'>");
        out.println("                    <span class='stat-label'>Inactive Listings</span>");
        out.println("                    <span class='stat-value'>" + inactiveCompanies + "</span>");
        out.println("                    <span class='stat-sub'>Hidden from Job Seekers</span>");
        out.println("                </div>");
        out.println("            </div>");

        out.println("            <div class='stat-card'>");
        out.println("                <div class='stat-icon stat-icon-purple'>👥</div>");
        out.println("                <div class='stat-details'>");
        out.println("                    <span class='stat-label'>Registered Users</span>");
        out.println("                    <span class='stat-value'>" + totalUsers + "</span>");
        out.println("                </div>");
        out.println("            </div>");
        out.println("        </section>");

        // Management Control Bar
        out.println("        <section class='admin-table-section'>");
        out.println("            <div class='table-header-bar'>");
        out.println("                <div class='table-title-group'>");
        out.println("                    <h2>Manage Companies & Job Openings</h2>");
        out.println("                    <p>Create, edit, toggle visibility, or delete company job listings in real-time.</p>");
        out.println("                </div>");
        out.println("                <div class='table-actions-group'>");
        out.println("                    <a href='add-company.html' class='btn btn-primary' style='display: inline-flex; align-items: center; gap: 8px; text-decoration: none;'>");
        out.println("                        ➕ Add New Company");
        out.println("                    </a>");
        out.println("                </div>");
        out.println("            </div>");

        // Filter and Search Toolbar
        out.println("            <div class='admin-filter-toolbar'>");
        out.println("                <div class='search-box'>");
        out.println("                    <span class='search-icon'>🔍</span>");
        out.println("                    <input type='text' id='adminSearchInput' placeholder='Search by company, role, or skill...' class='form-control form-control-sm' onkeyup='filterCompaniesTable()'>");
        out.println("                </div>");
        out.println("                <div class='status-filter-pills'>");
        out.println("                    <button class='filter-pill active' data-filter='ALL' onclick='setStatusFilter(\"ALL\", this)'>All (" + totalCompanies + ")</button>");
        out.println("                    <button class='filter-pill' data-filter='ACTIVE' onclick='setStatusFilter(\"ACTIVE\", this)'>Active (" + activeCompanies + ")</button>");
        out.println("                    <button class='filter-pill' data-filter='INACTIVE' onclick='setStatusFilter(\"INACTIVE\", this)'>Inactive (" + inactiveCompanies + ")</button>");
        out.println("                </div>");
        out.println("            </div>");

        // Data Table
        out.println("            <div class='table-responsive'>");
        out.println("                <table class='admin-table' id='companiesTable'>");
        out.println("                    <thead>");
        out.println("                        <tr>");
        out.println("                            <th>ID</th>");
        out.println("                            <th>Company</th>");
        out.println("                            <th>Role</th>");
        out.println("                            <th>Required Skills</th>");
        out.println("                            <th>Experience & Salary</th>");
        out.println("                            <th>Status</th>");
        out.println("                            <th>Actions</th>");
        out.println("                        </tr>");
        out.println("                    </thead>");
        out.println("                    <tbody>");

        if (companies.isEmpty()) {
            out.println("                        <tr>");
            out.println("                            <td colspan='7' class='text-center empty-table-cell'>");
            out.println("                                <div class='empty-state'>");
            out.println("                                    <span>🏢</span>");
            out.println("                                    <p>No companies found in database.</p>");
            out.println("                                    <a href='add-company.html' class='btn btn-primary btn-sm'>Add First Company</a>");
            out.println("                                </div>");
            out.println("                            </td>");
            out.println("                        </tr>");
        } else {
            for (CompanyItem c : companies) {
                boolean isActive = "ACTIVE".equalsIgnoreCase(c.status);
                String statusClass = isActive ? "badge-status-active" : "badge-status-inactive";
                String statusLabel = isActive ? "ACTIVE" : "INACTIVE";

                // Format Salary
                String displaySalary = c.salary;
                try {
                    long salVal = Long.parseLong(c.salary.replaceAll("[^0-9]", ""));
                    displaySalary = String.format("₹%,d / yr", salVal);
                } catch (Exception ignore) {}

                out.println("                        <tr data-status='" + escapeHtml(c.status) + "' data-search='" + escapeHtml((c.companyName + " " + c.role + " " + c.skills).toLowerCase()) + "'>");
                out.println("                            <td class='td-id'>#" + c.id + "</td>");
                out.println("                            <td class='td-company'>");
                out.println("                                <div class='company-table-cell'>");
                out.println("                                    <img src='" + escapeHtml(c.logo) + "' alt='" + escapeHtml(c.companyName) + "' class='table-logo' onerror=\"this.onerror=null;this.src='images/default-company.svg';\">");
                out.println("                                    <div>");
                out.println("                                        <span class='table-company-name'>" + escapeHtml(c.companyName) + "</span>");
                if (c.applyUrl != null && !c.applyUrl.equals("#")) {
                    out.println("                                        <a href='" + escapeHtml(c.applyUrl) + "' target='_blank' class='table-apply-link'>Portal ↗</a>");
                }
                out.println("                                    </div>");
                out.println("                                </div>");
                out.println("                            </td>");
                out.println("                            <td class='td-role'><strong>" + escapeHtml(c.role) + "</strong></td>");
                out.println("                            <td class='td-skills'>");
                out.println("                                <div class='table-skills-wrapper'>");
                if (c.skills != null) {
                    String[] tags = c.skills.split("[,;]+");
                    int count = 0;
                    for (String t : tags) {
                        String clean = t.trim();
                        if (!clean.isEmpty()) {
                            if (count < 3) {
                                out.println("                                    <span class='table-skill-chip'>" + escapeHtml(clean) + "</span>");
                            }
                            count++;
                        }
                    }
                    if (count > 3) {
                        out.println("                                    <span class='table-skill-chip-more'>+" + (count - 3) + "</span>");
                    }
                }
                out.println("                                </div>");
                out.println("                            </td>");
                out.println("                            <td class='td-meta'>");
                out.println("                                <div>💼 " + escapeHtml(c.experience) + " yrs</div>");
                out.println("                                <div class='text-salary'>" + escapeHtml(displaySalary) + "</div>");
                out.println("                            </td>");
                out.println("                            <td class='td-status'>");
                out.println("                                <span class='badge-status " + statusClass + "'>" + statusLabel + "</span>");
                out.println("                            </td>");
                out.println("                            <td class='td-actions'>");
                out.println("                                <div class='action-btn-group'>");

                // Toggle Status Button
                if (isActive) {
                    out.println("                                    <a href='updateCompanyStatus?id=" + c.id + "&status=INACTIVE' class='btn-action btn-toggle-inactive' title='Deactivate (Hide from users)'>⏸️ Hide</a>");
                } else {
                    out.println("                                    <a href='updateCompanyStatus?id=" + c.id + "&status=ACTIVE' class='btn-action btn-toggle-active' title='Activate (Show to users)'>▶️ Show</a>");
                }

                // Edit Button -> opens dedicated Edit page
                out.println("                                    <a href='editCompany?id=" + c.id + "' class='btn-action btn-edit' title='Edit Company'>✏️ Edit</a>");

                // Delete Button -> direct confirmation and delete
                out.println("                                    <a href='deleteCompany?id=" + c.id + "' class='btn-action btn-delete' onclick='return confirm(\"Are you sure you want to permanently delete " + escapeJs(c.companyName) + " (ID #" + c.id + ")?\");' title='Delete Company'>🗑️ Delete</a>");

                out.println("                                </div>");
                out.println("                            </td>");
                out.println("                        </tr>");
            }
        }

        out.println("                    </tbody>");
        out.println("                </table>");
        out.println("            </div>");
        out.println("        </section>");
        out.println("    </main>");

        out.println("    <script src='js/script.js'></script>");
        out.println("    <script>");
        out.println("        let currentStatusFilter = 'ALL';");
        out.println("        function setStatusFilter(filter, btn) {");
        out.println("            currentStatusFilter = filter;");
        out.println("            document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));");
        out.println("            btn.classList.add('active');");
        out.println("            filterCompaniesTable();");
        out.println("        }");
        out.println("        function filterCompaniesTable() {");
        out.println("            const searchVal = document.getElementById('adminSearchInput').value.toLowerCase().trim();");
        out.println("            const rows = document.querySelectorAll('#companiesTable tbody tr');");
        out.println("            rows.forEach(row => {");
        out.println("                const rowStatus = row.getAttribute('data-status');");
        out.println("                const rowSearch = row.getAttribute('data-search') || '';");
        out.println("                if (!rowStatus) return;");
        out.println("                const matchesStatus = (currentStatusFilter === 'ALL' || rowStatus === currentStatusFilter);");
        out.println("                const matchesSearch = (!searchVal || rowSearch.includes(searchVal));");
        out.println("                if (matchesStatus && matchesSearch) { row.style.display = ''; } else { row.style.display = 'none'; }");
        out.println("            });");
        out.println("        }");
        out.println("    </script>");
        out.println("</body>");
        out.println("</html>");
    }

    private String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    private String escapeJs(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("'", "\\'")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}
