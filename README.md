# Digital Forensics & File Integrity Checker

## Project Overview

**Digital Forensics & File Integrity Checker (DFFIC)** is a console-based Java application for monitoring file integrity in user-selected directories. It detects file changes (added, modified, deleted, unchanged) by comparing SHA-256 hashes against a stored baseline.

**Stage 1 - Core MVP**  
**Version:** 1.0.0  
**Language:** Core Java 17+  
**Database:** MySQL 8 via JDBC

---

## Features

- Recursive directory scanning using Java NIO
- SHA-256 hash calculation using MessageDigest
- MySQL database storage with JDBC PreparedStatement
- File integrity comparison (ADDED, MODIFIED, UNCHANGED, DELETED)
- Scan history preservation
- Command-line menu interface
- Transaction-based data persistence
- Symbolic link protection

---

## Technology Stack

- **Java 17+** - Core language
- **Java NIO** - File system traversal
- **MessageDigest** - SHA-256 hashing
- **JDBC** - Database connectivity
- **MySQL 8** - Database server
- **MySQL Connector/J** - JDBC driver (only external library)

---

## Requirements

- Java 17 or later
- MySQL 8 server
- MySQL Connector/J jar
- Operating system: Windows, macOS, or Linux

---

## Project Structure

```
DigitalForensics/
│
├── src/
│   └── com/
│       └── dffic/
│           ├── Main.java
│           ├── cli/
│           │   └── ConsoleMenu.java
│           ├── model/
│           │   ├── ScannedFile.java
│           │   ├── ScanResult.java
│           │   ├── ScanSummary.java
│           │   ├── ChangeRecord.java
│           │   ├── ScanRecord.java
│           │   ├── ChangeType.java
│           │   └── FileRecord.java
│           ├── scanner/
│           │   └── DirectoryScanner.java
│           ├── hashing/
│           │   ├── HashService.java
│           │   └── Sha256HashService.java
│           ├── service/
│           │   └── IntegrityChecker.java
│           ├── database/
│           │   ├── DatabaseConnection.java
│           │   └── ForensicsRepository.java
│           └── util/
│               ├── PathUtils.java
│               └── InputValidator.java
│
├── database/
│   ├── schema.sql
│   └── sample_queries.sql
│
├── config/
│   └── database.properties.example
│
├── reports/
├── docs/
├── tests/
├── lib/
├── .gitignore
├── README.md
└── PROJECT_EXPLANATION.md
```

---

## Database Setup

### 1. Install MySQL 8

**macOS (using Homebrew):**
```bash
brew install mysql
brew services start mysql
```

**Windows:**
Download MySQL Installer from https://dev.mysql.com/downloads/mysql/

**Linux:**
```bash
sudo apt update
sudo apt install mysql-server
sudo service mysql start
```

### 2. Create Database

```sql
mysql -u root -p
CREATE DATABASE digital_forensics;
exit;
```

Or run the schema file:
```bash
mysql -u root -p < database/schema.sql
```

### 3. Configure JDBC

Copy the example configuration and edit with your credentials:
```bash
cp config/database.properties.example config/database.properties
```

Edit `config/database.properties`:
```
db.host=localhost
db.port=3306
db.name=digital_forensics
db.user=root
db.password=your_password_here
```

---

## Compilation

```bash
# Ensure MySQL Connector/J is in lib/
# Compile all Java files
javac -cp "lib/mysql-connector-j.jar" -d out src/com/dffic/*.java src/com/dffic/**/*.java
```

---

## Execution

```bash
# Run the application
java -cp "out:lib/mysql-connector-j.jar" com.dffic.Main
```

---

## Sample Console Output

```
==============================================
  Digital Forensics & File Integrity Checker
  Version 1.0.0 - Stage 1 Core MVP
==============================================

Java Version: 17.0.1

Starting application...

==========================================
 DIGITAL FORENSICS & FILE INTEGRITY CHECKER
==========================================

1. Scan New Directory
2. Verify File Integrity
3. View All Scanned Files
4. View Modified Files
5. View Deleted Files
6. View Scan History
7. Exit

==========================================
Enter your choice: 1

--- SCAN NEW DIRECTORY ---
Enter directory path: /home/user/documents

Scanning directory: /home/user/documents
Please wait...

Scan ID: 1

This is the first scan. Establishing baseline...

========================================
           SCAN SUMMARY
========================================
Scan ID:          1
Directory:        /home/user/documents
Total Files:      5
Scan Duration:    0.123 seconds
----------------------------------------
Added:            5
Modified:         0
Unchanged:        0
Deleted:          0
========================================
```

---

## Testing

See `tests/MANUAL_TEST_CASES.md` for detailed test scenarios.

---

## Documentation

For complete project explanation, see [PROJECT_EXPLANATION.md](PROJECT_EXPLANATION.md)

---

## License

Educational use only. This is a second-year engineering college project.