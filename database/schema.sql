-- Digital Forensics & File Integrity Checker - Database Schema
-- Database: digital_forensics

-- Create the database
CREATE DATABASE IF NOT EXISTS digital_forensics;
USE digital_forensics;

-- Table 1: scans - Stores scan session information
CREATE TABLE IF NOT EXISTS scans (
    scan_id INT PRIMARY KEY AUTO_INCREMENT,
    directory_path VARCHAR(1000) NOT NULL,
    scan_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    total_files INT DEFAULT 0,
    scan_status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    error_message TEXT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 2: file_records - Stores current state of monitored files
CREATE TABLE IF NOT EXISTS file_records (
    file_id INT PRIMARY KEY AUTO_INCREMENT,
    root_path VARCHAR(1000) NOT NULL,
    file_path VARCHAR(1500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_hash CHAR(64) NOT NULL,
    last_modified DATETIME NOT NULL,
    last_scan_id INT NULL,
    file_status VARCHAR(30) NOT NULL DEFAULT 'UNCHANGED',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_file (root_path(400), file_path(400)),
    CONSTRAINT fk_file_scan FOREIGN KEY (last_scan_id) REFERENCES scans(scan_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table 3: file_history - Stores historical change records
CREATE TABLE IF NOT EXISTS file_history (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    file_id INT NULL,
    scan_id INT NOT NULL,
    file_path VARCHAR(1500) NOT NULL,
    old_hash CHAR(64) NULL,
    new_hash CHAR(64) NULL,
    change_status VARCHAR(30) NOT NULL,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_file FOREIGN KEY (file_id) REFERENCES file_records(file_id) ON DELETE SET NULL,
    CONSTRAINT fk_history_scan FOREIGN KEY (scan_id) REFERENCES scans(scan_id) ON DELETE CASCADE,
    INDEX idx_scan_id (scan_id),
    INDEX idx_file_path (file_path),
    INDEX idx_change_status (change_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index for faster file lookups
CREATE INDEX idx_file_records_root_path ON file_records(root_path);
CREATE INDEX idx_file_records_is_active ON file_records(is_active);
