package com.itz0cat.catcoinflip.eval;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionEvaluatorTest {

    @Test
    @DisplayName("Basic arithmetic with operator precedence")
    void testBasicArithmetic() {
        assertEquals(14.0, ExpressionEvaluator.evaluate("2 + 3 * 4", null));
        assertEquals(20.0, ExpressionEvaluator.evaluate("(2 + 3) * 4", null));
        assertEquals(2.5, ExpressionEvaluator.evaluate("5 / 2", null));
        assertEquals(1.0, ExpressionEvaluator.evaluate("10 % 3", null));
    }

    @Test
    @DisplayName("Variables and mathematical functions")
    void testVariablesAndFunctions() {
        Map<String, Double> vars = new HashMap<>();
        vars.put("amount", 1000.0);
        vars.put("house_cut", 0.05);

        assertEquals(50.0, ExpressionEvaluator.evaluate("amount * house_cut", vars));
        assertEquals(950.0, ExpressionEvaluator.evaluate("amount * (1 - house_cut)", vars));
        assertEquals(10.0, ExpressionEvaluator.evaluate("min(10, 20)", null));
        assertEquals(20.0, ExpressionEvaluator.evaluate("max(10, 20)", null));
        assertEquals(5.0, ExpressionEvaluator.evaluate("round(4.6)", null));
        assertEquals(4.0, ExpressionEvaluator.evaluate("round(4.2)", null));
    }
}
