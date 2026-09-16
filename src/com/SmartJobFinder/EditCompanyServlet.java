package com.SmartJobFinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet({"/editCompany", "/admin/editCompany"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 20)
public class EditCompanyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            String err = URLEncoder.encode("Please log in as administrator to edit companies.", StandardCharsets.UTF_8);
            response.sendRedirect("admin-login.html?error=" + err);
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect("adminDashboard");
            return;
        }

        String format = request.getParameter("format");

        try {
            int id = Integer.parseInt(idStr.trim());
            Connection connection = DBConnection.getConnection();

            if (connection == null) {
                if ("json".equalsIgnoreCase(format)) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Database connection error\"}");
                } else {
                    String err = URLEncoder.encode("Database connection error.", StandardCharsets.UTF_8);
                    response.sendRedirect("adminDashboard?error=" + err);
                }
                return;
            }

            String sql = "SELECT * FROM companies WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String companyName = rs.getString("company_name");
                String skills = rs.getString("skills");
                String role = rs.getString("role");
                String salary = rs.getString("salary");
                String experience = rs.getString("experience");
                String description = "";
                try {
                    description = rs.getString("description");
                } catch (Exception ignore) {}
                if (description == null) description = "";
                String logo = rs.getString("logo");
                if (logo == null || logo.trim().isEmpty()) logo = "images/default-company.svg";
                String applyUrl = rs.getString("apply_url");
                if (applyUrl == null || applyUrl.trim().isEmpty()) applyUrl = "#";
                String status = "ACTIVE";
                try {
                    String st = rs.getString("status");
                    if (st != null && !st.trim().isEmpty()) status = st.trim().toUpperCase();
                } catch (Exception ignore) {}

                if ("json".equalsIgnoreCase(format)) {
                    response.setContentType("application/json;charset=UTF-8");
                    PrintWriter out = response.getWriter();
                    out.print("{");
                    out.print("\"id\":" + id + ",");
                    out.print("\"company_name\":\"" + escapeJson(companyName) + "\",");
                    out.print("\"skills\":\"" + escapeJson(skills) + "\",");
                    out.print("\"role\":\"" + escapeJson(role) + "\",");
                    out.print("\"salary\":\"" + escapeJson(salary) + "\",");
                    out.print("\"experience\":\"" + escapeJson(experience) + "\",");
                    out.print("\"description\":\"" + escapeJson(description) + "\",");
                    out.print("\"logo\":\"" + escapeJson(logo) + "\",");
                    out.print("\"apply_url\":\"" + escapeJson(applyUrl) + "\",");
                    out.print("\"status\":\"" + escapeJson(status) + "\"");
                    out.print("}");
                } else {
                    // Render dedicated Edit Company Page
                    response.setContentType("text/html;charset=UTF-8");
                    PrintWriter out = response.getWriter();

                    out.println("<!DOCTYPE html>");
                    out.println("<html lang='en'>");
                    out.println("<head>");
                    out.println("    <meta charset='UTF-8'>");
                    out.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                    out.println("    <title>Edit Company: " + escapeHtml(companyName) + " - Smart Job Finder Admin</title>");
                    out.println("    <link rel='stylesheet' href='css/style.css'>");
                    out.println("</head>");
                    out.println("<body class='admin-body'>");

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
                    out.println("                <a href='adminDashboard' class='btn btn-secondary btn-sm'>← Back to Dashboard</a>");
                    out.println("                <a href='adminLogout' class='btn btn-outline btn-sm btn-logout'>Sign Out</a>");
                    out.println("            </div>");
                    out.println("        </div>");
                    out.println("    </header>");

                    out.println("    <main class='main-container admin-main' style='max-width: 820px;'>");
                    out.println("        <div class='search-form-card' style='max-width: 100%; padding: 36px 40px; margin-top: 10px;'>");
                    out.println("            <div class='search-header' style='text-align: left; border-bottom: 1px solid var(--border-color); padding-bottom: 20px; margin-bottom: 28px;'>");
                    out.println("                <div style='display: flex; justify-content: space-between; align-items: center;'>");
                    out.println("                    <div>");
                    out.println("                        <h2 style='font-size: 26px; color: var(--text-main);'>✏️ Edit Company Details (#" + id + ")</h2>");
                    out.println("                        <p style='color: var(--text-muted); font-size: 14px; margin-top: 4px;'>Update company information, requirements, compensation, and visibility status.</p>");
                    out.println("                    </div>");
                    out.println("                </div>");
                    out.println("            </div>");

                    out.println("            <form action='editCompany' method='post' enctype='multipart/form-data'>");
                    out.println("                <input type='hidden' name='id' value='" + id + "'>");

                    out.println("                <div class='form-grid-2'>");
                    out.println("                    <div class='form-group'>");
                    out.println("                        <label for='company_name'>Company Name *</label>");
                    out.println("                        <input type='text' id='company_name' name='company_name' class='form-control' value='" + escapeHtml(companyName) + "' required autofocus oninput='autoDetectLogo(this.value)'>");
                    out.println("                    </div>");

                    out.println("                    <div class='form-group'>");
                    out.println("                        <label for='role'>Target Job Role *</label>");
                    out.println("                        <input type='text' id='role' name='role' class='form-control' value='" + escapeHtml(role) + "' required>");
                    out.println("                    </div>");
                    out.println("                </div>");

                    out.println("                <div class='form-group'>");
                    out.println("                    <label for='skillsInput'>Required Technical Skills (Comma-separated) *</label>");
                    out.println("                    <input type='text' id='skillsInput' name='skills' class='form-control' value='" + escapeHtml(skills) + "' required>");
                    out.println("                    <div class='form-hint'>Click below to quick-add tech stack:</div>");
                    out.println("                    <div class='chip-container'>");
                    out.println("                        <span class='quick-chip' data-skill='Java'>+ Java</span>");
                    out.println("                        <span class='quick-chip' data-skill='Spring Boot'>+ Spring Boot</span>");
                    out.println("                        <span class='quick-chip' data-skill='SQL'>+ SQL</span>");
                    out.println("                        <span class='quick-chip' data-skill='Python'>+ Python</span>");
                    out.println("                        <span class='quick-chip' data-skill='React'>+ React</span>");
                    out.println("                        <span class='quick-chip' data-skill='Cloud'>+ Cloud</span>");
                    out.println("                        <span class='quick-chip' data-skill='AWS'>+ AWS</span>");
                    out.println("                        <span class='quick-chip' data-skill='Microservices'>+ Microservices</span>");
                    out.println("                        <span class='quick-chip' data-skill='Docker'>+ Docker</span>");
                    out.println("                        <span class='quick-chip' data-skill='HTML'>+ HTML</span>");
                    out.println("                        <span class='quick-chip' data-skill='JavaScript'>+ JavaScript</span>");
                    out.println("                        <span class='quick-chip' data-skill='C++'>+ C++</span>");
                    out.println("                    </div>");
                    out.println("                </div>");

                    out.println("                <div class='form-grid-2'>");
                    out.println("                    <div class='form-group'>");
                    out.println("                        <label for='salary'>Annual Expected Salary (₹) *</label>");
                    out.println("                        <input type='number' id='salary' name='salary' class='form-control' min='100000' step='50000' value='" + escapeHtml(salary) + "' required>");
                    out.println("                    </div>");

                    out.println("                    <div class='form-group'>");
                    out.println("                        <label for='experience'>Experience Bracket (Years)</label>");
                    out.println("                        <input type='text' id='experience' name='experience' class='form-control' value='" + escapeHtml(experience) + "'>");
                    out.println("                    </div>");
                    out.println("                </div>");

                    out.println("                <div class='form-group' style='background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 18px; margin-bottom: 20px;'>");
                    out.println("                    <label style='font-weight: 700; color: #1e293b; margin-bottom: 12px; display: block;'>Company Logo Management</label>");
                    out.println("                    <div style='display: flex; gap: 20px; align-items: center; flex-wrap: wrap;'>");
                    out.println("                        <div style='text-align: center;'>");
                    out.println("                            <img id='logoPreview' src='" + escapeHtml(logo) + "' alt='Logo Preview' style='width: 64px; height: 64px; object-fit: contain; background: #ffffff; border: 1px solid #cbd5e1; border-radius: 10px; padding: 6px; display: block;' onerror=\"this.onerror=null;this.src='images/default-company.svg';\">");
                    out.println("                            <span style='font-size: 11px; color: #64748b; margin-top: 4px; display: block;'>Current Logo</span>");
                    out.println("                        </div>");
                    out.println("                        <div style='flex: 1; min-width: 280px;'>");
                    out.println("                            <label for='logoSelect' style='font-size: 12px; font-weight: 600; color: #475569;'>Choose Supported Company Preset:</label>");
                    out.println("                            <select id='logoSelect' class='form-control' style='margin-bottom: 10px;' onchange='onPresetSelected(this.value)'>");
                    out.print(LogoHelper.renderLogoSelectOptions(logo));
                    out.println("                            </select>");
                    out.println("                            <div style='display: flex; gap: 12px;'>");
                    out.println("                                <div style='flex: 1;'>");
                    out.println("                                    <label for='logoFile' style='font-size: 12px; font-weight: 600; color: #475569;'>Upload New File (.svg, .png, .jpg):</label>");
                    out.println("                                    <input type='file' id='logoFile' name='logo_file' accept='.svg,.png,.jpg,.jpeg,.webp' class='form-control' onchange='onFileUploaded(this)'>");
                    out.println("                                </div>");
                    out.println("                                <div style='flex: 1;'>");
                    out.println("                                    <label for='logo' style='font-size: 12px; font-weight: 600; color: #475569;'>Logo Path Reference:</label>");
                    out.println("                                    <input type='text' id='logo' name='logo' class='form-control' value='" + escapeHtml(logo) + "' oninput='updateLogoPreview(this.value)'>");
                    out.println("                                </div>");
                    out.println("                            </div>");
                    out.println("                        </div>");
                    out.println("                    </div>");
                    out.println("                </div>");

                    out.println("                <div class='form-grid-2'>");
                    out.println("                    <div class='form-group'>");
                    out.println("                        <label for='status'>Company Visibility Status *</label>");
                    out.println("                        <select id='status' name='status' class='form-control' style='font-weight: 600;'>");
                    if ("ACTIVE".equalsIgnoreCase(status)) {
                        out.println("                            <option value='ACTIVE' selected>🟢 Active (Visible to Job Seekers)</option>");
                        out.println("                            <option value='INACTIVE'>⏸️ Inactive (Hidden from Job Seekers)</option>");
                    } else {
                        out.println("                            <option value='ACTIVE'>🟢 Active (Visible to Job Seekers)</option>");
                        out.println("                            <option value='INACTIVE' selected>⏸️ Inactive (Hidden from Job Seekers)</option>");
                    }
                    out.println("                        </select>");
                    out.println("                    </div>");

                    out.println("                    <div class='form-group'>");
                    out.println("                        <label for='apply_url'>Application Portal / Career Link</label>");
                    out.println("                        <input type='text' id='apply_url' name='apply_url' class='form-control' value='" + escapeHtml(applyUrl) + "'>");
                    out.println("                    </div>");
                    out.println("                </div>");

                    out.println("                <div class='form-group'>");
                    out.println("                    <label for='description'>Job Description / Overview</label>");
                    out.println("                    <textarea id='description' name='description' class='form-control' rows='3'>" + escapeHtml(description) + "</textarea>");
                    out.println("                </div>");

                    out.println("                <div style='display: flex; gap: 14px; margin-top: 24px;'>");
                    out.println("                    <button type='submit' class='btn btn-primary btn-lg' style='flex: 2;'>");
                    out.println("                        💾 Save Changes & Update");
                    out.println("                    </button>");
                    out.println("                    <a href='adminDashboard' class='btn btn-secondary btn-lg' style='flex: 1; text-align: center;'>");
                    out.println("                        Cancel");
                    out.println("                    </a>");
                    out.println("                </div>");
                    out.println("            </form>");
                    out.println("        </div>");
                    out.println("    </main>");

                    out.println("    <script src='js/script.js'></script>");
                    out.println("    <script>");
                    out.println("        function updateLogoPreview(path) {");
                    out.println("            if (!path || !path.trim()) path = 'images/default-company.svg';");
                    out.println("            document.getElementById('logoPreview').src = path;");
                    out.println("        }");
                    out.println("        function onPresetSelected(val) {");
                    out.println("            document.getElementById('logo').value = val;");
                    out.println("            updateLogoPreview(val);");
                    out.println("        }");
                    out.println("        function onFileUploaded(input) {");
                    out.println("            if (input.files && input.files[0]) {");
                    out.println("                const file = input.files[0];");
                    out.println("                const reader = new FileReader();");
                    out.println("                reader.onload = function(e) {");
                    out.println("                    document.getElementById('logoPreview').src = e.target.result;");
                    out.println("                    document.getElementById('logo').value = 'images/' + file.name;");
                    out.println("                };");
                    out.println("                reader.readAsDataURL(file);");
                    out.println("            }");
                    out.println("        }");
                    out.println("        function autoDetectLogo(name) {");
                    out.println("            if (!name) return;");
                    out.println("            const slug = name.toLowerCase().replace(/[^a-z0-9]/g, '');");
                    out.println("            const sel = document.getElementById('logoSelect');");
                    out.println("            for (let i = 0; i < sel.options.length; i++) {");
                    out.println("                const optVal = sel.options[i].value.toLowerCase();");
                    out.println("                if (optVal.includes(slug) && slug.length >= 3) {");
                    out.println("                    sel.selectedIndex = i;");
                    out.println("                    onPresetSelected(sel.options[i].value);");
                    out.println("                    break;");
                    out.println("                }");
                    out.println("            }");
                    out.println("        }");
                    out.println("    </script>");
                    out.println("</body>");
                    out.println("</html>");
                }

                rs.close();
                stmt.close();
                connection.close();
            } else {
                rs.close();
                stmt.close();
                connection.close();
                if ("json".equalsIgnoreCase(format)) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Company not found\"}");
                } else {
                    String err = URLEncoder.encode("Company with ID " + id + " was not found.", StandardCharsets.UTF_8);
                    response.sendRedirect("adminDashboard?error=" + err);
                }
            }

        } catch (Exception e) {
            if ("json".equalsIgnoreCase(format)) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"" + escapeJson(e.getMessage()) + "\"}");
            } else {
                String err = URLEncoder.encode("Error loading company: " + e.getMessage(), StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?error=" + err);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session validation: Only logged-in admin can edit companies
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))) {
            String err = URLEncoder.encode("Access denied. Please log in as administrator.", StandardCharsets.UTF_8);
            response.sendRedirect("admin-login.html?error=" + err);
            return;
        }

        String idStr = request.getParameter("id");
        String companyName = request.getParameter("company_name");
        String role = request.getParameter("role");
        String skills = request.getParameter("skills");
        String experience = request.getParameter("experience");
        String salary = request.getParameter("salary");
        String description = request.getParameter("description");
        String logo = request.getParameter("logo");
        String applyUrl = request.getParameter("apply_url");
        String status = request.getParameter("status");

        if (idStr == null || idStr.trim().isEmpty() ||
            companyName == null || companyName.trim().isEmpty() ||
            role == null || role.trim().isEmpty() ||
            skills == null || skills.trim().isEmpty() ||
            salary == null || salary.trim().isEmpty()) {

            String err = URLEncoder.encode("Company ID, Name, Job Role, Skills, and Salary are required.", StandardCharsets.UTF_8);
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
            int id = Integer.parseInt(idStr.trim());
            Connection connection = DBConnection.getConnection();

            if (connection == null) {
                String err = URLEncoder.encode("Database connection failed. Please ensure MySQL is running.", StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?error=" + err);
                return;
            }

            String sql = "UPDATE companies SET company_name = ?, skills = ?, role = ?, salary = ?, "
                       + "experience = ?, description = ?, logo = ?, apply_url = ?, status = ? WHERE id = ?";
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
            stmt.setInt(10, id);

            int result = stmt.executeUpdate();
            stmt.close();
            connection.close();

            if (result > 0) {
                String msg = URLEncoder.encode("Company '" + companyName.trim() + "' updated successfully!", StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?message=" + msg);
            } else {
                String err = URLEncoder.encode("Company not found or no changes made.", StandardCharsets.UTF_8);
                response.sendRedirect("adminDashboard?error=" + err);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String err = URLEncoder.encode("Error updating company: " + e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("adminDashboard?error=" + err);
        }
    }

    private String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
