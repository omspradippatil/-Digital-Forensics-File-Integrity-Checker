package com.dffic.cli;

import com.dffic.database.DatabaseConnection;
import com.dffic.database.ForensicsRepository;
import com.dffic.hashing.Sha256HashService;
import com.dffic.model.*;
import com.dffic.scanner.DirectoryScanner;
import com.dffic.service.IntegrityChecker;
import com.dffic.util.InputValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * ConsoleMenu class
 * Provides the command-line interface for the Digital Forensics application.
 * Handles all user interactions and displays results.
 */
public class ConsoleMenu {

    private final DatabaseConnection dbConnection;
    private final ForensicsRepository repository;
    private final DirectoryScanner scanner;
    private final Sha256HashService hashService;
    private final IntegrityChecker integrityChecker;
    private final BufferedReader reader;

    private boolean running;

    /**
     * Constructor for ConsoleMenu.
     *
     * @throws Exception If initialization fails
     */
    public ConsoleMenu() throws Exception {
        this.dbConnection = new DatabaseConnection();
        this.repository = new ForensicsRepository(dbConnection);
        this.scanner = new DirectoryScanner(false); // Don't follow symlinks
        this.hashService = new Sha256HashService();
        this.integrityChecker = new IntegrityChecker(repository, hashService);
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.running = true;
    }

