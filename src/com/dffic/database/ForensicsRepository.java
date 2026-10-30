package com.dffic.database;

import com.dffic.model.ChangeRecord;
import com.dffic.model.ChangeType;
import com.dffic.model.FileRecord;
import com.dffic.model.ScanRecord;
import com.dffic.model.ScannedFile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ForensicsRepository class
 * Handles all database operations for the Digital Forensics application.
 * Uses JDBC PreparedStatement for secure SQL operations.
 */
public class ForensicsRepository {

    private final DatabaseConnection dbConnection;

    /**
     * Constructor for ForensicsRepository.
     *
     * @param dbConnection Database connection manager
     */
    public ForensicsRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Create a new scan record in the database.
     *
     * @param directoryPath The directory being scanned
     * @return The generated scan ID
     * @throws SQLException If database operation fails
     */
    public int createScan(String directoryPath) throws SQLException {
        String sql = "INSERT INTO scans (directory_path, scan_date, scan_status) VALUES (?, NOW(), 'IN_PROGRESS')";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, directoryPath);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Failed to get generated scan ID");
                }
            }
        }
    }

    /**
     * Mark a scan as completed.
     *
     * @param scanId    The scan ID to complete
     * @param totalFiles Total number of files scanned
     * @throws SQLException If database operation fails
     */
    public void completeScan(int scanId, int totalFiles) throws SQLException {
        String sql = "UPDATE scans SET scan_status = 'COMPLETED', completed_at = NOW(), total_files = ? WHERE scan_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, totalFiles);
            stmt.setInt(2, scanId);
            stmt.executeUpdate();
        }
    }

    /**
     * Mark a scan as failed with an error message.
     *
     * @param scanId       The scan ID to mark as failed
     * @param errorMessage The error message describing the failure
     * @throws SQLException If database operation fails
     */
    public void failScan(int scanId, String errorMessage) throws SQLException {
        String sql = "UPDATE scans SET scan_status = 'FAILED', error_message = ? WHERE scan_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, errorMessage);
            stmt.setInt(2, scanId);
            stmt.executeUpdate();
        }
    }

    /**
     * Get all active file records for a directory.
     *
     * @param rootPath The root directory path
     * @return List of active file records
     * @throws SQLException If database operation fails
     */
    public List<FileRecord> getActiveFiles(String rootPath) throws SQLException {
        String sql = "SELECT * FROM file_records WHERE root_path = ? AND is_active = TRUE";
        List<FileRecord> files = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rootPath);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(mapToFileRecord(rs));
                }
            }
        }

        return files;
    }

    /**
     * Save or update a file record.
     *
     * @param rootPath     Root directory path
     * @param scannedFile  Scanned file information
     * @param scanId       Current scan ID
     * @param changeType   Type of change detected
     * @return The file ID
     * @throws SQLException If database operation fails
     */
    public int saveFileRecord(String rootPath, ScannedFile scannedFile, int scanId, ChangeType changeType) throws SQLException {
        // Check if file record exists
        String checkSql = "SELECT file_id FROM file_records WHERE root_path = ? AND file_path = ?";
        String insertSql = "INSERT INTO file_records (root_path, file_path, file_name, file_size, file_hash, last_modified, last_scan_id, file_status, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE)";
        String updateSql = "UPDATE file_records SET file_size = ?, file_hash = ?, last_modified = ?, last_scan_id = ?, file_status = ?, is_active = TRUE WHERE root_path = ? AND file_path = ?";

        try (Connection conn = dbConnection.getConnection()) {
            // Check for existing record
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, rootPath);
                checkStmt.setString(2, scannedFile.getAbsolutePath());

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Update existing record
                        int fileId = rs.getInt(1);
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setLong(1, scannedFile.getFileSize());
                            updateStmt.setString(2, scannedFile.getFileHash());
                            updateStmt.setTimestamp(3, Timestamp.valueOf(scannedFile.getLastModified()));
                            updateStmt.setInt(4, scanId);
                            updateStmt.setString(5, changeType.toString());
                            updateStmt.setString(6, rootPath);
                            updateStmt.setString(7, scannedFile.getAbsolutePath());
                            updateStmt.executeUpdate();
                        }
                        return fileId;
                    } else {
                        // Insert new record
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                            insertStmt.setString(1, rootPath);
                            insertStmt.setString(2, scannedFile.getAbsolutePath());
                            insertStmt.setString(3, scannedFile.getFileName());
                            insertStmt.setLong(4, scannedFile.getFileSize());
                            insertStmt.setString(5, scannedFile.getFileHash());
                            insertStmt.setTimestamp(6, Timestamp.valueOf(scannedFile.getLastModified()));
                            insertStmt.setInt(7, scanId);
                            insertStmt.setString(8, changeType.toString());
                            insertStmt.executeUpdate();

                            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    return generatedKeys.getInt(1);
                                }
                            }
                        }
                    }
                }
            }
        }

        throw new SQLException("Failed to save file record");
    }

    /**
     * Save a file history record.
     *
     * @param fileId      The file ID (can be null for deleted files)
     * @param scanId      The scan ID
     * @param filePath    The file path
     * @param oldHash     Previous hash value (null for new files)
     * @param newHash     Current hash value (null for deleted files)
     * @param changeType  Type of change
     * @throws SQLException If database operation fails
     */
    public void saveFileHistory(Integer fileId, int scanId, String filePath, String oldHash, String newHash, ChangeType changeType) throws SQLException {
        String sql = "INSERT INTO file_history (file_id, scan_id, file_path, old_hash, new_hash, change_status, change_date) VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (fileId != null) {
                stmt.setInt(1, fileId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, scanId);
            stmt.setString(3, filePath);
            stmt.setString(4, oldHash);
            stmt.setString(5, newHash);
            stmt.setString(6, changeType.toString());

            stmt.executeUpdate();
        }
    }

    /**
     * Mark a file as deleted (inactive).
     *
     * @param rootPath Root directory path
     * @param filePath File path to mark as deleted
     * @param scanId   Current scan ID
     * @throws SQLException If database operation fails
     */
    public void markFileDeleted(String rootPath, String filePath, int scanId) throws SQLException {
        String sql = "UPDATE file_records SET is_active = FALSE, file_status = 'DELETED', last_scan_id = ? WHERE root_path = ? AND file_path = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scanId);
            stmt.setString(2, rootPath);
            stmt.setString(3, filePath);
            stmt.executeUpdate();
        }
    }

    /**
     * Get all file records (active and inactive) for a directory.
     *
     * @param rootPath The root directory path
     * @return List of all file records
     * @throws SQLException If database operation fails
     */
    public List<FileRecord> getAllFiles(String rootPath) throws SQLException {
        String sql = "SELECT * FROM file_records WHERE root_path = ? ORDER BY file_name";
        List<FileRecord> files = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rootPath);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(mapToFileRecord(rs));
                }
            }
        }

        return files;
    }

    /**
     * Get modified files from the most recent scan.
     *
     * @param rootPath The root directory path
     * @return List of modified file records
     * @throws SQLException If database operation fails
     */
    public List<FileRecord> getModifiedFiles(String rootPath) throws SQLException {
        String sql = "SELECT * FROM file_records WHERE root_path = ? AND file_status = 'MODIFIED' AND is_active = TRUE ORDER BY updated_at DESC";
        List<FileRecord> files = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rootPath);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(mapToFileRecord(rs));
                }
            }
        }

        return files;
    }

    /**
     * Get deleted files (marked inactive).
     *
     * @param rootPath The root directory path
     * @return List of deleted file records
     * @throws SQLException If database operation fails
     */
    public List<FileRecord> getDeletedFiles(String rootPath) throws SQLException {
        String sql = "SELECT * FROM file_records WHERE root_path = ? AND is_active = FALSE ORDER BY updated_at DESC";
        List<FileRecord> files = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rootPath);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(mapToFileRecord(rs));
                }
            }
        }

        return files;
    }

    /**
     * Get scan history for a directory.
     *
     * @param rootPath The root directory path
     * @param limit    Maximum number of records to return
     * @return List of scan records
     * @throws SQLException If database operation fails
     */
    public List<ScanRecord> getScanHistory(String rootPath, int limit) throws SQLException {
        String sql = "SELECT * FROM scans WHERE directory_path = ? ORDER BY scan_date DESC LIMIT ?";
        List<ScanRecord> scans = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rootPath);
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    scans.add(mapToScanRecord(rs));
                }
            }
        }

        return scans;
    }

    /**
     * Get all completed scans.
     *
     * @param limit Maximum number of records to return
     * @return List of scan records
     * @throws SQLException If database operation fails
     */
    public List<ScanRecord> getAllScans(int limit) throws SQLException {
        String sql = "SELECT * FROM scans ORDER BY scan_date DESC LIMIT ?";
        List<ScanRecord> scans = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    scans.add(mapToScanRecord(rs));
                }
            }
        }

        return scans;
    }

    /**
     * Save complete scan results in a transaction.
     *
     * @param rootPath      Root directory path
     * @param scanId        Scan ID
     * @param changeRecords List of change records to save
     * @throws SQLException If database operation fails
     */
    public void saveScanResults(String rootPath, int scanId, List<ChangeRecord> changeRecords) throws SQLException {
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Process each change record
            for (ChangeRecord record : changeRecords) {
                switch (record.getChangeType()) {
                    case ADDED:
                    case MODIFIED:
                    case UNCHANGED:
                        // Create ScannedFile from ChangeRecord for saving
                        ScannedFile scannedFile = createScannedFileFromRecord(record);
                        int fileId = saveFileRecord(rootPath, scannedFile, scanId, record.getChangeType());
                        saveFileHistory(fileId, scanId, record.getFilePath(),
                                record.getOldHash(), record.getNewHash(), record.getChangeType());
                        break;

                    case DELETED:
                        // Mark file as deleted
                        markFileDeleted(rootPath, record.getFilePath(), scanId);
                        saveFileHistory(null, scanId, record.getFilePath(),
                                record.getOldHash(), null, ChangeType.DELETED);
                        break;
                }
            }

            // Calculate total files and complete the scan
            int totalFiles = (int) changeRecords.stream()
                    .filter(r -> r.getChangeType() != ChangeType.DELETED)
                    .count();

            completeScan(scanId, totalFiles);

            conn.commit(); // Commit transaction

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            // Mark scan as failed
            failScan(scanId, e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Connection close failed: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Map ResultSet to FileRecord object.
     *
     * @param rs ResultSet from database query
     * @return FileRecord object
     * @throws SQLException If column access fails
     */
    private FileRecord mapToFileRecord(ResultSet rs) throws SQLException {
        return new FileRecord(
                rs.getInt("file_id"),
                rs.getString("root_path"),
                rs.getString("file_path"),
                rs.getString("file_name"),
                rs.getLong("file_size"),
                rs.getString("file_hash"),
                rs.getTimestamp("last_modified"),
                rs.getObject("last_scan_id", Integer.class),
                rs.getString("file_status"),
                rs.getBoolean("is_active"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
    }

    /**
     * Map ResultSet to ScanRecord object.
     *
     * @param rs ResultSet from database query
     * @return ScanRecord object
     * @throws SQLException If column access fails
     */
    private ScanRecord mapToScanRecord(ResultSet rs) throws SQLException {
        return new ScanRecord(
                rs.getInt("scan_id"),
                rs.getString("directory_path"),
                rs.getTimestamp("scan_date"),
                rs.getTimestamp("completed_at"),
                rs.getInt("total_files"),
                rs.getString("scan_status"),
                rs.getString("error_message")
        );
    }

    /**
     * Create a ScannedFile from ChangeRecord for database operations.
     *
     * @param record ChangeRecord containing file information
     * @return ScannedFile object
     */
    private ScannedFile createScannedFileFromRecord(ChangeRecord record) {
        java.io.File file = new java.io.File(record.getFilePath());
        ScannedFile scannedFile = new ScannedFile(file);
        scannedFile.setFileHash(record.getNewHash());
        return scannedFile;
    }

    /**
     * Check if database has any previous scans for a directory.
     *
     * @param rootPath The root directory path
     * @return true if previous scans exist
     * @throws SQLException If database operation fails
     */
    public boolean hasPreviousScans(String rootPath) throws SQLException {
        String sql = "SELECT COUNT(*) FROM scans WHERE directory_path = ? AND scan_status = 'COMPLETED'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rootPath);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }
}
