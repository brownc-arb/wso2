package com.alrayan.wso2.webapp.management.client.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlElement;

/**
 * Al Rayan User active status response.
 *
 * @since 1.0.0
 */
@JsonRootName(value = "status")
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ValueResponse {

    private String strResponse;
    private int intResponse;
    @XmlElement()
    public String getStrResponse() {
        return strResponse;
    }

    public void setStrResponse(String strResponse) {
        this.strResponse = strResponse;
    }
    @XmlElement()
    public int getIntResponse() {
        return intResponse;
    }

    public void setIntResponse(int intResponse) {
        this.intResponse = intResponse;
    }

    /**
     * Builds and returns an instance of {@link ValueResponse}.
     *
     * @return built {@link UserResponse} instance
     */
    public ValueResponse build() {
        ValueResponse valueResponse = new ValueResponse();
        valueResponse.setStrResponse(strResponse);
        valueResponse.setIntResponse(intResponse);
        return valueResponse;
    }
}
