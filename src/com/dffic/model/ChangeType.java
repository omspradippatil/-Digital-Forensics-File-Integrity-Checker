package com.dffic.model;

/**
 * ChangeType enumeration
 * Defines the four possible file status types during integrity comparison.
 */
public enum ChangeType {
    /**
     * File exists in current scan but not in previous baseline.
     */
    ADDED("ADDED"),

    /**
     * File exists in both scans but hash has changed.
     */
    MODIFIED("MODIFIED"),

    /**
     * File exists in both scans with identical hash.
     */
    UNCHANGED("UNCHANGED"),

    /**
     * File existed in previous scan but missing from current scan.
     */
    DELETED("DELETED");

    private final String status;

    ChangeType(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
