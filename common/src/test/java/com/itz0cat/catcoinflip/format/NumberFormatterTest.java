package com.itz0cat.catcoinflip.format;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class NumberFormatterTest {

    private static final long THRESHOLD = 100_000L;

    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "999, 999",
        "1250, '1,250'",
        "99999, '99,999'"
    })
    @DisplayName("Numbers below 100,000 are shown in full with commas")
    void testBelowThreshold(long input, String expected) {
        assertEquals(expected, NumberFormatter.formatCompact(input, THRESHOLD));
    }

    @ParameterizedTest
    @CsvSource({
        "100000, 100K",
        "150000, 150K",
        "1230000, 1.23M",
        "1200000, 1.2M",
        "1000000, 1M",
        "22000000000, 22B",
        "1500000000000, 1.5T",
        "3000000000000000, 3Qa"
    })
    @DisplayName("Numbers at or above threshold use K, M, B, T, Qa with trimmed decimals")
    void testAboveThreshold(long input, String expected) {
        assertEquals(expected, NumberFormatter.formatCompact(input, THRESHOLD));
    }

    @Test
    @DisplayName("Rollover: 999,999.99 rolls over to 1M, never 1000K")
    void testRollover() {
        BigDecimal nearMillion = new BigDecimal("999999.99");
        String formatted = NumberFormatter.formatCompact(nearMillion, THRESHOLD);
        assertEquals("1M", formatted);
        assertFalse(formatted.contains("1000K"));
    }

    @Test
    @DisplayName("Percent formatting trims trailing zeroes")
    void testPercentFormatting() {
        assertEquals("50%", NumberFormatter.formatPercent(50.0));
        assertEquals("49.5%", NumberFormatter.formatPercent(49.5));
        assertEquals("0%", NumberFormatter.formatPercent(0.0));
    }
}
