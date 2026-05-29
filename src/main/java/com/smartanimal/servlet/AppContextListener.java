package com.smartanimal.servlet;

import com.smartanimal.dao.DBConnection;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String dbUrl = sce.getServletContext().getInitParameter("db.url");
        String dbUser = sce.getServletContext().getInitParameter("db.user");
        String dbPassword = sce.getServletContext().getInitParameter("db.password");
        
        System.out.println("Initializing Database Connection parameters from web.xml context parameters...");
        DBConnection.initialize(dbUrl, dbUser, dbPassword);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if necessary
    }
}
