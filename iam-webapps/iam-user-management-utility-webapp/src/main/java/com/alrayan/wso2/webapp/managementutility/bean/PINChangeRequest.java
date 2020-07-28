package com.alrayan.wso2.webapp.managementutility.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean class to handle change PIN request.
 *
 * @since 1.0.0
 */
@XmlRootElement(name = "pinChangeRequest")
public class PINChangeRequest {

    private String currentPIN;
    private String newPIN;

    /**
     * Returns the current PIN code.
     *
     * @return current PIN code
     */
    @XmlElement()
    public String getCurrentPIN() {
        return currentPIN;
    }

    /**
     * Sets the current PIN code.
     *
     * @param currentPIN current PIN code
     */
    public void setCurrentPIN(String currentPIN) {
        this.currentPIN = currentPIN;
    }

    /**
     * Returns the new PIN code.
     *
     * @return new PIN code
     */
    @XmlElement()
    public String getNewPIN() {
        return newPIN;
    }

    /**
     * Sets the new PIN code.
     *
     * @param newPIN new PIN code
     */
    public void setNewPIN(String newPIN) {
        this.newPIN = newPIN;
    }
}
