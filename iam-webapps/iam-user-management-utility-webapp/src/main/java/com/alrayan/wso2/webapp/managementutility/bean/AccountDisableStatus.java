package com.alrayan.wso2.webapp.managementutility.bean;

import com.alrayan.wso2.webapp.managementutility.utils.WebAppUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * CBSA User active status response.
 *
 * @since 1.0.0
 */
@XmlRootElement(name = "status")
public class AccountDisableStatus {

        private String disabled;

    /**
     * Returns whether the user is active or not.
     *
     * @return {@code true} if the user is active, {@code false} otherwise
     */
    @XmlElement()
    public String getDisabled() {
        if (WebAppUtils.isValidBoolean(disabled)) {
            return disabled;
        }
        throw new IllegalArgumentException("Invalid boolean " + disabled);
    }

    /**
     * Sets whether the user is active or not.
     *
     * @param disabled user active status
     */
    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }
}
