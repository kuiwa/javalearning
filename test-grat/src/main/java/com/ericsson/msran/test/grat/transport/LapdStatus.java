package com.ericsson.msran.test.grat.transport;

public enum LapdStatus {
    DOWN(0, "Down"), //
    AWAITING_EST(1, "Awaiting establishment"), //
    UP(2, "Up"), //
    AWAITING_REL(3, "Awaiting release"), //
    UNKNOWN(-1, "");

    private int value;
    private String stringValue;

    private LapdStatus(final int value, final String stringValue) {
        this.value = value;
        this.stringValue = stringValue;
    }

    public int getValue() {
        return value;
    }

    public static LapdStatus getByValue(final int value) {
        for (LapdStatus status : LapdStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return UNKNOWN;
    }

    public static LapdStatus getByValue(final String value) {
        if (value == null) {
            return UNKNOWN;
        }
        
        for (LapdStatus status : LapdStatus.values()) {
            if (value.equals(status.stringValue)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}