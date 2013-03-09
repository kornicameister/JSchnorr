package org.kornicameister.crypto.utils;

import java.math.BigInteger;
import java.util.Random;

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
public class MathUtils {
    public static final BigInteger INTEGER_2 = new BigInteger("2");

    enum Method {
        BASIC,
        MONTGOMERY
    }

    private static BigInteger fastExponentiationBasic(int n, BigInteger x) {
        if (n == 1) {
            return x;
        } else if (n % 2 == 0) {
            return org.kornicameister.crypto.utils.MathUtils
                    .fastExponentiationBasic(n / 2, x.pow(2));
        }
        return x.multiply(org.kornicameister.crypto.utils.MathUtils
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

    public static BigInteger fastExponentiation(int exponent, BigInteger base, final Method method) {
        assert base.compareTo(BigInteger.valueOf(0)) > 0;
        switch (method) {
            case BASIC:
                return org.kornicameister.crypto.utils.MathUtils.fastExponentiationBasic(exponent, base);
            case MONTGOMERY:
                return org.kornicameister.crypto.utils.MathUtils.fastExponentiationMontgomery(exponent, base);
        }
        return null;
    }

    public static BigInteger random(BigInteger high, Random seed) {
        BigInteger result;

        do {
            result = new BigInteger(high.bitLength(), seed);
        } while (result.compareTo(high) >= 0);

        return result;
    }

    public static BigInteger powModFast(BigInteger a, BigInteger b, BigInteger q) {
        BigInteger i, result, x;
        result = BigInteger.ONE;
        x = a.mod(q);

        for (i = BigInteger.ONE; i.compareTo(b) <= 0; i = i.shiftLeft(1)) {
            x = x.mod(q);
            if (b.and(i).compareTo(BigInteger.ZERO) != 0) {
                result = result.multiply(x);
                result = result.mod(q);
            }
            x = x.multiply(x);
        }
        return result;
    }

    public static boolean isOdd(BigInteger base) {
        return !org.kornicameister.crypto.utils.MathUtils.isEven(base);
    }

    public static boolean isEven(BigInteger base) {
        return base.mod(org.kornicameister.crypto.utils.MathUtils.INTEGER_2).equals(BigInteger.ZERO);
    }
}
