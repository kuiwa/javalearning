package com.ericsson.msran.test.grat.transport;

public enum LapdBlockStatus {
    NONE(0, "None"), //
    UL(1, "UL"), //
    DL(2, "DL"), //
    BOTH(3, "Both"), //
    UNKNOWN(-1, "");

    private int value;
    private String stringValue;

    private LapdBlockStatus(final int value, final String stringValue) {
        this.value = value;
        this.stringValue = stringValue;
    }

    public int getValue() {
        return value;
    }

    public static LapdBlockStatus getByValue(final int value) {
        for (LapdBlockStatus status : LapdBlockStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return UNKNOWN;
    }

    public static LapdBlockStatus getByValue(final String value) {
        if (value == null) {
            return UNKNOWN;
        }
        
        for (LapdBlockStatus status : LapdBlockStatus.values()) {
            if (value.equals(status.stringValue)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}