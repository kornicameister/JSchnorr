package org.kornicameister.crypto.schnorr.model;

import org.junit.Test;
import org.kornicameister.crypto.SchnorrCryptoKeyTest;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.assertNotNull;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class SchnorrCryptoSetKeyTest extends SchnorrCryptoKeyTest {

    @Test
    public void testSetSchnorrKey() throws Exception {
        cryptoKey.setModelId(modelId);
        cryptoKey.setFactorE(BigInteger.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        cryptoKey.setPrivateKey(BigInteger.valueOf(new Random().nextInt(Integer.MAX_VALUE)));
        cryptoKey.setPublicKey(BigInteger.valueOf(new Random().nextInt(Integer.MAX_VALUE)));

        assertNotNull("Failed to save key", cryptoKey.addSchnorrKey(cryptoKey));
    }


}
