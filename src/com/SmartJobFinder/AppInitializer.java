package com.SmartJobFinder;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[SmartJobFinder] Server starting up. Initializing database asynchronously...");
        // Run database initialization in background thread to not delay container boot
        new Thread(() -> {
            int retries = 6;
            while (retries > 0) {
                try {
                    Thread.sleep(2000);
                    java.sql.Connection conn = DBConnection.getConnection();
                    if (conn != null) {
                        conn.close();
                        DatabaseSetup.initializeDatabaseSilently();
                        System.out.println("[SmartJobFinder] ✅ Database auto-initialization completed successfully.");
                        break;
                    } else {
                        System.out.println("[SmartJobFinder] Database not ready yet, retrying... (" + retries + " attempts left)");
                    }
                } catch (Exception e) {
                    System.err.println("[SmartJobFinder] Startup retry notice: " + e.getMessage());
                }
                retries--;
            }
        }).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[SmartJobFinder] Application stopped.");
    }
}
