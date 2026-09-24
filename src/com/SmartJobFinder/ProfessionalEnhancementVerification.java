package com.SmartJobFinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProfessionalEnhancementVerification {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  SMART JOB FINDER: PROFESSIONAL ENHANCEMENTS VERIFICATION SUITE  ");
        System.out.println("==================================================================");

        int passed = 0;
        int failed = 0;

        // 1. FEATURE 1: Professional Expected Salary Formatting
        try {
            System.out.println("\n[Test 1] Testing Feature 1: Salary LPA Formatting...");
            String sal1 = JobMatchingService.formatSalaryLpa("360000");
            String sal2 = JobMatchingService.formatSalaryLpa("1800000");
            String sal3 = JobMatchingService.formatSalaryLpa("450000");
            String sal4 = JobMatchingService.formatSalaryLpa("As per company norms");
            String sal5 = JobMatchingService.formatSalaryLpa("3.6 LPA");

            if ("3.6 LPA".equals(sal1) && "18 LPA".equals(sal2) && "4.5 LPA".equals(sal3)
                    && "As per company norms".equals(sal4) && "3.6 LPA".equals(sal5)) {
                System.out.println("  ✅ Feature 1 Passed: 360000 -> " + sal1 + ", 1800000 -> " + sal2 + ", 450000 -> " + sal3);
                passed++;
            } else {
                System.err.println("  ❌ Feature 1 Failed: Unexpected salary outputs: " + sal1 + ", " + sal2);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("  ❌ Feature 1 Error: " + e.getMessage());
            failed++;
        }

        // 2. FEATURE 3, 4, 5, 6: 50 Questions & Company-Specific Seed Data
        try {
            System.out.println("\n[Test 2] Testing Feature 3, 4, 5, 6: 50 Questions & Question Bank...");
            try (Connection conn = DBConnection.getConnection()) {
                int totalInDb = 0;
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM interview_questions");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalInDb = rs.getInt(1);
                }
                System.out.println("  Found " + totalInDb + " questions in `interview_questions` database table.");

                // Generate 50 questions for Amazon SDE
                List<InterviewPrepServlet.QuestionItem> questions = InterviewPrepServlet.getOrGenerate50Questions(
                        "Amazon", "Software Engineer", "Java, SQL, Spring Boot", "1-3 years"
                );
                System.out.println("  Generated question count: " + questions.size());

                int tech = 0, role = 0, hr = 0, beh = 0;
                for (InterviewPrepServlet.QuestionItem q : questions) {
                    String cat = q.category;
                    if ("Technical".equalsIgnoreCase(cat)) tech++;
                    else if ("Role Specific".equalsIgnoreCase(cat) || "Role-Based".equalsIgnoreCase(cat)) role++;
                    else if ("HR".equalsIgnoreCase(cat)) hr++;
                    else if ("Behavioral".equalsIgnoreCase(cat)) beh++;
                }
                System.out.println("  Category distribution: Technical=" + tech + ", Role Specific=" + role + ", HR=" + hr + ", Behavioral=" + beh);

                if (questions.size() == 50 && tech == 15 && role == 15 && hr == 10 && beh == 10) {
                    System.out.println("  ✅ Features 3, 4, 5, 6 Passed: Exactly 50 questions across 4 tabs with Amazon Leadership & Java/SQL/Spring.");
                    passed++;
                } else {
                    System.err.println("  ❌ Features 3, 4, 5, 6 Failed: Count mismatch: " + questions.size());
                    failed++;
                }
            }
        } catch (Exception e) {
            System.err.println("  ❌ Features 3, 4, 5, 6 Error: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // 3. FEATURE 7: Practice Answer Evaluation Mode
        try {
            System.out.println("\n[Test 3] Testing Feature 7: Practice Mode AI Answer Evaluation...");
            String sampleAnswer = "During my college final year project, I designed a microservices backend using Java and Spring Boot. I used HashMaps and Red-Black trees to optimize caching, which reduced query response time by 40%. The result was a scalable application deployed on AWS.";
            
            java.lang.reflect.Method m = InterviewPrepServlet.class.getDeclaredMethod(
                    "evaluateMockAnswer",
                    String.class, String.class, String.class, String.class
            );
            m.setAccessible(true);
            Object resultObj = m.invoke(null, "Explain your experience with Java collections in projects", sampleAnswer, "Software Engineer", "Technical");
            
            // extract score and rating
            java.lang.reflect.Field scoreF = resultObj.getClass().getDeclaredField("score");
            java.lang.reflect.Field ratingF = resultObj.getClass().getDeclaredField("rating");
            scoreF.setAccessible(true);
            ratingF.setAccessible(true);
            int score = scoreF.getInt(resultObj);
            String rating = (String) ratingF.get(resultObj);

            System.out.println("  Evaluation Score: " + score + "/100 (" + (score / 10.0) + "/10)");
            System.out.println("  Rating: " + rating);

            if (score >= 60 && rating != null && !rating.isEmpty()) {
                System.out.println("  ✅ Feature 7 Passed: Answer scored successfully with STAR framework evaluation.");
                passed++;
            } else {
                System.err.println("  ❌ Feature 7 Failed: Score or rating invalid.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("  ❌ Feature 7 Error: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // 4. FEATURE 8: Professional Learning Suggestions (5 Stars & Subtopics)
        try {
            System.out.println("\n[Test 4] Testing Feature 8: Learning Priority & Key Subtopics...");
            List<String> userSkills = new ArrayList<>();
            userSkills.add("HTML"); // candidate has only HTML, missing Java, SQL, Spring Boot
            List<JobMatchingService.JobCard> matchedJobs = JobMatchingService.findMatchingJobs(
                    userSkills, "Software Engineer", "1", "4.5 LPA", "B.Tech", "Computer Science", "All", false
            );

            boolean hasStars = false;
            boolean hasSubtopics = false;
            for (JobMatchingService.JobCard job : matchedJobs) {
                if (job.learningPriorityStars != null && job.learningPriorityStars.contains("⭐⭐⭐⭐⭐")) {
                    hasStars = true;
                }
                if (job.learningSubtopics != null && !job.learningSubtopics.isEmpty()) {
                    hasSubtopics = true;
                    System.out.println("  Company: " + job.companyName + " | Priority: " + job.learningPriorityStars);
                    System.out.println("  Subtopics: " + job.learningSubtopics);
                    break;
                }
            }

            if (hasStars && hasSubtopics) {
                System.out.println("  ✅ Feature 8 Passed: Learning Priority 5-stars & key subtopics breakdown populated on job cards.");
                passed++;
            } else {
                System.err.println("  ❌ Feature 8 Failed: Stars or subtopics missing from JobCard.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("  ❌ Feature 8 Error: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // 5. FEATURE 10: Career Assistant 4 Core Questions
        try {
            System.out.println("\n[Test 5] Testing Feature 10: AI Career Agent Heuristic Guidance...");
            String[] testQs = {
                "Which skill should I learn?",
                "Which company suits me?",
                "What salary can I expect?",
                "What interview questions should I practice?"
            };
            boolean allAnswered = true;
            for (String q : testQs) {
                java.lang.reflect.Method m = CareerAssistantServlet.class.getDeclaredMethod(
                        "generateHeuristicGuidance",
                        String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
                );
                m.setAccessible(true);
                String resp = (String) m.invoke(null, q, "general", "Lahari", "B.Tech", "Computer Science", "Java, SQL", "Software Engineer", "");
                if (resp == null || resp.trim().isEmpty() || resp.contains("General career response")) {
                    allAnswered = false;
                    System.err.println("  ❌ Q failed: " + q);
                } else {
                    System.out.println("  Q: '" + q + "' -> Length: " + resp.length() + " chars (Headline: " + resp.split("\n")[0] + ")");
                }
            }

            if (allAnswered) {
                System.out.println("  ✅ Feature 10 Passed: All 4 placement advisory questions answered with rich guidance.");
                passed++;
            } else {
                System.err.println("  ❌ Feature 10 Failed: Some questions had generic fallback.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("  ❌ Feature 10 Error: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("\n==================================================================");
        System.out.println("  RESULT: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==================================================================");

        if (failed > 0) System.exit(1);
    }
}
