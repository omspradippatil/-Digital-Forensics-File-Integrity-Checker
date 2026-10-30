package com.dffic;

import com.dffic.cli.ConsoleMenu;

/**
 * Main class - Entry point for Digital Forensics & File Integrity Checker
 *
 * This is a console-based Java application for monitoring file integrity.
 * Stage 1 - Core MVP Implementation
 *
 * @author Digital Forensics Team
 * @version 1.0.0
 */
public class Main {

    /**
     * Main entry point for the application.
     *
     * @param args Command line arguments (not used in Stage 1)
     */
    public static void main(String[] args) {
        try {
            // Display startup information
            System.out.println();
            System.out.println("==============================================");
            System.out.println("  Digital Forensics & File Integrity Checker");
            System.out.println("  Version 1.0.0 - Stage 1 Core MVP");
            System.out.println("==============================================");
            System.out.println();

            // Check Java version
            String javaVersion = System.getProperty("java.version");
            System.out.println("Java Version: " + javaVersion);

            // Verify Java 17+
            int majorVersion = getMajorVersion(javaVersion);
            if (majorVersion < 17) {
                System.err.println("Error: This application requires Java 17 or later.");
                System.err.println("Current Java version: " + javaVersion);
                System.exit(1);
            }

            System.out.println("Starting application...");
            System.out.println();

            // Initialize and start the console menu
            ConsoleMenu menu = new ConsoleMenu();
            menu.start();

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Extract major version from Java version string.
     * Handles both old format (1.8.0) and new format (17, 21, etc.)
     *
     * @param versionString Java version string
     * @return Major version number
     */
    private static int getMajorVersion(String versionString) {
        try {
            // Handle new version format (17, 21, etc.)
            if (!versionString.startsWith("1.")) {
                int dotIndex = versionString.indexOf('.');
                if (dotIndex > 0) {
                    return Integer.parseInt(versionString.substring(0, dotIndex));
                }
                return Integer.parseInt(versionString);
            }

            // Handle old version format (1.8.0_301)
            String[] parts = versionString.split("\\.");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }

            return 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
