package com.alrayan.wso2.common.exception;

/**
 * Custom exception class to depict PIN validation failure.
 *
 * @since 1.0.0
 */
public class PINValidationFailedException extends Exception {

    private static final long serialVersionUID = -1561608376565733443L;

    /**
     * Constructs a new exception with {@code null} as its detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause(Throwable)}.
     */
    public PINValidationFailedException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message of the exception
     */
    public PINValidationFailedException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of {@code (cause==null ? null :
     * cause.toString())} which typically contains the class and detail message of the {@code cause}.
     *
     * @param cause the cause of the exception
     */
    public PINValidationFailedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message of the exception
     * @param cause   the cause of the exception
     */
    public PINValidationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
