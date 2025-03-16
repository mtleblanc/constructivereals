package ConstructiveReals;

import java.math.BigInteger;

public interface Real {
    /**
     * Returns a scaled approximation of the represented real.
     * | approximate(p) - x*2^p | < 1
     * 
     * @param p precision as a power of 2. Usually negative
     * @return
     */
    BigInteger approximate(int p);

    public static Real of(long n) {
        return new Constant(BigInteger.valueOf(n));
    }

    public static Real of(BigInteger n) {
        return new Constant(n);
    }

    default Real add(Real other) {
        return new Sum(this, other);
    }

    default Real negate() {
        return new Negative(this);
    }

    default Real subtract(Real other) {
        return this.add(other.negate());
    }

    default Real multiply(Real other) {
        return new Product(this, other);
    }

    default Real inverse() {
        return new Inverse(this);
    }

    default Real divide(Real other) {
        return new Product(this, other.inverse());
    }

    public record Constant(BigInteger i) implements Real {
        @Override
        public BigInteger approximate(int p) {
            return i.shiftLeft(p);
        }
    }

    abstract static class Cached implements Real {
        BigInteger mostPrecise = BigInteger.ZERO;
        int precision = Integer.MIN_VALUE;

        abstract BigInteger computeApproximation(int p);

        @Override
        public synchronized BigInteger approximate(int p) {
            if (p <= precision) {
                return mostPrecise.shiftRight(precision - p);
            } else {
                var newValue = computeApproximation(p);
                precision = p;
                mostPrecise = newValue;
                return newValue;
            }
        }
    }

    public class Sum extends Cached {
        private final Real left;
        private final Real right;

        public Sum(Real left, Real right) {
            this.left = left;
            this.right = right;
        }

        @Override
        BigInteger computeApproximation(int p) {
            return left.approximate(p + 2).add(right.approximate(p + 2)).shiftRight(2);
        }

    }

    public record Negative(Real other) implements Real {
        @Override
        public BigInteger approximate(int p) {
            return other.approximate(p).negate();
        }
    }

    public class Product extends Cached {
        private final Real left;
        private final Real right;

        public Product(Real left, Real right) {
            this.left = left;
            this.right = right;
        }

        @Override
        BigInteger computeApproximation(int p) {
            int leftPrecision = (p + 3) / 2;
            int rightPrecision = p + 3 - leftPrecision;
            BigInteger leftApproximation = left.approximate(leftPrecision);
            BigInteger rightApproximation = right.approximate(rightPrecision);
            while (leftApproximation.bitLength() < 2) {
                leftPrecision += 1;
                leftApproximation = left.approximate(leftPrecision);
            }
            while (rightApproximation.bitLength() < 2) {
                rightPrecision += 1;
                rightApproximation = left.approximate(rightPrecision);
            }
            int extraPrecision = leftPrecision + rightPrecision - p;
            int leftMissing = leftApproximation.bitLength() + 3 - extraPrecision;
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

    public class Inverse extends Cached {
        private final Real other;

        public Inverse(Real other) {
            this.other = other;
        }

        @Override
        BigInteger computeApproximation(int p) {
            int q = 0;
            BigInteger a = other.approximate(q);
            while (a.bitLength() < 1) {
                q += 1;
                a = other.approximate(q);
            }
            int deficit = a.bitLength() * 2 - p + q;
            if (deficit > 0) {
                q += deficit;
                a = other.approximate(q);
            }
            return BigInteger.ONE.shiftLeft(p+q).divide(a);
        }

    }
}
