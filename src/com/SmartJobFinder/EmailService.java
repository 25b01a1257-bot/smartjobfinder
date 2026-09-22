package com.SmartJobFinder;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Sends a welcome confirmation email asynchronously to avoid blocking user registration.
     */
    public static void sendWelcomeEmailAsync(String recipientName, String recipientEmail) {
        executor.submit(() -> {
            try {
                sendWelcomeEmail(recipientName, recipientEmail);
            } catch (Exception e) {
                System.err.println("[EmailService] Background email error for " + recipientEmail + ": " + e.getMessage());
            }
        });
    }

    /**
     * Synchronous email dispatch with comprehensive environment variable fallback and error safety.
     */
    public static boolean sendWelcomeEmail(String recipientName, String recipientEmail) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            return false;
        }

        String host = System.getenv("SMTP_HOST");
        String port = System.getenv("SMTP_PORT");
        String user = System.getenv("SMTP_USER");
        String pass = System.getenv("SMTP_PASSWORD");
        String from = System.getenv("SMTP_FROM");

        // If not configured, provide a safe simulated dispatch so registration is 100% smooth
        if (host == null || host.trim().isEmpty() || user == null || pass == null) {
            System.out.println("[EmailService] (Notice) SMTP credentials not configured in environment (SMTP_HOST / SMTP_USER / SMTP_PASSWORD).");
            System.out.println("[EmailService] Simulated confirmation email dispatched to: " + recipientEmail + " for candidate: " + recipientName);
            return true;
        }

        if (port == null || port.trim().isEmpty()) port = "587";
        if (from == null || from.trim().isEmpty()) from = user;

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host.trim());
            props.put("mail.smtp.port", port.trim());
            props.put("mail.smtp.auth", "true");

            String starttls = System.getenv("SMTP_STARTTLS");
            if (starttls == null || !starttls.equalsIgnoreCase("false")) {
                props.put("mail.smtp.starttls.enable", "true");
            }

            String ssl = System.getenv("SMTP_SSL");
            if ("true".equalsIgnoreCase(ssl) || "465".equals(port.trim())) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.port", port.trim());
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }

            final String authUser = user.trim();
            final String authPass = pass.trim();

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(authUser, authPass);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from.trim(), "Smart Job Finder Career Portal"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail.trim(), recipientName != null ? recipientName.trim() : "Job Seeker"));
            msg.setSubject("Welcome to Smart Job Finder, " + (recipientName != null ? recipientName : "Candidate") + "! Your Account is Confirmed ⚡");

            String htmlBody = buildWelcomeEmailHtml(recipientName, recipientEmail);
            msg.setContent(htmlBody, "text/html; charset=UTF-8");

            Transport.send(msg);
            System.out.println("[EmailService] ✅ Registration confirmation email successfully sent to: " + recipientEmail);
            return true;

        } catch (Exception e) {
            System.err.println("[EmailService] ⚠️ Failed to send SMTP confirmation email to " + recipientEmail + ": " + e.getMessage());
            // Safe degradation: do not break registration
            return false;
        }
    }

    private static String buildWelcomeEmailHtml(String name, String email) {
        String displayName = (name != null && !name.trim().isEmpty()) ? name.trim() : "Future Tech Leader";
        return "<!DOCTYPE html>"
                + "<html><head><meta charset='UTF-8'>"
                + "<style>"
                + "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f1f5f9; margin: 0; padding: 20px; }"
                + ".card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.06); border: 1px solid #e2e8f0; }"
                + ".header { background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%); color: #ffffff; padding: 36px 30px; text-align: center; border-bottom: 4px solid #6366f1; }"
                + ".logo-badge { font-size: 38px; margin-bottom: 8px; }"
                + ".header h1 { margin: 0; font-size: 24px; font-weight: 800; letter-spacing: -0.5px; }"
                + ".header p { margin: 8px 0 0; color: #cbd5e1; font-size: 14px; }"
                + ".body { padding: 32px 30px; color: #334155; line-height: 1.6; font-size: 15px; }"
                + ".feature-box { background: #f8fafc; border-left: 4px solid #6366f1; border-radius: 8px; padding: 16px 20px; margin: 20px 0; }"
                + ".feature-title { font-weight: 700; color: #1e293b; margin-bottom: 6px; display: flex; align-items: center; gap: 8px; }"
                + ".btn-cta { display: inline-block; background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%); color: #ffffff !important; text-decoration: none; padding: 14px 28px; border-radius: 10px; font-weight: 700; font-size: 15px; text-align: center; margin: 20px 0 10px; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.35); }"
                + ".footer { background: #f8fafc; padding: 20px 30px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }"
                + "</style></head><body>"
                + "<div class='card'>"
                + "  <div class='header'>"
                + "    <div class='logo-badge'>⚡</div>"
                + "    <h1>Smart Job Finder</h1>"
                + "    <p>AI-Powered Career Intelligence &amp; Opportunity Matching</p>"
                + "  </div>"
                + "  <div class='body'>"
                + "    <h2>Welcome aboard, " + displayName + "! 🎉</h2>"
                + "    <p>Your registration with <strong>Smart Job Finder</strong> has been successfully confirmed. We are excited to support your professional journey with verified company opportunities and personalized career intelligence.</p>"
                + "    <div class='feature-box'>"
                + "      <div class='feature-title'>📋 What You Can Do Right Now:</div>"
                + "      <ul style='margin: 8px 0; padding-left: 20px;'>"
                + "        <li><strong>Branch &amp; Qualification Matching:</strong> See instantly which tech companies match your degree and branch.</li>"
                + "        <li><strong>AI Skill Gap Analyzer:</strong> Uncover the exact high-priority skills needed to boost your compatibility.</li>"
                + "        <li><strong>Direct Verified Applications:</strong> View job details and proceed to official corporate portals.</li>"
                + "        <li><strong>Application Tracker:</strong> Record and track your applications from Saved to Offer.</li>"
                + "        <li><strong>AI Interview Prep:</strong> Practice technical and behavioral questions tailored to target roles.</li>"
                + "      </ul>"
                + "    </div>"
                + "    <p>Your registered email is: <strong>" + email + "</strong></p>"
                + "    <div style='text-align: center;'>"
                + "      <a href='https://smartjobfinder-web-production.up.railway.app/login.html' class='btn-cta'>Access Your Career Portal →</a>"
                + "    </div>"
                + "  </div>"
                + "  <div class='footer'>"
                + "    <p>© 2026 Smart Job Finder Platform. All rights reserved.<br>Built for ambitious engineers and students pursuing tech excellence.</p>"
                + "  </div>"
                + "</div></body></html>";
    }
}
