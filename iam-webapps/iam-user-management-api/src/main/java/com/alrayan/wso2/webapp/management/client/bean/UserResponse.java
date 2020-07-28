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
public class UserResponse {

    private String salesforceId;
    private String activeusername;

    /**
     * Returns the salesforce ID of the user.
     *
     * @return salesforce
     */
    @XmlElement()
    public String getSalesforceId() {
        return salesforceId;
    }

    /**
     * Sets the salesforce Id.
     *
     * @param salesforceId salesforceId
     */
    public void setSalesforceId(String salesforceId) {
        this.salesforceId = salesforceId;
    }

    /**
     * Returns the salesforce ID of the user.
     *
     * @return salesforce
     */
    @XmlElement()
    public String getActiveusername() {
        return activeusername;
    }

    /**
     * Sets the salesforce Id.
     *
     * @param activeusername salesforceId
     */
    public void setActiveusername(String activeusername) {
        this.activeusername = activeusername;
    }


    /**
     * User response object builder.
     *
     * @since 1.0.0
     */
    public static class UserResponseBuilder {

        private String salesforceId;
        private String activeusername;


        /**
         * Sets the salesforce Id for the user status builder.
         *
         * @param salesforceId salesforce Id
         * @return this {@link UserResponseBuilder} instance
         */
        public UserResponseBuilder setSalesforceId(String salesforceId) {
            this.salesforceId = salesforceId;
            return this;
        }

        public UserResponseBuilder setActiveUsername(String activeusername) {
            this.activeusername = activeusername;
            return this;
        }


        /**
         * Builds and returns an instance of {@link UserResponse}.
         *
         * @return built {@link UserResponse} instance
         */
        public UserResponse build() {
            UserResponse userResponse = new UserResponse();
            userResponse.setSalesforceId(salesforceId);
            userResponse.setActiveusername(activeusername);
            return userResponse;
        }
    }
}
