package com.SmartJobFinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet({"/api/applications", "/applications", "/applicationTracker"})
public class ApplicationTrackerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static class AppRecord {
        public int id;
        public int userId;
        public int companyId;
        public String companyName;
        public String jobTitle;
        public String status;
        public String appliedDate;
        public String officialUrl;
        public String notes;
        public String followUpDate;
        public String createdAt;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            String format = request.getParameter("format");
            if ("json".equalsIgnoreCase(format)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized. Please sign in to view your applications.\"}");
                return;
            }
            response.sendRedirect("login.html?error=" + java.net.URLEncoder.encode("Please sign in to view your Application Tracker.", "UTF-8"));
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String filterStatus = request.getParameter("status");

        List<AppRecord> records = new ArrayList<>();
        int totalSaved = 0;
        int totalApplied = 0;
        int totalInterview = 0;
        int totalOffers = 0;
        int totalRejected = 0;

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                // Fetch stats for all applications of this user
                String countSql = "SELECT status, COUNT(*) AS cnt FROM job_applications WHERE user_id = ? GROUP BY status";
                try (PreparedStatement psCount = conn.prepareStatement(countSql)) {
                    psCount.setInt(1, userId);
                    try (ResultSet rsCount = psCount.executeQuery()) {
                        while (rsCount.next()) {
                            String st = rsCount.getString("status");
                            int cnt = rsCount.getInt("cnt");
                            if ("Saved".equalsIgnoreCase(st)) totalSaved += cnt;
                            else if ("Applied".equalsIgnoreCase(st)) totalApplied += cnt;
                            else if ("Interview".equalsIgnoreCase(st) || "Shortlisted".equalsIgnoreCase(st)) totalInterview += cnt;
                            else if ("Offer".equalsIgnoreCase(st)) totalOffers += cnt;
                            else if ("Rejected".equalsIgnoreCase(st)) totalRejected += cnt;
                        }
                    }
                }

                // Query specific records
                StringBuilder query = new StringBuilder("SELECT * FROM job_applications WHERE user_id = ?");
                if (filterStatus != null && !filterStatus.trim().isEmpty() && !filterStatus.equalsIgnoreCase("all")) {
                    query.append(" AND LOWER(status) = LOWER(?)");
                }
                query.append(" ORDER BY id DESC");

                try (PreparedStatement ps = conn.prepareStatement(query.toString())) {
                    ps.setInt(1, userId);
                    if (filterStatus != null && !filterStatus.trim().isEmpty() && !filterStatus.equalsIgnoreCase("all")) {
                        ps.setString(2, filterStatus.trim());
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            AppRecord r = new AppRecord();
                            r.id = rs.getInt("id");
                            r.userId = rs.getInt("user_id");
                            r.companyId = rs.getInt("company_id");
                            r.companyName = rs.getString("company_name");
                            r.jobTitle = rs.getString("job_title");
                            r.status = rs.getString("status");
                            Date d = rs.getDate("applied_date");
                            r.appliedDate = d != null ? d.toString() : "";
                            r.officialUrl = rs.getString("official_url");
                            r.notes = rs.getString("notes");
                            Date f = rs.getDate("follow_up_date");
                            r.followUpDate = f != null ? f.toString() : "";
                            r.createdAt = rs.getString("created_at");
                            records.add(r);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ApplicationTrackerServlet] Query error: " + e.getMessage());
        }

        String format = request.getParameter("format");
        if ("json".equalsIgnoreCase(format)) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{");
            out.print("\"count\":" + records.size() + ",");
            out.print("\"stats\":{");
            out.print("\"saved\":" + totalSaved + ",");
            out.print("\"applied\":" + totalApplied + ",");
            out.print("\"interview\":" + totalInterview + ",");
            out.print("\"offers\":" + totalOffers + ",");
            out.print("\"rejected\":" + totalRejected);
            out.print("},");
            out.print("\"applications\":[");
            for (int i = 0; i < records.size(); i++) {
                AppRecord r = records.get(i);
                if (i > 0) out.print(",");
                out.print("{");
                out.print("\"id\":" + r.id + ",");
                out.print("\"companyId\":" + r.companyId + ",");
                out.print("\"companyName\":\"" + escapeJson(r.companyName) + "\",");
                out.print("\"jobTitle\":\"" + escapeJson(r.jobTitle) + "\",");
                out.print("\"status\":\"" + escapeJson(r.status) + "\",");
                out.print("\"appliedDate\":\"" + escapeJson(r.appliedDate) + "\",");
                out.print("\"officialUrl\":\"" + escapeJson(r.officialUrl) + "\",");
                out.print("\"notes\":\"" + escapeJson(r.notes) + "\",");
                out.print("\"followUpDate\":\"" + escapeJson(r.followUpDate) + "\"");
                out.print("}");
            }
            out.print("]");
            out.print("}");
            return;
        }

        // Redirect to HTML UI
        response.sendRedirect("application-tracker.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"Authentication required\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");
        if (action == null) action = "add";

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                out.write("{\"success\":false,\"error\":\"Database connection failed\"}");
                return;
            }

            if ("add".equalsIgnoreCase(action)) {
                String companyIdStr = request.getParameter("companyId");
                String companyName = request.getParameter("companyName");
                String jobTitle = request.getParameter("jobTitle");
                String status = request.getParameter("status");
                String officialUrl = request.getParameter("officialUrl");
                String notes = request.getParameter("notes");
                String followUpDateStr = request.getParameter("followUpDate");
                String appliedDateStr = request.getParameter("appliedDate");

                int companyId = 0;
                try {
                    if (companyIdStr != null) companyId = Integer.parseInt(companyIdStr.trim());
                } catch (Exception ignore) {}

                if (companyName == null || companyName.trim().isEmpty()) companyName = "Company";
                if (jobTitle == null || jobTitle.trim().isEmpty()) jobTitle = "Job Opening";
                if (status == null || status.trim().isEmpty()) status = "Applied";
                if (officialUrl == null) officialUrl = "#";
                if (notes == null) notes = "";

                Date appliedDate = (appliedDateStr != null && !appliedDateStr.trim().isEmpty())
                        ? Date.valueOf(appliedDateStr.trim())
                        : Date.valueOf(LocalDate.now());

                Date followUpDate = null;
                if (followUpDateStr != null && !followUpDateStr.trim().isEmpty()) {
                    try {
                        followUpDate = Date.valueOf(followUpDateStr.trim());
                    } catch (Exception ignore) {}
                }

                // Check if user already tracked this company & role
                String checkSql = "SELECT id FROM job_applications WHERE user_id = ? AND company_name = ? AND job_title = ?";
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setInt(1, userId);
                    psCheck.setString(2, companyName.trim());
                    psCheck.setString(3, jobTitle.trim());
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        if (rsCheck.next()) {
                            int existingId = rsCheck.getInt("id");
                            // Update existing record
                            String upSql = "UPDATE job_applications SET status = ?, notes = ?, follow_up_date = ?, official_url = ? WHERE id = ? AND user_id = ?";
                            try (PreparedStatement psUp = conn.prepareStatement(upSql)) {
                                psUp.setString(1, status.trim());
                                psUp.setString(2, notes.trim());
                                psUp.setDate(3, followUpDate);
                                psUp.setString(4, officialUrl.trim());
                                psUp.setInt(5, existingId);
                                psUp.setInt(6, userId);
                                psUp.executeUpdate();
                            }
                            out.write("{\"success\":true,\"message\":\"Application updated in your tracker!\",\"id\":" + existingId + "}");
                            return;
                        }
                    }
                }

                // Insert new application
                String insertSql = "INSERT INTO job_applications (user_id, company_id, company_name, job_title, status, applied_date, official_url, notes, follow_up_date) "
                                 + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psIns = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    psIns.setInt(1, userId);
                    psIns.setInt(2, companyId);
                    psIns.setString(3, companyName.trim());
                    psIns.setString(4, jobTitle.trim());
                    psIns.setString(5, status.trim());
                    psIns.setDate(6, appliedDate);
                    psIns.setString(7, officialUrl.trim());
                    psIns.setString(8, notes.trim());
                    psIns.setDate(9, followUpDate);
                    psIns.executeUpdate();

                    int newId = 0;
                    try (ResultSet rsKeys = psIns.getGeneratedKeys()) {
                        if (rsKeys.next()) newId = rsKeys.getInt(1);
                    }
                    out.write("{\"success\":true,\"message\":\"Application successfully recorded in your Tracker!\",\"id\":" + newId + "}");
                }

            } else if ("update".equalsIgnoreCase(action)) {
                String idStr = request.getParameter("id");
                String status = request.getParameter("status");
                String notes = request.getParameter("notes");
                String followUpDateStr = request.getParameter("followUpDate");

                if (idStr == null || idStr.trim().isEmpty()) {
                    out.write("{\"success\":false,\"error\":\"Record ID required\"}");
                    return;
                }

                int appId = Integer.parseInt(idStr.trim());
                Date followUpDate = null;
                if (followUpDateStr != null && !followUpDateStr.trim().isEmpty()) {
                    try {
                        followUpDate = Date.valueOf(followUpDateStr.trim());
                    } catch (Exception ignore) {}
                }

                String updateSql = "UPDATE job_applications SET status = ?, notes = ?, follow_up_date = ? WHERE id = ? AND user_id = ?";
                try (PreparedStatement psUp = conn.prepareStatement(updateSql)) {
                    psUp.setString(1, status != null ? status.trim() : "Applied");
                    psUp.setString(2, notes != null ? notes.trim() : "");
                    psUp.setDate(3, followUpDate);
                    psUp.setInt(4, appId);
                    psUp.setInt(5, userId);
                    int rows = psUp.executeUpdate();
                    if (rows > 0) {
                        out.write("{\"success\":true,\"message\":\"Application status updated successfully.\"}");
                    } else {
                        out.write("{\"success\":false,\"error\":\"Record not found or access denied.\"}");
                    }
                }

            } else if ("delete".equalsIgnoreCase(action)) {
                String idStr = request.getParameter("id");
                if (idStr == null || idStr.trim().isEmpty()) {
                    out.write("{\"success\":false,\"error\":\"Record ID required\"}");
                    return;
                }
                int appId = Integer.parseInt(idStr.trim());

                String delSql = "DELETE FROM job_applications WHERE id = ? AND user_id = ?";
                try (PreparedStatement psDel = conn.prepareStatement(delSql)) {
                    psDel.setInt(1, appId);
                    psDel.setInt(2, userId);
                    int rows = psDel.executeUpdate();
                    if (rows > 0) {
                        out.write("{\"success\":true,\"message\":\"Application removed from tracker.\"}");
                    } else {
                        out.write("{\"success\":false,\"error\":\"Record not found or access denied.\"}");
                    }
                }
            } else {
                out.write("{\"success\":false,\"error\":\"Unknown action\"}");
            }

        } catch (Exception e) {
            out.write("{\"success\":false,\"error\":\"Error processing request: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
