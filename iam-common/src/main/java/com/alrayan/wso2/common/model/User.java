package com.alrayan.wso2.common.model;

/**
 * Represents the TPP user information.
 *
 * @since 1.0.0
 */
public class User {

    private String firstName;
    private String lastName;
    private String username;
    private String rolesEnrolled;

    /**
     * Returns the first name of the TPP.
     *
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the TPP.
     *
     * @param firstName first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name of the TPP.
     *
     * @return last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the TPP.
     *
     * @param lastName last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the username of the TPP.
     *
     * @return username of the TPP
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the TPP.
     *
     * @param username username of the TPP
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the roles user is enrolled for.
     *
     * @return roles user is enrolled for
     */
    public String getRolesEnrolled() {
        return rolesEnrolled;
    }

    /**
     * Sets the roles user is enrolled for.
     *
     * @param rolesEnrolled roles user is enrolled for
     */
    public void setRolesEnrolled(String rolesEnrolled) {
        this.rolesEnrolled = rolesEnrolled;
    }
}
