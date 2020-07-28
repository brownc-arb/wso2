package com.alrayan.wso2.webapp.managementutility.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Al Rayan Bank User management response.
 *
 * @since 1.0.0
 */
@XmlRootElement(name = "Result")
public class ResponseBean {

    private String message;

    /**
     * Returns the response message.
     *
     * @return message
     */
    @XmlElement()
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message message
     */
    public ResponseBean setMessage(String message) {
        this.message = message;
        return this;
    }
}
