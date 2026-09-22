package com.SmartJobFinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet({"/findJobs", "/api/jobs", "/api/findJobs"})
public class FindJobsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String format = request.getParameter("format");
        String accept = request.getHeader("Accept");
        boolean isJson = "json".equalsIgnoreCase(format) || (accept != null && accept.contains("application/json"));

        if (isJson || request.getParameter("skills") != null || request.getParameter("role") != null || request.getParameter("branch") != null) {
            handleSearch(request, response, isJson);
        } else {
            response.sendRedirect("job-search.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String format = request.getParameter("format");
        String accept = request.getHeader("Accept");
        boolean isJson = "json".equalsIgnoreCase(format) || (accept != null && accept.contains("application/json"));

        handleSearch(request, response, isJson);
    }

    private void handleSearch(HttpServletRequest request, HttpServletResponse response, boolean isJson)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String sessionDegree = (session != null && session.getAttribute("userDegree") != null) ? (String) session.getAttribute("userDegree") : "B.Tech / B.E.";
        String sessionBranch = (session != null && session.getAttribute("userBranch") != null) ? (String) session.getAttribute("userBranch") : "Computer Science & Engineering";

        String skillsInput = request.getParameter("skills");
        String roleInput = request.getParameter("role");
        String experienceInput = request.getParameter("experience");
        String salaryInput = request.getParameter("salary");
        String degreeInput = request.getParameter("degree");
        String branchInput = request.getParameter("branch");
        String locationInput = request.getParameter("location");
        String eligibleOnlyInput = request.getParameter("eligibleOnly");

        String skills = (skillsInput != null) ? skillsInput.trim() : "";
        String role = (roleInput != null) ? roleInput.trim() : "";
        String experience = (experienceInput != null) ? experienceInput.trim() : "1";
        String salary = (salaryInput != null) ? salaryInput.trim() : "";
        String userDegree = (degreeInput != null && !degreeInput.trim().isEmpty()) ? degreeInput.trim() : sessionDegree;
        String userBranch = (branchInput != null && !branchInput.trim().isEmpty()) ? branchInput.trim() : sessionBranch;
        String location = (locationInput != null) ? locationInput.trim() : "";
        boolean eligibleOnly = "true".equalsIgnoreCase(eligibleOnlyInput) || "1".equals(eligibleOnlyInput);

        // Tokenize skills
        List<String> skillTokens = new ArrayList<>();
        if (!skills.isEmpty()) {
            String[] tokens = skills.split("[,;]+");
            for (String t : tokens) {
                String clean = t.trim();
                if (!clean.isEmpty() && !skillTokens.contains(clean)) {
                    skillTokens.add(clean);
                }
            }
        }

        // Fetch prioritized matching jobs via JobMatchingService (Phase 2 & 9)
        List<JobMatchingService.JobCard> matchingJobs = JobMatchingService.findMatchingJobs(
                skillTokens, role, experience, salary, userDegree, userBranch, location, eligibleOnly
        );

        if (isJson) {
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            PrintWriter out = response.getWriter();
            out.print(JobMatchingService.toJson(matchingJobs, skillTokens, role, experience));
            return;
        }

        // Render HTML Output
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("    <meta charset='UTF-8'>");
        out.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("    <title>Matching Jobs - Smart Job Finder</title>");
        out.println("    <link rel='stylesheet' href='css/style.css'>");
        out.println("</head>");
        out.println("<body>");

        out.println("    <header class='app-header'>");
        out.println("        <div class='header-content'>");
        out.println("            <a href='index.html' class='logo-title'>");
        out.println("                <span class='logo-icon'>⚡</span>");
        out.println("                <h1>Smart Job Finder</h1>");
        out.println("            </a>");
        out.println("            <nav class='header-nav-links'>");
        out.println("                <a href='job-search.html' class='nav-link active'>Job Search</a>");
        out.println("                <a href='application-tracker.html' class='nav-link'>📋 Tracker</a>");
        out.println("                <a href='interview-prep.html' class='nav-link'>🎯 Interview Prep</a>");
        out.println("                <a href='career-assistant.html' class='nav-link'>🤖 Career AI</a>");
        out.println("                <a href='profile.html' class='nav-link'>👤 Profile</a>");
        out.println("            </nav>");
        out.println("            <div class='header-actions'>");
        out.println("                <a href='job-search.html' class='btn btn-secondary btn-sm'>← Modify Search</a>");
        out.println("                <a href='logout' class='btn btn-outline btn-sm'>Sign Out</a>");
        out.println("            </div>");
        out.println("        </div>");
        out.println("    </header>");

        out.println("    <div class='main-container'>");

        // Search Criteria Summary Card
        out.println("        <div class='search-summary-card'>");
        out.println("            <div class='summary-header'>");
        out.println("                <h2>Candidate Profile & Search Preferences</h2>");
        out.println("                <span class='summary-profile-pill'>🎓 " + escapeHtml(userDegree) + " · " + escapeHtml(userBranch) + "</span>");
        out.println("            </div>");
        out.println("            <div class='summary-tags'>");
        if (!skills.isEmpty()) {
            out.println("                <div class='summary-item'><span class='label'>Skills:</span> <span class='value-tag'>" + escapeHtml(skills) + "</span></div>");
        }
        if (!role.isEmpty()) {
            out.println("                <div class='summary-item'><span class='label'>Target Role:</span> <span class='value-tag'>" + escapeHtml(role) + "</span></div>");
        }
        if (!experience.isEmpty()) {
            out.println("                <div class='summary-item'><span class='label'>Experience:</span> <span class='value-tag'>" + escapeHtml(experience) + " Years</span></div>");
        }
        if (!salary.isEmpty()) {
            out.println("                <div class='summary-item'><span class='label'>Expected Salary:</span> <span class='value-tag'>₹" + escapeHtml(salary) + " / yr</span></div>");
        }
        if (!location.isEmpty()) {
            out.println("                <div class='summary-item'><span class='label'>Location:</span> <span class='value-tag'>📍 " + escapeHtml(location) + "</span></div>");
        }
        out.println("                <div class='summary-item'><span class='label'>Eligibility Filter:</span> <span class='value-tag'>" + (eligibleOnly ? "Eligible Only" : "All Results") + "</span></div>");
        out.println("            </div>");
        out.println("        </div>");

        out.println("        <div class='results-section-header' style='display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; margin-bottom: 20px;'>");
        out.println("            <div>");
        out.println("                <h2>Verified Opportunities (" + matchingJobs.size() + " Found)</h2>");
        out.println("                <p style='font-size:13px; color:var(--text-muted); margin-top:4px;'>Hard eligibility verified against degree & branch. Soft skills ranked by compatibility score.</p>");
        out.println("            </div>");
        out.println("            <div style='display:flex; gap:10px; align-items:center;'>");
        out.println("                <a href='application-tracker.html' class='btn btn-outline btn-sm'>📋 View My Applications</a>");
        out.println("                <a href='job-search.html' class='btn btn-secondary btn-sm'>🔍 Adjust Filters</a>");
        out.println("            </div>");
        out.println("        </div>");

        out.println("        <div class='job-cards-grid'>");

        for (JobMatchingService.JobCard job : matchingJobs) {
            out.println("            <div class='job-card " + (!job.isEligible ? "job-card-ineligible" : "") + "'>");
            out.println("                <div class='job-card-top'>");
            out.println("                    <div class='logo-wrapper'>");
            out.println("                        <img src='" + job.logo + "' alt='" + escapeHtml(job.companyName) + " Logo' class='company-logo' onerror=\"this.onerror=null;this.src='images/default-company.svg';\">");
            out.println("                    </div>");
            out.println("                    <div class='company-info'>");
            out.println("                        <div style='display:flex; justify-content:space-between; align-items:flex-start; gap:8px;'>");
            out.println("                            <h3 class='company-name'>" + escapeHtml(job.companyName) + "</h3>");
            out.println("                            <div style='display:flex; gap:6px; align-items:center; flex-wrap:wrap;'>");
            // Phase 2 Eligibility Badge
            if (job.isEligible) {
                out.println("                                <span class='eligibility-badge eligible-badge'>✅ Eligible</span>");
            } else {
                out.println("                                <span class='eligibility-badge not-eligible-badge' title='" + escapeHtml(job.eligibilityExplanation) + "'>⚠️ Not Eligible</span>");
            }
            out.println("                                <div class='ai-match-card-badge " + job.matchTierClass + "' title='" + job.matchScore + "% Compatibility with your profile'>");
            out.println("                                    <span class='ai-badge-sparkle'>✨</span>");
            out.println("                                    <span class='ai-badge-score'>" + job.matchScore + "%</span>");
            out.println("                                </div>");
            out.println("                            </div>");
            out.println("                        </div>");
            out.println("                        <h4 class='job-role'>" + escapeHtml(job.role) + "</h4>");
            out.println("                    </div>");
            out.println("                </div>");

            // Match Meter Fill
            out.println("                <div class='ai-match-meter'>");
            out.println("                    <div class='ai-match-meter-fill " + job.matchTierClass + "' style='width: " + job.matchScore + "%;'></div>");
            out.println("                </div>");

            // Phase 2 Eligibility Explanation Banner
            out.println("                <div class='eligibility-detail-banner " + (job.isEligible ? "eligible-banner" : "ineligible-banner") + "'>");
            out.println("                    <span class='eligibility-banner-icon'>" + (job.isEligible ? "✓" : "!") + "</span>");
            out.println("                    <span class='eligibility-banner-text'>" + escapeHtml(job.eligibilityExplanation) + "</span>");
            out.println("                </div>");

            out.println("                <div class='job-meta-badges'>");
            out.println("                    <span class='badge badge-salary'>💰 ₹" + job.displaySalary + " / yr</span>");
            if (job.experience != null && !job.experience.isEmpty()) {
                out.println("                    <span class='badge badge-exp'>💼 " + escapeHtml(job.experience) + " yrs exp</span>");
            }
            out.println("                    <span class='badge badge-location'>📍 " + escapeHtml(job.location) + "</span>");
            out.println("                    <span class='badge badge-degree'>🎓 " + escapeHtml(job.requiredDegree) + "</span>");
            out.println("                </div>");

            if (job.description != null && !job.description.trim().isEmpty()) {
                out.println("                <p class='job-description-text'>" + escapeHtml(job.description.trim()) + "</p>");
            }

            // Skills Breakdown: Matching vs Recommended to Learn
            out.println("                <div class='skills-container'>");
            if (!job.matchedSkills.isEmpty()) {
                out.println("                    <div class='skills-summary-block'>");
                out.println("                        <div class='skills-summary-header matched-header'>");
                out.println("                            <span>✅ Matched Strengths (" + job.matchedSkills.size() + ")</span>");
                out.println("                        </div>");
                out.println("                        <div class='skills-tags-wrapper'>");
                for (String m : job.matchedSkills) {
                    out.println("                            <span class='skill-chip skill-chip-matched'>" + escapeHtml(m) + "</span>");
                }
                out.println("                        </div>");
                out.println("                    </div>");
            }

            if (!job.missingSkills.isEmpty()) {
                out.println("                    <div class='skills-summary-block'>");
                out.println("                        <div class='skills-summary-header gap-header'>");
                out.println("                            <span>💡 Recommended to Learn (" + job.missingSkills.size() + ")</span>");
                out.println("                        </div>");
                out.println("                        <div class='skills-tags-wrapper'>");
                for (String g : job.missingSkills) {
                    out.println("                            <span class='skill-chip skill-chip-gap'>" + escapeHtml(g) + "</span>");
                }
                out.println("                        </div>");
                out.println("                    </div>");
            }
            out.println("                </div>");

            // Recommended Learning Topics Preview (Phase 6)
            if (!job.recommendedTopics.isEmpty()) {
                out.println("                <div class='learning-topics-box'>");
                out.println("                    <div class='learning-topics-title'>📚 Beginner-Friendly Learning Topics:</div>");
                out.println("                    <ul class='learning-topics-list'>");
                int maxTopics = Math.min(3, job.recommendedTopics.size());
                for (int i = 0; i < maxTopics; i++) {
                    out.println("                        <li>" + escapeHtml(job.recommendedTopics.get(i)) + "</li>");
                }
                out.println("                    </ul>");
                out.println("                </div>");
            }

            // Card Action Buttons: AI Skill Analysis, Practice Interview, Save Job, Apply Now
            out.println("                <div class='job-card-actions'>");
            out.println("                    <button type='button' class='btn btn-ai-analysis'");
            out.println("                            data-id='" + job.id + "'");
            out.println("                            data-company='" + escapeHtml(job.companyName) + "'");
            out.println("                            data-role='" + escapeHtml(job.role) + "'");
            out.println("                            data-skills='" + escapeHtml(job.skills) + "'");
            out.println("                            data-exp='" + escapeHtml(job.experience) + "'");
            out.println("                            data-salary='" + escapeHtml(job.displaySalary) + "'");
            out.println("                            data-logo='" + escapeHtml(job.logo) + "'");
            out.println("                            data-score='" + job.matchScore + "'");
            out.println("                            data-url='" + escapeHtml(job.applyUrl) + "'");
            out.println("                            data-degree='" + escapeHtml(job.requiredDegree) + "'");
            out.println("                            data-branches='" + escapeHtml(job.eligibleBranches) + "'");
            out.println("                            data-location='" + escapeHtml(job.location) + "'");
            out.println("                            data-eligible='" + job.isEligible + "'");
            out.println("                            data-explanation='" + escapeHtml(job.eligibilityExplanation) + "'");
            out.println("                            data-desc='" + escapeHtml(job.description) + "'");
            out.println("                            data-user-skills='" + escapeHtml(skills) + "'");
            out.println("                            data-user-degree='" + escapeHtml(userDegree) + "'");
            out.println("                            data-user-branch='" + escapeHtml(userBranch) + "'");
            out.println("                            onclick='openJobWorkflowModal(this)'>");
            out.println("                        🚀 Apply Now Workflow");
            out.println("                    </button>");

            out.println("                    <a href='interview-prep.html?companyId=" + job.id + "&role=" + java.net.URLEncoder.encode(job.role, "UTF-8") + "&skills=" + java.net.URLEncoder.encode(job.skills, "UTF-8") + "' class='btn btn-secondary btn-sm'>");
            out.println("                        🎯 Interview Prep");
            out.println("                    </a>");

            out.println("                    <button type='button' class='btn btn-outline btn-sm' onclick='quickSaveApplication(" + job.id + ", \"" + escapeHtml(job.companyName) + "\", \"" + escapeHtml(job.role) + "\", \"" + escapeHtml(job.applyUrl) + "\")'>");
            out.println("                        📌 Save");
            out.println("                    </button>");
            out.println("                </div>");
            out.println("            </div>");
        }

        out.println("        </div>");

        if (matchingJobs.isEmpty()) {
            out.println("        <div class='no-results-card'>");
            out.println("            <div class='empty-icon'>🔍</div>");
            out.println("            <h3>No Matching Companies Found</h3>");
            out.println("            <p>We couldn't find openings matching your selected filters. Try removing some skills or adjusting branch/degree filters.</p>");
            out.println("            <div class='empty-actions'>");
            out.println("                <a href='job-search.html' class='btn btn-primary'>Modify Search Criteria</a>");
            out.println("            </div>");
            out.println("        </div>");
        }

        out.println("    </div>");

        // Include Apply Now & Job Workflow Modal
        renderWorkflowModalMarkup(out);

        // Include AI Skill Modal
        renderAiSkillModalMarkup(out);

        out.println("    <script src='js/script.js'></script>");
        out.println("</body>");
        out.println("</html>");
    }

    private void renderWorkflowModalMarkup(PrintWriter out) {
        out.println("    <!-- Phase 3 Apply Now Complete Workflow Modal -->");
        out.println("    <div id='applyWorkflowModalOverlay' class='ai-modal-overlay' style='display: none;'>");
        out.println("        <div class='ai-modal-card' style='max-width: 720px;' role='dialog' aria-modal='true'>");
        out.println("            <button type='button' class='ai-modal-close' onclick='closeJobWorkflowModal()'>✕</button>");
        out.println("            <div class='ai-modal-header'>");
        out.println("                <div class='ai-modal-logo-wrap'>");
        out.println("                    <img id='workflowLogo' src='images/default-company.svg' alt='Company Logo' class='ai-modal-logo'>");
        out.println("                </div>");
        out.println("                <div class='ai-modal-header-text'>");
        out.println("                    <h3 id='workflowCompanyName'>Company Name</h3>");
        out.println("                    <p id='workflowRole'>Job Role</p>");
        out.println("                    <div id='workflowEligibilityPill' class='eligibility-badge eligible-badge'>✅ Eligible</div>");
        out.println("                </div>");
        out.println("            </div>");
        out.println("            <div class='ai-modal-body' style='max-height: 65vh; overflow-y: auto;'>");
        out.println("                <!-- Job Overview Grid -->");
        out.println("                <div class='workflow-meta-grid'>");
        out.println("                    <div class='meta-box'><strong>💰 Compensation:</strong> <span id='workflowSalary'>Competitive</span></div>");
        out.println("                    <div class='meta-box'><strong>💼 Experience:</strong> <span id='workflowExp'>0-2 yrs</span></div>");
        out.println("                    <div class='meta-box'><strong>📍 Location:</strong> <span id='workflowLocation'>Pan India</span></div>");
        out.println("                    <div class='meta-box'><strong>🎓 Degree:</strong> <span id='workflowDegree'>B.Tech</span></div>");
        out.println("                    <div class='meta-box' style='grid-column: span 2;'><strong>🏛️ Eligible Branches:</strong> <span id='workflowBranches'>All</span></div>");
        out.println("                </div>");
        out.println("                <div style='margin-top: 16px;'>");
        out.println("                    <strong>📋 Role Overview:</strong>");
        out.println("                    <p id='workflowDescription' style='font-size: 14px; color: var(--text-muted); margin-top: 4px;'></p>");
        out.println("                </div>");
        out.println("                <!-- Application Checklist (Phase 3) -->");
        out.println("                <div class='workflow-checklist-card'>");
        out.println("                    <h4>📋 Pre-Application Checklist</h4>");
        out.println("                    <ul class='checklist-items'>");
        out.println("                        <li><label><input type='checkbox' checked> Verified degree and branch eligibility criteria</label></li>");
        out.println("                        <li><label><input type='checkbox' checked> Reviewed required technical skills and compatibility score</label></li>");
        out.println("                        <li><label><input type='checkbox'> Updated resume tailored with relevant project highlights</label></li>");
        out.println("                        <li><label><input type='checkbox'> Prepared official transcripts and portfolio links</label></li>");
        out.println("                        <li><label><input type='checkbox'> Ready to complete application on the official company careers portal</label></li>");
        out.println("                    </ul>");
        out.println("                </div>");
        out.println("                <!-- External Application Warning -->");
        out.println("                <div class='workflow-warning-box'>");
        out.println("                    <span style='font-size: 20px;'>🛡️</span>");
        out.println("                    <div>");
        out.println("                        <strong>Official Portal Notice:</strong><br>");
        out.println("                        Please complete your application on the official company website. Smart Job Finder guides you to verified portals and does not submit external forms on your behalf.");
        out.println("                    </div>");
        out.println("                </div>");
        out.println("                <!-- Return & Record in Application Tracker -->");
        out.println("                <div class='workflow-tracker-box'>");
        out.println("                    <h4>📌 Track this Application in Your Portal</h4>");
        out.println("                    <div class='form-grid-2'>");
        out.println("                        <div class='form-group'>");
        out.println("                            <label>Application Status</label>");
        out.println("                            <select id='workflowStatusSelect' class='form-control'>");
        out.println("                                <option value='Applied' selected>Applied</option>");
        out.println("                                <option value='Saved'>Saved for Later</option>");
        out.println("                                <option value='Interview'>Interview Scheduled</option>");
        out.println("                                <option value='Shortlisted'>Shortlisted</option>");
        out.println("                            </select>");
        out.println("                        </div>");
        out.println("                        <div class='form-group'>");
        out.println("                            <label>Follow-up Reminder Date</label>");
        out.println("                            <input type='date' id='workflowFollowUpDate' class='form-control'>");
        out.println("                        </div>");
        out.println("                    </div>");
        out.println("                    <div class='form-group'>");
        out.println("                        <label>Requisition Notes / Job ID</label>");
        out.println("                        <input type='text' id='workflowNotes' class='form-control' placeholder='e.g. Requisition #12345, applied via campus portal'>");
        out.println("                    </div>");
        out.println("                </div>");
        out.println("            </div>");
        out.println("            <div class='ai-modal-footer'>");
        out.println("                <button type='button' class='btn btn-secondary' onclick='closeJobWorkflowModal()'>Close</button>");
        out.println("                <button type='button' class='btn btn-primary' onclick='saveFromWorkflowModal()'>💾 Save in Application Tracker</button>");
        out.println("                <a id='workflowApplyBtn' href='#' target='_blank' rel='noopener noreferrer' class='btn btn-apply' onclick='handleExternalPortalRedirect()'>");
        out.println("                    Proceed to Official Company Careers Portal ↗");
        out.println("                </a>");
        out.println("            </div>");
        out.println("        </div>");
        out.println("    </div>");
    }

    private void renderAiSkillModalMarkup(PrintWriter out) {
        out.println("    <!-- AI Skill Analysis Interactive Modal -->");
        out.println("    <div id='aiSkillModalOverlay' class='ai-modal-overlay' style='display: none;'>");
        out.println("        <div class='ai-modal-card' role='dialog' aria-modal='true'>");
        out.println("            <button type='button' class='ai-modal-close' onclick='closeAiSkillModal()' title='Close (Esc)'>✕</button>");
        out.println("            <div class='ai-modal-header'>");
        out.println("                <div class='ai-modal-logo-wrap'>");
        out.println("                    <img id='modalCompanyLogo' src='images/default-company.svg' alt='Company Logo' class='ai-modal-logo'>");
        out.println("                </div>");
        out.println("                <div class='ai-modal-header-text'>");
        out.println("                    <h3 id='modalCompanyName'>Company Name</h3>");
        out.println("                    <p id='modalCompanyRole'>Job Role</p>");
        out.println("                    <div class='ai-modal-meta-row'>");
        out.println("                        <span id='modalMetaExp' class='meta-item'>💼 Exp</span>");
        out.println("                        <span id='modalMetaSalary' class='meta-item'>💰 Salary</span>");
        out.println("                        <span id='modalRoleFitBadge' class='role-fit-badge'>🎯 Strong Fit</span>");
        out.println("                    </div>");
        out.println("                </div>");
        out.println("                <div class='ai-modal-score-wrap'>");
        out.println("                    <div id='modalScoreValue' class='score-number'>85%</div>");
        out.println("                    <div class='score-label'>Match Fit</div>");
        out.println("                </div>");
        out.println("            </div>");
        out.println("            <div id='modalLoadingState' class='ai-modal-loading' style='display: none;'>");
        out.println("                <div class='ai-spinner'></div>");
        out.println("                <p>Generating AI Career Intelligence...</p>");
        out.println("            </div>");
        out.println("            <div id='modalContentArea' class='ai-modal-body'>");
        out.println("                <div class='ai-engine-banner'>");
        out.println("                    <div class='engine-badge'>");
        out.println("                        <span id='modalEngineIcon' class='engine-icon'>✨</span>");
        out.println("                    </div>");
        out.println("                    <div class='engine-info'>");
        out.println("                        <strong id='modalEngineTitle'>Google Gemini 1.5 Flash</strong>");
        out.println("                        <span id='modalEngineNotice'>Contextual generative skill evaluation.</span>");
        out.println("                    </div>");
        out.println("                </div>");
        out.println("                <div class='ai-modal-section'>");
        out.println("                    <div class='ai-section-title'>");
        out.println("                        <span>📋</span> Executive Evaluation &amp; Fit Analysis");
        out.println("                    </div>");
        out.println("                    <p id='modalExplanation' class='ai-explanation-text'></p>");
        out.println("                </div>");
        out.println("                <div class='ai-modal-section'>");
        out.println("                    <div class='ai-skills-grid'>");
        out.println("                        <div class='ai-skills-column column-matched'>");
        out.println("                            <div class='column-header'><span class='dot-icon green'></span> Verified Matching Strengths</div>");
        out.println("                            <div id='modalMatchedSkillsList' class='ai-chips-flow'></div>");
        out.println("                        </div>");
        out.println("                        <div class='ai-skills-column column-missing'>");
        out.println("                            <div class='column-header'><span class='dot-icon purple'></span> High-Priority Skill Gaps</div>");
        out.println("                            <div id='modalMissingSkillsList' class='ai-chips-flow'></div>");
        out.println("                        </div>");
        out.println("                    </div>");
        out.println("                </div>");
        out.println("                <div class='ai-modal-section'>");
        out.println("                    <div class='ai-section-title'>");
        out.println("                        <span>🚀</span> Personalized Learning Roadmap & Action Plan");
        out.println("                    </div>");
        out.println("                    <div id='modalRoadmapList' class='ai-roadmap-list'></div>");
        out.println("                </div>");
        out.println("            </div>");
        out.println("            <div class='ai-modal-footer'>");
        out.println("                <button type='button' class='btn btn-secondary' onclick='closeAiSkillModal()'>Close</button>");
        out.println("                <a id='modalApplyBtn' href='#' target='_blank' rel='noopener noreferrer' class='btn btn-apply'>");
        out.println("                    Apply on Company Portal ↗");
        out.println("                </a>");
        out.println("            </div>");
        out.println("        </div>");
        out.println("    </div>");
    }

    private String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}