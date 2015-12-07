package com.ericsson.msran.test.grat.transport;

public enum AvailabilityStatus {
    NO_STATUS(0, "NO_STATUS"), //
    IN_TEST(1, "IN_TEST"), //
    FAILED(2, "FAILED"), //
    POWER_OFF(3, "POWER_OFF"), //
    OFF_LINE(4, "OFF_LINE"), //
    OFF_DUTY(5, "OFF_DUTY"), //
    DEGRADED(6, "DEGRADED"), //
    LOG_FULL(7, "LOG_FULL"), //
    NOT_INSTALLED(8, "NOT_INSTALLED"), //
    DEPENDENCY_LOCKED(9, "DEPENDENCY_LOCKED"), //
    DEPENDENCY_FAILED(10, "DEPENDENCY_FAILED"), //
    DEPENDENCY(11, "DEPENDENCY"), //
    UNKNOWN(-1, "");

    private int value;
    private String stringValue;

    private AvailabilityStatus(final int value, final String stringValue) {
        this.value = value;
        this.stringValue = stringValue;
    }

    public int getValue() {
        return value;
    }

    public static AvailabilityStatus getByValue(final int value) {
        for (AvailabilityStatus status : AvailabilityStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return UNKNOWN;
    }

    public static AvailabilityStatus getByValue(final String value) {
        if (value == null) {
            return UNKNOWN;
        }
        
        for (AvailabilityStatus status : AvailabilityStatus.values()) {
            if (value.equals(status.stringValue)) {
                return status;
            }
        }
        return UNKNOWN;
    }

}
