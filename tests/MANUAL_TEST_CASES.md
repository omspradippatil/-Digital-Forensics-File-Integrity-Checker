# Manual Test Cases - Digital Forensics & File Integrity Checker

## Overview

This document contains comprehensive test cases for the Digital Forensics & File Integrity Checker application.
All tests must be run with a valid MySQL database and the application compiled with MySQL Connector/J.

---

## Test 1: First Directory Scan

**Scenario:** Initial scan of a directory to establish baseline.

**Steps:**
1. Start the application
2. Select "Scan New Directory"
3. Enter a test directory (create `/tmp/dffic_test/` first)
4. Create several test files:
   - `file1.txt` (size: ~100 bytes)
   - `subdir/file2.txt` (size: ~200 bytes)
   - `empty_file.dat` (size: 0 bytes)
   - `large_file.bin` (size: ~5KB)
5. Include a read-protected file (chmod 000) to test error handling

**Expected Results:**
- All 4 files reported as ADDED
- Total files: 4
- Modified: 0, Unchanged: 0, Deleted: 0
- Scan status: COMPLETED
- Database: 1 scan record, 4 file_records (is_active=true)

---

## Test 2: Second Scan Without Modifications

**Scenario:** Verify unchanged detection.

**Steps:**
1. Run scan on same directory as Test 1
2. Verify no file changes occurred

**Expected Results:**
- All 4 files reported as UNCHANGED
- Total files: 4
- Added: 0, Modified: 0, Deleted: 0
- New scan ID created (scan_id + 1)
- File records updated with new scan_id
- History table populated for each file

**Verification:**
```sql
-- All files should have same hash across scans
SELECT fr.file_id, fr.file_hash, fr.last_scan_id
FROM file_records fr
WHERE fr.is_active = TRUE;
```

---

## Test 3: File Modification Detection

**Scenario:** Modify a file's content and detect changes.

**Steps:**
1. Modify `file1.txt` content (append text)
2. Run scan again

**Expected Results:**
- `file1.txt` reported as MODIFIED
- Other 3 files (subdir/file2.txt, empty_file.dat, large_file.bin) as UNCHANGED
- Total files: 4
- Added: 0, Modified: 1, Unchanged: 3, Deleted: 0

**Verification:**
```sql
-- Modified file should have new hash
SELECT fr.file_name, fr.file_hash, fr.file_status
FROM file_records fr
WHERE fr.file_name = 'file1.txt' AND fr.is_active = TRUE;
```

---

## Test 4: New File Detection

**Scenario:** Add a new file and detect it as added.

**Steps:**
1. Create new file `new_file.txt` in directory
2. Run scan

**Expected Results:**
- `new_file.txt` reported as ADDED
- Previous 4 files remain UNCHANGED (1 modified, 3 unchanged)
- Total files: 5
- Added: 1, Modified: 1, Unchanged: 3, Deleted: 0

---

## Test 5: File Deletion Detection

**Scenario:** Delete a file and detect it as deleted.

**Steps:**
1. Delete `subdir/file2.txt` from filesystem
2. Run scan
3. Files in /tmp/dffic_test/ should now be: file1.txt (modified), empty_file.dat, large_file.bin (unchanged), new_file.txt (unchanged)

**Expected Results:**
- `subdir/file2.txt` reported as DELETED
- Other 3 files should be UNCHANGED
- Total files: 3
- Added: 0, Modified: 1, Unchanged: 3, Deleted: 1

**Verification:**
```sql
-- Deleted file should be marked inactive
SELECT fr.file_id, fr.file_name, fr.is_active, fr.file_status
FROM file_records fr
WHERE fr.file_name = 'file2.txt';
```

---

## Test 6: File Deletion and Re-addition

**Scenario:** Delete a file and then re-add it (should be detected as ADDED again).

**Steps:**
1. Delete `empty_file.dat`
2. Run scan (file should be DELETED)
3. Restore `empty_file.dat` with same content
4. Run scan (file should be ADDED again)

