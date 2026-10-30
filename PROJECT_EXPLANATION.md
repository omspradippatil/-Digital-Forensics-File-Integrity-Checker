# Digital Forensics & File Integrity Checker - Project Explanation

## 1. Project Introduction

### What is this project?
This is Stage 1 of the Digital Forensics & File Integrity Checker (DFFIC), a console-based Java application that monitors file integrity in directories. It calculates SHA-256 hashes for all files in a directory, stores them in a MySQL database, and compares them with previous scans to detect changes.

### Why was it developed?
Digital forensics requires accurate tracking of file changes. This project demonstrates how a simple Java application can provide forensic capabilities: tracking when files are added, modified, deleted, or unchanged over time.

### What problem does it solve?
It solves the problem of detecting unauthorized or unintended file changes. By maintaining a database of file hashes and comparing them with current directory states, users can quickly identify modifications.

### What is file integrity?
File integrity means confirming that a file has not been altered from its original state. We verify this by comparing SHA-256 hash values, which are unique fingerprints of file content.

### What is digital forensics?
Digital forensics is the practice of investigating digital evidence. This project provides a basic tool for tracking file changes over time, which is a fundamental forensic capability.

---

## 2. Technology Explanation

### Core Java
Java is the programming language. We use Core Java (no external frameworks) to demonstrate fundamental programming concepts.

### OOP (Object-Oriented Programming)
We use classes (ScannedFile, ChangeRecord), encapsulation (private fields with getters), and abstraction (interfaces like HashService).

### Java NIO (New Input/Output)
Used for directory traversal (DirectoryScanner) and file operations. NIO provides efficient file system access.

### MessageDigest
Java's MessageDigest class provides cryptographic hashing algorithms. We use it for SHA-256.

### SHA-256
Secure Hash Algorithm 256-bit produces a 64-character hexadecimal string that uniquely identifies file content. Changing one bit changes the hash completely.

### JDBC (Java Database Connectivity)
Standard Java API for connecting to databases. We use it with MySQL.

### MySQL 8
Relational database that stores scan results, file records, and history.

### Java Collections
We use List (ArrayList), Map (HashMap), and other collections to manage data in memory.

### Exception Handling
We use try-with-resources and proper exception handling for database connections and file operations.

---

## 3. Architecture Explanation

### Overall Architecture
Layered architecture:
- **Presentation:** ConsoleMenu (CLI)
- **Service:** IntegrityChecker, DirectoryScanner
- **Data:** ForensicsRepository, DatabaseConnection
- **Model:** ScannedFile, ChangeRecord, ScanResult

### Package Structure
- com.dffic.cli: User interface
- com.dffic.model: Data classes
- com.dffic.scanner: File scanning
- com.dffic.hashing: Hash generation
- com.dffic.service: Integrity comparison
- com.dffic.database: Database access
- com.dffic.util: Helper methods

### Module Relationships
DirectoryScanner → ScannedFile → HashService → IntegrityChecker → ForensicsRepository → DatabaseConnection

### Application Workflow
1. User selects menu option
2. Directory is scanned (NIO)
3. Hashes calculated (MessageDigest)
4. Previous records retrieved (JDBC)
5. Comparison performed (IntegrityChecker)
6. Results saved (Transaction)
7. Summary displayed (ConsoleMenu)

---

## 4. Every Folder and File

### Main Entry Point
**File:** src/com/dffic/Main.java  
**Package:** com.dffic  
**Purpose:** Application entry point, validates Java version, starts console menu  
**Classes:** Main (with getMajorVersion helper)

### CLI Package
**File:** src/com/dffic/cli/ConsoleMenu.java  
**Package:** com.dffic.cli  
**Purpose:** Main menu interface  
**Classes:** ConsoleMenu  
**Methods:** start(), displayMainMenu(), scanNewDirectory(), verifyFileIntegrity(), viewAllScannedFiles(), etc.

### Model Package
**File:** src/com/dffic/model/ChangeType.java  
**Package:** com.dffic.model  
**Purpose:** Enum defining four change types  
**Values:** ADDED, MODIFIED, UNCHANGED, DELETED

**File:** src/com/dffic/model/ChangeRecord.java  
**Package:** com.dffic.model  
**Purpose:** Stores single file change details  
**Fields:** filePath, fileName, fileSize, oldHash, newHash, changeType  
**Methods:** All getters, toString()

**File:** src/com/dffic/model/ScannedFile.java  
**Package:** com.dffic.model  
**Purpose:** Represents scanned file metadata  
**Fields:** fileName, absolutePath, extension, fileSize, lastModified, fileHash  
**Methods:** Constructors, getters, setters

