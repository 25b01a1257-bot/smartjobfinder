package com.SmartJobFinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet({"/api/careerAssistant", "/careerAssistant", "/api/career-assistant", "/career-assistant"})
public class CareerAssistantServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("career-assistant.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        String userName = "Candidate";
        String userDegree = "B.Tech / B.E.";
        String userBranch = "Computer Science & Engineering";
        String userSkills = "Java, Python, SQL";
        String userRole = "Software Engineer";

        if (session != null) {
            if (session.getAttribute("userName") != null) userName = (String) session.getAttribute("userName");
            if (session.getAttribute("userDegree") != null) userDegree = (String) session.getAttribute("userDegree");
            if (session.getAttribute("userBranch") != null) userBranch = (String) session.getAttribute("userBranch");
            if (session.getAttribute("userSkills") != null) userSkills = (String) session.getAttribute("userSkills");
            if (session.getAttribute("userRole") != null) userRole = (String) session.getAttribute("userRole");
        }

        String message = request.getParameter("message");
        String category = request.getParameter("category"); // eligibility, skillgap, roadmap, resume, checklist, interview, career
        String companyParam = request.getParameter("company");

        if (message == null) message = "";
        if (category == null) category = "general";
        if (companyParam == null) companyParam = "";

        // Check if query is about a specific company
        String companyDetails = "";
        if (!companyParam.trim().isEmpty()) {
            try (Connection conn = DBConnection.getConnection()) {
                if (conn != null) {
                    String sql = "SELECT * FROM companies WHERE LOWER(company_name) LIKE ? LIMIT 1";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, "%" + companyParam.trim().toLowerCase() + "%");
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                companyDetails = "Target Company: " + rs.getString("company_name")
                                        + "\nRole: " + rs.getString("role")
                                        + "\nRequired Skills: " + rs.getString("skills")
                                        + "\nRequired Degree: " + rs.getString("required_degree")
                                        + "\nEligible Branches: " + rs.getString("eligible_branches")
                                        + "\nLocation: " + rs.getString("location");
                            }
                        }
                    }
                }
            } catch (Exception ignore) {}
        }

        String geminiApiKey = System.getenv("GEMINI_API_KEY");
        String reply = null;

        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty()) {
            try {
                reply = queryGeminiAgent(geminiApiKey.trim(), message, category, userName, userDegree, userBranch, userSkills, userRole, companyDetails);
            } catch (Exception e) {
                System.err.println("[CareerAssistant] Gemini query notice: " + e.getMessage() + ". Using Heuristic Agent.");
            }
        }

        if (reply == null || reply.trim().isEmpty()) {
            reply = generateHeuristicGuidance(message, category, userName, userDegree, userBranch, userSkills, userRole, companyDetails);
        }

        // Enforce Agentic Safety Guardrail: Never claim external submission
        if (reply.toLowerCase().contains("submitted your application") || reply.toLowerCase().contains("i have applied for you")) {
            reply = reply.replace("submitted your application", "provided the official link so you can complete your external application")
                         .replace("i have applied for you", "I have guided you to the verified company portal");
        }

        out.print("{");
        out.print("\"success\":true,");
        out.print("\"reply\":\"" + escapeJson(reply) + "\",");
        out.print("\"userContext\":{");
        out.print("\"name\":\"" + escapeJson(userName) + "\",");
        out.print("\"degree\":\"" + escapeJson(userDegree) + "\",");
        out.print("\"branch\":\"" + escapeJson(userBranch) + "\",");
        out.print("\"skills\":\"" + escapeJson(userSkills) + "\",");
        out.print("\"role\":\"" + escapeJson(userRole) + "\"");
        out.print("}");
        out.print("}");
    }

    private static String generateHeuristicGuidance(String message, String category, String name, String degree, String branch, String skills, String role, String companyDetails) {
        String msgLower = message.toLowerCase();

        if (category.equalsIgnoreCase("eligibility") || msgLower.contains("eligible") || msgLower.contains("branch") || msgLower.contains("degree")) {
            return "### 🏛️ Eligibility Verification Guidance for " + name + "\n\n"
                    + "**Your Profile:** " + degree + " in " + branch + "\n\n"
                    + "**Eligibility Rules on Smart Job Finder:**\n"
                    + "1. **Strict Degree Requirement:** Roles specifying Bachelor's or B.Tech require degree completion or current enrollment. (Diploma candidates are restricted from senior engineering listings unless explicitly accepted).\n"
                    + "2. **Branch Compatibility:** If you are in **" + branch + "**:\n"
                    + "   - Major tech enterprises like *Google, Microsoft, and Amazon* accept **Computer Science, IT, and Electronics (ECE)**.\n"
                    + "   - IT Services leaders like *TCS, Infosys, Wipro, and Accenture* accept **All Engineering Branches** (including Mechanical, Civil, and Electrical)!\n\n"
                    + (companyDetails.isEmpty() ? "" : "**Target Company Specifics:**\n" + companyDetails + "\n\n")
                    + "💡 **Next Step:** Toggle the *'Only Show Eligible Jobs'* filter on your search page to instantly see verified openings tailored to your branch!";
        }

        if (category.equalsIgnoreCase("roadmap") || msgLower.contains("learn") || msgLower.contains("roadmap") || msgLower.contains("skill gap")) {
            return "### 🚀 Personalized 4-Week Skill Bridge Roadmap\n\n"
                    + "**Target Career Role:** " + role + "\n"
                    + "**Your Active Strengths:** " + skills + "\n\n"
                    + "**Week 1: Core Architecture Foundations**\n"
                    + "- Deep dive into System Design basics (Client-Server, RESTful APIs, HTTP status codes).\n"
                    + "- *Free Resource:* FreeCodeCamp & MDN Web Docs.\n\n"
                    + "**Week 2: Enterprise Backend & Persistence**\n"
                    + "- Spring Boot or FastAPI: Build a CRUD API with relational database persistence (MySQL / PostgreSQL).\n"
                    + "- *Free Resource:* Spring.io Quickstart Guide & Baeldung.\n\n"
                    + "**Week 3: Containerization & Cloud Fundamentals**\n"
                    + "- Write Dockerfiles, build multi-stage images, and learn container networking.\n"
                    + "- *Free Resource:* Docker 101 Tutorial on Docker.com.\n\n"
                    + "**Week 4: Deployment & Portfolio Integration**\n"
                    + "- Deploy your API live on Railway or Render with automated GitHub Actions CI/CD.\n"
                    + "- Practice technical questions using the *AI Interview Prep* studio!";
        }

        if (category.equalsIgnoreCase("resume") || msgLower.contains("resume") || msgLower.contains("cv") || msgLower.contains("ats")) {
            return "### 📄 ATS-Friendly Resume Optimization Tips\n\n"
                    + "1. **Quantify Accomplishments (XYZ Formula):** Instead of *'Built a web app'*, write *'Engineered a full-stack job portal using Java Servlet & MySQL, reducing match latency by 45% for 1,000+ candidates.'*\n"
                    + "2. **Target Technical Keywords:** Align your skills section with target job keywords: `" + skills + "`.\n"
                    + "3. **Single-Column Standard Layout:** Avoid multi-column graphical templates which confuse ATS parsers.\n"
                    + "4. **Include Live Links:** Always include direct links to your GitHub profile and live deployed applications.\n"
                    + "5. **Education Section:** Clearly state your **" + degree + "** in **" + branch + "** with your graduation year!";
        }

        if (category.equalsIgnoreCase("checklist") || msgLower.contains("apply") || msgLower.contains("checklist")) {
            return "### 📋 Complete Job Application Checklist\n\n"
                    + "Before submitting your application to any company:\n"
                    + "✅ **1. Check Qualification & Branch:** Confirm your degree (" + degree + ") and branch (" + branch + ") match company requirements.\n"
                    + "✅ **2. Review Skill Match Score:** Verify your core skills align with required technologies.\n"
                    + "✅ **3. Tailor Your Resume:** Highlight the specific tech stack requested in the job description.\n"
                    + "✅ **4. Visit Official Careers Portal:** Click *Apply Now* to redirect safely to the company's verified application page.\n"
                    + "✅ **5. Complete External Application:** Fill in the company's portal fields directly.\n"
                    + "✅ **6. Return & Track:** Return to Smart Job Finder and click *Mark as Applied* to record your follow-up date and requisition notes!";
        }

        if (category.equalsIgnoreCase("interview") || msgLower.contains("interview") || msgLower.contains("questions")) {
            return "### 🎯 AI Interview Preparation Strategy (STAR Method)\n\n"
                    + "Structure all behavioral and situational responses using **STAR**:\n"
                    + "- **S (Situation):** Set the context and challenge you faced.\n"
                    + "- **T (Task):** Explain your specific responsibility.\n"
                    + "- **A (Action):** Detail the technical steps you took (e.g. debugging, architecture choices).\n"
                    + "- **R (Result):** Conclude with measurable outcomes.\n\n"
                    + "💡 **Ready to practice?** Launch the *AI Interview Preparation* module from the navigation bar to practice role-specific technical and HR questions!";
        }

        // General career response
        return "### ⚡ Career Assistant Guidance for " + name + "\n\n"
                + "Hello " + name + "! As your dedicated Career Assistant, I am here to guide you step-by-step toward landing your target role as **" + role + "**.\n\n"
                + "**What would you like to explore next?**\n"
                + "- 🏛️ **Check Eligibility:** See which companies match your " + branch + " background.\n"
                + "- 💡 **Bridge Skill Gaps:** Get custom learning recommendations for in-demand technologies.\n"
                + "- 📄 **Optimize Resume:** Actionable guidelines to pass corporate ATS screeners.\n"
                + "- 📋 **Application Checklist:** Step-by-step guidance before applying externally.\n"
                + "- 🎯 **Mock Interview Prep:** Practice technical questions tailored to your skills.\n\n"
                + "Ask me any career question or choose a topic above to begin!";
    }

    private static String queryGeminiAgent(String apiKey, String message, String category, String name, String degree, String branch, String skills, String role, String company) {
        try {
            String prompt = "You are the Antigravity Career Assistant in Smart Job Finder. "
                    + "Candidate Name: " + name + ", Degree: " + degree + ", Branch: " + branch + ", Skills: " + skills + ", Target Role: " + role + ". "
                    + (company.isEmpty() ? "" : "Company context:\n" + company + "\n")
                    + "User question (" + category + "): " + message + "\n"
                    + "Strict instructions:\n"
                    + "- Do NOT claim that you or Smart Job Finder submitted an external job application.\n"
                    + "- Remind the user to complete applications on the official company careers portal.\n"
                    + "- Provide structured, encouraging, highly professional markdown with bullet points and realistic tech resources.\n"
                    + "- Beginner-friendly explanations.";

            String escapedPrompt = escapeJson(prompt);
            String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapedPrompt + "\"}]}],\"generationConfig\":{\"temperature\":0.4,\"maxOutputTokens\":800}}";

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6)).build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return extractTextFromGemini(resp.body());
            }
        } catch (Exception ignore) {}
        return null;
    }

    private static String extractTextFromGemini(String json) {
        int idx = json.indexOf("\"text\":");
        if (idx == -1) return null;
        int start = json.indexOf("\"", idx + 7);
        if (start == -1) return null;
        start++;
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                if (c == 'n') sb.append("\n");
                else if (c == 'r') sb.append("\r");
                else if (c == 't') sb.append("\t");
                else sb.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
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
