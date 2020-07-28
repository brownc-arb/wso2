package com.alrayan.wso2.user.core.internal;

import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.user.core.AlRayanUserStoreManager;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OSGi service component responsible for activating Al Rayan user store.
 *
 * @since 1.0.0
 */
@Component(name = "com.alrayan.wso2.user.core.AlRayanUserStoreManager",
           service = AlRayanUserStoreManagerComponent.class,
           immediate = true)
public class AlRayanUserStoreManagerComponent {

    private static final Logger log = LoggerFactory.getLogger(AlRayanUserStoreManagerComponent.class);
    private static RealmService realmService;

    /**
     * Returns the realm service instance.
     *
     * @return realm service instance
     */
    public static RealmService getRealmService() {
        return realmService;
    }

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
        AlRayanUserStoreManagerComponent.realmService = realmService;
    }

    /**
     * Un sets the realm service.
     *
     * @param realmService realm service
     */
    protected void unSetRealmService(RealmService realmService) {
        log.debug("UnSetting the Realm Service.");
        AlRayanUserStoreManagerComponent.realmService = null;
    }

    /**
     * Activates Al Rayan user store manager.
     *
     * @param context component context
     */
    @Activate
    protected void activate(ComponentContext context) {
        // Activate the user store manager.
        activateUserStoreManager(context);
    }

    /**
     * Deactivates Al Rayan user store manager.
     *
     * @param context component context
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("Al Rayan user store manager bundle deactivated.");
    }

    /**
     * Activates the user store manager.
     *
     * @param context bundle component context
     */
    private void activateUserStoreManager(ComponentContext context) {
        try {
            UserStoreManager userStoreManager = new AlRayanUserStoreManager();
            context.getBundleContext()
                    .registerService(UserStoreManager.class.getName(), userStoreManager, null);
            log.debug("Al Rayan psu user store manager is activated.");
        } catch (Throwable e) {
            log.error(AlRayanError.ERROR_WHILE_ACTIVATING_PSU_USER_STORE.getErrorMessageWithCode(), e);
        }
    }
}
