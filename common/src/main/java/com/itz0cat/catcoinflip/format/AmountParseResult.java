package com.itz0cat.catcoinflip.format;

public class AmountParseResult {

    private final boolean success;
    private final long value;
    private final String plainDigits;
    private final String errorMessage;

    private AmountParseResult(boolean success, long value, String plainDigits, String errorMessage) {
        this.success = success;
        this.value = value;
        this.plainDigits = plainDigits;
        this.errorMessage = errorMessage;
    }

    public static AmountParseResult success(long value, String plainDigits) {
        return new AmountParseResult(true, value, plainDigits, null);
    }

    public static AmountParseResult failure(String errorMessage) {
        return new AmountParseResult(false, 0L, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public long getValue() {
        return value;
    }

    public String getPlainDigits() {
        return plainDigits;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
