package com.ericsson.msran.test.grat.transport;

public enum OperationalState {
	DISABLED(0, "DISABLED"), //
	ENABLED(1, "ENABLED"), //
    UNKNOWN(-1, "");

    private int value;
    private String stringValue;

    private OperationalState(final int value, final String stringValue) {
        this.value = value;
        this.stringValue = stringValue;
    }

    public int getValue() {
        return value;
    }

    public static OperationalState getByValue(final int value) {
        for (OperationalState status : OperationalState.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return UNKNOWN;
    }

    public static OperationalState getByValue(final String value) {
        if (value == null) {
            return UNKNOWN;
        }
        
        for (OperationalState status : OperationalState.values()) {
            if (value.equals(status.stringValue)) {
                return status;
            }
        }
        return UNKNOWN;
    }

}