    /**
     * Start the main menu loop.
     */
    public void start() {
        displayWelcome();

        while (running) {
            try {
                displayMainMenu();
                int choice = getMenuChoice();

                switch (choice) {
                    case 1:
                        scanNewDirectory();
                        break;
                    case 2:
                        verifyFileIntegrity();
                        break;
                    case 3:
                        viewAllScannedFiles();
                        break;
                    case 4:
                        viewModifiedFiles();
                        break;
                    case 5:
                        viewDeletedFiles();
                        break;
                    case 6:
                        viewScanHistory();
                        break;
                    case 7:
                        exit();
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 7.");
                }

                System.out.println(); // Blank line for readability

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                System.out.println("Press Enter to continue...");
                try {
                    reader.readLine();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Display welcome message.
     */
    private void displayWelcome() {
        System.out.println();
        System.out.println("==========================================");
        System.out.println(" DIGITAL FORENSICS & FILE INTEGRITY CHECKER");
        System.out.println("==========================================");
        System.out.println();
        System.out.println("Stage 1 - Core MVP");
        System.out.println("Version 1.0.0");
        System.out.println();

        // Test database connection
        System.out.print("Testing database connection... ");
        if (dbConnection.testConnection()) {
            System.out.println("SUCCESS");
        } else {
            System.out.println("FAILED");
            System.out.println("Warning: Database connection failed. Some features may not work.");
            System.out.println("Please check your database configuration in config/database.properties");
        }
        System.out.println();
    }

    /**
     * Display the main menu.
     */
    private void displayMainMenu() {
        System.out.println("==========================================");
        System.out.println(" DIGITAL FORENSICS & FILE INTEGRITY CHECKER");
        System.out.println("==========================================");
        System.out.println();
        System.out.println("1. Scan New Directory");
        System.out.println("2. Verify File Integrity");
        System.out.println("3. View All Scanned Files");
        System.out.println("4. View Modified Files");
        System.out.println("5. View Deleted Files");
        System.out.println("6. View Scan History");
        System.out.println("7. Exit");
        System.out.println();
        System.out.println("==========================================");
        System.out.print("Enter your choice: ");
    }

    /**
     * Get menu choice from user.
     *
     * @return User's menu choice
     */
    private int getMenuChoice() {
        try {
            String input = reader.readLine();
            if (InputValidator.isInteger(input)) {
                return Integer.parseInt(input);
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Option 1: Scan a new directory.
     */
    private void scanNewDirectory() {
        System.out.println("\n--- SCAN NEW DIRECTORY ---");
        System.out.print("Enter directory path (or 'cancel' to return): ");

        String directoryPath;
        try {
            directoryPath = reader.readLine().trim();
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return;
        }

        if (directoryPath.equalsIgnoreCase("cancel")) {
            System.out.println("Scan cancelled.");
            return;
        }

        if (!InputValidator.isValidDirectory(directoryPath)) {
            System.err.println("Error: Invalid directory path or directory does not exist.");
            return;
        }

        try {
            System.out.println("\nScanning directory: " + directoryPath);
            System.out.println("Please wait...");

            // Perform scan
            ScanResult scanResult = scanner.scanDirectory(directoryPath);

            // Check if scan is complete
            if (!scanResult.isComplete()) {
                System.err.println("\nWarning: Scan incomplete. Some files could not be read:");
                for (String error : scanResult.getErrorMessages()) {
                    System.err.println("  - " + error);
                }
                System.out.print("\nDo you want to continue with the partial scan? (yes/no): ");

                String confirm = reader.readLine().trim();
                if (!confirm.equalsIgnoreCase("yes")) {
                    System.out.println("Scan cancelled.");
                    return;
                }
            }

            // Create scan record
            int scanId = repository.createScan(directoryPath);
            System.out.println("\nScan ID: " + scanId);

            // Check if this is the first scan
            boolean isFirstScan = !repository.hasPreviousScans(directoryPath);

            if (isFirstScan) {
                System.out.println("This is the first scan. Establishing baseline...");
            } else {
                System.out.println("Comparing with previous baseline...");
            }

            // Compare with baseline
            List<ChangeRecord> changeRecords = integrityChecker.compareWithBaseline(
                    scanResult, directoryPath, scanId);

            // Save results
            repository.saveScanResults(directoryPath, scanId, changeRecords);

            // Calculate summary
            int[] counts = integrityChecker.calculateSummaryCounts(changeRecords);
            int added = counts[0];
            int modified = counts[1];
            int unchanged = counts[2];
            int deleted = counts[3];

            // Display summary
            System.out.println("\n========================================");
            System.out.println("           SCAN SUMMARY");
            System.out.println("========================================");
            System.out.println("Scan ID:          " + scanId);
            System.out.println("Directory:        " + directoryPath);
            System.out.println("Total Files:      " + scanResult.getFileCount());
            System.out.println("Scan Duration:    " + scanResult.getScanDurationString());
            System.out.println("----------------------------------------");
            System.out.println("Added:            " + added);
            System.out.println("Modified:         " + modified);
            System.out.println("Unchanged:        " + unchanged);
            System.out.println("Deleted:          " + deleted);
            System.out.println("========================================");

            // Display detailed changes if not too many
            if (changeRecords.size() <= 20) {
                System.out.println("\n--- DETAILED RESULTS ---");
                for (ChangeRecord record : changeRecords) {
                    System.out.printf("[%s] %s%n", record.getChangeType(), record.getFilePath());
                }
            } else {
                System.out.println("\nUse 'View All Scanned Files' to see detailed results.");
            }

            System.out.println("\nScan completed successfully.");

        } catch (Exception e) {
            System.err.println("Scan failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Option 2: Verify file integrity for an existing directory.
     */
    private void verifyFileIntegrity() {
        System.out.println("\n--- VERIFY FILE INTEGRITY ---");
        System.out.println("This option re-scans a previously scanned directory.");
        System.out.print("Enter directory path (or 'cancel' to return): ");

        String directoryPath;
        try {
            directoryPath = reader.readLine().trim();
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return;
        }

        if (directoryPath.equalsIgnoreCase("cancel")) {
            System.out.println("Verification cancelled.");
            return;
        }

        try {
            // Check if directory was previously scanned
            if (!repository.hasPreviousScans(directoryPath)) {
                System.out.println("No previous scans found for this directory.");
                System.out.println("Use 'Scan New Directory' to perform the first scan.");
                return;
            }

            // Perform verification scan
            System.out.println("\nRe-scanning directory: " + directoryPath);
            ScanResult scanResult = scanner.scanDirectory(directoryPath);

            if (!scanResult.isComplete()) {
                System.err.println("Warning: Scan incomplete. Some files could not be read.");
            }

            int scanId = repository.createScan(directoryPath);
            List<ChangeRecord> changeRecords = integrityChecker.compareWithBaseline(
                    scanResult, directoryPath, scanId);
            repository.saveScanResults(directoryPath, scanId, changeRecords);

            int[] counts = integrityChecker.calculateSummaryCounts(changeRecords);

            System.out.println("\nVerification complete.");
            System.out.println("Added: " + counts[0] + " | Modified: " + counts[1] +
                    " | Unchanged: " + counts[2] + " | Deleted: " + counts[3]);

        } catch (Exception e) {
            System.err.println("Verification failed: " + e.getMessage());
        }
    }

    /**
     * Option 3: View all scanned files for a directory.
     */
    private void viewAllScannedFiles() {
        System.out.println("\n--- VIEW ALL SCANNED FILES ---");
        System.out.print("Enter directory path (or 'cancel' to return): ");

        String directoryPath;
        try {
            directoryPath = reader.readLine().trim();
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return;
        }

        if (directoryPath.equalsIgnoreCase("cancel")) {
            return;
        }

        try {
            List<FileRecord> files = repository.getAllFiles(directoryPath);

            if (files.isEmpty()) {
                System.out.println("No files found for this directory.");
                return;
            }

            System.out.println("\nTotal files: " + files.size());
            System.out.println("----------------------------------------");
            System.out.printf("%-40s %-15s %-10s%n", "File Name", "Status", "Size");
            System.out.println("----------------------------------------");

            for (FileRecord file : files) {
                System.out.printf("%-40s %-15s %-10s%n",
                        truncate(file.getFileName(), 40),
                        file.getFileStatus(),
                        file.getFormattedFileSize());
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Option 4: View modified files.
     */
    private void viewModifiedFiles() {
        System.out.println("\n--- VIEW MODIFIED FILES ---");
        System.out.print("Enter directory path (or 'cancel' to return): ");

        String directoryPath;
        try {
            directoryPath = reader.readLine().trim();
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return;
        }

        if (directoryPath.equalsIgnoreCase("cancel")) {
            return;
        }

        try {
            List<FileRecord> files = repository.getModifiedFiles(directoryPath);

            if (files.isEmpty()) {
                System.out.println("No modified files found.");
                return;
            }

            System.out.println("\nModified files: " + files.size());
            System.out.println("----------------------------------------");

            for (FileRecord file : files) {
                System.out.println("File: " + file.getFileName());
                System.out.println("Path: " + file.getFilePath());
                System.out.println("Hash: " + file.getFileHash());
                System.out.println("Updated: " + file.getUpdatedAt());
                System.out.println("----------------------------------------");
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Option 5: View deleted files.
     */
    private void viewDeletedFiles() {
        System.out.println("\n--- VIEW DELETED FILES ---");
        System.out.print("Enter directory path (or 'cancel' to return): ");

        String directoryPath;
        try {
            directoryPath = reader.readLine().trim();
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return;
        }

        if (directoryPath.equalsIgnoreCase("cancel")) {
            return;
        }

        try {
            List<FileRecord> files = repository.getDeletedFiles(directoryPath);

            if (files.isEmpty()) {
                System.out.println("No deleted files found.");
                return;
            }

            System.out.println("\nDeleted files: " + files.size());
            System.out.println("----------------------------------------");

            for (FileRecord file : files) {
                System.out.println("File: " + file.getFileName());
                System.out.println("Path: " + file.getFilePath());
                System.out.println("Last Hash: " + file.getFileHash());
                System.out.println("Marked Deleted: " + file.getUpdatedAt());
                System.out.println("----------------------------------------");
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Option 6: View scan history.
     */
    private void viewScanHistory() {
        System.out.println("\n--- VIEW SCAN HISTORY ---");
        System.out.print("Enter directory path (or leave blank for all scans): ");

        String directoryPath;
        try {
            directoryPath = reader.readLine().trim();
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return;
        }

        try {
            List<ScanRecord> scans;
            if (directoryPath.isEmpty()) {
                scans = repository.getAllScans(20);
            } else {
                scans = repository.getScanHistory(directoryPath, 20);
            }

            if (scans.isEmpty()) {
                System.out.println("No scan history found.");
                return;
            }

            System.out.println("\nScan History:");
            System.out.println("========================================");

            for (ScanRecord scan : scans) {
                System.out.println(scan.format());
                System.out.println("----------------------------------------");
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Option 7: Exit the application.
     */
    private void exit() {
        System.out.println("\nExiting Digital Forensics & File Integrity Checker...");
        System.out.println("Goodbye!");
        running = false;

        try {
            reader.close();
        } catch (IOException e) {
            // Ignore close errors
        }
    }

    /**
     * Truncate a string to a maximum length.
     *
     * @param str    String to truncate
     * @param length Maximum length
     * @return Truncated string
     */
    private String truncate(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }
}
