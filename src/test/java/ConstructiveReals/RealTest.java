package ConstructiveReals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class RealTest {

    boolean validFloat(float approximation, double value) {
        double widened = approximation;
        double error = widened - value;
        widened += error / 2;
        return ((float) widened) == approximation;

    }

    @Test
    void testConstant() {
        var constant = Real.valueOf(42);

        assertEquals(42, constant.longValue());

        assertEquals(84, constant.approximate(1).longValue());

        assertEquals(21, constant.approximate(-1).longValue());

        assertEquals(10, constant.approximate(-2).longValue());

    }

    @Test
    void testSum() {

        var val = Real.valueOf(42).add(Real.valueOf(13));

        assertEquals(55, val.longValue());

        assertEquals(110, val.approximate(1).longValue());

        assertEquals(27, val.approximate(-1).longValue());

        assertEquals(13, val.approximate(-2).longValue());
    }

    @Test
    void testProduct() {
        var val = Real.valueOf(42).multiply(Real.valueOf(13));
        assertEquals(13 * 42, val.longValue());
    }

    @Test
    void testBigPrecision() {
        var big = Real.valueOf(1 << 62).multiply(Real.valueOf(1 << 62));
        var val = big.add(Real.valueOf(1)).add(big.negate());
        assertEquals(1, val.longValue());
    }

    @Test
    void testSubnormalNumbers() {
        // Test exponent 0 and 1. Both have the same p=-126, and exponent 1 has an
        // implicit leading 1 in the same bit
        for (int i = 1; i < 0x0090_0000; i += 16) {
            float f = Float.intBitsToFloat(i);
            Real positive = Real.valueOf(f);
            Real negative = Real.valueOf(-f);
            assertEquals(i, positive.approximate(149).intValue());
            assertEquals(f, positive.floatValue());
            assertEquals(-i, negative.approximate(149).intValue());
            assertEquals(-f, negative.floatValue());
        }

        for (long i = 1L; i < 0x0090_0000L; i += 16) {
            double d = Double.longBitsToDouble(i << 29);
            Real positive = Real.valueOf(d);
            Real negative = Real.valueOf(-d);
            assertEquals(i, positive.approximate(1022 + 52 - 29).longValue());
            assertEquals(d, positive.doubleValue());
            assertEquals(-i, negative.approximate(1022 + 52 - 29).longValue());
            assertEquals(-d, negative.doubleValue());
        }
    }

    @Test
    void testInverse() {
        var val = Real.valueOf(3).inverse();
        assertEquals(0.3333333f, val.floatValue());
        assertEquals(0.3333333333333333, val.doubleValue());

        var val2 = Real.valueOf(300000000L).inverse();
        assertEquals(0.0000000033333332f, val2.floatValue());
        var r = new Random();
        for (int i = 0; i < 100; i++) {
            double d = Double.longBitsToDouble(r.nextLong());
            // double d = -31057005286.479670;
            var original = Real.valueOf(d);
            var doubleInverse = original.inverse().inverse();
            assertEquals(d, doubleInverse.doubleValue());
        }
    }

    @Test
    void testValueOfDouble() {
        var r = new Random();
        for (int i = 0; i < 1000; i++) {
            double d = Double.longBitsToDouble(r.nextLong());
            if (!Double.isFinite(d)) {
                continue;
            }
            var val = Real.valueOf(d);
            assertEquals(d, val.doubleValue(), () -> "Found double not returning itself: " + Double.toHexString(d));
        }

        for (int i = 0; i < 1000; i++) {
            float f = Float.intBitsToFloat(r.nextInt());
            if (!Float.isFinite(f)) {
                continue;
            }
            var val = Real.valueOf(f);
            assertEquals(f, val.floatValue(),
                    () -> "Found float not returning itself: " + Integer.toHexString(Float.floatToIntBits(f)));
        }
    }

    @Test
    void testMultiplication() {
        var r = new Random();
        int successes = 0;
        for (int i = 0; i < 1000; i++) {
            double d1 = Float.intBitsToFloat(r.nextInt());
            double d2 = Float.intBitsToFloat(r.nextInt());
            double expected = d1 * d2;
            float expectedFloat = (float) expected;
            if (!Float.isFinite(expectedFloat) || expectedFloat == 0.0) {
                continue;
            }
            var val = Real.valueOf(d1).multiply(Real.valueOf(d2));
            float actual = val.floatValue();

            long d1Bits = Double.doubleToLongBits(d1);
            long d2Bits = Double.doubleToLongBits(d2);
            long expectedBits = Double.doubleToLongBits(expected);
            long actualBits = Double.doubleToLongBits(actual);
            float error = (float) (((double) actual) - expected);
            if(actual + error/2 != actual) {
                var x = 7;
            }
            assertEquals(actual, actual + error / 2);
            successes += 1;
        }
        assertTrue(successes > 20);
    }

    @Test
    void testDivision() {
        var r = new Random();
        int successes = 0;
        for (int i = 0; i < 100; i++) {
            double d1 = Float.intBitsToFloat(r.nextInt());
            double d2 = Float.intBitsToFloat(r.nextInt());
            double expected = d1 / d2;
            float expectedFloat = (float) expected;
            if (!Float.isFinite(expectedFloat) || expectedFloat == 0.0) {
                continue;
            }
            var val = Real.valueOf(d1).divide(Real.valueOf(d2));
            float actual = val.floatValue();

            long d1Bits = Double.doubleToLongBits(d1);
            long d2Bits = Double.doubleToLongBits(d2);
            long expectedBits = Double.doubleToLongBits(expected);
            long actualBits = Double.doubleToLongBits(actual);
            float error = (float) (((double) actual) - expected);
            if(actual + error/2 != actual) {
                var x = 7;
            }
            assertEquals(actual, actual + error / 2);
            successes += 1;
        }
        assertTrue(successes > 20);
    }
}
