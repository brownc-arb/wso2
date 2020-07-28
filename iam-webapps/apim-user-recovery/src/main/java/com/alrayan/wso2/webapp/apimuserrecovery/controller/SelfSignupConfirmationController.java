package com.alrayan.wso2.webapp.apimuserrecovery.controller;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.webapp.apimuserrecovery.client.UserInformationRecoveryClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Controller class for handelling user signup confirmation.
 *
 * @since 1.0.0
 */
public class SelfSignupConfirmationController extends HttpServlet {

    private UserInformationRecoveryClient client;
    private static final Logger log = LoggerFactory.getLogger(SelfSignupConfirmationController.class);
    private static final long serialVersionUID = 9145038381276887879L;
    private static final String CONFIRMATION = "confirmation";
    private static final String USER_STORE_DOMAIN = "userstoredomain";
    private static final String USER_NAME = "userName";
    private static final String CAPTCHA = "captcha";
    private static final String CAPTCHA_IMAGE_URL = "captchaImageUrl";
    private static final String CAPTCHA_ANSWER = "captchaAnswer";
    private static final String STATUS = "status";

    @Override
    public void init() {
        try {
            client = new UserInformationRecoveryClient();
        } catch (APIManagementException e) {
            log.error("Error on initialising " + UserInformationRecoveryClient.class, e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext context = this.getServletContext();
        String captchaDisable = context.getInitParameter("captchaDisable");
        HttpSession session = request.getSession();
        session.setAttribute(SelfSignupConfirmationController.CONFIRMATION,
                request.getParameter(SelfSignupConfirmationController.CONFIRMATION));
        String userStoreDomain = request.getParameter(SelfSignupConfirmationController.USER_STORE_DOMAIN);
        String username = request.getParameter(SelfSignupConfirmationController.USER_NAME.toLowerCase());
        String tenantDomain = request.getParameter("tenantdomain");
        if (!("PRIMARY".equalsIgnoreCase(userStoreDomain)) && userStoreDomain != null) {
            username = userStoreDomain + "/" + username;
        }
        session.setAttribute(SelfSignupConfirmationController.USER_NAME, username);
        session.setAttribute("tenantdomain", tenantDomain);

        if (!"true".equals(captchaDisable)) {
            String configuredServerURL = AlRayanConfiguration.USER_INFO_RECOVERY_SERVER_URL.getValue();
            APIManagerConfiguration config =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                            .getAPIManagerConfiguration();
            String serverUrl = configuredServerURL.toLowerCase().startsWith("$config{") ?
                               config.getFirstProperty(StringUtils.substring(configuredServerURL, 8, -1)) :
                               StringUtils.substring(configuredServerURL, 8, -1);
            String carbonServerUrl = StringUtils.removeEnd(serverUrl, "services/");
            CaptchaInfoBean bean = client.generateCaptcha();
            session.setAttribute(SelfSignupConfirmationController.CAPTCHA, bean);
            session.setAttribute(SelfSignupConfirmationController.CAPTCHA_IMAGE_URL,
                    carbonServerUrl + bean.getImagePath());
            RequestDispatcher view = request.getRequestDispatcher("signup_confirm.jsp");
            view.forward(request, response);
        } else {
            doPost(request, response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext context = this.getServletContext();
        String captchaDisable = context.getInitParameter("captchaDisable");
        HttpSession session = request.getSession(false);
        String userName = (String) session.getAttribute(SelfSignupConfirmationController.USER_NAME);
        String code = (String) session.getAttribute(SelfSignupConfirmationController.CONFIRMATION);
        String tenantDomain = (String) session.getAttribute("tenantdomain");
        CaptchaInfoBean captcha = null;
        if (!"true".equals(captchaDisable)) {
            captcha = (CaptchaInfoBean) session.getAttribute(SelfSignupConfirmationController.CAPTCHA);
            captcha.setUserAnswer(request.getParameter(SelfSignupConfirmationController.CAPTCHA_ANSWER));
        }
        VerificationBean bean = client.confirmUserSelfRegistration(userName, code, captcha, tenantDomain);
        request.setAttribute(SelfSignupConfirmationController.STATUS, bean);
        RequestDispatcher view = request.getRequestDispatcher("signup_confirm_status.jsp");
        view.forward(request, response);
    }
}
