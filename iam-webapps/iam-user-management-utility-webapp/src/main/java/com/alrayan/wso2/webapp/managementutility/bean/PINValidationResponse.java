package com.alrayan.wso2.webapp.managementutility.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean class to maintain the result of the PIN validation response.
 *
 * @since 1.0.0
 */
@XmlRootElement(name = "pinValidationResponse")
public class PINValidationResponse {

    private boolean isPinValid;

    /**
     * Returns whether the PIN is valid or not.
     *
     * @return {@code true} if the PIN is valid, {@code false} otherwise
     */
    @XmlElement()
    public boolean isPinValid() {
        return isPinValid;
    }

    /**
     * Sets whether the PIN code is valid or not.
     *
     * @param pinValid PIN validation response
     * @return this {@link PINValidationResponse} instance
     */
    public PINValidationResponse setPinValid(boolean pinValid) {
        isPinValid = pinValid;
        return this;
    }
}