**File:** src/com/dffic/model/ScanResult.java  
**Package:** com.dffic.model  
**Purpose:** Complete scan results  
**Fields:** directoryPath, files (List), unreadableFiles, errorMessages, isComplete, scanDurationMillis  
**Methods:** All getters, isComplete(), hasErrors()

**File:** src/com/dffic/model/ScanSummary.java  
**Package:** com.dffic.model  
**Purpose:** Summary statistics for display  
**Fields:** scanId, directoryPath, scanDate, totalFiles, addedCount, etc.  
**Methods:** formatSummary(), formatDetailedResults()

**File:** src/com/dffic/model/ScanRecord.java  
**Package:** com.dffic.model  
**Purpose:** Database scan record representation  
**Fields:** scanId, directoryPath, scanDate, completedAt, totalFiles, scanStatus, errorMessage  
**Methods:** isCompleted(), isFailed(), getDurationSeconds()

**File:** src/com/dffic/model/FileRecord.java  
**Package:** com.dffic.model  
**Purpose:** Database file record representation  
**Fields:** fileId, rootPath, filePath, fileName, fileSize, fileHash, etc.  
**Methods:** isActive(), isModified(), getFormattedFileSize()

### Scanner Package
**File:** src/com/dffic/scanner/DirectoryScanner.java  
**Package:** com.dffic.scanner  
**Purpose:** Recursive directory scanning  
**Methods:** scanDirectory(), validateDirectory(), scanForStatistics()

### Hashing Package
**File:** src/com/dffic/hashing/HashService.java  
**Package:** com.dffic.hashing  
**Purpose:** Hash algorithm interface  
**Methods:** generateHash(), verifyHash(), getAlgorithmName()

**File:** src/com/dffic/hashing/Sha256HashService.java  
**Package:** com.dffic.hashing  
**Purpose:** SHA-256 implementation  
**Methods:** generateHash(), verifyHash(), generateHashes(), getAlgorithmName(), getHashLength()

### Service Package
**File:** src/com/dffic/service/IntegrityChecker.java  
**Package:** com.dffic.service  
**Purpose:** File comparison logic  
**Methods:** compareWithBaseline(), calculateSummaryCounts(), filterByType()

### Database Package
**File:** src/com/dffic/database/DatabaseConnection.java  
**Package:** com.dffic.database  
**Purpose:** JDBC connection management  
**Methods:** getConnection(), testConnection(), closeConnection()

**File:** src/com/dffic/database/ForensicsRepository.java  
**Package:** com.dffic.database  
**Purpose:** All database operations  
**Methods:** createScan(), completeScan(), failScan(), getActiveFiles(), saveFileRecord(), saveFileHistory(), etc.

### Util Package
**File:** src/com/dffic/util/PathUtils.java  
**Package:** com.dffic.util  
**Purpose:** Path manipulation utilities  
**Methods:** normalizePath(), isDirectory(), getFileName(), getExtension()

**File:** src/com/dffic/util/InputValidator.java  
**Package:** com.dffic.util  
**Purpose:** Input validation  
**Methods:** isValidDirectory(), isNonEmptyString(), isInRange()

---

## 5. Method-by-Method Explanation

### Sha256HashService.java

**Method:** generateHash(File file)
- Purpose: Calculate SHA-256 hash for a file
- Parameters: File file (file to hash)
- Return: String (64-char hex hash)
- Concepts: MessageDigest, FileInputStream, try-with-resources
- Internal: Reads in 8KB chunks, updates digest, converts to hex
- Example: `String hash = hashService.generateHash(new File("test.txt"));`

**Method:** bytesToHex(byte[] bytes)
- Purpose: Convert byte array to hex string
- Parameters: byte[] bytes
- Returns: String
- Concepts: String formatting, byte manipulation

### DirectoryScanner.java

**Method:** scanDirectory(String directoryPath)
- Purpose: Recursively scan directory
- Parameters: String directoryPath
- Returns: ScanResult
- Concepts: Java NIO, FileVisitor, try-with-resources
- Internal: Creates FileVisitor, walks file tree, collects metadata

---

## 6. OOP Concepts

- **Classes/Objects:** ScannedFile, ChangeRecord, DirectoryScanner
- **Encapsulation:** Private fields with public getters/setters
- **Abstraction:** HashService interface
- **Polymorphism:** IntegrityChecker uses HashService (interface)
- **Inheritance:** Not used (simple structure preferred)
- **Exception Handling:** try-catch with finally blocks
- **Collections:** ArrayList, HashMap for data management

---

## 7. Database Explanation

