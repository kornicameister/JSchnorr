package org.kornicameister.crypto.schnorr;

import org.apache.log4j.Logger;
import org.kornicameister.crypto.sqlite.SQLiteController;
import org.kornicameister.crypto.utils.MathUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Random;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class SAlgorithm {
    private final static Logger LOGGER = Logger.getLogger(SAlgorithm.class);
    private final SAlgorithmPQA pqa;
    private final Random seed;
    private final SQLiteController controller;
    private int keyLength;

    public SAlgorithm(SAlgorithmPQA sAlgorithmPQA, SQLiteController controller) {
        this.pqa = sAlgorithmPQA;
        this.keyLength = 0;
        this.controller = controller;

        switch (this.pqa.getComplexity()) {
            case S_320:
                this.keyLength = 160;
                break;
            case S_448:
                this.keyLength = 224;
                break;
            case S_512:
                this.keyLength = 256;
                break;
        }

        this.seed = new Random(System.nanoTime());
    }

    /**
     * Method to sing message
     *
     * @param message message
     */
    public Integer sign(final String message) throws NoSuchAlgorithmException {
        SchnorrCryptoKey cryptoKey = new SchnorrCryptoKey();
        BigInteger privKey, pubKey;
        BigInteger x, y, e, r;

        // keys
        privKey = new BigInteger(this.keyLength, this.seed);
        pubKey = this.pqa.getA().modPow(privKey.negate(), this.pqa.getP());

        // signing
        r = new BigInteger(this.keyLength, this.seed);
        x = MathUtils.powModFast(this.pqa.getA(), r, this.pqa.getP());  //  x = a ^ r mod p
        e = this.hashCode(message, x);                                  //  hashing message H(M,e)
        y = r.add(privKey.multiply(e)).mod(this.pqa.getQ());            //  y = (r + s * e) mod q

        // saving
        cryptoKey.setPrivateKey(privKey);
        cryptoKey.setPublicKey(pubKey);
        cryptoKey.setFactorE(e);
        cryptoKey.setFactorY(y);
        cryptoKey.setModelId((int) System.nanoTime());

        try {
            return SchnorrCryptoKey.addSchnorrKey(cryptoKey, this.controller);
        } catch (Exception e1) {
            LOGGER.fatal("Failed to save crypto key", e1);
        }
        return null;
    }

    public boolean verify(String message, Integer recordId) throws NoSuchAlgorithmException {
        try {
            BigInteger x1, e1, tmp;
            SchnorrCryptoKey cryptoKey = SchnorrCryptoKey.getSchnorrKey(recordId, this.controller);

            // x1 = ((a^y)*(v^e)) mod p

            x1 = MathUtils.powModFast(this.pqa.getA(), cryptoKey.getFactorY(), this.pqa.getP());            // a^y mod p
            tmp = MathUtils.powModFast(cryptoKey.getPublicKey(), cryptoKey.getFactorE(), this.pqa.getP());  // v^e mod p
            x1 = x1.multiply(tmp).mod(this.pqa.getP());                                                     // x1 = ((a^y)*(v^e)) mod p

            e1 = this.hashCode(message, x1);                                                                // e1 = H(M,x1)

            return e1.equals(cryptoKey.getFactorE());
        } catch (SQLException e) {
            LOGGER.fatal("Failed to load crypto key", e);
        }
        return false;
    }

    /**
     * Not overridden method, but simple utility allowing to
     * calculate hashCode
     *
     * @param message
     * @return
     */
    public BigInteger hashCode(String message, BigInteger x) throws NoSuchAlgorithmException {
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

        sha512.update(message.getBytes());
        sha512.update(x.toByteArray());

        return new BigInteger(1, sha512.digest());
    }

    public SAlgorithmPQA getPQA() {
        return pqa;
    }

}
