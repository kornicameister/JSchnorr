package org.kornicameister.crypto.schnorr.model;

import org.junit.Test;
import org.kornicameister.crypto.SchnorrCryptoKeyTest;

import static org.junit.Assert.assertNotNull;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class SchnorrCryptoGetKeyTest extends SchnorrCryptoKeyTest {

    @Test
    public void testGetSchnorrKey() throws Exception {
        assertNotNull("Failed to load key", (cryptoKey = cryptoKey.getSchnorrKey(320803968)));
        System.out.println(cryptoKey);
    }
}
