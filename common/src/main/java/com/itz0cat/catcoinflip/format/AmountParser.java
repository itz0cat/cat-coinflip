package com.itz0cat.catcoinflip.format;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AmountParser {

    private static final Map<String, BigDecimal> DEFAULT_SUFFIXES;
    private static final Pattern INPUT_PATTERN = Pattern.compile("^([0-9.,_]+)\\s*([a-zA-Z]*)$");

    static {
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("k", new BigDecimal("1000"));
        map.put("m", new BigDecimal("1000000"));
        map.put("b", new BigDecimal("1000000000"));
        map.put("t", new BigDecimal("1000000000000"));
        map.put("qa", new BigDecimal("1000000000000000"));
        map.put("q", new BigDecimal("1000000000000000")); // Support 'q' as alias to 'qa'
        DEFAULT_SUFFIXES = Collections.unmodifiableMap(map);
    }

    private final Map<String, BigDecimal> suffixMap;

    public AmountParser() {
        this(DEFAULT_SUFFIXES);
    }

    public AmountParser(Map<String, BigDecimal> customSuffixes) {
        this.suffixMap = customSuffixes != null ? customSuffixes : DEFAULT_SUFFIXES;
    }

    public static AmountParseResult parse(String input) {
        return new AmountParser().parseInput(input);
    }

    public AmountParseResult parseInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return AmountParseResult.failure("Amount cannot be empty");
        }

        String raw = input.trim();

        // Check for negative signs
        if (raw.startsWith("-")) {
            return AmountParseResult.failure("Negative amounts are not allowed");
        }

        // Clean leading '$' if user typed $28m
        if (raw.startsWith("$")) {
            raw = raw.substring(1).trim();
        }

        Matcher matcher = INPUT_PATTERN.matcher(raw);
        if (!matcher.matches()) {
            return AmountParseResult.failure("Invalid amount format: '" + input + "'");
        }

        String numberPart = matcher.group(1).replace(",", "").replace("_", "");
        String suffixPart = matcher.group(2).toLowerCase(Locale.ROOT);

        // Disallow consecutive dots or multiple dots
        if (numberPart.chars().filter(ch -> ch == '.').count() > 1) {
            return AmountParseResult.failure("Invalid decimal format: '" + input + "'");
        }

        // Handle bare dot without digits
        if (numberPart.equals(".")) {
            return AmountParseResult.failure("Invalid number: '" + input + "'");
        }

        BigDecimal baseNumber;
        try {
            baseNumber = new BigDecimal(numberPart);
        } catch (NumberFormatException e) {
            return AmountParseResult.failure("Could not parse number: '" + numberPart + "'");
        }

        BigDecimal multiplier = BigDecimal.ONE;
        if (!suffixPart.isEmpty()) {
            multiplier = suffixMap.get(suffixPart);
            if (multiplier == null) {
                return AmountParseResult.failure("Unknown amount suffix: '" + suffixPart + "'");
            }
        }

        BigDecimal total = baseNumber.multiply(multiplier);

        // Check if zero or negative
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return AmountParseResult.failure("Amount must be greater than zero");
        }

        // Check whole number (no fractional cents / parts)
        if (total.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            return AmountParseResult.failure("Amount must result in a whole number (got " + total.toPlainString() + ")");
        }

        // Check upper limit (Long.MAX_VALUE)
        BigDecimal maxLong = new BigDecimal(Long.MAX_VALUE);
        if (total.compareTo(maxLong) > 0) {
            return AmountParseResult.failure("Amount exceeds maximum limit (" + Long.MAX_VALUE + ")");
        }

        BigInteger wholeAmount = total.toBigIntegerExact();
        return AmountParseResult.success(wholeAmount.longValue(), wholeAmount.toString());
    }
}
