package com.SmartJobFinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet({"/api/interviewPrep", "/interviewPrep", "/api/interview-prep"})
public class InterviewPrepServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static class QuestionItem {
        public int id;
        public String company;
        public String type; // Technical, HR, Behavioral, Role Specific
        public String category;
        public String question;
        public String sampleAnswer;
        public String keyConcepts;
        public String difficulty;

        public QuestionItem(int id, String company, String type, String question, String sampleAnswer, String keyConcepts, String difficulty) {
            this.id = id;
            this.company = company;
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
        String companyParam = request.getParameter("company");
        String roleParam = request.getParameter("role");
        String skillsParam = request.getParameter("skills");
        String expParam = request.getParameter("experience");
        String categoryParam = request.getParameter("category"); // All, Technical, HR, Behavioral, Role Specific

        String companyName = companyParam != null && !companyParam.trim().isEmpty() ? companyParam.trim() : "Google";
        String role = roleParam != null && !roleParam.trim().isEmpty() ? roleParam.trim() : "Software Engineer";
        String skills = skillsParam != null && !skillsParam.trim().isEmpty() ? skillsParam.trim() : "Java, SQL, Spring Boot, DSA";
        String exp = expParam != null && !expParam.trim().isEmpty() ? expParam.trim() : "1-3 years";

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
                                    if (roleParam == null || roleParam.trim().isEmpty()) role = rs.getString("role");
                                    if (skillsParam == null || skillsParam.trim().isEmpty()) skills = rs.getString("skills");
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignore) {}
        }

        // Generate full 50 Questions Pack
        List<QuestionItem> allQuestions = getOrGenerate50Questions(companyName, role, skills, exp);

        // Filter by category if requested
        List<QuestionItem> filteredQuestions = new ArrayList<>();
        int countTech = 0, countRole = 0, countHr = 0, countBehavioral = 0;

        for (QuestionItem q : allQuestions) {
            if ("Technical".equalsIgnoreCase(q.category)) countTech++;
            else if ("Role Specific".equalsIgnoreCase(q.category) || "Role-Based".equalsIgnoreCase(q.category)) countRole++;
            else if ("HR".equalsIgnoreCase(q.category)) countHr++;
            else if ("Behavioral".equalsIgnoreCase(q.category)) countBehavioral++;

            if (categoryParam == null || categoryParam.trim().isEmpty() || "All".equalsIgnoreCase(categoryParam) || "All Questions".equalsIgnoreCase(categoryParam)) {
                filteredQuestions.add(q);
            } else if (categoryParam.equalsIgnoreCase(q.category) || (categoryParam.equalsIgnoreCase("Role Specific") && q.category.startsWith("Role"))) {
                filteredQuestions.add(q);
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.print("{");
        out.print("\"companyName\":\"" + escapeJson(companyName) + "\",");
        out.print("\"role\":\"" + escapeJson(role) + "\",");
        out.print("\"skills\":\"" + escapeJson(skills) + "\",");
        out.print("\"experience\":\"" + escapeJson(exp) + "\",");
        out.print("\"totalCount\":" + allQuestions.size() + ",");
        out.print("\"counts\":{");
        out.print("\"all\":" + allQuestions.size() + ",");
        out.print("\"technical\":" + countTech + ",");
        out.print("\"roleSpecific\":" + countRole + ",");
        out.print("\"hr\":" + countHr + ",");
        out.print("\"behavioral\":" + countBehavioral);
        out.print("},");
        out.print("\"questions\":[");
        for (int i = 0; i < filteredQuestions.size(); i++) {
            QuestionItem q = filteredQuestions.get(i);
            if (i > 0) out.print(",");
            out.print("{");
            out.print("\"id\":" + q.id + ",");
            out.print("\"company\":\"" + escapeJson(q.company) + "\",");
            out.print("\"type\":\"" + escapeJson(q.type) + "\",");
            out.print("\"category\":\"" + escapeJson(q.category) + "\",");
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

        // Handle Practice Mode Evaluation (Feature 7)
        String action = request.getParameter("action");
        if ("evaluateAnswer".equalsIgnoreCase(action) || "evaluate".equalsIgnoreCase(action)) {
            String question = request.getParameter("question");
            String userAnswer = request.getParameter("answer");
            String role = request.getParameter("role");
            String category = request.getParameter("category");

            if (userAnswer == null || userAnswer.trim().isEmpty()) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"error\":\"Please provide an answer to receive feedback.\"}");
                return;
            }

            FeedbackResult feedback = evaluateMockAnswer(question, userAnswer.trim(), role, category);

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

    private static FeedbackResult evaluateMockAnswer(String question, String answer, String role, String category) {
        FeedbackResult res = new FeedbackResult();
        String lower = answer.toLowerCase();
        int words = answer.split("\\s+").length;

        int score = 45; // baseline

        // Evaluate answer length and depth
        if (words >= 60) score += 20;
        else if (words >= 30) score += 10;
        else score -= 5;

        // Check for structural indicators (STAR method: Situation, Task, Action, Result)
        boolean hasSituation = lower.contains("situation") || lower.contains("when") || lower.contains("during") || lower.contains("project");
        boolean hasAction = lower.contains("i implemented") || lower.contains("i designed") || lower.contains("i analyzed") || lower.contains("i used") || lower.contains("i solved") || lower.contains("i decided");
        boolean hasResult = lower.contains("result") || lower.contains("improved") || lower.contains("reduced") || lower.contains("learned") || lower.contains("successfully") || lower.contains("outcome");
        boolean hasTechnicalKeyword = lower.contains("java") || lower.contains("sql") || lower.contains("api") || lower.contains("database") || lower.contains("complexity") || lower.contains("performance") || lower.contains("architecture") || lower.contains("algorithm") || lower.contains("test");

        if (hasSituation) score += 5;
        if (hasAction) score += 12;
        if (hasResult) score += 12;
        if (hasTechnicalKeyword) score += 10;

        res.score = Math.min(95, Math.max(45, score));

        if (res.score >= 80) {
            res.rating = "Strong Answer 🌟 (Score: " + (res.score / 10.0) + "/10)";
            res.strengths = "Excellent technical terminology, structured STAR progression, and clear demonstration of ownership.";
            res.missingElements = "Consider quantifying results with concrete business metrics (e.g., 'reduced API latency by 35%') and discussing edge-case handling.";
            res.suggestion = "Improve explanation with real-world examples: Mention trade-offs of chosen algorithms or alternative architectures.";
        } else if (res.score >= 65) {
            res.rating = "Good Solid Foundation 👍 (Score: " + (res.score / 10.0) + "/10)";
            res.strengths = "Demonstrates foundational domain knowledge and directly addresses the core interview prompt.";
            res.missingElements = "Could provide a more detailed step-by-step technical explanation and conclusive measurable outcome.";
            res.suggestion = "Adopt the STAR framework: Explicitly explain the Situation, your specific Task, the Action taken with technologies used, and the measurable Result.";
        } else {
            res.rating = "Needs Elaboration 💡 (Score: " + (res.score / 10.0) + "/10)";
            res.strengths = "Good concise start with the right general direction.";
            res.missingElements = "Answer is quite brief (< 30 words). Lacks specific technical implementation details, trade-offs, and project context.";
            res.suggestion = "Expand your response with a concrete project example. Describe which tools or algorithms you used and what you learned from the experience.";
        }

        return res;
    }

    /**
     * Backward-compatible overload for existing test suites and callers.
     */
    public static List<QuestionItem> generateInterviewQuestions(String company, String role, String skills) {
        return getOrGenerate50Questions(company, role, skills, "1-3 years");
    }

    /**
     * Fulfills Features 3, 4, 5 & 6:
     * Generates precisely 50 questions:
     * - 15 Technical
     * - 15 Role Specific
     * - 10 HR
     * - 10 Behavioral
     * Blending Company-Specific questions, user skills (Java, SQL, Spring Boot, DSA, OOP), role, and experience.
     */
    public static List<QuestionItem> getOrGenerate50Questions(String company, String role, String skills, String exp) {
        List<QuestionItem> result = new ArrayList<>();
        List<QuestionItem> dbQuestions = new ArrayList<>();

        // 1. Try fetching from interview_questions table
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                // Ensure seeded
                InterviewQuestionSeedData.seedQuestionsIfEmpty(conn);

                // Fetch matching questions for this company and general
                String query = "SELECT * FROM interview_questions WHERE LOWER(company_name) = ? OR LOWER(company_name) = 'all' OR LOWER(company_name) = 'generic' ORDER BY id ASC";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, company.toLowerCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            dbQuestions.add(new QuestionItem(
                                    rs.getInt("id"),
                                    rs.getString("company_name"),
                                    rs.getString("category"),
                                    rs.getString("question"),
                                    rs.getString("answer"),
                                    rs.getString("skills"),
                                    rs.getString("difficulty")
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[InterviewPrepServlet] DB query notice: " + e.getMessage());
        }

        // 2. Separate fetched questions into buckets
        List<QuestionItem> techList = new ArrayList<>();
        List<QuestionItem> roleList = new ArrayList<>();
        List<QuestionItem> hrList = new ArrayList<>();
        List<QuestionItem> behList = new ArrayList<>();

        for (QuestionItem q : dbQuestions) {
            if ("Technical".equalsIgnoreCase(q.category)) techList.add(q);
            else if ("Role Specific".equalsIgnoreCase(q.category) || "Role-Based".equalsIgnoreCase(q.category)) roleList.add(q);
            else if ("HR".equalsIgnoreCase(q.category)) hrList.add(q);
            else if ("Behavioral".equalsIgnoreCase(q.category)) behList.add(q);
        }

        // 3. Fill buckets dynamically to exact quotas: 15 Tech, 15 Role, 10 HR, 10 Behavioral (Total: 50)
        fillTechnicalQuestions(techList, company, role, skills, exp);
        fillRoleSpecificQuestions(roleList, company, role, skills, exp);
        fillHrQuestions(hrList, company, role, skills, exp);
        fillBehavioralQuestions(behList, company, role, skills, exp);

        // Combine to 50 items
        int idCounter = 1;
        for (int i = 0; i < 15 && i < techList.size(); i++) {
            QuestionItem q = techList.get(i);
            q.id = idCounter++;
            result.add(q);
        }
        for (int i = 0; i < 15 && i < roleList.size(); i++) {
            QuestionItem q = roleList.get(i);
            q.id = idCounter++;
            result.add(q);
        }
        for (int i = 0; i < 10 && i < hrList.size(); i++) {
            QuestionItem q = hrList.get(i);
            q.id = idCounter++;
            result.add(q);
        }
        for (int i = 0; i < 10 && i < behList.size(); i++) {
            QuestionItem q = behList.get(i);
            q.id = idCounter++;
            result.add(q);
        }

        return result;
    }

    private static void fillTechnicalQuestions(List<QuestionItem> list, String company, String role, String skills, String exp) {
        String[][] techBank = {
                {"How does HashMap work internally in Java 8+, and how are hash collisions handled?",
                 "HashMap uses bucket array hashing with `hash(key) & (n - 1)`. When collisions occur, elements form linked lists. In Java 8+, once a bucket reaches 8 elements (TREEIFY_THRESHOLD) and capacity >= 64, it converts to a Red-Black Tree, reducing worst-case lookup from O(n) to O(log n).",
                 "Hashing, Buckets, Red-Black Trees", "Intermediate"},
                {"What is the difference between WHERE and HAVING in SQL, and when do you use Window Functions?",
                 "`WHERE` filters rows before any aggregation takes place; `HAVING` filters aggregated groups created by `GROUP BY`. Window functions (`ROW_NUMBER()`, `DENSE_RANK()`, `LEAD()`) compute values across partition rows without collapsing them.",
                 "SQL, Aggregation, Window Functions", "Beginner / Intermediate"},
                {"Explain the difference between mutable and immutable objects in Python, and how memory reference counting works.",
                 "Immutable objects (integers, strings, tuples) cannot be altered after creation, creating new memory objects on change. Mutable objects (lists, dictionaries, sets) can be altered in-place. Reference counting tracks references, recycling memory via cyclical garbage collection.",
                 "Python, Memory Management, Mutability", "Intermediate"},
                {"What is Inversion of Control (IoC) and Dependency Injection in Spring Boot, and why use Constructor Injection?",
                 "IoC delegates object lifecycle and dependency assembly to the Spring container. Constructor injection ensures required dependencies cannot be null, supports `final` immutability, and simplifies standalone unit testing without mock frameworks.",
                 "Spring Boot, IoC, Dependency Injection", "Intermediate"},
                {"How does Java garbage collection work (Generational Hypothesis: Young, Old, Metaspace)?",
                 "JVM divides heap into Eden, Survivor (S0, S1), and Tenured/Old generation. Short-lived objects are cleared rapidly in Minor GC; surviving objects advance to Old generation where Major/Full GC runs using algorithms like G1GC or ZGC.",
                 "Java, JVM, Garbage Collection", "Advanced"},
                {"Explain B-Trees vs B+ Trees and why relational databases (MySQL InnoDB) use B+ Trees for primary indexes.",
                 "B+ Trees store data records only in leaf nodes, keeping internal nodes lean for high fan-out and shallow tree depth (3-4 levels for millions of rows). Leaf nodes are doubly linked, enabling lightning-fast range queries (`BETWEEN`, `>` scans).",
                 "Database, Indexing, B+ Trees", "Advanced"},
                {"Explain the concept of Thread Safety in Java, and how does ConcurrentHashMap achieve lock striping / CAS?",
                 "Thread safety ensures shared state is accessed without race conditions. ConcurrentHashMap avoids whole-table synchronization by using volatile variables, fine-grained bucket node locking (synchronized on bucket head), and CAS (Compare-And-Swap) operations.",
                 "Java, Concurrency, Multithreading", "Advanced"},
                {"What are Database Deadlocks, and what strategies prevent them in high-throughput applications?",
                 "A deadlock occurs when two transactions hold locks each other needs. Prevention: Access database tables and rows in a globally consistent order, keep transactions brief, use row-level optimistic locking, and configure appropriate deadlock timeouts.",
                 "SQL, Transactions, Deadlocks", "Intermediate"},
                {"What is the difference between Synchronous and Asynchronous execution, and how do Java CompletableFutures work?",
                 "Synchronous execution blocks the calling thread until completion. Asynchronous execution yields execution to the event loop or thread pool. CompletableFuture provides composable non-blocking callbacks (`thenApply`, `thenCombine`) for parallel tasks.",
                 "Java, Async, CompletableFuture", "Intermediate"},
                {"Explain the difference between TCP and UDP, and why HTTP/3 adopted QUIC (UDP) instead of TCP.",
                 "TCP provides reliable, ordered, byte-stream delivery via 3-way handshakes and retransmissions. UDP is connectionless and low-overhead. HTTP/3 uses QUIC over UDP to eliminate head-of-line blocking across multiplexed streams and support 0-RTT handshakes.",
                 "Networking, Protocols, HTTP/3", "Intermediate"},
                {"Explain the difference between Process and Thread, and what is Context Switching overhead?",
                 "A process is an isolated execution environment with dedicated address space and resources. A thread is the smallest schedulable unit of CPU execution sharing heap memory within a process. Context switching saves/restores register state and invalidates CPU caches.",
                 "Operating Systems, Threads, Concurrency", "Beginner / Intermediate"},
                {"How do you implement binary search on a rotated sorted array in O(log n) time?",
                 "Compute mid. At least one half (left or right) is guaranteed to be normally sorted. Check if the target lies within the sorted half's bounds; if so, search that half, otherwise search the opposing rotated half, maintaining O(log n) efficiency.",
                 "DSA, Binary Search, Arrays", "Intermediate"},
                {"What is the Virtual DOM in React, and how does the Reconciliation (Diffing) Algorithm work?",
                 "Virtual DOM is an in-memory lightweight JavaScript representation of the real DOM. When state changes, React creates a new VDOM tree, diffs it against previous snapshot using heuristic O(n) algorithms, and batches real DOM mutations.",
                 "React, Frontend, Web", "Intermediate"},
                {"Explain Microservices Circuit Breaker pattern and how Resilience4j prevents cascading microservice failures.",
                 "A Circuit Breaker monitors downstream RPC failure rates. In Closed state, calls pass through. If failure threshold (e.g. 50%) is breached, it trips to Open state, failing fast with fallback responses without overwhelming the struggling service. It transitions to Half-Open to test recovery.",
                 "Microservices, Resilience, Architecture", "Advanced"},
                {"Explain the difference between Relational (SQL) and NoSQL databases, and when would you choose MongoDB / DynamoDB over PostgreSQL?",
                 "Relational DBs provide strict schemas, ACID transactions, and complex JOINs (ideal for payments, ledgers). NoSQL DBs offer horizontal scaling, schema flexibility, and low-latency key-value or document lookups (ideal for clickstreams, catalogs, user sessions).",
                 "Database, SQL vs NoSQL, Architecture", "Intermediate"}
        };

        for (String[] t : techBank) {
            if (list.size() >= 15) break;
            boolean already = false;
            for (QuestionItem ex : list) {
                if (ex.question.equalsIgnoreCase(t[0])) { already = true; break; }
            }
            if (!already) {
                list.add(new QuestionItem(0, company, "Technical", t[0], t[1], t[2], t[3]));
            }
        }
    }

    private static void fillRoleSpecificQuestions(List<QuestionItem> list, String company, String role, String skills, String exp) {
        String[][] roleBank = {
                {"How would you design a scalable Notification Service supporting Push, SMS, and Email for 10M daily users?",
                 "Use decoupled microservices. Producers submit notification events to Apache Kafka partitioned by user ID. Rate-limiting workers pull events and dispatch through external providers (SendGrid, Twilio, FCM) with exponential backoff retries and Dead Letter Queues (DLQ).",
                 "System Design, Kafka, Microservices", "Advanced"},
                {"How do you implement Idempotency in payment and transaction APIs to prevent double charging on network timeouts?",
                 "Clients generate a unique Idempotency-Key (UUID) per transaction. The API server stores this key in Redis/DB with an atomic lock before processing. If a duplicate request arrives with the same key, return the cached original response without reprocessing.",
                 "API Design, Security, Transactions", "Advanced"},
                {"How do you optimize an application query taking 8 seconds on a table with 10 million rows?",
                 "Run `EXPLAIN ANALYZE` to inspect execution plan. Check for full table scans. Add composite indexes matching `WHERE` and `ORDER BY` clauses. Eliminate `SELECT *`, partition data by date if applicable, and implement Redis caching for hot read paths.",
                 "Performance, SQL, Optimization", "Intermediate"},
                {"Describe your end-to-end CI/CD deployment pipeline for modern production web applications.",
                 "Developers push code to GitHub. GitHub Actions runs linter, unit tests, and integration tests. Docker builds multi-stage images pushed to Amazon ECR. Canary deployment deploys to Kubernetes cluster with automated Prometheus health checks before rolling update.",
                 "DevOps, CI/CD, Docker, Cloud", "Intermediate"},
                {"How would you design a Rate Limiting algorithm (Token Bucket or Leaky Bucket) to protect public API endpoints?",
                 "Token Bucket maintains tokens added at a constant fill rate up to max capacity. Each request consumes 1 token; if empty, return HTTP 429 Too Many Requests. In distributed systems, implement using Redis Lua scripts for atomic increments per client IP.",
                 "System Design, Redis, Security", "Advanced"},
                {"Explain the difference between Monolithic, Microservices, and Event-Driven Architectures with trade-offs.",
                 "Monolith is simple to build, test, and deploy initially but harder to scale independently. Microservices offer independent deployment and tech flexibility at the cost of network complexity and distributed data management. Event-Driven maximizes asynchronous decoupling.",
                 "Architecture, Software Engineering", "Intermediate"},
                {"How do you handle Distributed Transactions across multiple microservices without 2-Phase Commit?",
                 "Use the Saga Pattern (Orchestration or Choreography). Each service performs local database transaction and emits domain events. If any step fails, compensating transactions are published in reverse order to restore consistent state.",
                 "Microservices, Sagas, Distributed Systems", "Advanced"},
                {"How do you secure REST APIs against common vulnerabilities like CSRF, XSS, and Broken Object Level Authorization (BOLA)?",
                 "Use OAuth2/JWT with short expiry and HttpOnly SameSite cookies. Sanitize and escape all input/output to stop XSS. Implement strict authorization checks ensuring authenticated user owns requested resource ID on every database query.",
                 "Security, OWASP, Authentication", "Intermediate"},
                {"How would you architect a live Real-Time Chat feature supporting 100k concurrent online connections?",
                 "Use WebSockets managed by distributed gateway servers (Netty/Node.js). Maintain connection state and distribute messages using Redis Pub/Sub or Kafka topics per chat channel. Store permanent chat history asynchronously in Apache Cassandra / ScyllaDB.",
                 "WebSockets, Real-Time, Redis, Concurrency", "Advanced"},
                {"Explain how you structure automated tests (Unit, Integration, End-to-End) following the Testing Pyramid.",
                 "70% Unit Tests (fast, isolated, mocking dependencies with Mockito), 20% Integration Tests (testing database persistence using Testcontainers), and 10% End-to-End Tests (verifying user workflows with Cypress or Selenium).",
                 "Testing, Quality Assurance, Clean Code", "Beginner / Intermediate"},
                {"How do you manage database migrations safely in production without downtime (Zero-Downtime Schema Changes)?",
                 "Use the Expand-Contract pattern. Step 1: Add new nullable column/table. Step 2: Deploy code writing to both old and new columns. Step 3: Backfill old data. Step 4: Deploy code reading exclusively from new column. Step 5: Deprecate and drop old column.",
                 "Database, Zero-Downtime, Migrations", "Advanced"},
                {"How do you design a high-throughput Cache Invalidation strategy (Cache-Aside vs Write-Through vs Write-Back)?",
                 "Cache-Aside (read from cache; on miss, query DB and populate cache) is most common. On write, update DB and invalidate (delete) the cache key rather than updating it, avoiding race conditions.",
                 "Caching, Redis, High Availability", "Intermediate"},
                {"Explain Domain-Driven Design (DDD) concepts: Entities, Value Objects, Aggregates, and Repositories.",
                 "Entities have distinct identities that persist over time (e.g. User). Value Objects are defined purely by their attributes and are immutable (e.g. Money). Aggregates encapsulate boundaries of consistency. Repositories manage aggregate persistence.",
                 "Software Architecture, DDD, Clean Code", "Intermediate"},
                {"What strategies would you use to diagnose and fix high CPU utilization on a live production microservice?",
                 "Inspect CPU profiling using `top`, `htop`, or JProfiler / async-profiler. Collect thread dumps to look for infinite loops, excessive garbage collection pauses, lock contention, or unindexed database queries consuming compute.",
                 "Production Debugging, Performance, Linux", "Intermediate"},
                {"How do you ensure data privacy and compliance (GDPR / Indian DPDP Act) in enterprise data pipelines?",
                 "Anonymize or pseudonymize personally identifiable information (PII), encrypt sensitive fields at rest (AES-256) and in transit (TLS 1.3), provide data subject access and deletion mechanisms (Right to be Forgotten), and log audit trails.",
                 "Compliance, Security, Privacy, Data", "Intermediate"}
        };

        for (String[] r : roleBank) {
            if (list.size() >= 15) break;
            boolean already = false;
            for (QuestionItem ex : list) {
                if (ex.question.equalsIgnoreCase(r[0])) { already = true; break; }
            }
            if (!already) {
                list.add(new QuestionItem(0, company, "Role Specific", r[0], r[1], r[2], r[3]));
            }
        }
    }

    private static void fillHrQuestions(List<QuestionItem> list, String company, String role, String skills, String exp) {
        String[][] hrBank = {
                {"Why do you want to join " + company + " specifically, and what excites you about our engineering culture?",
                 "I am deeply impressed by " + company + "'s scale, product innovation, and commitment to engineering impact. Joining gives me the opportunity to contribute to high-impact software, learn from seasoned industry engineers, and grow continuously.",
                 "Company Alignment, Motivation", "HR"},
                {"Where do you see yourself professionally in the next 3 to 5 years?",
                 "In 3 years, I aim to have established myself as a high-performing core engineer taking ownership of end-to-end distributed modules. In 5 years, I see myself mentoring newer developers and contributing to architectural system design.",
                 "Ambition, Career Growth", "HR"},
                {"What is your greatest technical strength, and what is one area you are currently working to improve?",
                 "My greatest strength is analytical debugging and quickly grasping new backend frameworks. One area I am actively improving is distributed systems tracing and cloud infrastructure automation by completing hands-on AWS lab projects.",
                 "Self-Awareness, Growth Mindset", "HR"},
                {"How do you handle tight project deadlines, unexpected production bugs, and high-pressure situations?",
                 "I stay calm by prioritizing tasks based on business urgency, communicating transparently with leads, breaking down blockers into actionable sub-tasks, and maintaining focus on root causes rather than panic.",
                 "Pressure Management, Resilience", "HR"},
                {"Tell me about a time you received constructive criticism during a code review. How did you react?",
                 "Situation: A senior peer pointed out that my PR lacked comprehensive boundary unit tests. Action: Rather than being defensive, I thanked them, studied their test examples, added the required edge cases, and created a personal testing checklist for future PRs.",
                 "Feedback, Humility, Collaboration", "HR"},
                {"Are you open to relocating to other development centers, and how do you adapt to hybrid/remote work?",
                 "Yes, I am fully open to relocation. In hybrid and remote environments, I excel by maintaining proactive written communication, participating actively in sprint standups, and documenting my progress diligently in Jira/Confluence.",
                 "Flexibility, Remote Work", "HR"},
                {"What are your salary expectations for this " + role + " role at " + company + "?",
                 "My expectation is aligned with company norms and market standards for entry-to-junior engineers in this role, typically in the 6 to 12 LPA range, with greater emphasis on learning scope, mentorship, and career growth.",
                 "Salary Negotiation, Professionalism", "HR"},
                {"How do you keep yourself updated with rapidly evolving technology trends and programming tools?",
                 "I follow engineering tech blogs (Google Research, Netflix Tech Blog, Martin Fowler), contribute to open source and personal projects on GitHub, and practice LeetCode and System Design case studies weekly.",
                 "Continuous Learning, Curiosity", "HR"},
                {"Why should " + company + " hire you over other qualified candidates applying for this role?",
                 "Beyond my technical foundation in " + skills + ", I bring strong ownership, curiosity, and rapid adaptability. I don't just write code; I care about user impact, clean documentation, and collaborating selflessly with team members.",
                 "Value Proposition, Confidence", "HR"},
                {"Do you have any questions for us regarding the team, engineering culture, or upcoming challenges?",
                 "Yes! What are the primary technical initiatives the team is tackling this quarter, and what qualities differentiate the most successful engineers who join this department?",
                 "Engagement, Inquisitiveness", "HR"}
        };

        for (String[] h : hrBank) {
            if (list.size() >= 10) break;
            boolean already = false;
            for (QuestionItem ex : list) {
                if (ex.question.equalsIgnoreCase(h[0])) { already = true; break; }
            }
            if (!already) {
                list.add(new QuestionItem(0, company, "HR", h[0], h[1], h[2], h[3]));
            }
        }
    }

    private static void fillBehavioralQuestions(List<QuestionItem> list, String company, String role, String skills, String exp) {
        String[][] behBank = {
                {"Describe a challenging technical project bug or obstacle you encountered and how you solved it (STAR method).",
                 "Situation: Our web application response time slowed to 4 seconds during load tests. Task: Diagnose and optimize before release. Action: Profiled SQL queries using EXPLAIN, identified unindexed table scans, added composite indexes, and implemented connection pooling with try-with-resources. Result: Query execution dropped to 120ms, a 97% improvement.",
                 "STAR Framework, Problem Solving, Persistence", "Behavioral"},
                {"Tell me about a time you had a disagreement with a team member on a technical decision. How did you resolve it?",
                 "Situation: A team member preferred storing configurations in source code while I advocated environment variables. Action: I scheduled a short meeting, demonstrated the security risk of leaked API keys in Git, and shared official Twelve-Factor App guidelines. Result: We aligned respectfully on using .env files without hard feelings.",
                 "Conflict Resolution, Communication, Teamwork", "Behavioral"},
                {"Describe a situation where a requirement changed drastically shortly before a project deadline.",
                 "Situation: Two days before demo day, the client requested adding Google OAuth login alongside email login. Task: Implement without breaking core authentication. Action: Reorganized authentication using Strategy pattern, isolating OAuth logic into a modular provider. Result: Successfully demoed with zero regressions on time.",
                 "Adaptability, Agile, Fast Learning", "Behavioral"},
                {"Tell me about a time you failed or made a mistake in a software project. What did you learn from it?",
                 "Situation: Early in college, I accidentally dropped a development database table without a recent backup. Action: Owned up immediately, spent the night writing automated data recreation scripts, and instituted mandatory daily automated schema backups. Result: Learned the paramount importance of data durability.",
                 "Ownership, Learning from Mistakes", "Behavioral"},
                {"Give an example of when you went above and beyond your assigned project responsibilities.",
                 "Situation: Our team had no automated test coverage for backend API endpoints. Action: On my own initiative, I researched JUnit 5 and Mockito, wrote 45 unit tests covering core business workflows, and integrated them into our GitHub pull request checks. Result: Prevented 3 major regressions.",
                 "Proactiveness, Ownership, Initiative", "Behavioral"},
                {"Describe how you collaborate with a non-technical stakeholder or team member who doesn't understand coding jargon.",
                 "Situation: Needed approval from product marketing on database schema trade-offs. Action: Avoided technical jargon like 'B-Trees' and 'Normalization', instead illustrating concepts using user journey flowcharts and latency impacts. Result: Stakeholder made an informed decision easily.",
                 "Communication, Empathy, Collaboration", "Behavioral"},
                {"Tell me about a time you had to balance multiple competing project priorities with overlapping deadlines.",
                 "Situation: Final exam submissions coincided with hackathon deliverables. Action: Created an Eisenhower Matrix, dedicated mornings to high-concentration technical coding and afternoons to documentation, and communicated status transparently. Result: Successfully completed both with high grades.",
                 "Time Management, Prioritization", "Behavioral"},
                {"Describe a situation where you noticed an inefficiency in a process and took action to improve it.",
                 "Situation: Developers were manually deploying WAR files to local Tomcat servers taking 15 minutes each run. Action: Created a single-click `run.bat` script compiling classes, bundling assets, and booting the container automatically. Result: Saved every team member 45 minutes daily.",
                 "Automation, Process Improvement", "Behavioral"},
                {"Tell me about a time you had to deliver a technical presentation or demo to a critical audience.",
                 "Situation: Presented our semester capstone to faculty evaluators and corporate judges. Action: Structured the presentation around the real-world user problem, demonstrated a live working demo, and prepared backup architecture slides for Q&A. Result: Won First Prize in departmental awards.",
                 "Presentation, Communication, Impact", "Behavioral"},
                {"Describe a time when you helped a teammate who was struggling with their assigned task.",
                 "Situation: A junior peer was stuck for two days on recursion and binary tree traversal. Action: Conducted a 1-on-1 whiteboarding pair programming session, drew call stacks visually, and guided them to solve the problem themselves. Result: Teammate gained confidence and finished on schedule.",
                 "Mentorship, Empathy, Teamwork", "Behavioral"}
        };

        for (String[] b : behBank) {
            if (list.size() >= 10) break;
            boolean already = false;
            for (QuestionItem ex : list) {
                if (ex.question.equalsIgnoreCase(b[0])) { already = true; break; }
            }
            if (!already) {
                list.add(new QuestionItem(0, company, "Behavioral", b[0], b[1], b[2], b[3]));
            }
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
