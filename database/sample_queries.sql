-- Digital Forensics & File Integrity Checker - Sample Queries
-- Database: digital_forensics

-- Select the database
USE digital_forensics;

-- 1. View all completed scans with summary
SELECT
    scan_id,
    directory_path,
    scan_date,
    completed_at,
    total_files,
    scan_status,
    TIMESTAMPDIFF(SECOND, scan_date, completed_at) as duration_seconds
FROM scans
WHERE scan_status = 'COMPLETED'
ORDER BY scan_date DESC
LIMIT 10;

-- 2. View all currently monitored (active) files
SELECT
    file_id,
    root_path,
    file_path,
    file_name,
    file_size,
    file_hash,
    last_modified,
    file_status,
    is_active
FROM file_records
WHERE is_active = TRUE
ORDER BY file_name;

-- 3. View files added in the most recent scan
SELECT
    fh.*,
    s.scan_date
FROM file_history fh
JOIN scans s ON fh.scan_id = s.scan_id
WHERE fh.change_status = 'ADDED'
AND s.scan_id = (SELECT MAX(scan_id) FROM scans WHERE scan_status = 'COMPLETED')
ORDER BY fh.change_date;

-- 4. View files modified in the most recent scan
SELECT
    fh.*,
    s.scan_date
FROM file_history fh
JOIN scans s ON fh.scan_id = s.scan_id
WHERE fh.change_status = 'MODIFIED'
AND s.scan_id = (SELECT MAX(scan_id) FROM scans WHERE scan_status = 'COMPLETED')
ORDER BY fh.change_date;

-- 5. View files deleted in the most recent scan
SELECT
    fh.*,
    s.scan_date
FROM file_history fh
JOIN scans s ON fh.scan_id = s.scan_id
WHERE fh.change_status = 'DELETED'
AND s.scan_id = (SELECT MAX(scan_id) FROM scans WHERE scan_status = 'COMPLETED')
ORDER BY fh.change_date;

-- 6. View scan history for a specific file path
SELECT
    fh.*,
    s.scan_date,
    s.completed_at
FROM file_history fh
JOIN scans s ON fh.scan_id = s.scan_id
WHERE fh.file_path LIKE '%your_file_extension%'
ORDER BY fh.change_date DESC;

-- 7. Get count of files by status in the most recent scan
SELECT
    fh.change_status,
    COUNT(*) as count
FROM file_history fh
WHERE fh.scan_id = (SELECT MAX(scan_id) FROM scans WHERE scan_status = 'COMPLETED')
GROUP BY fh.change_status;

-- 8. View all files that have been modified at least once
SELECT DISTINCT
    fr.file_id,
    fr.root_path,
    fr.file_path,
    fr.file_name,
    fr.file_hash,
    fr.is_active
FROM file_records fr
JOIN file_history fh ON fr.file_id = fh.file_id
WHERE fh.change_status = 'MODIFIED';

-- 9. Get total file count and scan count statistics
SELECT
    (SELECT COUNT(*) FROM scans WHERE scan_status = 'COMPLETED') as total_scans,
    (SELECT COUNT(*) FROM file_records WHERE is_active = TRUE) as active_files,
    (SELECT COUNT(DISTINCT root_path) FROM file_records WHERE is_active = TRUE) as unique_directories;

-- 10. View scan with errors
SELECT
    scan_id,
    directory_path,
    scan_date,
    scan_status,
    error_message
FROM scans
WHERE scan_status = 'FAILED'
AND error_message IS NOT NULL
ORDER BY scan_date DESC;

-- 11. Find duplicate file names across different directories
SELECT
    file_name,
    COUNT(*) as occurrence_count,
    GROUP_CONCAT(root_path) as directories
FROM file_records
WHERE is_active = TRUE
GROUP BY file_name
HAVING COUNT(*) > 1;

-- 12. Get file records for a specific scan (for comparison)
SELECT
    file_id,
    file_path,
    file_name,
    file_size,
    file_hash,
    file_status
FROM file_records
WHERE root_path = 'your_directory_path'
AND file_id IN (
    SELECT fr.file_id
    FROM file_records fr
    JOIN file_history fh ON fr.file_id = fh.file_id
    WHERE fh.scan_id = your_scan_id
);
