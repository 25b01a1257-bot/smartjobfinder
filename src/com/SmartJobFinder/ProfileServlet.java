package com.SmartJobFinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet({"/api/profile", "/profile", "/userProfile"})
public class ProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static class ProfileCompleteness {
        public int score = 0;
        public List<String> missingFields = new ArrayList<>();
        public List<String> suggestions = new ArrayList<>();
        public String statusLabel = "Incomplete";
    }

    public static ProfileCompleteness evaluate(String name, String email, String degree, String branch,
                                               Integer gradYear, String skills, String role, String exp,
                                               String salary, String resume, String bio) {
        ProfileCompleteness pc = new ProfileCompleteness();
        int score = 0;

        if (name != null && !name.trim().isEmpty()) score += 10;
        else {
            pc.missingFields.add("Full Name");
            pc.suggestions.add("Add your full name so recruiters can identify your application.");
        }

        if (email != null && !email.trim().isEmpty()) score += 10;
        else {
            pc.missingFields.add("Email Address");
            pc.suggestions.add("Provide a valid email address to receive application confirmations.");
        }

        if (degree != null && !degree.trim().isEmpty() && !degree.equalsIgnoreCase("Any")) score += 10;
        else {
            pc.missingFields.add("Degree / Qualification");
            pc.suggestions.add("Select your degree (e.g. B.Tech, M.Tech, MCA) to unlock qualification eligibility checking.");
        }

        if (branch != null && !branch.trim().isEmpty() && !branch.equalsIgnoreCase("All Branches")) score += 10;
        else {
            pc.missingFields.add("Branch / Major");
            pc.suggestions.add("Select your engineering or science branch (e.g. CSE, IT, ECE) to verify company branch criteria.");
        }

        if (gradYear != null && gradYear > 2000) score += 10;
        else {
            pc.missingFields.add("Graduation Year");
            pc.suggestions.add("Enter your expected graduation year to filter fresh graduate and internship openings.");
        }

        if (skills != null && !skills.trim().isEmpty() && skills.trim().length() > 3) {
            String[] tokens = skills.split("[,;]+");
            if (tokens.length >= 3) score += 15;
            else if (tokens.length >= 1) score += 8;
        } else {
            pc.missingFields.add("Technical Skills (At least 3)");
            pc.suggestions.add("Add at least 3 core technical skills (e.g. Java, Python, SQL) to match with top tech employers.");
        }

        if (role != null && !role.trim().isEmpty()) score += 10;
        else {
            pc.missingFields.add("Preferred Job Role");
            pc.suggestions.add("Specify your target job role (e.g. Software Engineer, Data Analyst) to rank relevant roles first.");
        }

        if (exp != null && !exp.trim().isEmpty()) score += 10;
        else {
            pc.missingFields.add("Experience Level");
            pc.suggestions.add("Specify your years of professional or internship experience (e.g. 0-1 years).");
        }

        if (salary != null && !salary.trim().isEmpty()) score += 5;
        else {
            pc.missingFields.add("Expected Compensation");
            pc.suggestions.add("Set your target annual salary expectations in ₹ LPA.");
        }

        if ((resume != null && !resume.trim().isEmpty()) || (bio != null && !bio.trim().isEmpty())) score += 10;
        else {
            pc.missingFields.add("Professional Bio or Resume Title");
            pc.suggestions.add("Add a concise summary or portfolio/resume link highlighting your project strengths.");
        }

        pc.score = Math.min(100, Math.max(0, score));
        if (pc.score >= 85) pc.statusLabel = "All-Star Profile ⭐";
        else if (pc.score >= 60) pc.statusLabel = "Intermediate Profile 👍";
        else pc.statusLabel = "Needs Completion ⚠️";

        return pc;
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
                response.getWriter().write("{\"error\":\"Please sign in\"}");
                return;
            }
            response.sendRedirect("login.html?error=" + java.net.URLEncoder.encode("Please sign in to view your profile.", "UTF-8"));
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        String name = "";
        String email = "";
        String degree = "B.Tech / B.E.";
        String branch = "Computer Science & Engineering";
        int gradYear = 2025;
        String skills = "";
        String role = "Software Engineer";
        String exp = "0-1";
        String salary = "800000";
        String resume = "";
        String bio = "";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String sql = "SELECT * FROM users WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            name = rs.getString("name");
                            email = rs.getString("email");
                            if (rs.getString("degree") != null) degree = rs.getString("degree");
                            if (rs.getString("branch") != null) branch = rs.getString("branch");
                            gradYear = rs.getInt("graduation_year");
                            if (gradYear <= 0) gradYear = 2025;
                            if (rs.getString("skills") != null) skills = rs.getString("skills");
                            if (rs.getString("preferred_role") != null) role = rs.getString("preferred_role");
                            if (rs.getString("experience") != null) exp = rs.getString("experience");
                            if (rs.getString("expected_salary") != null) salary = rs.getString("expected_salary");
                            if (rs.getString("resume_name") != null) resume = rs.getString("resume_name");
                            if (rs.getString("bio") != null) bio = rs.getString("bio");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ProfileServlet] Fetch error: " + e.getMessage());
        }

        ProfileCompleteness pc = evaluate(name, email, degree, branch, gradYear, skills, role, exp, salary, resume, bio);

        String format = request.getParameter("format");
        if ("json".equalsIgnoreCase(format)) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{");
            out.print("\"userId\":" + userId + ",");
            out.print("\"name\":\"" + escapeJson(name) + "\",");
            out.print("\"email\":\"" + escapeJson(email) + "\",");
            out.print("\"degree\":\"" + escapeJson(degree) + "\",");
            out.print("\"branch\":\"" + escapeJson(branch) + "\",");
            out.print("\"graduationYear\":" + gradYear + ",");
            out.print("\"skills\":\"" + escapeJson(skills) + "\",");
            out.print("\"preferredRole\":\"" + escapeJson(role) + "\",");
            out.print("\"experience\":\"" + escapeJson(exp) + "\",");
            out.print("\"expectedSalary\":\"" + escapeJson(salary) + "\",");
            out.print("\"resumeName\":\"" + escapeJson(resume) + "\",");
            out.print("\"bio\":\"" + escapeJson(bio) + "\",");
            out.print("\"completeness\":{");
            out.print("\"score\":" + pc.score + ",");
            out.print("\"statusLabel\":\"" + escapeJson(pc.statusLabel) + "\",");
            out.print("\"missingFields\":[");
            for (int i = 0; i < pc.missingFields.size(); i++) {
                if (i > 0) out.print(",");
                out.print("\"" + escapeJson(pc.missingFields.get(i)) + "\"");
            }
            out.print("],");
            out.print("\"suggestions\":[");
            for (int i = 0; i < pc.suggestions.size(); i++) {
                if (i > 0) out.print(",");
                out.print("\"" + escapeJson(pc.suggestions.get(i)) + "\"");
            }
            out.print("]");
            out.print("}");
            out.print("}");
            return;
        }

        response.sendRedirect("profile.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"Please sign in\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        String name = request.getParameter("name");
        String degree = request.getParameter("degree");
        String branch = request.getParameter("branch");
        String gradYearStr = request.getParameter("graduationYear");
        String skills = request.getParameter("skills");
        String preferredRole = request.getParameter("preferredRole");
        String experience = request.getParameter("experience");
        String expectedSalary = request.getParameter("expectedSalary");
        String resumeName = request.getParameter("resumeName");
        String bio = request.getParameter("bio");

        int gradYear = 2025;
        try {
            if (gradYearStr != null) gradYear = Integer.parseInt(gradYearStr.replaceAll("[^0-9]", ""));
        } catch (Exception ignore) {}

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                out.write("{\"success\":false,\"error\":\"Database connection failed\"}");
                return;
            }

            String sql = "UPDATE users SET name = ?, degree = ?, branch = ?, graduation_year = ?, skills = ?, preferred_role = ?, experience = ?, expected_salary = ?, resume_name = ?, bio = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name != null ? name.trim() : "");
                ps.setString(2, degree != null ? degree.trim() : "B.Tech / B.E.");
                ps.setString(3, branch != null ? branch.trim() : "Computer Science & Engineering");
                ps.setInt(4, gradYear);
                ps.setString(5, skills != null ? skills.trim() : "");
                ps.setString(6, preferredRole != null ? preferredRole.trim() : "Software Engineer");
                ps.setString(7, experience != null ? experience.trim() : "0-1");
                ps.setString(8, expectedSalary != null ? expectedSalary.trim() : "800000");
                ps.setString(9, resumeName != null ? resumeName.trim() : "");
                ps.setString(10, bio != null ? bio.trim() : "");
                ps.setInt(11, userId);

                int rows = ps.executeUpdate();

                // Update session attributes
                if (name != null) session.setAttribute("userName", name.trim());
                if (degree != null) session.setAttribute("userDegree", degree.trim());
                if (branch != null) session.setAttribute("userBranch", branch.trim());
                if (skills != null) session.setAttribute("userSkills", skills.trim());
                if (preferredRole != null) session.setAttribute("userRole", preferredRole.trim());

                ProfileCompleteness pc = evaluate(name, (String) session.getAttribute("userEmail"), degree, branch, gradYear, skills, preferredRole, experience, expectedSalary, resumeName, bio);

                out.write("{\"success\":true,\"message\":\"Profile updated successfully!\",\"score\":" + pc.score + ",\"statusLabel\":\"" + escapeJson(pc.statusLabel) + "\"}");
            }
        } catch (Exception e) {
            out.write("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
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
