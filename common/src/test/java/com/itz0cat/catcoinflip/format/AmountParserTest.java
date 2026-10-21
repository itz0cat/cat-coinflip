package com.itz0cat.catcoinflip.format;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class AmountParserTest {

    private final AmountParser parser = new AmountParser();

    @ParameterizedTest
    @CsvSource({
        "28m, 28000000",
        "1.5m, 1500000",
        ".5m, 500000",
        "28M, 28000000",
        "'1,500,000', 1500000",
        "1_500_000, 1500000",
        "150k, 150000",
        "2b, 2000000000",
        "1t, 1000000000000",
        "3qa, 3000000000000000",
        "28000000, 28000000",
        "$28m, 28000000"
    })
    @DisplayName("Valid amount strings parse into exact whole numbers")
    void testValidAmounts(String input, String expectedDigits) {
        AmountParseResult result = parser.parseInput(input);
        assertTrue(result.isSuccess(), "Expected success for " + input + ", got error: " + result.getErrorMessage());
        assertEquals(expectedDigits, result.getPlainDigits());
        assertEquals(Long.parseLong(expectedDigits), result.getValue());
    }

    @ParameterizedTest
    @CsvSource({
        "0.0001k",
        "1.5",
        ".75",
        "0.000001k"
    })
    @DisplayName("Non-whole numbers are rejected with an error")
    void testNonWholeNumbersRejected(String input) {
        AmountParseResult result = parser.parseInput(input);
        assertFalse(result.isSuccess(), "Expected failure for non-whole: " + input);
        assertTrue(result.getErrorMessage().contains("whole number"));
    }

    @ParameterizedTest
    @CsvSource({
        "0",
        "-50",
        "-28m",
        "abc",
        "10xyz",
        "''",
        "'   '",
        "1..5m"
    })
    @DisplayName("Zero, negatives, garbage are rejected")
    void testInvalidRejections(String input) {
        AmountParseResult result = parser.parseInput(input);
        assertFalse(result.isSuccess(), "Expected rejection for: " + input);
    }
}
