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
public class UserResponseWithCode {

    private String salesforceId;
    private String activeusername;
    private String resetcode;

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
     * Returns the activeusername of the user.
     *
     * @return activeusername
     */
    @XmlElement()
    public String getActiveusername() {
        return activeusername;
    }

    /**
     * Sets the activeusername.
     *
     * @param activeusername salesforceId
     */
    public void setActiveusername(String activeusername) {
        this.activeusername = activeusername;
    }



    /**
     * Returns the resetcode ID of the user.
     *
     * @return resetcode
     */
    @XmlElement()
    public String getResetcode() {
        return resetcode;
    }

    /**
     * Sets the salesforce Id.
     *
     * @param resetcode salesforceId
     */
    public void setResetcode(String resetcode) {
        this.resetcode = resetcode;
    }


    /**
     * User response object builder.
     *
     * @since 1.0.0
     */
    public static class UserResponseWithCodeBuilder {

        private String salesforceId;
        private String activeusername;
        private String resetcode;


        /**
         * Sets the salesforce Id for the user status builder.
         *
         * @param salesforceId salesforce Id
         * @return this {@link UserResponseWithCodeBuilder} instance
         */
        public UserResponseWithCodeBuilder setSalesforceId(String salesforceId) {
            this.salesforceId = salesforceId;
            return this;
        }

        public UserResponseWithCodeBuilder setActiveUsername(String activeusername) {
            this.activeusername = activeusername;
            return this;
        }

        public UserResponseWithCodeBuilder setResetCode(String resetcode) {
            this.resetcode = resetcode;
            return this;
        }



        /**
         * Builds and returns an instance of {@link UserResponseWithCode}.
         *
         * @return built {@link UserResponseWithCode} instance
         */
        public UserResponseWithCode build() {
            UserResponseWithCode userResponseWithCode = new UserResponseWithCode();
            userResponseWithCode.setSalesforceId(salesforceId);
            userResponseWithCode.setActiveusername(activeusername);
            userResponseWithCode.setResetcode(resetcode);
            return userResponseWithCode;
        }
    }
}
