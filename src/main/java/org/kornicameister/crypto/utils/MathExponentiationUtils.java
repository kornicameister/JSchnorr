package org.kornicameister.crypto.utils;

import java.math.BigInteger;

/**
 * Utility class for generating
 * <ul>
 * <li><b>p</b></li>
 * <li><b>q</b></li>
 * <li><b>a</b></li>
 * </ul>
 *
 * @author kornicameister
 * @since 0.0.1
 */
public class MathExponentiationUtils {
    private static final BigInteger INTEGER_0 = new BigInteger("0");
    public static final BigInteger INTEGER_1 = new BigInteger("1");
    public static final BigInteger INTEGER_2 = new BigInteger("2");

    enum Method {
        BASIC,
        MONTGOMERY
    }

    private static BigInteger fastExponentiationBasic(int n, BigInteger x) {
        if (n == 1) {
            return x;
        } else if (n % 2 == 0) {
            return MathExponentiationUtils
                    .fastExponentiationBasic(n / 2, x.pow(2));
        }
        return x.multiply(MathExponentiationUtils
                .fastExponentiationBasic((n - 1) / 2, x.pow(2)));
    }

    // @TODO not working
    private static BigInteger fastExponentiationMontgomery(int exponent, BigInteger base) {
        int bitLength = base.bitLength();
        if (!base.testBit(bitLength - 1)) {
            return base;
        }
        BigInteger x1 = base, x2 = base.pow(2);
        for (int i = bitLength - 2; i >= 0; i--) {
            if (!base.testBit(i)) {
                x2 = x1.multiply(x2);
                x1 = x1.pow(2);
            } else {
                x1 = x1.multiply(x2);
                x2 = x2.pow(2);
            }
        }

        return x1;
    }

    public static BigInteger fastExponentiaton(int exponent, BigInteger base, final Method method) {
        assert base.compareTo(BigInteger.valueOf(0)) > 0;
        switch (method) {
            case BASIC:
                return MathExponentiationUtils.fastExponentiationBasic(exponent, base);
            case MONTGOMERY:
                return MathExponentiationUtils.fastExponentiationMontgomery(exponent, base);
        }
        return null;
    }

    public static boolean isOdd(BigInteger base) {
        return !MathExponentiationUtils.isEven(base);
    }

    public static boolean isEven(BigInteger base) {
        return base.mod(MathExponentiationUtils.INTEGER_2).equals(INTEGER_0);
    }
}