**Expected Results:**
- First scan: `empty_file.dat` = DELETED
- Second scan: `empty_file.dat` = ADDED (because it's not in the active baseline)
- File hash should be same as original

**Verification:**
Check history for multiple status changes for the same file.

---

## Test 7: Empty Directory Scan

**Scenario:** Scan an empty directory.

**Steps:**
1. Create empty directory `/tmp/dffic_empty/`
2. Run scan

**Expected Results:**
- Scan completes successfully
- Total files: 0
- All change counts: 0
- Database: 1 scan record, 0 file_records

---

## Test 8: Invalid Directory Input

**Scenario:** Handle invalid directory paths.

**Steps:**
1. Try to scan `/nonexistent/path/xyz123`

**Expected Results:**
- Application displays error message
- No scan created in database
- Application returns to main menu

---

## Test 9: Nested Directory Structure

**Scenario:** Scan a directory with many nested subdirectories.

**Steps:**
1. Create structure:
   - `/tmp/dffic_nested/level1/file_a.txt`
   - `/tmp/dffic_nested/level1/level2a/file_b.txt`
   - `/tmp/dffic_nested/level1/level2a/level3/file_c.txt`
   - `/tmp/dffic_nested/level1/level2b/file_d.txt`
2. Run scan

**Expected Results:**
- All 4 files detected
- Directory scanning works recursively
- No symbolic links created (per security requirement)

---

## Test 10: Large File Hashing

**Scenario:** Test with large files to verify chunked reading.

**Steps:**
1. Create 10MB test file
2. Run scan
3. Monitor memory usage (should not spike with entire file in memory)

**Expected Results:**
- File scanned successfully
- Hash calculated correctly
- No OutOfMemoryError
- File reported as ADDED

---

## Test 11: MySQL Connection Failure

**Scenario:** Handle database connection issues gracefully.

**Steps:**
1. Temporarily stop MySQL server
2. Run scan

**Expected Results:**
- Application displays connection error
- No data corruption (all operations rolled back)
- Application continues to run

---

## Test 12: Database Transaction Failure

**Scenario:** Test transaction rollback on error.

**Steps:**
1. Insert a file record
2. Cause database error (simulate foreign key violation)
3. Run scan

**Expected Results:**
- Transaction rolled back
- No partial data saved
- Scan marked as FAILED

---

## Test 13: Scan History Verification

**Scenario:** Verify scan history is preserved correctly.

**Steps:**
1. Run 5 different scans of the same directory
2. Check scan history view

**Expected Results:**
- 5 scan records in scans table
- Each scan has unique scan_id
- Total files per scan may differ
- Complete scan history retrievable

---

## Test 14: Hash Verification for Known File

**Scenario:** Verify SHA-256 hash for a file with known content.

**Steps:**
1. Create file with known content: "Hello, World!"
2. Calculate expected hash manually
3. Run scan

**Expected Results:**
- File reported as ADDED
- Hash matches expected value
- Database stores correct hash

---

## Test 15: File Status Transitions

**Scenario:** Test complete file status lifecycle.

**Steps:**
1. First scan: New file → ADDED
2. Second scan: Unchanged → UNCHANGED
3. Modify file
4. Third scan: Modified → MODIFIED
5. Delete file
6. Fourth scan: Deleted → DELETED
7. Delete directory, create same file path
8. Fifth scan: File not in baseline → ADDED again

**Expected Results:**
- Complete status tracking through all states
- History records for each transition
- Active/inactive flags correctly managed

---

## Test 16: Symbolic Link Handling

**Scenario:** Test that symbolic links are skipped.

**Steps:**
1. Create regular file `real.txt`
2. Create symbolic link `link.txt` pointing to `real.txt`
3. Run scan

**Expected Results:**
- `real.txt` scanned as normal file
- `link.txt` is NOT scanned (error message shows it was skipped)
- Symbolic link traversal disabled by default

---

## Test 17: File Permissions

**Scenario:** Handle files with different permissions.

**Steps:**
1. Create file with read permissions only
2. Run scan
3. Try to create file with no read permissions

**Expected Results:**
- Readable files processed normally
- Unreadable files reported as errors
- Scan may be marked as INCOMPLETE (per spec)
- Application continues operation

---

## Test 18: Menu Navigation

**Scenario:** Test all menu options and navigation.

**Steps:**
1. Test menu display
2. Test option 1 (scan)
3. Test option 2 (verify)
4. Test option 3 (view all files)
5. Test option 4 (view modified)
6. Test option 5 (view deleted)
7. Test option 6 (view history)
8. Test option 7 (exit)

**Expected Results:**
- Menu displays correctly
- All options function without crash
- Input validation works
- Return to main menu works

---

## Test 19: Database Schema Integrity

**Scenario:** Verify database schema matches design.

**Steps:**
1. Connect to database
2. Check all required tables exist
3. Verify constraints and indexes

**Expected Results:**
- All 3 tables exist: `scans`, `file_records`, `file_history`
- Correct data types and constraints
- Foreign keys properly defined

---

## Test 20: Multiple Directories

**Scenario:** Test scanning different directories.

**Steps:**
1. Create `/tmp/dffic_test1/` and `/tmp/dffic_test2/`
2. Populate each with different files
3. Run scan on test1
4. Run scan on test2
5. Verify isolation between directories

**Expected Results:**
- Each directory has independent baseline
- Files from test1 don't affect test2
- Separate scan histories
- Different file records for each root directory

---

## Test 21: Error Recovery

**Scenario:** Test application recovery from errors.

**Steps:**
1. Start application
2. Cause an error (e.g., enter invalid menu choice)
3. Verify application continues running
4. Perform normal operations

**Expected Results:**
- Error handled gracefully
- Application remains usable
- No crashes or data corruption

---

## Test 22: Performance with Many Files

**Scenario:** Test with large number of files.

**Steps:**
1. Create directory with 100 small files
2. Run scan
3. Run second scan

**Expected Results:**
- Scan completes in reasonable time
- Memory usage remains stable
- All files processed correctly
- Comparison completes within acceptable time

---

## Test 23: Application Initialization

**Scenario:** Test application startup.

**Steps:**
1. Check Java version compatibility
2. Test database connection
3. Verify all components initialize

**Expected Results:**
- Valid Java version (17+)
- Database connection successful
- All components ready
- Menu displayed successfully

---

## Test 24: Configuration File

**Scenario:** Test database configuration.

**Steps:**
1. Check config/database.properties.example
2. Verify all required properties
3. Test configuration loading

**Expected Results:**
- All configuration properties present
- File is valid Java properties format
- Loading mechanism works

---

## Test 25: Cross-Platform Compatibility

**Scenario:** Test on different platforms.

**Steps:**
1. Run application and check for platform-specific issues
2. Test with different path formats
3. Verify file separators

**Expected Results:**
- Works on macOS, Windows, Linux
- Path handling correct for each platform
- File separators handled properly

---

## Test 26: Memory Management

**Scenario:** Verify efficient memory usage.

**Steps:**
1. Run scan on large directory
2. Monitor memory (if possible)
3. Verify no memory leaks

**Expected Results:**
- Memory usage remains stable
- No excessive memory allocation
- Proper cleanup of resources

---

## Test 27: Concurrent Access

**Scenario:** Test basic concurrent operations.

**Steps:**
1. Run two separate scans quickly
2. Verify data integrity

**Expected Results:**
- No data corruption
- Each scan gets unique ID
- Proper transaction isolation

---

## Test 28: Export/Import

**Scenario:** Test database data extraction.

**Steps:**
1. Create test data
2. Export scan results using sample_queries.sql
3. Verify export format

**Expected Results:**
- Data can be exported
- Export preserves integrity
- Can be imported back

---

## Test 29: Backup and Recovery

**Scenario:** Test basic backup scenarios.

**Steps:**
1. Create comprehensive test data
2. Backup database
3. Simulate failure
4. Restore from backup

**Expected Results:**
- Backup successful
- Data integrity maintained
- Recovery works

---

## Test 30: Production Readiness

**Scenario:** Final validation before deployment.

**Steps:**
1. Run all previous tests
2. Check code quality
3. Verify documentation
4. Test edge cases

**Expected Results:**
- All tests pass
- Code follows Java best practices
- Documentation complete
- Application ready for production

---

## Test 31: Error Message Validation

**Scenario:** Test error message clarity.

**Steps:**
1. Trigger various error conditions
2. Check error messages are user-friendly
3. Verify no stack traces to user

**Expected Results:**
- Clear, actionable error messages
- No sensitive data in errors
- Graceful error handling

---

## Test 32: User Experience

**Scenario:** Test overall user experience.

**Steps:**
1. Run through common workflow
2. Check usability
3. Verify performance
4. Check output formatting

**Expected Results:**
- Intuitive interface
- Good performance
- Clear output
- Smooth operation

---

## Test 33: Security Validation

**Scenario:** Test security aspects.

**Steps:**
1. Verify no hardcoded credentials
2. Check input validation
3. Test directory traversal attempts
4. Verify symbolic link protection

**Expected Results:**
- No credentials in code
- Input validation works
- No directory traversal
- Symbolic links protected

---

## Test 34: Data Validation

**Scenario:** Test data integrity.

**Steps:**
1. Verify hash consistency
2. Check data relationships
3. Validate constraints
4. Test transaction integrity

**Expected Results:**
- Hashes consistent
- Relationships maintained
- Constraints enforced
- Transactions work

---

## Test 35: Scalability

**Scenario:** Test system scalability.

**Steps:**
1. Test with increasing file counts
2. Measure performance
3. Check resource usage
4. Verify limits

**Expected Results:**
- Performance acceptable
- Resource usage controlled
- No hard limits exceeded
- Scalable architecture

---

## Test 36: Monitoring and Logging

**Scenario:** Test monitoring features.

**Steps:**
1. Create errors
2. Check error logging
3. Verify monitoring output
4. Check status messages

**Expected Results:**
- Errors logged
- Monitoring works
- Status information clear
- User notified

---

## Test 37: Customization

**Scenario:** Test configuration flexibility.

**Steps:**
1. Modify configuration
2. Test impact
3. Verify changes
4. Reset configuration

**Expected Results:**
- Configuration changes work
- System adapts
- Can be reset
- Flexible setup

---

## Test 38: Internationalization

**Scenario:** Test international support.

**Steps:**
1. Test non-ASCII characters
2. Test different locales
3. Check encoding
4. Verify display

**Expected Results:**
- Non-ASCII handled
- Different locales supported
- Encoding correct
- Display works

---

## Test 39: Backup and Restore

**Scenario:** Test complete backup cycle.

**Steps:**
1. Create full dataset
2. Backup
3. Simulate catastrophic failure
4. Restore

**Expected Results:**
- Backup complete
- Data preserved
- Restore successful
- Full recovery

---

## Test 40: Future Extensibility

**Scenario:** Test extensibility points.

**Steps:**
1. Identify extension points
2. Test modification
3. Verify impact
4. Check compatibility

**Expected Results:**
- Easy to extend
- Backward compatible
- Minimal impact
- Well-designed

---

## General Testing Notes

1. **Test Environment:** Use a dedicated test database
2. **Isolation:** Each test should be isolated
3. **Cleanup:** Remove test files after each test
4. **Documentation:** Record test results
5. **Automation:** Consider scripting tests for CI/CD
6. **Monitoring:** Track performance metrics
7. **Security:** Never expose sensitive data
8. **Validation:** Verify all assumptions

---

## Troubleshooting

**Common Issues:**

1. **Java Version:** Ensure Java 17+
2. **Database:** MySQL running and accessible
3. **Permissions:** Read access to directories
4. **Configuration:** Valid database.properties
5. **Memory:** Check system resources

**Debugging:**
- Check logs for error details
- Verify database connectivity
- Test with simple directories first
- Check file permissions
- Review configuration

---

## Test Success Criteria

A test is successful if:

1. All expected outcomes occur
2. No unexpected errors
3. Database integrity maintained
4. Application remains usable
5. Resources properly managed
6. User experience is acceptable

---

This document is updated continuously as the application evolves. Ensure all tests are run before releasing Stage 1.