package org.kornicameister.crypto.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class MathUtilsTest {
    private BigInteger base;
    private int exponent;
    private BigInteger result;

    @Before
    public void setUp() throws Exception {
        this.base = new BigInteger("10");
        this.exponent = 2;
        this.result = this.base.pow(this.exponent);
    }

    @Test
    public void testFastExponentiationBasic() throws Exception {
        System.out.print("Basic :: ");
        Assert.assertEquals("By basic not equals",
                this.result,
                org.kornicameister.crypto.utils.MathUtils.fastExponentiation(
                        this.exponent,
                        this.base,
                        org.kornicameister.crypto.utils.MathUtils.Method.BASIC)
        );
    }

    @Test
    public void testFastExponentiationMontgomery() throws Exception {
        System.out.print("Montgomery :: ");
        Assert.assertEquals("By basic not equals",
                this.result,
                org.kornicameister.crypto.utils.MathUtils.fastExponentiation(
                        this.exponent,
                        this.base,
                        org.kornicameister.crypto.utils.MathUtils.Method.MONTGOMERY)
        );
    }
}
