package com.alrayan.wso2.auth.arbmobile.internal;


import com.alrayan.wso2.auth.arbmobile.authenticator.ARBMobileAuthenticator;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;

import java.util.Hashtable;

/**
 * OSGi service component responsible for activating ARB federated authenticator.
 *
 * @since 1.0.0
 */
@Component(name = "com.alrayan.wso2.auth.arbmobile.authenticator.ARBMobileAuthenticator",
           service = ARBMobileAuthenticatorComponent.class,
           immediate = true)
public class ARBMobileAuthenticatorComponent {

    private static final Logger log = LoggerFactory.getLogger(ARBMobileAuthenticatorComponent.class);

    /**
     * Activates Al Rayan ARB federated authenticator bundle.
     *
     * @param context component context
     */
    @Activate
    protected void activate(ComponentContext context) {
        try {
            ARBMobileAuthenticator arbMobileAuthenticator = new ARBMobileAuthenticator();
            Hashtable<String, String> properties = new Hashtable<>();
            context.getBundleContext()
                    .registerService(ApplicationAuthenticator.class.getName(), arbMobileAuthenticator, properties);
            log.debug("Al Rayan ARB Mobile authenticator bundle is activated.");
        } catch (Throwable e) {
            log.error("Error while activating Al Rayan ARB Mobile authenticator bundle.", e);
        }
    }

    /**
     * Deactivates Al Rayan ARB federated authenticator bundle.
     *
     * @param context component context
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("Al Rayan ARB Mobile authenticator bundle deactivated.");
    }
}
