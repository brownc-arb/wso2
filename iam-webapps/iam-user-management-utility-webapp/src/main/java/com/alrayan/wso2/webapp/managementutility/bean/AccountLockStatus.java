package com.alrayan.wso2.webapp.managementutility.bean;

import com.alrayan.wso2.webapp.managementutility.utils.WebAppUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean class to maintain the account lock status.
 *
 * @since 1.0.0
 */
@XmlRootElement(name = "accountLockStatus")
public class AccountLockStatus {

    private String locked;

    /**
     * Returns the account lock status.
     *
     * @return account lock status
     */
    @XmlElement()
    public String getLocked() {
        if (WebAppUtils.isValidBoolean(locked)) {
            return locked;
        }
        throw new IllegalArgumentException("Invalid boolean " + locked);
    }

    /**
     * Sets the account lock status.
     *
     * @param locked account lock status
     */
    public void setLocked(String locked) {
        this.locked = locked;
    }
}