### Tables
- **scans:** Scan session info (ID, path, dates, status, error)
- **file_records:** Current file state with foreign key to scans
- **file_history:** Historical changes with foreign keys

### Constraints
- Primary keys: scan_id, file_id, history_id
- Foreign keys: file_records.last_scan_id, file_history.file_id/scan_id
- Unique: (root_path, file_path) on file_records
- Indexes on scan_id, file_path, change_status

### JDBC Process
1. Load driver: `Class.forName("com.mysql.cj.jdbc.Driver")`
2. Create connection: `DriverManager.getConnection(url, user, pass)`
3. Prepare statement: `conn.prepareStatement(sql)`
4. Set parameters
5. Execute: `executeQuery()` or `executeUpdate()`
6. Process results
7. Close resources (try-with-resources)

### Transactions
- `conn.setAutoCommit(false)` starts transaction
- All operations succeed = commit
- Any failure = rollback
- Never commit incomplete scans

---

## 8. Algorithm Explanation

### Directory Scanning
```
Walk directory tree recursively
For each file found:
  Create ScannedFile with metadata
  Add to result list
  Calculate hash (separate step)
```

### SHA-256 Calculation
```
Create MessageDigest instance
Read file in 8192 byte chunks
Update digest with each chunk
Get digest bytes
Convert to 64-char hex
```

### File Comparison
```
For each current file:
  Find previous record by path
  If no previous record → ADDED
  If hash different → MODIFIED
  If hash same → UNCHANGED

For previous files not in current:
  → DELETED (only if scan complete)
```

---

## 9. Execution Flow

1. User runs `java -cp ... com.dffic.Main`
2. Main validates Java version
3. ConsoleMenu displays welcome
4. User selects option
5. For scan: DirectoryScanner scans directory
6. HashService calculates hashes
7. IntegrityChecker compares with database
8. ForensicsRepository saves results
9. Summary displayed to user
10. User exits or continues

---

## 10. Installation

### Java
- Check: `java -version`
- Must show 17 or higher

### MySQL
- Install MySQL 8
- Create database
- Configure credentials

### Compilation
```bash
javac -cp "lib/mysql-connector-j.jar" -d out src/com/dffic/*.java src/com/dffic/**/*.java
```

### Execution
```bash
java -cp "out:lib/mysql-connector-j.jar" com.dffic.Main
```

---

## 11. Testing

See `tests/MANUAL_TEST_CASES.md` for 40 test scenarios covering:
- First scan, second scan, modifications
- Deletions, additions, re-additions
- Error conditions, database operations
- Performance, security

---

## 12. Limitations and Future Scope

### Current Limitations
- Console only (no GUI)
- Requires manual database setup
- No automated scheduling
- Basic comparison algorithm

### Future Scope
- Real-time file monitoring
- PDF/text forensic reports
- Historical scan browser
- More advanced file analysis
- Tamper-evident audit logs
- GUI interface option

---

## 13. Viva Preparation (30 Questions)

1. What is this project? (Console file integrity checker)
2. Why is it developed? (Forensic file tracking)
3. What is SHA-256? (Cryptographic hash function)
4. How does hashing work? (MessageDigest, 8KB chunks)
5. What are the four statuses? (ADDED, MODIFIED, UNCHANGED, DELETED)
6. Why use NIO? (Efficient file traversal)
7. What is JDBC? (Java Database Connectivity)
8. What is MySQL? (Relational database)
9. How is database secured? (PreparedStatement, no hardcoded passwords)
10. What is encapsulation? (Private fields, public methods)
11. What is inheritance? (Not used - simple design)
12. How is exception handling done? (try-with-resources)
13. What is a transaction? (Atomic database operations)
14. How are files compared? (Normalized paths, hash comparison)
15. What is a scan summary? (Counts by status)
16. How is progress tracked? (Scan ID, history table)
17. What is file integrity? (Content verification via hash)
18. Why decode from hex? (Hash output is hexadecimal)
19. How is path normalization done? (Absolute paths, separator handling)
20. What is a symbolic link? (File pointing to another file)
21. Why skip symlinks? (Security - prevent traversal)
22. What is a foreign key? (Database relationship constraint)
23. How is rollback handled? (On SQL exception)
24. What is a prepared statement? (Parameterized SQL)
25. How is memory managed? (Buffered streams, no full file loading)
26. What is the database schema? (3 tables: scans, file_records, file_history)
27. How is a file marked deleted? (is_active = FALSE)
28. What happens if scan fails? (Status = FAILED, no updates)
29. How is the first scan different? (Establishes baseline)
30. What is the project structure? (Modular package organization)

---

This document is the authoritative explanation of the implemented source code. All method names, class names, and functionality descriptions match the actual code in the project.