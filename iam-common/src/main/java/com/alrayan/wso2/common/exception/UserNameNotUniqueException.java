package com.alrayan.wso2.common.exception;

/**
 * Custom exception class to depict username not unique.
 *
 * @since 1.0.0
 */
public class UserNameNotUniqueException extends Exception {

    private static final long serialVersionUID = -3584880470456390036L;

    /**
     * Constructs a new exception with {@code null} as its detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause(Throwable)}.
     */
    public UserNameNotUniqueException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message of the exception
     */
    public UserNameNotUniqueException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of {@code (cause==null ? null :
     * cause.toString())} which typically contains the class and detail message of the {@code cause}.
     *
     * @param cause the cause of the exception
     */
    public UserNameNotUniqueException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message of the exception
     * @param cause   the cause of the exception
     */
    public UserNameNotUniqueException(String message, Throwable cause) {
        super(message, cause);
    }
}
