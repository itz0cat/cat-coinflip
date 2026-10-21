package com.itz0cat.catcoinflip.format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberFormatter {

    private static final DecimalFormat COMMA_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        COMMA_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    private static final long K = 1_000L;
    private static final long M = 1_000_000L;
    private static final long B = 1_000_000_000L;
    private static final long T = 1_000_000_000_000L;
    private static final long QA = 1_000_000_000_000_000L;

    /**
     * Formats a number with commas below threshold, and compact suffixes (K, M, B, T, Qa) at or above.
     * Trim trailing zeros up to 2 decimal places. Never outputs 1000K, rolls over to 1M.
     */
    public static String formatCompact(BigDecimal value, long shortNumbersFrom) {
        if (value == null) {
            return "0";
        }

        boolean negative = value.signum() < 0;
        BigDecimal absVal = value.abs();

        if (absVal.compareTo(BigDecimal.valueOf(shortNumbersFrom)) < 0) {
            // Full number with commas
            BigDecimal rounded = absVal.setScale(0, RoundingMode.HALF_UP);
            String formatted = COMMA_FORMAT.format(rounded.longValue());
            return negative ? "-" + formatted : formatted;
        }

        // Check suffix thresholds with rollover consideration
        String suffix;
        BigDecimal divisor;

        if (absVal.compareTo(BigDecimal.valueOf(QA)) >= 0) {
            suffix = "Qa";
            divisor = BigDecimal.valueOf(QA);
        } else if (absVal.compareTo(BigDecimal.valueOf(T)) >= 0) {
            suffix = "T";
            divisor = BigDecimal.valueOf(T);
        } else if (absVal.compareTo(BigDecimal.valueOf(B)) >= 0) {
            suffix = "B";
            divisor = BigDecimal.valueOf(B);
        } else if (absVal.compareTo(BigDecimal.valueOf(M)) >= 0) {
            suffix = "M";
            divisor = BigDecimal.valueOf(M);
        } else {
            suffix = "K";
            divisor = BigDecimal.valueOf(K);
        }

        BigDecimal scaled = absVal.divide(divisor, 2, RoundingMode.HALF_UP);

        // Check rollover: e.g. 999,999 rounded to 2 decimals divided by 1000 could be 1000.00K -> must roll over to 1M
        if (scaled.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            if ("K".equals(suffix)) {
                suffix = "M";
                scaled = scaled.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            } else if ("M".equals(suffix)) {
                suffix = "B";
                scaled = scaled.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            } else if ("B".equals(suffix)) {
                suffix = "T";
                scaled = scaled.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            } else if ("T".equals(suffix)) {
                suffix = "Qa";
                scaled = scaled.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            }
        }

        // Format up to 2 decimal places with trailing zeros trimmed
        String numStr = stripTrailingZeros(scaled);
        String res = numStr + suffix;
        return negative ? "-" + res : res;
    }

    public static String formatCompact(long value, long shortNumbersFrom) {
        return formatCompact(BigDecimal.valueOf(value), shortNumbersFrom);
    }

    public static String formatFullCommas(BigDecimal value) {
        if (value == null) return "0";
        boolean negative = value.signum() < 0;
        BigDecimal rounded = value.abs().setScale(0, RoundingMode.HALF_UP);
        String res = COMMA_FORMAT.format(rounded.longValue());
        return negative ? "-" + res : res;
    }

    public static String formatFullCommas(long value) {
        return formatFullCommas(BigDecimal.valueOf(value));
    }

    public static String stripTrailingZeros(BigDecimal bd) {
        String str = bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
        if (str.contains(".")) {
            str = str.replaceAll("0*$", "").replaceAll("\\.$", "");
        }
        return str;
    }

    public static String formatPercent(double percent) {
        if (Double.isNaN(percent) || Double.isInfinite(percent)) {
            return "N/A";
        }
        BigDecimal bd = BigDecimal.valueOf(percent).setScale(1, RoundingMode.HALF_UP);
        return stripTrailingZeros(bd) + "%";
    }
}
