package com.dffic.util;

import java.io.File;

/**
 * InputValidator class
 * Provides validation methods for user inputs.
 * Ensures all inputs meet requirements before processing.
 */
public class InputValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private InputValidator() {
        throw new AssertionError("InputValidator cannot be instantiated");
    }

    /**
     * Validate a directory path input.
     *
     * @param path The path to validate
     * @return true if path is valid and points to an existing directory
     */
    public static boolean isValidDirectory(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    /**
     * Validate a file path input.
     *
     * @param path The path to validate
     * @return true if path is valid and points to an existing file
     */
    public static boolean isValidFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    /**
     * Validate a non-empty string input.
     *
     * @param input The string to validate
     * @return true if string is not null and not empty after trimming
     */
    public static boolean isNonEmptyString(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Validate that a number is within a range (inclusive).
     *
     * @param value  The value to check
     * @param min    Minimum allowed value
     * @param max    Maximum allowed value
     * @return true if value is within range
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Validate a menu choice.
     *
     * @param choice The user's menu choice
     * @param min    Minimum valid choice
     * @param max    Maximum valid choice
     * @return true if choice is valid
     */
    public static boolean isValidMenuChoice(int choice, int min, int max) {
        return isInRange(choice, min, max);
    }

    /**
     * Check if string represents a valid integer.
     *
     * @param str The string to check
     * @return true if string can be parsed as integer
     */
    public static boolean isInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if string represents a valid long (for file sizes).
     *
     * @param str The string to check
     * @return true if string can be parsed as long
     */
    public static boolean isLong(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
