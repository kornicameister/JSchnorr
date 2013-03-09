package org.kornicameister.crypto.schnorr.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kornicameister.crypto.SchnorrTest;
import org.kornicameister.crypto.schnorr.SchnorrCryptoKey;

import java.math.BigInteger;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class SchnorrCryptoKeyTest extends SchnorrTest {
    protected SchnorrCryptoKey cryptoKey;
    private BigInteger pubKey = BigInteger.TEN;
    private BigInteger privKey = BigInteger.TEN;
    private BigInteger eFactor = BigInteger.TEN;
    private BigInteger xFactor = BigInteger.TEN;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.cryptoKey = new SchnorrCryptoKey();

        this.cryptoKey.setCryptoEPart(eFactor);
        this.cryptoKey.setCryptoYPart(xFactor);
        this.cryptoKey.setPublicKey(pubKey);
        this.cryptoKey.setPrivateKey(privKey);
    }

    @Test
    public void testAddGet() throws Exception {
        Integer id = SchnorrCryptoKey.addSchnorrKey(cryptoKey, this.sqlitecontroller);
        SchnorrCryptoKey cryptoKey1 = SchnorrCryptoKey.getSchnorrKey(id, this.sqlitecontroller);

        cryptoKey.setPrimaryKey(id);
        Assert.assertEquals(cryptoKey, cryptoKey1);
    }
}
