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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet({"/aiAnalysis", "/api/aiAnalysis"})
public class AISkillGapServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processAnalysis(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processAnalysis(request, response);
    }

    private void processAnalysis(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();

        String companyIdStr = request.getParameter("companyId");
        String companyNameParam = request.getParameter("companyName");
        String companyRoleParam = request.getParameter("companyRole");
        String companySkillsParam = request.getParameter("companySkills");
        String companyExpParam = request.getParameter("companyExp");
        String companyDescParam = request.getParameter("companyDesc");

        String userSkills = request.getParameter("userSkills");
        String targetRole = request.getParameter("targetRole");
        String userExp = request.getParameter("userExp");
        String expectedSalary = request.getParameter("salary");

        if (userSkills == null) userSkills = "";
        if (targetRole == null) targetRole = "";
        if (userExp == null) userExp = "1";
        if (expectedSalary == null) expectedSalary = "";

        String companyName = (companyNameParam != null) ? companyNameParam.trim() : "";
        String companyRole = (companyRoleParam != null) ? companyRoleParam.trim() : "";
        String companySkills = (companySkillsParam != null) ? companySkillsParam.trim() : "";
        String companyExp = (companyExpParam != null) ? companyExpParam.trim() : "0-2";
        String companyDesc = (companyDescParam != null) ? companyDescParam.trim() : "";

        // If companyId is passed and details are missing, fetch from database
        if ((companyName.isEmpty() || companySkills.isEmpty()) && companyIdStr != null && !companyIdStr.trim().isEmpty()) {
            try {
                int cid = Integer.parseInt(companyIdStr.trim());
                Connection conn = DBConnection.getConnection();
                if (conn != null) {
                    PreparedStatement ps = conn.prepareStatement("SELECT * FROM companies WHERE id = ?");
                    ps.setInt(1, cid);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        companyName = rs.getString("company_name");
                        companyRole = rs.getString("role");
                        companySkills = rs.getString("skills");
                        companyExp = rs.getString("experience");
                        try {
                            companyDesc = rs.getString("description");
                        } catch (Exception ignore) {}
                    }
                    rs.close();
                    ps.close();
                    conn.close();
                }
            } catch (Exception ignore) {}
        }

        if (companyName.isEmpty()) {
            companyName = "Tech Organization";
        }
        if (companyRole.isEmpty()) {
            companyRole = "Software Engineer";
        }

        // Perform Skill Matching & Heuristic Evaluation
        List<String> userSkillList = cleanTokens(userSkills);
        List<String> requiredSkillList = cleanTokens(companySkills);

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String req : requiredSkillList) {
            boolean matched = false;
            for (String usr : userSkillList) {
                if (isSkillMatch(req, usr)) {
                    matched = true;
                    break;
                }
            }
            if (matched) {
                matchedSkills.add(req);
            } else {
                missingSkills.add(req);
            }
        }

        // Calculate Match Score
        int score = calculateMatchScore(userSkillList, requiredSkillList, matchedSkills, targetRole, companyRole, userExp, companyExp);

        // Check if real Gemini AI API key is configured
        String geminiApiKey = System.getenv("GEMINI_API_KEY");
        boolean hasGemini = (geminiApiKey != null && !geminiApiKey.trim().isEmpty());

        String status = "heuristic";
        String engineTitle = "Smart Heuristic Career Engine (Rule-based Analysis)";
        String engineNotice = "Deterministic analysis based on skill graph & role ontology. (Configure GEMINI_API_KEY for real-time generative AI).";
        String explanation = null;
        List<String> learningSuggestions = null;
        String roleFit = evaluateRoleFit(score, userExp, companyExp);

        if (hasGemini) {
            try {
                String aiResult = callGeminiApi(geminiApiKey.trim(), companyName, companyRole, requiredSkillList, userSkillList, matchedSkills, missingSkills, targetRole, userExp, score);
                if (aiResult != null && !aiResult.isEmpty()) {
                    status = "gemini_ai";
                    engineTitle = "Google Gemini 1.5 Flash (Generative AI)";
                    engineNotice = "Deep contextual AI recommendations generated in real-time by Google Gemini.";
                    explanation = extractFieldFromJson(aiResult, "explanation");
                    learningSuggestions = extractArrayFromJson(aiResult, "learningSuggestions");
                }
            } catch (Exception e) {
                System.err.println("[AISkillGapServlet] Gemini API notice: " + e.getMessage() + ". Falling back to Heuristic Engine.");
            }
        }

        // If not using Gemini or if Gemini call failed/empty, generate rich Heuristic Insights
        if (explanation == null || explanation.isEmpty()) {
            explanation = generateHeuristicExplanation(companyName, companyRole, score, matchedSkills, missingSkills, targetRole);
        }
        if (learningSuggestions == null || learningSuggestions.isEmpty()) {
            learningSuggestions = generateHeuristicLearningSuggestions(companyRole, missingSkills, matchedSkills, targetRole);
        }

        // Build JSON response
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"score\":").append(score).append(",");
        json.append("\"status\":").append(escapeJson(status)).append(",");
        json.append("\"engineTitle\":").append(escapeJson(engineTitle)).append(",");
        json.append("\"engineNotice\":").append(escapeJson(engineNotice)).append(",");
        json.append("\"roleFit\":").append(escapeJson(roleFit)).append(",");
        json.append("\"companyName\":").append(escapeJson(companyName)).append(",");
        json.append("\"companyRole\":").append(escapeJson(companyRole)).append(",");
        json.append("\"explanation\":").append(escapeJson(explanation)).append(",");

        // matchedSkills array
        json.append("\"matchingSkills\":[");
        for (int i = 0; i < matchedSkills.size(); i++) {
            if (i > 0) json.append(",");
            json.append(escapeJson(matchedSkills.get(i)));
        }
        json.append("],");

        // missingSkills array
        json.append("\"missingSkills\":[");
        for (int i = 0; i < missingSkills.size(); i++) {
            if (i > 0) json.append(",");
            json.append(escapeJson(missingSkills.get(i)));
        }
        json.append("],");

        // learningSuggestions array
        json.append("\"learningSuggestions\":[");
        for (int i = 0; i < learningSuggestions.size(); i++) {
            if (i > 0) json.append(",");
            json.append(escapeJson(learningSuggestions.get(i)));
        }
        json.append("]");

        json.append("}");

        out.print(json.toString());
        out.flush();
    }

    public static int calculateMatchScore(List<String> userSkills, List<String> requiredSkills, List<String> matchedSkills, String targetRole, String companyRole, String userExp, String companyExp) {
        if (requiredSkills.isEmpty()) return 75;

        // 1. Skill Match Component (65% weight)
        double skillRatio = (double) matchedSkills.size() / (double) requiredSkills.size();
        double skillScore = skillRatio * 65.0;

        // 2. Role Alignment Component (25% weight)
        double roleScore = 15.0; // baseline
        if (targetRole != null && !targetRole.trim().isEmpty() && companyRole != null) {
            String tRole = targetRole.trim().toLowerCase();
            String cRole = companyRole.trim().toLowerCase();
            if (tRole.equals(cRole) || cRole.contains(tRole) || tRole.contains(cRole)) {
                roleScore = 25.0; // exact or direct substring match
            } else if (isRoleCompatible(tRole, cRole)) {
                roleScore = 21.0; // semantic domain compatibility
            }
        }

        // 3. Experience Alignment Component (10% weight)
        double expScore = 7.0;
        try {
            int uExpVal = Integer.parseInt(userExp.replaceAll("[^0-9]", ""));
            if (companyExp != null && companyExp.contains("-")) {
                String[] parts = companyExp.split("-");
                int minExp = Integer.parseInt(parts[0].trim());
                int maxExp = Integer.parseInt(parts[1].replaceAll("[^0-9]", "").trim());
                if (uExpVal >= minExp && uExpVal <= maxExp) {
                    expScore = 10.0;
                } else if (uExpVal < minExp) {
                    expScore = 5.0;
                } else {
                    expScore = 9.0;
                }
            } else {
                expScore = 8.0;
            }
        } catch (Exception ignore) {}

        int total = (int) Math.round(skillScore + roleScore + expScore);
        // Bonus for having extra valuable user skills
        if (matchedSkills.size() >= 3 && userSkills.size() > matchedSkills.size()) {
            total = Math.min(98, total + 3);
        }
        return Math.max(25, Math.min(98, total));
    }

    private static boolean isRoleCompatible(String r1, String r2) {
        // Data Analyst & Business Intelligence
        if ((r1.contains("data") || r1.contains("analyst") || r1.contains("bi")) &&
            (r2.contains("data") || r2.contains("analyst") || r2.contains("analytics"))) {
            return true;
        }
        // Software Engineering & SDE & Developer
        if ((r1.contains("software") || r1.contains("developer") || r1.contains("sde") || r1.contains("engineer")) &&
            (r2.contains("software") || r2.contains("developer") || r2.contains("sde") || r2.contains("engineer"))) {
            return true;
        }
        // Technology Analyst & Systems Engineer
        if ((r1.contains("analyst") || r1.contains("technology") || r1.contains("system")) &&
            (r2.contains("analyst") || r2.contains("technology") || r2.contains("system"))) {
            return true;
        }
        return false;
    }

    public static boolean isSkillMatch(String req, String usr) {
        if (req == null || usr == null) return false;
        String r = req.trim().toLowerCase();
        String u = usr.trim().toLowerCase();
        if (r.equals(u)) return true;

        // Common tech aliases & synonyms
        if ((r.equals("c") || r.equals("c language")) && (u.equals("c") || u.equals("c language"))) return true;
        if ((r.equals("c++") || r.equals("cpp")) && (u.equals("c++") || u.equals("cpp"))) return true;
        if ((r.equals("c#") || r.equals("csharp")) && (u.equals("c#") || u.equals("csharp"))) return true;
        if ((r.equals("go") || r.equals("golang")) && (u.equals("go") || u.equals("golang"))) return true;
        if ((r.equals("spring") || r.equals("spring boot")) && (u.equals("spring") || u.equals("spring boot"))) return true;
        if ((r.equals("react") || r.equals("reactjs") || r.equals("react.js")) && (u.equals("react") || u.equals("reactjs") || u.equals("react.js"))) return true;
        if ((r.equals("node") || r.equals("nodejs") || r.equals("node.js")) && (u.equals("node") || u.equals("nodejs") || u.equals("node.js"))) return true;
        if ((r.equals("cloud") || r.equals("aws") || r.equals("azure") || r.equals("gcp")) && (u.equals("cloud") || u.equals("aws") || u.equals("azure") || u.equals("gcp"))) return true;
        if ((r.equals("sql") || r.equals("mysql") || r.equals("dbms") || r.equals("pl/sql")) && (u.equals("sql") || u.equals("mysql") || u.equals("dbms") || u.equals("pl/sql"))) return true;
        if ((r.contains("data structure") || r.equals("dsa")) && (u.contains("data structure") || u.equals("dsa"))) return true;
        if ((r.equals("power bi") || r.equals("powerbi") || r.equals("tableau")) && (u.equals("power bi") || u.equals("powerbi") || u.equals("tableau"))) return true;
        if ((r.equals("data analysis") || r.equals("data analytics")) && (u.equals("data analysis") || u.equals("data analytics"))) return true;

        // Substring match only for tokens of length >= 3
        if (r.length() >= 3 && u.length() >= 3) {
            if (r.contains(u) || u.contains(r)) return true;
        }
        return false;
    }

    private static String evaluateRoleFit(int score, String userExp, String companyExp) {
        if (score >= 85) return "Strong Candidate Fit • High Interview Likelihood";
        if (score >= 70) return "Competitive Fit • Bridgeable Skill Gap";
        if (score >= 50) return "Moderate Fit • Skill Upskilling Recommended";
        return "Emerging Fit • Core Foundations Recommended";
    }

    private static String generateHeuristicExplanation(String company, String role, int score, List<String> matched, List<String> missing, String targetRole) {
        StringBuilder sb = new StringBuilder();
        if (score >= 85) {
            sb.append("Outstanding alignment with ").append(company).append("'s ").append(role).append(" opening. ");
            sb.append("Your background in ").append(String.join(", ", matched)).append(" directly satisfies their core architectural and technical prerequisites. ");
            if (!missing.isEmpty()) {
                sb.append("Familiarity with ").append(String.join(", ", missing)).append(" will finalize your technical readiness.");
            } else {
                sb.append("You meet all listed key qualifications.");
            }
        } else if (score >= 65) {
            sb.append("Solid baseline compatibility for the ").append(role).append(" position at ").append(company).append(". ");
            sb.append("You possess verified proficiency in ").append(String.join(", ", matched)).append(", which covers critical foundational components. ");
            if (!missing.isEmpty()) {
                sb.append("Focusing on ").append(String.join(", ", missing)).append(" will substantially increase your interview candidacy.");
            }
        } else {
            sb.append("You share initial common competencies with this ").append(role).append(" opportunity at ").append(company).append(". ");
            if (!matched.isEmpty()) {
                sb.append("Your skills in ").append(String.join(", ", matched)).append(" provide a valuable launchpad. ");
            }
            if (!missing.isEmpty()) {
                sb.append("Accelerating your mastery of ").append(String.join(", ", missing)).append(" will bridge the qualification gap effectively.");
            }
        }
        return sb.toString();
    }

    private static List<String> generateHeuristicLearningSuggestions(String role, List<String> missing, List<String> matched, String targetRole) {
        List<String> list = new ArrayList<>();
        String r = role.toLowerCase();

        // Specific tailored suggestions based on role category
        if (r.contains("data") || r.contains("analyst") || r.contains("analytics")) {
            if (missing.contains("Tableau") || missing.contains("Power BI") || missing.contains("Data Visualization")) {
                list.add("Build an interactive end-to-end Dashboard in Tableau or Power BI analyzing real-world KPI metrics (e.g. customer churn or sales growth).");
            }
            if (missing.contains("SQL") || missing.contains("BigQuery")) {
                list.add("Master advanced SQL analytical queries: Window Functions (ROW_NUMBER, DENSE_RANK, LAG/LEAD), CTEs, and query execution plans.");
            }
            if (missing.contains("Python") || missing.contains("Statistics") || missing.contains("Data Modeling")) {
                list.add("Develop automated data wrangling scripts with Python (Pandas/NumPy) and implement hypothesis testing or exploratory data analysis.");
            }
            list.add("Document a business case study portfolio on GitHub showcasing data cleaning pipelines, business insights, and executive summaries.");
        } else if (r.contains("software") || r.contains("developer") || r.contains("sde")) {
            if (missing.contains("Spring") || missing.contains("Spring Boot") || missing.contains("Microservices")) {
                list.add("Architect a modular Microservices backend with Spring Boot, Spring Security JWT authentication, and Docker containerization.");
            }
            if (missing.contains("Cloud") || missing.contains("AWS") || missing.contains("Azure") || missing.contains("Go")) {
                list.add("Deploy a resilient containerized cloud workload on AWS (ECS/EKS) or GCP, configuring CI/CD pipelines and load balancers.");
            }
            if (missing.contains("Data Structures") || missing.contains("Algorithms")) {
                list.add("Target High-Yield DSA patterns on LeetCode: Binary Search, Graph Traversals (BFS/DFS), Dynamic Programming, and System Design fundamentals.");
            }
            list.add("Build and publish a full-stack production application demonstrating database indexing, caching (Redis), and automated test coverage.");
        } else if (r.contains("technology analyst") || r.contains("system")) {
            if (!missing.isEmpty()) {
                list.add("Strengthen hands-on enterprise integration by building client-facing REST APIs with " + String.join(" & ", missing) + ".");
            }
            list.add("Familiarize yourself with Enterprise Agile lifecycles, API contract testing, and relational database schema migrations.");
            list.add("Complete foundational cloud architecture certification (e.g. AWS Certified Cloud Practitioner or Microsoft Azure AZ-900).");
        } else {
            // General tech roles
            if (!missing.isEmpty()) {
                list.add("Complete a hands-on technical project focusing on " + String.join(", ", missing) + " to establish verified portfolio evidence.");
            }
            list.add("Review common technical interview problem sets and core architectural trade-offs relevant to " + role + ".");
            list.add("Publish an open-source GitHub repository detailing your application architecture, design patterns, and deployment guide.");
        }

        // Ensure at least 3 high quality suggestions
        while (list.size() < 3) {
            list.add("Engage in mock technical interviews focusing on system design trade-offs and code refactoring for " + role + ".");
        }
        return list;
    }

    private static String callGeminiApi(String apiKey, String company, String role, List<String> reqSkills, List<String> userSkills, List<String> matched, List<String> missing, String targetRole, String userExp, int score) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String prompt = "You are an expert AI Career Coach and Tech Recruiter. Analyze this candidate for a job opening.\n"
                    + "Company: " + company + "\n"
                    + "Role: " + role + "\n"
                    + "Company Required Skills: " + String.join(", ", reqSkills) + "\n"
                    + "Candidate Skills: " + String.join(", ", userSkills) + "\n"
                    + "Candidate Target Role: " + targetRole + "\n"
                    + "Candidate Experience: " + userExp + " years\n"
                    + "Matched Skills: " + String.join(", ", matched) + "\n"
                    + "Missing Skills: " + String.join(", ", missing) + "\n"
                    + "Calculated Match Score: " + score + "%\n\n"
                    + "Respond ONLY with a valid JSON object in this exact schema with no extra markdown formatting or backticks:\n"
                    + "{\n"
                    + "  \"explanation\": \"A professional, 2-3 sentence personalized evaluation of candidate fit for this opening at " + company + ".\",\n"
                    + "  \"learningSuggestions\": [\n"
                    + "    \"Specific high-impact learning suggestion 1\",\n"
                    + "    \"Specific high-impact learning suggestion 2\",\n"
                    + "    \"Specific high-impact learning suggestion 3\"\n"
                    + "  ]\n"
                    + "}";

            String jsonPayload = "{\"contents\":[{\"parts\":[{\"text\":" + escapeJson(prompt) + "}]}]}";

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(6))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                String body = resp.body();
                // Extract inner text from Gemini response structure: candidates[0].content.parts[0].text
                int textIdx = body.indexOf("\"text\": \"");
                if (textIdx != -1) {
                    int start = textIdx + 9;
                    int end = body.lastIndexOf("\"");
                    if (end > start) {
                        String rawInner = body.substring(start, end);
                        rawInner = unescapeJsonString(rawInner);
                        // Clean any ```json wrapping if model included it
                        rawInner = rawInner.replaceAll("```json", "").replaceAll("```", "").trim();
                        return rawInner;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AISkillGapServlet] Gemini call error: " + e.getMessage());
        }
        return null;
    }

    private static String extractFieldFromJson(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":\\s*\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) {
            pattern = "\"" + fieldName + "\":\"";
            idx = json.indexOf(pattern);
        }
        if (idx != -1) {
            int start = idx + pattern.length();
            int end = start;
            boolean escaped = false;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (c == '\\') {
                    escaped = !escaped;
                } else if (c == '"' && !escaped) {
                    break;
                } else {
                    escaped = false;
                }
                end++;
            }
            return json.substring(start, end);
        }
        return "";
    }

    private static List<String> extractArrayFromJson(String json, String arrayName) {
        List<String> list = new ArrayList<>();
        String pattern = "\"" + arrayName + "\":\\s*\\[";
        int idx = json.indexOf(pattern);
        if (idx != -1) {
            int start = json.indexOf("[", idx) + 1;
            int end = json.indexOf("]", start);
            if (start > 0 && end > start) {
                String arrayContent = json.substring(start, end);
                String[] items = arrayContent.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (String it : items) {
                    String clean = it.trim();
                    if (clean.startsWith("\"") && clean.endsWith("\"") && clean.length() >= 2) {
                        clean = clean.substring(1, clean.length() - 1);
                    }
                    if (!clean.isEmpty()) {
                        list.add(clean.replace("\\\"", "\"").replace("\\n", " "));
                    }
                }
            }
        }
        return list;
    }

    private static String unescapeJsonString(String str) {
        return str.replace("\\n", "\n")
                  .replace("\\r", "")
                  .replace("\\\"", "\"")
                  .replace("\\\\", "\\");
    }

    private static String escapeJson(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static List<String> cleanTokens(String input) {
        List<String> list = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) return list;
        String[] parts = input.split("[,;]+");
        Set<String> seen = new LinkedHashSet<>();
        for (String p : parts) {
            String clean = p.trim();
            if (!clean.isEmpty()) {
                seen.add(clean);
            }
        }
        list.addAll(seen);
        return list;
    }
}
