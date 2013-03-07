package org.kornicameister.crypto;

import org.junit.Before;
import org.kornicameister.crypto.schnorr.SchnorrCryptoKey;

import java.util.Random;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class SchnorrCryptoKeyTest extends SchnorrTest {
    protected SchnorrCryptoKey cryptoKey;
    protected Integer modelId;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.cryptoKey = new SchnorrCryptoKey(this.SQLiteController);
        this.modelId = new Random().nextInt();
    }
}
