package com.itz0cat.catcoinflip.eval;

import java.util.Map;

/**
 * Safe expression evaluator supporting +, -, *, /, %, min, max, round, and variable substitutions.
 * Strictly mathematical without any scripting or execution capabilities.
 */
public class ExpressionEvaluator {

    public static double evaluate(String expression, Map<String, Double> variables) {
        if (expression == null || expression.trim().isEmpty()) {
            return 0.0;
        }
        return new Parser(expression.trim(), variables).parse();
    }

    private static class Parser {
        private final String input;
        private final Map<String, Double> variables;
        private int pos = -1;
        private int ch;

        Parser(String input, Map<String, Double> variables) {
            this.input = input;
            this.variables = variables;
            nextChar();
        }

        private void nextChar() {
            ch = (++pos < input.length()) ? input.charAt(pos) : -1;
        }

        private boolean eat(int charToEat) {
            while (ch == ' ' || ch == '\t') nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        public double parse() {
            double x = parseExpression();
            if (pos < input.length()) {
                throw new IllegalArgumentException("Unexpected character: " + (char) ch);
            }
            return x;
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor | term `%` factor
        // factor = `+` factor | `-` factor | `(` expression `)` | number | function | variable

        private double parseExpression() {
            double x = parseTerm();
            for (;;) {
                if (eat('+')) x += parseTerm();
                else if (eat('-')) x -= parseTerm();
                else return x;
            }
        }

        private double parseTerm() {
            double x = parseFactor();
            for (;;) {
                if (eat('*')) x *= parseFactor();
                else if (eat('/')) {
                    double divisor = parseFactor();
                    x = (divisor == 0) ? 0 : (x / divisor);
                } else if (eat('%')) {
                    double divisor = parseFactor();
                    x = (divisor == 0) ? 0 : (x % divisor);
                } else {
                    return x;
                }
            }
        }

        private double parseFactor() {
            if (eat('+')) return +parseFactor();
            if (eat('-')) return -parseFactor();

            double x;
            int startPos = this.pos;
            if (eat('(')) {
                x = parseExpression();
                eat(')');
            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                x = Double.parseDouble(input.substring(startPos, this.pos));
            } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') {
                while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_') nextChar();
                String name = input.substring(startPos, this.pos);
                if (eat('(')) {
                    // Function call: min(a, b), max(a, b), round(a)
                    if (name.equalsIgnoreCase("min")) {
                        double a = parseExpression();
                        eat(',');
                        double b = parseExpression();
                        x = Math.min(a, b);
                    } else if (name.equalsIgnoreCase("max")) {
                        double a = parseExpression();
                        eat(',');
                        double b = parseExpression();
                        x = Math.max(a, b);
                    } else if (name.equalsIgnoreCase("round")) {
                        double a = parseExpression();
                        x = Math.round(a);
                    } else {
                        throw new IllegalArgumentException("Unknown function: " + name);
                    }
                    eat(')');
                } else {
                    // Variable lookup
                    if (variables != null && variables.containsKey(name)) {
                        x = variables.get(name);
                    } else {
                        x = 0.0;
                    }
                }
            } else {
                throw new IllegalArgumentException("Unexpected character: " + (char) ch);
            }

            return x;
        }
    }
}
