package com.alrayan.wso2.auth.arbfederated.internal;

import com.alrayan.wso2.auth.arbfederated.authenticator.ARBFederatedAuthenticator;
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
@Component(name = "com.alrayan.wso2.auth.arbfederated.authenticator.ARBFederatedAuthenticator",
           service = ARBFederatedAuthenticatorComponent.class,
           immediate = true)
public class ARBFederatedAuthenticatorComponent {

    private static final Logger log = LoggerFactory.getLogger(ARBFederatedAuthenticatorComponent.class);

    /**
     * Activates Al Rayan ARB federated authenticator bundle.
     *
     * @param context component context
     */
    @Activate
    protected void activate(ComponentContext context) {
        try {
            ARBFederatedAuthenticator arbFederatedAuthenticator = new ARBFederatedAuthenticator();
            Hashtable<String, String> properties = new Hashtable<>();
            context.getBundleContext()
                    .registerService(ApplicationAuthenticator.class.getName(), arbFederatedAuthenticator, properties);
            log.debug("Al Rayan ARB federated authenticator bundle is activated.");
        } catch (Throwable e) {
            log.error("Error while activating Al Rayan ARB federated authenticator bundle.", e);
        }
    }

    /**
     * Deactivates Al Rayan ARB federated authenticator bundle.
     *
     * @param context component context
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("Al Rayan ARB federated authenticator bundle deactivated.");
    }
}
