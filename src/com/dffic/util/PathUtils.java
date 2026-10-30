package com.dffic.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * PathUtils class
 * Provides utility methods for file path operations.
 * Handles path normalization, validation, and manipulation.
 */
public class PathUtils {

    /**
     * Private constructor to prevent instantiation.
     * All methods in this class are static utility methods.
     */
    private PathUtils() {
        throw new AssertionError("PathUtils cannot be instantiated");
    }

    /**
     * Normalize a file path by resolving any relative components.
     * Converts backslashes to forward slashes for consistency.
     *
     * @param path The file path to normalize
     * @return Normalized absolute path
     */
    public static String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        Path pathObj = Paths.get(path);
        Path normalized = pathObj.toAbsolutePath().normalize();
        return normalized.toString().replace("\\", "/");
    }

    /**
     * Check if a path points to an existing directory.
     *
     * @param path The path to check
     * @return true if path exists and is a directory
     */
    public static boolean isDirectory(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    /**
     * Check if a path points to an existing file.
     *
     * @param path The path to check
     * @return true if path exists and is a file
     */
    public static boolean isFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    /**
     * Check if a path is a symbolic link.
     *
     * @param path The path to check
     * @return true if path is a symbolic link
     */
    public static boolean isSymbolicLink(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        Path pathObj = Paths.get(path);
        return Files.isSymbolicLink(pathObj);
    }

    /**
     * Get the parent directory path for a given path.
     *
     * @param path The file or directory path
     * @return Parent directory path or empty string if none exists
     */
    public static String getParentPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        Path pathObj = Paths.get(path);
        Path parent = pathObj.getParent();
        return (parent != null) ? parent.toString() : "";
    }

    /**
     * Get the file name from a full path.
     *
     * @param path The full file path
     * @return File name or empty string if none exists
     */
    public static String getFileName(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        Path pathObj = Paths.get(path);
        Path fileName = pathObj.getFileName();
        return (fileName != null) ? fileName.toString() : "";
    }

    /**
     * Get the file extension from a file name or path.
     * Returns everything after the last dot, or empty string if none.
     *
     * @param path The file name or full path
     * @return Extension without the dot
     */
    public static String getExtension(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        String fileName = getFileName(path);
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }

    /**
     * Get the file name without the extension.
     *
     * @param path The file name or full path
     * @return File name without extension
     */
    public static String getFileNameWithoutExtension(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        String fileName = getFileName(path);
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
    }

    /**
     * Check if a path is within the specified root directory.
     * This helps detect directory traversal attempts.
     *
     * @param path     The path to check
     * @param rootPath The root directory path
     * @return true if path is within root
     */
    public static boolean isWithinRoot(String path, String rootPath) {
        String normalizedPath = normalizePath(path);
        String normalizedRoot = normalizePath(rootPath);

        // Ensure root ends with separator for proper comparison
        if (!normalizedRoot.endsWith("/")) {
            normalizedRoot += "/";
        }

        return normalizedPath.startsWith(normalizedRoot);
    }

    /**
     * Get a relative path from a root directory to a file.
     *
     * @param rootPath The root directory path
     * @param filePath The file path
     * @return Relative path or empty string if calculation fails
     */
    public static String getRelativePath(String rootPath, String filePath) {
        try {
            Path root = Paths.get(normalizePath(rootPath));
            Path file = Paths.get(normalizePath(filePath));
            return root.relativize(file).toString();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
