package com.alrayan.wso2.common.exception;

/**
 * Custom exception class to depict access token validation failure.
 *
 * @since 1.0.0
 */
public class AccessTokenValidationFailedException extends Exception {

    private static final long serialVersionUID = 3838784131710570854L;

    /**
     * Constructs a new exception with {@code null} as its detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause(Throwable)}.
     */
    public AccessTokenValidationFailedException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message of the exception
     */
    public AccessTokenValidationFailedException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of {@code (cause==null ? null :
     * cause.toString())} which typically contains the class and detail message of the {@code cause}.
     *
     * @param cause the cause of the exception
     */
    public AccessTokenValidationFailedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message of the exception
     * @param cause   the cause of the exception
     */
    public AccessTokenValidationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
