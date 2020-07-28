package com.alrayan.wso2.vasco.model;

/**
 * This class is responsible for holding the result of the secure challenge result.
 *
 * @since 1.0.0
 */
public class SecureChallengeResult {

    private final String challengeKey;
    private final String requestMessage;
    private boolean isError = false;
    private String errorMessage = "Unknown error when obtaining secure challenge code.";

    /**
     * Creates an instance of {@link SecureChallengeResult}.
     *
     * @param challengeKey   secure challenge challenge key
     * @param requestMessage secure challenge request message
     */
    public SecureChallengeResult(String challengeKey, String requestMessage) {
        this.challengeKey = challengeKey;
        this.requestMessage = requestMessage;
    }

    /**
     * Returns the challenge key from the secure challenge response.
     *
     * @return challenge key from the secure challenge response
     */
    public String getChallengeKey() {
        return challengeKey;
    }

    /**
     * Returns the request message from the secure challenge response.
     *
     * @return request message from the secure challenge response
     */
    public String getRequestMessage() {
        return requestMessage;
    }

    /**
     * Returns whether the challenge result is an error or not.
     *
     * @return {@code true} if the challenge result is an error, {@code false} otherwise.
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Sets whether the challenge result is an error or not.
     *
     * @param error challenge result status
     */
    public void setError(boolean error) {
        isError = error;
    }

    /**
     * Returns the challenge result error message if {@code isError} is {@code true}.
     *
     * @return challenge result error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the challenge result error message if {@code isError} is {@code true}.
     *
     * @param errorMessage challenge result error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
