package com.SmartJobFinder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecurityUtil {

    /**
     * Computes SHA-256 hash of a string in lowercase hex format.
     */
    public static String hashPassword(String password) {
        if (password == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Checks candidate password against stored password with backward compatibility
     * for existing plaintext records.
     */
    public static boolean verifyPassword(String candidatePassword, String storedPassword) {
        if (candidatePassword == null || storedPassword == null) return false;
        String trimmedCandidate = candidatePassword.trim();
        String trimmedStored = storedPassword.trim();

        // 1. Direct SHA-256 match
        String hashedCandidate = hashPassword(trimmedCandidate);
        if (hashedCandidate.equalsIgnoreCase(trimmedStored)) {
            return true;
        }

        // 2. Legacy plaintext match (for existing demo and user records)
        return trimmedCandidate.equals(trimmedStored);
    }
}
