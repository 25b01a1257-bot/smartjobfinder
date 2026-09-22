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

@WebServlet({"/api/interviewPrep", "/interviewPrep", "/api/interview-prep"})
public class InterviewPrepServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static class QuestionItem {
        public String type; // Technical, HR, Role-Based, Situational
        public String category;
        public String question;
        public String sampleAnswer;
        public String keyConcepts;
        public String difficulty;

        public QuestionItem(String type, String question, String sampleAnswer, String keyConcepts, String difficulty) {
            this.type = type;
            this.category = type;
            this.question = question;
            this.sampleAnswer = sampleAnswer;
            this.keyConcepts = keyConcepts;
            this.difficulty = difficulty;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String format = request.getParameter("format");
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        boolean isApi = uri != null && uri.contains("/api/");
        boolean wantsJson = "json".equalsIgnoreCase(format) || (accept != null && accept.contains("application/json")) || isApi;

        if (!wantsJson) {
            response.sendRedirect("interview-prep.html");
            return;
        }

        String companyIdStr = request.getParameter("companyId");
        String roleParam = request.getParameter("role");
        String skillsParam = request.getParameter("skills");

        String companyName = "Top Tech Organization";
        String role = roleParam != null && !roleParam.trim().isEmpty() ? roleParam.trim() : "Software Engineer";
        String skills = skillsParam != null && !skillsParam.trim().isEmpty() ? skillsParam.trim() : "Java, Python, SQL, Cloud";

        if (companyIdStr != null && !companyIdStr.trim().isEmpty()) {
            try {
                int cid = Integer.parseInt(companyIdStr.trim());
                try (Connection conn = DBConnection.getConnection()) {
                    if (conn != null) {
                        String sql = "SELECT company_name, role, skills FROM companies WHERE id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setInt(1, cid);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    companyName = rs.getString("company_name");
                                    role = rs.getString("role");
                                    skills = rs.getString("skills");
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignore) {}
        }

        List<QuestionItem> questions = generateInterviewQuestions(companyName, role, skills);

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.print("{");
        out.print("\"companyName\":\"" + escapeJson(companyName) + "\",");
        out.print("\"role\":\"" + escapeJson(role) + "\",");
        out.print("\"skills\":\"" + escapeJson(skills) + "\",");
        out.print("\"questions\":[");
        for (int i = 0; i < questions.size(); i++) {
            QuestionItem q = questions.get(i);
            if (i > 0) out.print(",");
            out.print("{");
            out.print("\"type\":\"" + escapeJson(q.type) + "\",");
            out.print("\"question\":\"" + escapeJson(q.question) + "\",");
            out.print("\"sampleAnswer\":\"" + escapeJson(q.sampleAnswer) + "\",");
            out.print("\"keyConcepts\":\"" + escapeJson(q.keyConcepts) + "\",");
            out.print("\"difficulty\":\"" + escapeJson(q.difficulty) + "\"");
            out.print("}");
        }
        out.print("]");
        out.print("}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Handle Mock Interview Evaluation
        String action = request.getParameter("action");
        if ("evaluateAnswer".equalsIgnoreCase(action) || "evaluate".equalsIgnoreCase(action)) {
            String question = request.getParameter("question");
            String userAnswer = request.getParameter("answer");
            String role = request.getParameter("role");

            if (userAnswer == null || userAnswer.trim().isEmpty()) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"error\":\"Please provide an answer to receive feedback.\"}");
                return;
            }

            FeedbackResult feedback = evaluateMockAnswer(question, userAnswer.trim(), role);

            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{");
            out.print("\"success\":true,");
            out.print("\"score\":" + feedback.score + ",");
            out.print("\"rating\":\"" + escapeJson(feedback.rating) + "\",");
            out.print("\"strengths\":\"" + escapeJson(feedback.strengths) + "\",");
            out.print("\"missingElements\":\"" + escapeJson(feedback.missingElements) + "\",");
            out.print("\"suggestion\":\"" + escapeJson(feedback.suggestion) + "\",");
            out.print("\"evaluation\":{");
            out.print("\"score\":" + (feedback.score / 10) + ",");
            out.print("\"feedback\":\"" + escapeJson(feedback.rating + ": " + feedback.suggestion) + "\",");
            out.print("\"strengths\":[\"" + escapeJson(feedback.strengths) + "\"],");
            out.print("\"improvements\":[\"" + escapeJson(feedback.missingElements) + "\",\"" + escapeJson(feedback.suggestion) + "\"]");
            out.print("}");
            out.print("}");
            return;
        }

        doGet(request, response);
    }

    static class FeedbackResult {
        int score;
        String rating;
        String strengths;
        String missingElements;
        String suggestion;
    }

    private static FeedbackResult evaluateMockAnswer(String question, String answer, String role) {
        FeedbackResult res = new FeedbackResult();
        String lower = answer.toLowerCase();
        int words = answer.split("\\s+").length;

        int score = 40; // baseline

        // Evaluate answer length and depth
        if (words >= 60) score += 20;
        else if (words >= 30) score += 10;

        // Check for structural indicators (STAR method: Situation, Task, Action, Result)
        boolean hasAction = lower.contains("i decided") || lower.contains("implemented") || lower.contains("designed") || lower.contains("used") || lower.contains("solved");
        boolean hasResult = lower.contains("result") || lower.contains("improved") || lower.contains("learned") || lower.contains("successfully") || lower.contains("outcome");
        boolean hasTechnicalKeyword = lower.contains("java") || lower.contains("sql") || lower.contains("api") || lower.contains("data") || lower.contains("test") || lower.contains("database") || lower.contains("architecture");

        if (hasAction) score += 15;
        if (hasResult) score += 15;
        if (hasTechnicalKeyword) score += 10;

        res.score = Math.min(95, Math.max(45, score));

        if (res.score >= 80) {
            res.rating = "Strong Answer 🌟";
            res.strengths = "Clear technical vocabulary, explicit personal contribution, and structured thought progression.";
            res.missingElements = "Consider highlighting measurable metrics or trade-offs between alternative technical approaches.";
            res.suggestion = "To elevate this to elite level: Quantify your results (e.g. 'reduced query latency by 30%') and mention how you handled edge cases.";
        } else if (res.score >= 65) {
            res.rating = "Good Solid Foundation 👍";
            res.strengths = "Demonstrates relevant domain awareness and directly addresses the core question prompt.";
            res.missingElements = "Could provide a more detailed step-by-step technical explanation and conclusive outcome.";
            res.suggestion = "Adopt the STAR framework: Explicitly explain the Situation, your specific Task, the Action taken with technologies used, and the measurable Result.";
        } else {
            res.rating = "Needs Elaboration 💡";
            res.strengths = "Good concise start with the right general direction.";
            res.missingElements = "Answer is quite brief. Lacks specific technical implementation details and project context.";
            res.suggestion = "Expand your response with a concrete project example. Describe which tools or algorithms you used and what you learned from the experience.";
        }

        return res;
    }

    public static List<QuestionItem> generateInterviewQuestions(String company, String role, String skills) {
        List<QuestionItem> list = new ArrayList<>();
        String lowerRole = role.toLowerCase();
        String lowerSkills = skills.toLowerCase();

        // 1. Core Technical Questions based on role & skills
        if (lowerSkills.contains("java") || lowerRole.contains("software") || lowerRole.contains("sde")) {
            list.add(new QuestionItem(
                    "Technical",
                    "How does HashMap work internally in Java, and how are hash collisions resolved in Java 8+?",
                    "HashMap uses an array of Node (bucket) elements where bucket index is calculated as `hash(key) & (n - 1)`. When collisions occur, elements are stored in a linked list. In Java 8+, once a bucket reaches 8 elements (TREEIFY_THRESHOLD) and total capacity >= 64, it converts the linked list to a balanced Red-Black Tree, reducing lookup complexity from O(n) to O(log n).",
                    "Hashing, Buckets, Red-Black Trees, Time Complexity",
                    "Intermediate"
            ));
        }

        if (lowerSkills.contains("sql") || lowerRole.contains("data") || lowerRole.contains("analyst") || lowerRole.contains("software")) {
            list.add(new QuestionItem(
                    "Technical",
                    "What is the difference between WHERE and HAVING in SQL, and when do you use Window Functions?",
                    "`WHERE` filters rows before any aggregation takes place, while `HAVING` filters aggregated groups created by `GROUP BY`. Window functions (such as `ROW_NUMBER()`, `RANK()`, `LEAD()`) perform calculations across a set of rows related to the current row without collapsing them into a single row like `GROUP BY` does.",
                    "SQL Execution Order, Aggregation, Window Functions",
                    "Beginner / Intermediate"
            ));
        }

        if (lowerSkills.contains("python")) {
            list.add(new QuestionItem(
                    "Technical",
                    "Explain the difference between mutable and immutable types in Python, and how memory management (GIL) works.",
                    "Immutable types (tuples, strings, ints) cannot be changed after creation, creating a new object on modification. Mutable types (lists, dicts, sets) can be altered in-place. The Global Interpreter Lock (GIL) is a mutex that prevents multiple native threads from executing Python bytecodes simultaneously in CPython, ensuring thread safety around reference counting.",
                    "Mutability, GIL, Memory Management, Reference Counting",
                    "Intermediate"
            ));
        }

        if (lowerSkills.contains("spring") || lowerSkills.contains("microservices")) {
            list.add(new QuestionItem(
                    "Technical",
                    "What is Dependency Injection in Spring Boot, and what are the advantages of constructor injection over field injection?",
                    "Dependency Injection (DI) allows the Spring IoC container to inject dependent objects at runtime rather than having classes instantiate them directly. Constructor injection is preferred because it guarantees required dependencies cannot be null, promotes immutability with `final` fields, and makes unit testing straightforward without relying on reflection.",
                    "Inversion of Control, Autowiring, Testability, Immutability",
                    "Intermediate"
            ));
        }

        // 2. Role-Based & System Architecture Question
        if (lowerRole.contains("data") || lowerRole.contains("analyst")) {
            list.add(new QuestionItem(
                    "Role-Based",
                    "How would you approach handling missing or noisy data in a production dataset before presenting executive reports?",
                    "First, analyze missingness patterns (MCAR, MAR, MNAR) to understand the root cause. For small proportions (<5%), imputation via median (skewed numerical) or mode (categorical) is practical. For structural noise, build automated validation pipelines. Always document assumptions and verify with business stakeholders before finalizing executive metrics.",
                    "Data Cleaning, Imputation, Business Intelligence, Data Pipelines",
                    "Intermediate"
            ));
        } else {
            list.add(new QuestionItem(
                    "Role-Based",
                    "How do you design a RESTful API for high scalability, rate limiting, and fault tolerance?",
                    "Use stateless architecture with idempotency keys for write operations (POST/PUT). Employ an API Gateway with Redis token-bucket rate limiting to mitigate DDoS. Utilize distributed caching (Redis) for hot read endpoints, asynchronous messaging (Kafka/RabbitMQ) for long-running workflows, and circuit breakers (Resilience4j) to prevent cascading failures.",
                    "Statelessness, Caching, Rate Limiting, Circuit Breakers",
                    "Advanced"
            ));
        }

        // 3. Behavioral & HR Questions (STAR framework)
        list.add(new QuestionItem(
                "HR & Behavioral",
                "Describe a situation where a technical project bug or requirement conflict occurred. How did you resolve it?",
                "Situation: During our final semester project, our API response time spiked due to unindexed database queries. Task: As backend lead, I was responsible for restoring sub-200ms latency. Action: I used MySQL EXPLAIN query execution plan to identify table scans, added composite indexes, and implemented connection pooling with try-with-resources. Result: Query execution improved by 65% and our team completed the demo smoothly.",
                "STAR Framework, Problem Solving, Communication, Ownership",
                "Behavioral"
        ));

        list.add(new QuestionItem(
                "HR & Behavioral",
                "Why do you want to join " + company + " specifically, and where do you see your career in 3 years?",
                "I am deeply inspired by " + company + "'s commitment to engineering scalability and impactful technology solutions. In 3 years, I aim to have grown into a high-impact core contributor, taking ownership of distributed features, mentoring junior engineers, and mastering end-to-end cloud software delivery.",
                "Company Alignment, Ambition, Cultural Fit, Long-term Vision",
                "Culture Fit"
        ));

        return list;
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
