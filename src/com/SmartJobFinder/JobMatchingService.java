package com.SmartJobFinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobMatchingService {

    public static class JobCard {
        public int id;
        public String companyName;
        public String role;
        public String skills;
        public String salary;
        public String displaySalary;
        public String experience;
        public String description;
        public String logo;
        public String applyUrl;
        public String status;

        // Qualification & Branch Eligibility Fields (Phase 2)
        public String requiredDegree = "B.Tech / B.E., MCA, M.Tech";
        public String eligibleBranches = "Computer Science, Information Technology, Electronics & Communication";
        public String minQualification = "Bachelor's Degree";
        public String location = "Pan India / Hybrid";

        public boolean isEligible = true;
        public String eligibilityExplanation = "Eligible: Profile satisfies required degree and branch requirements.";
        public String missingQualification = "";
        public String missingBranch = "";

        // Skill Matching & AI Skill Gap Fields (Phase 6)
        public List<String> matchedSkills = new ArrayList<>();
        public List<String> missingSkills = new ArrayList<>();
        public List<String> recommendedTopics = new ArrayList<>();
        public List<String> learningResources = new ArrayList<>();
        public String learningPriority = "Medium";

        public int matchCount = 0;
        public int matchScore = 0;
        public String matchTierClass = "match-low";
        public String matchTierLabel = "Developing Fit";

        public JobCard() {}
    }

    /**
     * Curated skill learning roadmap dictionary providing realistic topics & beginner-friendly resources.
     */
    private static final Map<String, SkillGuide> SKILL_RESOURCES = new HashMap<>();

    static class SkillGuide {
        String topic;
        String resource;
        String priority;

        SkillGuide(String topic, String resource, String priority) {
            this.topic = topic;
            this.resource = resource;
            this.priority = priority;
        }
    }

    static {
        SKILL_RESOURCES.put("java", new SkillGuide("OOP Concepts, Collections Framework, Multithreading, Lambdas & Streams", "dev.java (Official Java Tutorials) & Mooc.fi", "High"));
        SKILL_RESOURCES.put("python", new SkillGuide("Data Structures, OOP, List Comprehensions, File I/O, Generators", "docs.python.org & Automate the Boring Stuff with Python", "High"));
        SKILL_RESOURCES.put("c++", new SkillGuide("Pointers, Memory Management, STL Containers, RAII, Modern C++17", "LearnCpp.com & CppReference", "High"));
        SKILL_RESOURCES.put("c", new SkillGuide("Pointers, Dynamic Memory Allocation (malloc/free), Structs, System Calls", "Programiz C & Harvard CS50", "High"));
        SKILL_RESOURCES.put("sql", new SkillGuide("Relational Modeling, Complex JOINs, Window Functions, Indexing & Aggregations", "W3Schools SQL & Mode Analytics SQL Tutorial", "High"));
        SKILL_RESOURCES.put("spring boot", new SkillGuide("Inversion of Control (IoC), Spring Data JPA, REST Controllers, Actuator", "spring.io/guides & Baeldung Spring Boot", "High"));
        SKILL_RESOURCES.put("spring", new SkillGuide("Core Spring Beans, Dependency Injection, AOP, Spring Security", "spring.io/quickstart", "Medium"));
        SKILL_RESOURCES.put("react", new SkillGuide("Functional Components, Hooks (useState, useEffect), Virtual DOM, Context API", "react.dev (Interactive Official Documentation)", "High"));
        SKILL_RESOURCES.put("javascript", new SkillGuide("ES6+ Syntax, Promises, Async/Await, Event Loop, DOM Manipulation", "javascript.info & MDN Web Docs", "High"));
        SKILL_RESOURCES.put("typescript", new SkillGuide("Static Typing, Interfaces, Generics, Type Unions, tsconfig setup", "typescriptlang.org handbook", "Medium"));
        SKILL_RESOURCES.put("node.js", new SkillGuide("Event-Driven Architecture, Express.js Middleware, RESTful APIs, NPM modules", "nodejs.org guides & FreeCodeCamp Backend", "High"));
        SKILL_RESOURCES.put("aws", new SkillGuide("EC2 Virtual Servers, S3 Storage, IAM Policies, Lambda Serverless, VPC", "AWS Skill Builder Free Tier & AWS Cloud Practitioner Essentials", "High"));
        SKILL_RESOURCES.put("docker", new SkillGuide("Containerization, Dockerfile Writing, Image Layering, Multi-stage Builds, Compose", "docker.com 101 Tutorial & Play with Docker", "High"));
        SKILL_RESOURCES.put("kubernetes", new SkillGuide("Pods, Deployments, Services, ConfigMaps, Ingress Controllers", "kubernetes.io/docs/tutorials", "Medium"));
        SKILL_RESOURCES.put("cloud", new SkillGuide("Cloud Architecture, High Availability, Scalability, Object Storage", "Google Cloud / AWS Digital Training", "Medium"));
        SKILL_RESOURCES.put("microservices", new SkillGuide("API Gateways, Service Discovery, Eventual Consistency, Circuit Breakers", "microservices.io & Martin Fowler Microservices Guide", "High"));
        SKILL_RESOURCES.put("kafka", new SkillGuide("Event Streaming, Producers, Consumers, Topics, Partitioning & Offsets", "Apache Kafka Documentation & Confluent Developer", "Medium"));
        SKILL_RESOURCES.put("redis", new SkillGuide("In-Memory Caching, Key-Value Structures, Pub/Sub, TTL, Cache Eviction", "redis.io/university & Redis University Free Courses", "Medium"));
        SKILL_RESOURCES.put("data structures", new SkillGuide("Arrays, LinkedLists, Trees, Graphs, Hash Tables, Big-O Complexity", "NeetCode.io & GeeksforGeeks DSA Self-Paced", "High"));
        SKILL_RESOURCES.put("algorithms", new SkillGuide("Binary Search, Two Pointers, Dynamic Programming, BFS/DFS Graph Traversal", "LeetCode Curated 75 & Visualgo.net", "High"));
        SKILL_RESOURCES.put("machine learning", new SkillGuide("Supervised Learning, Regression, Classification, Scikit-Learn, Feature Scaling", "Google Machine Learning Crash Course", "High"));
        SKILL_RESOURCES.put("power bi", new SkillGuide("Data Transformation with Power Query, DAX Measures, Relational Modeling", "Microsoft Learn: Power BI Fundamentals", "Medium"));
        SKILL_RESOURCES.put("tableau", new SkillGuide("Calculated Fields, Visual Storytelling, Dashboards, Data Blending", "Tableau Free Training Videos & Tableau Public", "Medium"));
        SKILL_RESOURCES.put("excel", new SkillGuide("XLOOKUP, INDEX/MATCH, Pivot Tables, Conditional Formatting, Data Cleaning", "Excel Exposure & Chandoo.org", "Medium"));
        SKILL_RESOURCES.put("linux", new SkillGuide("Bash Shell Scripting, File Permissions, Process Management, Cron Jobs, SSH", "LinuxJourney.com & OverTheWire Bandit", "Medium"));
        SKILL_RESOURCES.put("git", new SkillGuide("Branching Strategies, Merge vs Rebase, Pull Requests, Resolving Conflicts", "Git-SCM Pro Git Book & GitHub Skills", "High"));
    }

    /**
     * Backward-compatible overload for legacy callers.
     */
    public static List<JobCard> findMatchingJobs(List<String> userSkills, String targetRole, String userExp, String userSalary) {
        return findMatchingJobs(userSkills, targetRole, userExp, userSalary, "B.Tech / B.E.", "Computer Science & Engineering", "", false);
    }

    /**
     * Primary matching engine supporting Phase 2 Qualification & Branch Eligibility,
     * Phase 6 AI Skill Gap analysis, and advanced multi-parameter filtering.
     */
    public static List<JobCard> findMatchingJobs(List<String> userSkills, String targetRole, String userExp, String userSalary,
                                                String userDegree, String userBranch, String locationFilter, boolean eligibleOnly) {
        List<JobCard> results = new ArrayList<>();

        if (userSkills == null) userSkills = new ArrayList<>();
        if (targetRole == null) targetRole = "";
        if (userExp == null) userExp = "1";
        if (userSalary == null) userSalary = "";
        if (userDegree == null) userDegree = "";
        if (userBranch == null) userBranch = "";
        if (locationFilter == null) locationFilter = "";

        // Clean candidate skills
        List<String> cleanUserSkills = new ArrayList<>();
        for (String s : userSkills) {
            if (s != null && !s.trim().isEmpty()) {
                String c = s.trim();
                if (!cleanUserSkills.contains(c)) {
                    cleanUserSkills.add(c);
                }
            }
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.err.println("[JobMatchingService] Database connection was null.");
                return results;
            }

            String sql = "SELECT * FROM companies WHERE (status = 'ACTIVE' OR status IS NULL) ORDER BY id ASC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    JobCard job = new JobCard();
                    job.id = rs.getInt("id");
                    job.companyName = rs.getString("company_name");
                    job.role = rs.getString("role");
                    job.skills = rs.getString("skills");
                    job.salary = rs.getString("salary");
                    job.experience = rs.getString("experience");
                    try {
                        job.description = rs.getString("description");
                    } catch (Exception ignore) {
                        job.description = "";
                    }
                    if (job.description == null) job.description = "";

                    job.logo = rs.getString("logo");
                    job.applyUrl = rs.getString("apply_url");
                    try {
                        job.status = rs.getString("status");
                    } catch (Exception ignore) {
                        job.status = "ACTIVE";
                    }

                    // Qualification & Branch columns
                    try {
                        job.requiredDegree = rs.getString("required_degree");
                    } catch (Exception ignore) {}
                    if (job.requiredDegree == null || job.requiredDegree.trim().isEmpty()) {
                        job.requiredDegree = "B.Tech / B.E., MCA, M.Tech";
                    }

                    try {
                        job.eligibleBranches = rs.getString("eligible_branches");
                    } catch (Exception ignore) {}
                    if (job.eligibleBranches == null || job.eligibleBranches.trim().isEmpty()) {
                        job.eligibleBranches = "Computer Science, Information Technology, Electronics & Communication";
                    }

                    try {
                        job.minQualification = rs.getString("min_qualification");
                    } catch (Exception ignore) {}
                    if (job.minQualification == null || job.minQualification.trim().isEmpty()) {
                        job.minQualification = "Bachelor's Degree";
                    }

                    try {
                        job.location = rs.getString("location");
                    } catch (Exception ignore) {}
                    if (job.location == null || job.location.trim().isEmpty()) {
                        job.location = "Pan India / Hybrid";
                    }

                    // Location filter check
                    if (!locationFilter.isEmpty() && !locationFilter.equalsIgnoreCase("All")) {
                        if (job.location != null && !job.location.toLowerCase().contains(locationFilter.toLowerCase())) {
                            continue; // Skip if location does not match filter
                        }
                    }

                    // Normalize Logo
                    if (job.logo == null || job.logo.trim().isEmpty()) {
                        job.logo = "images/default-company.svg";
                    } else {
                        job.logo = job.logo.trim();
                        if (!job.logo.startsWith("images/") && !job.logo.startsWith("image/") && !job.logo.startsWith("http://") && !job.logo.startsWith("https://")) {
                            job.logo = "images/" + job.logo;
                        }
                    }

                    // Normalize Apply URL
                    if (job.applyUrl != null && !job.applyUrl.trim().isEmpty()) {
                        job.applyUrl = job.applyUrl.trim();
                        if (!job.applyUrl.startsWith("http://") && !job.applyUrl.startsWith("https://")) {
                            job.applyUrl = "https://" + job.applyUrl;
                        }
                    } else {
                        job.applyUrl = "#";
                    }

                    // Format Salary with comma separators
                    job.displaySalary = job.salary;
                    try {
                        long salVal = Long.parseLong(job.salary.replaceAll("[^0-9]", ""));
                        job.displaySalary = String.format("%,d", salVal);
                    } catch (Exception ignore) {}

                    // ========================================================
                    // Phase 2: HARD ELIGIBILITY EVALUATION (Degree & Branch)
                    // ========================================================
                    evaluateEligibility(job, userDegree, userBranch);

                    // If user toggled "Only Show Eligible Jobs", skip non-eligible
                    if (eligibleOnly && !job.isEligible) {
                        continue;
                    }

                    // ========================================================
                    // Phase 6: SOFT SKILL MATCHING & GAP ANALYSIS
                    // ========================================================
                    List<String> companySkillTokens = AISkillGapServlet.cleanTokens(job.skills);

                    int userSkillsMatchedCount = 0;
                    for (String usr : cleanUserSkills) {
                        boolean matched = false;
                        for (String req : companySkillTokens) {
                            if (AISkillGapServlet.isSkillMatch(req, usr)) {
                                matched = true;
                                if (!job.matchedSkills.contains(req)) {
                                    job.matchedSkills.add(req);
                                }
                            }
                        }
                        if (!matched && job.role != null && job.role.toLowerCase().contains(usr.toLowerCase())) {
                            matched = true;
                            if (!job.matchedSkills.contains(usr)) {
                                job.matchedSkills.add(usr);
                            }
                        }
                        if (matched) {
                            userSkillsMatchedCount++;
                        }
                    }

                    for (String req : companySkillTokens) {
                        boolean hasMatched = false;
                        for (String m : job.matchedSkills) {
                            if (AISkillGapServlet.isSkillMatch(req, m)) {
                                hasMatched = true;
                                break;
                            }
                        }
                        if (!hasMatched && !job.missingSkills.contains(req)) {
                            job.missingSkills.add(req);
                        }
                    }

                    job.matchCount = userSkillsMatchedCount;

                    // Generate Learning Topics & Curated Resources for missing skills
                    populateSkillGapsAndResources(job);

                    // Calculate overall match score (0-100)
                    job.matchScore = AISkillGapServlet.calculateMatchScore(
                            cleanUserSkills,
                            companySkillTokens,
                            job.matchedSkills,
                            targetRole,
                            job.role,
                            userExp,
                            job.experience
                    );

                    job.matchTierClass = (job.matchScore >= 80) ? "match-high" : (job.matchScore >= 60 ? "match-medium" : "match-low");
                    job.matchTierLabel = (job.matchScore >= 80) ? "High Match" : (job.matchScore >= 60 ? "Good Match" : "Developing Fit");

                    // Filter by skill tokens / target role
                    if (!cleanUserSkills.isEmpty()) {
                        boolean roleMatches = !targetRole.isEmpty() && job.role != null && job.role.toLowerCase().contains(targetRole.toLowerCase());
                        if (job.matchCount > 0 || roleMatches) {
                            results.add(job);
                        }
                    } else if (!targetRole.isEmpty()) {
                        if (job.role != null && job.role.toLowerCase().contains(targetRole.toLowerCase())) {
                            results.add(job);
                        }
                    } else {
                        results.add(job);
                    }
                }
            }

            // SORTING & PRIORITIZATION:
            // 1. isEligible DESC: Eligible companies always rank above Not Eligible companies.
            // 2. matchCount DESC: Jobs matching more candidate skills appear FIRST.
            // 3. matchScore DESC: Rank by overall score.
            // 4. id ASC: Deterministic order.
            Collections.sort(results, new Comparator<JobCard>() {
                @Override
                public int compare(JobCard a, JobCard b) {
                    if (a.isEligible != b.isEligible) {
                        return a.isEligible ? -1 : 1;
                    }
                    if (b.matchCount != a.matchCount) {
                        return Integer.compare(b.matchCount, a.matchCount);
                    }
                    if (b.matchScore != a.matchScore) {
                        return Integer.compare(b.matchScore, a.matchScore);
                    }
                    return Integer.compare(a.id, b.id);
                }
            });

        } catch (Exception e) {
            System.err.println("[JobMatchingService] Error querying matching jobs: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Strict Hard-Rule Evaluation for Qualification and Branch Eligibility (Phase 2).
     */
    public static void evaluateEligibility(JobCard job, String userDegree, String userBranch) {
        if (userDegree == null || userDegree.trim().isEmpty()) {
            userDegree = "B.Tech / B.E.";
        }
        if (userBranch == null || userBranch.trim().isEmpty()) {
            userBranch = "Computer Science & Engineering";
        }

        String uDeg = userDegree.trim().toLowerCase();
        String uBr = userBranch.trim().toLowerCase();
        String reqDeg = job.requiredDegree != null ? job.requiredDegree.trim().toLowerCase() : "any";
        String elBr = job.eligibleBranches != null ? job.eligibleBranches.trim().toLowerCase() : "all";

        boolean degreeOk = true;
        boolean branchOk = true;
        String degReason = "";
        String brReason = "";

        // 1. Degree Evaluation
        if (!reqDeg.contains("any") && !reqDeg.contains("all")) {
            // Check if diploma candidate applying for bachelor/master role
            if (uDeg.contains("diploma") && !reqDeg.contains("diploma")) {
                degreeOk = false;
                degReason = "Requires Bachelor's Degree (" + job.requiredDegree + "), but candidate profile has Diploma";
            } else if ((uDeg.contains("bca") || uDeg.contains("b.sc")) && !reqDeg.contains("bca") && !reqDeg.contains("b.sc") && !reqDeg.contains("any bachelor")) {
                if (reqDeg.contains("b.tech") || reqDeg.contains("b.e.") || reqDeg.contains("m.tech")) {
                    degreeOk = false;
                    degReason = "Requires " + job.requiredDegree + "; candidate has " + userDegree;
                }
            }
        }

        // 2. Branch Evaluation
        if (!elBr.contains("all") && !elBr.contains("any")) {
            boolean matchesBranch = false;

            // Check CSE / IT match
            if ((uBr.contains("computer") || uBr.contains("cse") || uBr.contains("information") || uBr.contains("it"))
                    && (elBr.contains("computer") || elBr.contains("cse") || elBr.contains("information") || elBr.contains("it"))) {
                matchesBranch = true;
            }

            // Check Electronics / ECE match
            if ((uBr.contains("electronics") || uBr.contains("ece") || uBr.contains("communication"))
                    && (elBr.contains("electronics") || elBr.contains("ece") || elBr.contains("communication") || elBr.contains("embedded"))) {
                matchesBranch = true;
            }

            // Check Electrical / EEE match
            if ((uBr.contains("electrical") || uBr.contains("eee"))
                    && (elBr.contains("electrical") || elBr.contains("eee"))) {
                matchesBranch = true;
            }

            // Check Data Science / AI match
            if ((uBr.contains("data") || uBr.contains("ai") || uBr.contains("artificial"))
                    && (elBr.contains("data") || elBr.contains("ai") || elBr.contains("computer") || elBr.contains("information"))) {
                matchesBranch = true;
            }

            // Check Mechanical Engineering match
            if (uBr.contains("mechanical") && (elBr.contains("mechanical") || elBr.contains("all engineering"))) {
                matchesBranch = true;
            }

            // Check Civil Engineering match
            if (uBr.contains("civil") && (elBr.contains("civil") || elBr.contains("all engineering"))) {
                matchesBranch = true;
            }

            if (!matchesBranch) {
                branchOk = false;
                brReason = "Eligible branches: " + job.eligibleBranches + "; your profile branch: " + userBranch;
            }
        }

        job.isEligible = degreeOk && branchOk;
        job.missingQualification = degReason;
        job.missingBranch = brReason;

        if (job.isEligible) {
            job.eligibilityExplanation = "✅ Eligible: Your qualification (" + userDegree + ") and branch (" + userBranch + ") satisfy the opening criteria.";
        } else {
            if (!degreeOk && !branchOk) {
                job.eligibilityExplanation = "⚠️ Not Eligible: " + degReason + " & " + brReason;
            } else if (!branchOk) {
                job.eligibilityExplanation = "⚠️ Not Eligible (Branch Mismatch): Role is restricted to [" + job.eligibleBranches + "]. Your profile indicates " + userBranch + ".";
            } else {
                job.eligibilityExplanation = "⚠️ Not Eligible (Degree Mismatch): " + degReason + ".";
            }
        }
    }

    /**
     * Enriches missing skills with structured learning topics and recommended resources (Phase 6).
     */
    public static void populateSkillGapsAndResources(JobCard job) {
        for (String missing : job.missingSkills) {
            String lower = missing.trim().toLowerCase();
            SkillGuide guide = SKILL_RESOURCES.get(lower);

            if (guide == null) {
                for (Map.Entry<String, SkillGuide> entry : SKILL_RESOURCES.entrySet()) {
                    if (lower.contains(entry.getKey()) || entry.getKey().contains(lower)) {
                        guide = entry.getValue();
                        break;
                    }
                }
            }

            if (guide != null) {
                if (!job.recommendedTopics.contains(guide.topic)) {
                    job.recommendedTopics.add(missing + ": " + guide.topic);
                }
                if (!job.learningResources.contains(guide.resource)) {
                    job.learningResources.add(missing + " → " + guide.resource);
                }
                if ("High".equals(guide.priority)) {
                    job.learningPriority = "High Priority";
                }
            } else {
                job.recommendedTopics.add(missing + ": Core syntax, API reference & hands-on application");
                job.learningResources.add(missing + " → Official Documentation & freeCodeCamp");
            }
        }
    }

    /**
     * Converts a list of JobCards to rich JSON format for dynamic client rendering.
     */
    public static String toJson(List<JobCard> jobs, List<String> userSkills, String targetRole, String userExp) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"count\": ").append(jobs.size()).append(",\n");
        sb.append("  \"userSkills\": ").append(listToJsonArray(userSkills)).append(",\n");
        sb.append("  \"jobs\": [\n");

        for (int i = 0; i < jobs.size(); i++) {
            JobCard j = jobs.get(i);
            sb.append("    {\n");
            sb.append("      \"id\": ").append(j.id).append(",\n");
            sb.append("      \"companyName\": ").append(escapeJson(j.companyName)).append(",\n");
            sb.append("      \"role\": ").append(escapeJson(j.role)).append(",\n");
            sb.append("      \"skills\": ").append(escapeJson(j.skills)).append(",\n");
            sb.append("      \"salary\": ").append(escapeJson(j.salary)).append(",\n");
            sb.append("      \"displaySalary\": ").append(escapeJson(j.displaySalary)).append(",\n");
            sb.append("      \"experience\": ").append(escapeJson(j.experience)).append(",\n");
            sb.append("      \"description\": ").append(escapeJson(j.description)).append(",\n");
            sb.append("      \"logo\": ").append(escapeJson(j.logo)).append(",\n");
            sb.append("      \"applyUrl\": ").append(escapeJson(j.applyUrl)).append(",\n");
            sb.append("      \"requiredDegree\": ").append(escapeJson(j.requiredDegree)).append(",\n");
            sb.append("      \"eligibleBranches\": ").append(escapeJson(j.eligibleBranches)).append(",\n");
            sb.append("      \"minQualification\": ").append(escapeJson(j.minQualification)).append(",\n");
            sb.append("      \"location\": ").append(escapeJson(j.location)).append(",\n");
            sb.append("      \"isEligible\": ").append(j.isEligible).append(",\n");
            sb.append("      \"eligibilityExplanation\": ").append(escapeJson(j.eligibilityExplanation)).append(",\n");
            sb.append("      \"missingQualification\": ").append(escapeJson(j.missingQualification)).append(",\n");
            sb.append("      \"missingBranch\": ").append(escapeJson(j.missingBranch)).append(",\n");
            sb.append("      \"matchedSkills\": ").append(listToJsonArray(j.matchedSkills)).append(",\n");
            sb.append("      \"missingSkills\": ").append(listToJsonArray(j.missingSkills)).append(",\n");
            sb.append("      \"recommendedTopics\": ").append(listToJsonArray(j.recommendedTopics)).append(",\n");
            sb.append("      \"learningResources\": ").append(listToJsonArray(j.learningResources)).append(",\n");
            sb.append("      \"learningPriority\": ").append(escapeJson(j.learningPriority)).append(",\n");
            sb.append("      \"matchCount\": ").append(j.matchCount).append(",\n");
            sb.append("      \"matchScore\": ").append(j.matchScore).append(",\n");
            sb.append("      \"matchTierClass\": ").append(escapeJson(j.matchTierClass)).append(",\n");
            sb.append("      \"matchTierLabel\": ").append(escapeJson(j.matchTierLabel)).append("\n");
            sb.append("    }");
            if (i < jobs.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    private static String listToJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(escapeJson(list.get(i)));
            if (i < list.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
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
        sb.append("\"");
        return sb.toString();
    }
}
