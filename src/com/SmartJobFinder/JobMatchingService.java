package com.SmartJobFinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

        public List<String> matchedSkills = new ArrayList<>();
        public List<String> missingSkills = new ArrayList<>();
        public int matchCount = 0;
        public int matchScore = 0;
        public String matchTierClass = "match-low";
        public String matchTierLabel = "Developing Fit";

        public JobCard() {}
    }

    /**
     * Matches and ranks company openings based on selected user skills, target role, and experience.
     * When userSkills are provided, strictly prioritizes jobs matching more skills first (matchCount DESC).
     * Excludes jobs with 0 matching skills unless no skills are specified at all.
     */
    public static List<JobCard> findMatchingJobs(List<String> userSkills, String targetRole, String userExp, String userSalary) {
        List<JobCard> results = new ArrayList<>();

        if (userSkills == null) userSkills = new ArrayList<>();
        if (targetRole == null) targetRole = "";
        if (userExp == null) userExp = "1";
        if (userSalary == null) userSalary = "";

        // Clean user skills
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

                    // Tokenize required skills for this opening
                    List<String> companySkillTokens = AISkillGapServlet.cleanTokens(job.skills);

                    // Check which user skills match this job
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
                        // Also check if user skill directly matches the job role (e.g. "Python" -> "Python Developer")
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

                    // Determine missing required skills
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

                    // FILTERING RULE:
                    // If the user selected one or more skills, ONLY include jobs that match at least 1 skill
                    // (Or if target role was specified and role matches closely)
                    if (!cleanUserSkills.isEmpty()) {
                        boolean roleMatches = !targetRole.isEmpty() && job.role != null && job.role.toLowerCase().contains(targetRole.toLowerCase());
                        if (job.matchCount > 0 || roleMatches) {
                            results.add(job);
                        }
                    } else {
                        // If no skills are selected at all, include the job (zero-filter baseline)
                        results.add(job);
                    }
                }
            }

            // SORTING & PRIORITIZATION:
            // 1. matchCount DESC: Jobs matching more of the user's selected skills appear FIRST.
            // 2. matchScore DESC: For jobs matching the same count of skills, rank by matchScore.
            // 3. id ASC: Deterministic order for identical scores.
            Collections.sort(results, new Comparator<JobCard>() {
                @Override
                public int compare(JobCard a, JobCard b) {
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
     * Converts a list of JobCards to a JSON string array for AJAX responses.
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
            sb.append("      \"matchedSkills\": ").append(listToJsonArray(j.matchedSkills)).append(",\n");
            sb.append("      \"missingSkills\": ").append(listToJsonArray(j.missingSkills)).append(",\n");
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
