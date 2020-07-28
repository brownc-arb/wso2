package com.alrayan.wso2.webapp.managementutility.utils;

/**
 * Utility class to handle common functionality related to web app.
 *
 * @since 1.0.0
 */
public class WebAppUtils {

    /**
     * Validates the given boolean string.
     * <p>
     * Allowed values are null, true and false
     *
     * @param booleanValue boolean value to validate
     * @return {@code true} if the boolean is valid, {@code false} otherwise
     */
    public static boolean isValidBoolean(String booleanValue) {
        if (booleanValue == null) {
            return true;
        }
        return booleanValue.equalsIgnoreCase("false") || booleanValue.equalsIgnoreCase("true");
    }
}
