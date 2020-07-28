package com.alrayan.wso2.store.jaggery.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class RootContextRedirect {

    public static final Logger log = LoggerFactory.getLogger(RootContextRedirect.class);

    public static void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {

        System.out.println("checkDetails");
        log.info("Request will be redirected to carbon store");
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/store");
        requestDispatcher.forward(request, response);
    }

}
