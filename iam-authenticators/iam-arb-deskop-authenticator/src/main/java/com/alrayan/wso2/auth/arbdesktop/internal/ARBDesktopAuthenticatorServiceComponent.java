package com.alrayan.wso2.auth.arbdesktop.internal;

import com.alrayan.wso2.auth.arbdesktop.ARBDesktopAuthenticator;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;

import java.util.Hashtable;

/**
 * OSGi service component responsible for activating ARB desktop journey authenticator.
 *
 * @since 1.0.0
 */
@Component(name = "com.alrayan.wso2.auth.arbdesktop.ARBDesktopAuthenticator",
           service = ARBDesktopAuthenticatorServiceComponent.class,
           immediate = true)
public class ARBDesktopAuthenticatorServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(ARBDesktopAuthenticatorServiceComponent.class);

    /**
     * Activates Al Rayan desktop journey authenticator bundle.
     *
     * @param context component context
     */
    @Activate
    protected void activate(ComponentContext context) {
        try {
            ARBDesktopAuthenticator arbDesktopAuthenticator = new ARBDesktopAuthenticator();
            Hashtable<String, String> properties = new Hashtable<>();
            context.getBundleContext()
                    .registerService(ApplicationAuthenticator.class.getName(), arbDesktopAuthenticator, properties);
            log.debug("Al Rayan desktop journey authenticator bundle is activated.");
        } catch (Throwable e) {
            log.error("Error while activating Al Rayan desktop journey authenticator bundle.", e);
        }
    }

    /**
     * Deactivates Al Rayan desktop journey authenticator bundle.
     *
     * @param context component context
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("Al Rayan desktop journey authenticator bundle deactivated.");
    }
}
