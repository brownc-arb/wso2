package com.alrayan.wso2.vasco;

/**
 * Interface defining the behaviour of VASCO commands.
 *
 * @param <T> return type of the execution
 * @since 1.0.0
 */
public interface VASCOCommand<T> {

    /**
     * Executes the VASCO command and returns a string containing the result from the VASCO command execution.
     *
     * @return result of the VASCO command execution
     * @throws VASCOException thrown when error on executing the VASCO command
     */
    T execute() throws VASCOException;
}
