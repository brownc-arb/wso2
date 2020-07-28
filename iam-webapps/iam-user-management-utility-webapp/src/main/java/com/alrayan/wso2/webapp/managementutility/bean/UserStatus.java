package com.alrayan.wso2.webapp.managementutility.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;

/**
 * Al Rayan User active status response.
 *
 * @since 1.0.0
 */
@JsonRootName(value = "status")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserStatus {

    private String salesforceId;
    private String disabled;
    private String locked;

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
     * Returns whether the user is active or not.
     *
     * @return {@code true} if the user is active, {@code false} otherwise
     */
    @XmlElement()
    public String getDisabled() {
        if (isValidBoolean(disabled)) {
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

    /**
     * Returns whether the user is locked or not.
     *
     * @return {@code true} if the user is locked, {@code false} otherwise
     */
    @XmlElement()
    public String getLocked() {
        if (isValidBoolean(locked)) {
            return locked;
        }
        throw new IllegalArgumentException("Invalid boolean " + locked);
    }

    /**
     * Sets whether the user is locked or not.
     *
     * @param locked user lock status
     */
    public void setLocked(String locked) {
        this.locked = locked;
    }

    /**
     * Validates the given boolean string.
     * <p>
     * Allowed values are null, true and false
     *
     * @param booleanValue boolean value to validate
     * @return {@code true} if the boolean is valid, {@code false} otherwise
     */
    private boolean isValidBoolean(String booleanValue) {
        if (booleanValue == null) {
            return true;
        }
        return booleanValue.equalsIgnoreCase("false") || booleanValue.equalsIgnoreCase("true");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserStatus)) {
            return false;
        }
        UserStatus that = (UserStatus) o;
        return Objects.equals(salesforceId, that.salesforceId) &&
               Objects.equals(disabled, that.disabled) &&
               Objects.equals(locked, that.locked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salesforceId, disabled, locked);
    }

    /**
     * User status object builder.
     *
     * @since 1.0.0
     */
    public static class UserStatusBuilder {

        private String salesforceId;
        private String locked;
        private String disabled;

        /**
         * Sets the salesforce Id for the user status builder.
         *
         * @param salesforceId salesforce Id
         * @return this {@link UserStatusBuilder} instance
         */
        public UserStatusBuilder setSalesforceId(String salesforceId) {
            this.salesforceId = salesforceId;
            return this;
        }

        /**
         * Sets the account lock status for the user status builder.
         *
         * @param locked whether the account is locked or not
         * @return this {@link UserStatusBuilder} instance
         */
        public UserStatusBuilder setLocked(String locked) {
            this.locked = locked;
            return this;
        }

        /**
         * Sets the account lock disable for the user status builder.
         *
         * @param disabled whether the account is disabled or not
         * @return this {@link UserStatusBuilder} instance
         */
        public UserStatusBuilder setDisabled(String disabled) {
            this.disabled = disabled;
            return this;
        }

        /**
         * Builds and returns an instance of {@link UserStatus}.
         *
         * @return built {@link UserStatus} instance
         */
        public UserStatus build() {
            UserStatus userStatus = new UserStatus();
            userStatus.setSalesforceId(salesforceId);
            userStatus.setDisabled(disabled);
            userStatus.setLocked(locked);
            return userStatus;
        }
    }
}
