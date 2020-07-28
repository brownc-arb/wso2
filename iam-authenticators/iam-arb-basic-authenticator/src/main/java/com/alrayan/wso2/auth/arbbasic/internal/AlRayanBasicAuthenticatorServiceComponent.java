package com.alrayan.wso2.auth.arbbasic.internal;

import com.alrayan.wso2.auth.arbbasic.AlRayanBasicAuthenticator;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OSGi service component responsible for activating Al Rayan Basic authenticator.
 *
 * @since 1.0.0
 */
@Component(name = "com.alrayan.wso2.auth.arbbasic.AlRayanBasicAuthenticator",
           service = AlRayanBasicAuthenticatorServiceComponent.class,
           immediate = true)
public class AlRayanBasicAuthenticatorServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(AlRayanBasicAuthenticatorServiceComponent.class);
    private static RealmService realmService;

    /**
     * Sets the realm service.
     *
     * @param realmService realm service
     */
    @Reference(name = "realm.service", service = RealmService.class,
               policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MANDATORY,
               unbind = "unSetRealmService")
    protected void setRealmService(RealmService realmService) {
        log.debug("Setting the Realm Service.");
        AlRayanBasicAuthenticatorServiceComponent.realmService = realmService;
    }

    /**
     * Un sets the realm service.
     *
     * @param realmService realm service
     */
    protected void unSetRealmService(RealmService realmService) {
        log.debug("UnSetting the Realm Service.");
        AlRayanBasicAuthenticatorServiceComponent.realmService = null;
    }

    /**
     * Returns the realm service instance.
     *
     * @return realm service instance
     */
    public static RealmService getRealmService() {
        return realmService;
    }

    /**
     * Activates Al Rayan basic authenticator.
     *
     * @param context component context
     */
    @Activate
    protected void activate(ComponentContext context) {
        try {
            ApplicationAuthenticator applicationAuthenticator = new AlRayanBasicAuthenticator();
            context.getBundleContext()
                    .registerService(ApplicationAuthenticator.class.getName(), applicationAuthenticator, null);
            log.debug("Al Rayan basic authenticator bundle is activated.");
        } catch (Exception e) {
            log.error("Error while activating Al Rayan basic authenticator.", e);
        }
    }

    /**
     * Deactivates Al Rayan basic authenticator.
     *
     * @param context component context
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("Al Rayan basic authenticator bundle deactivated.");
    }
}
