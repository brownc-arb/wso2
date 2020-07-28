package com.alrayan.wso2.common.exception;

/**
 * Custom exception class to depict consent validation related failure.
 *
 * @since 1.0.0
 */
public class ConsentValidationException extends Exception {

    /**
     * Constructs a new exception with {@code null} as its detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause(Throwable)}.
     */
    public ConsentValidationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message of the exception
     */
    public ConsentValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message of the exception
     * @param cause   the cause of the exception
     */
    public ConsentValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
