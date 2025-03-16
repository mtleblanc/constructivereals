package ConstructiveReals;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class RealTest {

    @Test
    void testConstant() {
        var constant = Real.of(42);

        assertEquals(42, constant.approximate(0).longValue());

        assertEquals(84, constant.approximate(1).longValue());

        assertEquals(21, constant.approximate(-1).longValue());

        assertEquals(10, constant.approximate(-2).longValue());

    }

    @Test
    void testSum() {

        var val = Real.of(42).add(Real.of(13));

        assertEquals(55, val.approximate(0).longValue());

        assertEquals(110, val.approximate(1).longValue());

        assertEquals(27, val.approximate(-1).longValue());

        assertEquals(13, val.approximate(-2).longValue());
    }

    @Test
    void testProduct() {
        var val = Real.of(42).multiply(Real.of(13));
        assertEquals(13*42, val.approximate(0).longValue());
    }

    @Test 
    void testBigPrecision() {
        var big = Real.of(1<<62).multiply(Real.of(1<<62));
        var val = big.add(Real.of(1)).add(big.negate());
        assertEquals(1, val.approximate(0).longValue());
    }

    @Test
    void testInverse() {
        var val = Real.of(3).inverse();
        assertEquals(1, val.approximate(2).longValue());

        var tough = Real.of(7321).divide(Real.of(992838488282L));
        assertEquals(8107, tough.approximate(40).longValue());

    }
}
