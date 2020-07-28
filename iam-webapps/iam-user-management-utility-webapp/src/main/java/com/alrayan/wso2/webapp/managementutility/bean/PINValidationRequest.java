package com.alrayan.wso2.webapp.managementutility.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean class to handle PIN validation code.
 *
 * @since 1.0.0
 */
@XmlRootElement(name = "pinValidationRequest")
public class PINValidationRequest {

    private String pIN;
    private String pinCodePositions;

    /**
     * Returns the PIN code.
     *
     * @return PIN code
     */
    @XmlElement()
    public String getpIN() {
        return pIN;
    }

    /**
     * Sets the PIN code.
     *
     * @param pIN PIN code
     */
    public void setpIN(String pIN) {
        this.pIN = pIN;
    }

    /**
     * Returns the PIN code position.
     *
     * @return PIN code position
     */
    @XmlElement()
    public String getPinCodePositions() {
        return pinCodePositions;
    }

    /**
     * Sets the PIN code validation positions.
     *
     * @param pinCodePositions PIN code validation positions
     */
    public void setPinCodePositions(String pinCodePositions) {
        this.pinCodePositions = pinCodePositions;
    }
}
