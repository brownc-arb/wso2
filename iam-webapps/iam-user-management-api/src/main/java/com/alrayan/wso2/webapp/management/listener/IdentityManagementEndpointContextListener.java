package com.alrayan.wso2.webapp.management.listener;

import com.alrayan.wso2.webapp.management.IdentityManagementServiceUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This class loads respective app level configurations.
 */
public class IdentityManagementEndpointContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        IdentityManagementServiceUtil.getInstance().init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
