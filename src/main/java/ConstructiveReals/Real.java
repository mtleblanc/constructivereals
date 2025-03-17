package ConstructiveReals;

import java.math.BigInteger;

public abstract class Real extends Number {

    private BigInteger estimate = BigInteger.ZERO;
    private int estimatePrecision = Integer.MIN_VALUE;
    private boolean estimateValid = false;
    private int msd = 0;

    

    /**
     * Returns a scaled approximation of the represented real.
     * | approximate(p) - x*2^p | < 1
     * 
     * @param p precision as a power of 2. Usually negative
     * @return
     */
    public synchronized BigInteger approximate(int p) {
        if (estimateValid && p <= estimatePrecision) {
            return estimate.shiftRight(estimatePrecision - p);
        } else {
            var newValue = computeApproximation(p);
            estimatePrecision = p;
            estimate = newValue;
            estimateValid = true;
            msd = newValue.bitLength() - p;
            return newValue;
        }
    }

    public synchronized int size() {
        return msd;
    }

    abstract BigInteger computeApproximation(int p);

    public static Real valueOf(BigInteger n) {
        return new BigIntegerConstant(n);
    }

    public static Real valueOf(long n) {
        return valueOf(BigInteger.valueOf(n));
    }

    public static Real valueOf(double f) {
        return new DoubleConstant(f);
    }

    public Real add(Real other) {
        return new Sum(this, other);
    }

    public Real negate() {
        return new Negative(this);
    }

    public Real subtract(Real other) {
        return this.add(other.negate());
    }

    public Real multiply(Real other) {
        return new Product(this, other);
    }

    public Real inverse() {
        return new Inverse(this);
    }

    public Real divide(Real other) {
        return new Product(this, other.inverse());
    }

    static class DoubleConstant extends Real {
        private final BigInteger value;
        private final int bias;

        DoubleConstant(double d) {
            long bits = Double.doubleToLongBits(d);
            long mantissaBits = bits & ((1L << 52) - 1);
            long exponentBits = bits & ((1L << 63) - 1) & ~((1L << 52) - 1);
            if (exponentBits != 0) {
                long scaledValue = mantissaBits | (1L << 52);
                value = BigInteger.valueOf(bits < 0 ? -scaledValue : scaledValue);
                bias = ((int) (exponentBits >> 52)) - 1023 - 52;
            } else {
                value = BigInteger.valueOf(bits < 0 ? -mantissaBits : mantissaBits);
                bias = -1022 - 52;
            }
        }

        @Override
        public BigInteger approximate(int p) {
            return value.shiftLeft(p + bias);
        }

        @Override
        BigInteger computeApproximation(int p) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'computeApproximation'");
        }
    }

    static class BigIntegerConstant extends Real {
        private final BigInteger i;

        BigIntegerConstant(BigInteger i) {
            this.i = i;
        }

        @Override
        public BigInteger approximate(int p) {
            return i.shiftLeft(p);
        }

        @Override
        BigInteger computeApproximation(int p) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'computeApproximation'");
        }
    }

    static class Sum extends Real {
        private final Real left;
        private final Real right;

        Sum(Real left, Real right) {
            this.left = left;
            this.right = right;
        }

        @Override
        BigInteger computeApproximation(int p) {
            return left.approximate(p + 2).add(right.approximate(p + 2)).shiftRight(2);
        }

    }

    static class Negative extends Real {
        private final Real other;

        Negative(Real other) {
            this.other = other;
        }

        @Override
        public BigInteger approximate(int p) {
            return other.approximate(p).negate();
        }

        @Override
        BigInteger computeApproximation(int p) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'computeApproximation'");
        }
    }

    static class Product extends Real {
        private final Real left;
        private final Real right;

        Product(Real left, Real right) {
            this.left = left;
            this.right = right;
        }

        @Override
        BigInteger computeApproximation(int p) {
            int leftPrecision = (p + 4) / 2;
            int rightPrecision = p + 4 - leftPrecision;
            BigInteger leftApproximation = left.approximate(leftPrecision);
            BigInteger rightApproximation = right.approximate(rightPrecision);
            while (leftApproximation.bitLength() < 3) {
                leftPrecision += 1;
                leftApproximation = left.approximate(leftPrecision);
            }
            while (rightApproximation.bitLength() < 3) {
                rightPrecision += 1;
                rightApproximation = left.approximate(rightPrecision);
            }
            int extraPrecision = leftPrecision + rightPrecision - p;
            int leftMissing = leftApproximation.bitLength() + 4 - extraPrecision;
            if (leftMissing > 0) {
                // This is correct. The right error is multiplied by the left value and vice
                // versa
                rightPrecision += leftMissing;
                rightApproximation = right.approximate(rightPrecision);
                extraPrecision += leftMissing;
            }
            int rightMissing = rightApproximation.bitLength() + 3 - extraPrecision;
            if (rightMissing > 0) {
                leftPrecision += rightMissing;
                leftApproximation = left.approximate(leftPrecision);
                extraPrecision += rightMissing;
            }
            return leftApproximation.multiply(rightApproximation).shiftRight(extraPrecision);
        }
    }

    static class Inverse extends Real {
        private final Real other;

        Inverse(Real other) {
            this.other = other;
        }

        @Override
        BigInteger computeApproximation(int p) {
            int q = 0;
            BigInteger a = other.approximate(q);
            while (a.bitLength() < 2) {
                q += 1;
                a = other.approximate(q);
            }
            int deficit = -a.bitLength() * 2 + p + q + 2;
            if (deficit > 0) {
                q += deficit;
                a = other.approximate(q);
            }
            return BigInteger.ONE.shiftLeft(p + q).divide(a);
        }

    }

    @Override
    public int intValue() {
        return approximate(0).intValue();
    }

    @Override
    public long longValue() {
        return approximate(0).longValue();
    }

    @Override
    public float floatValue() {
        var p = 0;
        var a = approximate(p);
        while (a.bitLength() != 24) {
            p += 24 - a.bitLength();
            a = approximate(p);
        }
        int v = a.intValue();
        boolean s = v < 0;
        int sb = s ? 1 << 31 : 0;
        int e = (-p + 127 + 23);
        if (e > 0) {
            int m = (s ? -v : v) - (1 << 23);
            return Float.intBitsToFloat(sb | e << 23 | m);
        } else {
            int m = (s ? -v : v);
            return Float.intBitsToFloat(sb | ((m >> -e) >> 1)); 
        }
    }

    @Override
    public double doubleValue() {
        var p = 0;
        var a = approximate(p);
        while (a.bitLength() != 53) {
            p += 53 - a.bitLength();
            a = approximate(p);
        }
        long v = a.longValue();
        boolean s = v < 0;
        long sb = s ? 1L << 63 : 0;
        long e = (-p + 1023L + 52L);
        if (e > 0) {
            long m = (s ? -v : v) - (1L << 52);
            return Double.longBitsToDouble(sb | e << 52 | m);
        } else {
            long m = (s ? -v : v);
            return Double.longBitsToDouble(sb | ((m >> -e) >> 1));
        }

    }
}
