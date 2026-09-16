package com.SmartJobFinder;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static String getFirstEnv(String... names) {
        for (String name : names) {
            String val = System.getenv(name);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return null;
    }

    public static String getJdbcUrl() {
        // 1. Check direct connection URLs (Railway MYSQL_URL, DATABASE_URL, or DB_URL)
        String rawUrl = getFirstEnv("MYSQL_URL", "DATABASE_URL", "DB_URL");
        if (rawUrl != null && !rawUrl.isEmpty()) {
            String cleanUrl = rawUrl;
            if (cleanUrl.startsWith("mysql://")) {
                cleanUrl = "jdbc:" + cleanUrl;
            }
            if (cleanUrl.contains("@")) {
                try {
                    String noJdbc = cleanUrl.startsWith("jdbc:") ? cleanUrl.substring(5) : cleanUrl;
                    URI uri = URI.create(noJdbc);
                    String host = uri.getHost();
                    int port = (uri.getPort() > 0) ? uri.getPort() : 3306;
                    String path = uri.getPath();
                    String db = (path != null && path.length() > 1) ? path.substring(1) : "railway";
                    return "jdbc:mysql://" + host + ":" + port + "/" + db
                            + "?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true";
                } catch (Exception ignore) {}
            }
            if (!cleanUrl.contains("?")) {
                cleanUrl += "?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true";
            }
            return cleanUrl;
        }

        // 2. Check individual host/port/database variables (Railway MYSQLHOST, Render DB_HOST, etc.)
        String host = getFirstEnv("MYSQLHOST", "DB_HOST");
        if (host != null && !host.isEmpty()) {
            String port = getFirstEnv("MYSQLPORT", "DB_PORT");
            String p = (port != null && !port.isEmpty()) ? port : "3306";

            String dbName = getFirstEnv("MYSQLDATABASE", "DB_NAME");
            String d = (dbName != null && !dbName.isEmpty()) ? dbName : "smartjobfinder";

            return "jdbc:mysql://" + host + ":" + p + "/" + d
                    + "?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true";
        }

        // 3. Local MySQL database default fallback
        return "jdbc:mysql://localhost:3306/smartjobfinder"
                + "?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true";
    }

    public static String getUser() {
        String user = getFirstEnv("MYSQLUSER", "DB_USER");
        if (user != null && !user.isEmpty()) {
            return user;
        }

        // Check if user is embedded in MYSQL_URL or DATABASE_URL
        String rawUrl = getFirstEnv("MYSQL_URL", "DATABASE_URL", "DB_URL");
        if (rawUrl != null && rawUrl.contains("@")) {
            try {
                String noJdbc = rawUrl.startsWith("jdbc:") ? rawUrl.substring(5) : rawUrl;
                URI uri = URI.create(noJdbc);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    return userInfo.split(":")[0];
                } else if (userInfo != null && !userInfo.isEmpty()) {
                    return userInfo;
                }
            } catch (Exception ignore) {}
        }

        return "root";
    }

    public static String getPassword() {
        String password = getFirstEnv("MYSQLPASSWORD", "DB_PASSWORD");
        if (password != null && !password.isEmpty()) {
            return password;
        }

        // Check if password is embedded in MYSQL_URL or DATABASE_URL
        String rawUrl = getFirstEnv("MYSQL_URL", "DATABASE_URL", "DB_URL");
        if (rawUrl != null && rawUrl.contains("@")) {
            try {
                String noJdbc = rawUrl.startsWith("jdbc:") ? rawUrl.substring(5) : rawUrl;
                URI uri = URI.create(noJdbc);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    return userInfo.substring(userInfo.indexOf(':') + 1);
                }
            } catch (Exception ignore) {}
        }

        return "";
    }

    private static String cachedWorkingPassword = null;

    public static Connection getConnection() {
        Connection connection = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = getJdbcUrl();
            String user = getUser();
            String password = getPassword();

            // If environment credentials are provided, use them directly
            boolean hasEnvHost = getFirstEnv("MYSQLHOST", "DB_HOST", "MYSQL_URL", "DATABASE_URL", "DB_URL") != null;
            if (hasEnvHost || (password != null && !password.isEmpty())) {
                try {
                    return DriverManager.getConnection(url, user, password);
                } catch (Exception e) {
                    System.err.println("[DBConnection] Primary connection attempt failed: " + e.getMessage());
                }
            }

            // Local fallback with cached working password
            if (cachedWorkingPassword != null) {
                try {
                    return DriverManager.getConnection(url, user, cachedWorkingPassword);
                } catch (Exception ignore) {
                    cachedWorkingPassword = null;
                }
            }

            // Candidate passwords for local development
            String[] candidatePasswords = new String[]{
                password, "", "0000", "lahari0405", "root", "admin", "password", 
                "1234", "123456", "12345678", "root123", "mysql"
            };

            for (String pwd : candidatePasswords) {
                if (pwd == null) continue;
                try {
                    connection = DriverManager.getConnection(url, user, pwd);
                    cachedWorkingPassword = pwd;
                    return connection;
                } catch (Exception ignore) {
                    // Try next candidate
                }
            }

        } catch (Exception e) {
            System.err.println("Database Connection Error: " + e.getMessage());
        }

        return connection;
    }
}