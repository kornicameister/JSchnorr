package org.kornicameister.crypto.schnorr;

import org.apache.log4j.Logger;
import org.kornicameister.crypto.sqlite.SQLiteController;
import org.kornicameister.crypto.utils.MathUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Random;

/**
 * Class handling actual tasks that Schnorr should provide.
 * Therefore this class can sign and verify the message.
 * Its working is based on {@link SAlgorithmPQA} class that
 * holds algorithm's parameters. To work properly this class requires
 * working {@link SQLiteController}, by which it can save and retrieve
 * {@link SchnorrCryptoKey} objects.
 * </br>
 * <h2>About keys and functionality [private,public]</h2>
 * <h3>Keys</h3>
 * <ol>
 * <li>Private key - Is randomly chosen number less than q</li>
 * <li>Public key - Is computed from this equation: <pre>pub_key = (a ^ priv_key) mod p</pre></li>
 * </ol>
 * <h3>Signing</h3>
 * <ol>
 * <li>Generating randomly chosen kSingParam smaller than q</li>
 * <li>Computing rSignParam from this equation: <pre>rSignParam = (a ^ kSingParam) mod p</pre></li>
 * <li>Generating signature
 * <ol>
 *     <li>eSignParam computed by hash coding the message via rSignParam seed</li>
 *     <li>ySignParam computed from this equation: <pre>ySignParam = kSignParam + (privKey * eSignParam) mod q</pre></li>
 * </ol>
 * </li>
 * </ol>
 * <h3>Verifying</h3>
 * Verifying is the process that does exactly same job as signing but in reverse way.
 * <ol>
 * <li>Computing x' from this equation = <pre>x' = ((a ^ ySignParam) * (pubKey * eSignParam)) mod p</pre></li>
 * <li>Computing hash code again using provided input and computed x'</li>
 * <li>compares eSignParam' with persisted eSingParam from cryptoKey used in signing</li>
 * </ol>
 *
 * @author kornicameister
 * @since 0.0.1
 */
public class SAlgorithm {
    private final static Logger LOGGER = Logger.getLogger(SAlgorithm.class);
    private static final String SHA_512 = "SHA-512";
    private final SAlgorithmPQA pqa;
    private final Random seed;
    private final SQLiteController controller;
    private int keyLength;

    /**
     * Constructs new SAlgorithm.
     *
     * @param sAlgorithmPQA placeholder for entry points for algorithm
     * @param controller    database bridge
     */
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
     * Method to sing message. Result of this method
     * is an integer that must be remembered in order to
     * retrieve valid {@link SchnorrCryptoKey} object holding
     * <strong>eSingParam</strong> and <strong>ySignParam</strong> parameters which
     * are further recognized as signature.
     * <p/>
     * <pre>Notice</pre>
     * <ul>
     * <li><a href="http://en.wikipedia.org/wiki/Digital_Signature_Algorithm#Signing">Wiki Schnorr</a></li>
     * </ul>
     *
     * @param message message
     *
     * @return id of the record
     *
     * @see SchnorrCryptoKey
     */
    public Integer sign(final String message) throws NoSuchAlgorithmException {
        SchnorrCryptoKey cryptoKey = new SchnorrCryptoKey();
        BigInteger privKey, pubKey;
        BigInteger rSignParam, ySignParam, eSingParam, kSignParam;

        // keys
        privKey = new BigInteger(this.keyLength - 1, this.seed);
        pubKey = this.pqa.getA().modPow(privKey.negate(), this.pqa.getP());

        // signing
        kSignParam = new BigInteger(this.keyLength - 1, this.seed);
        rSignParam = MathUtils.powModFast(this.pqa.getA(), kSignParam, this.pqa.getP());    // x = a^r mod p

        // signature
        eSingParam = this.hashCode(message, rSignParam);                                    // H(M, e)
        ySignParam = kSignParam.add(privKey.multiply(eSingParam)).mod(this.pqa.getQ());     // y = (r + s*e) mod q

        // saving
        cryptoKey.setPrivateKey(privKey);
        cryptoKey.setPublicKey(pubKey);
        cryptoKey.setCryptoEPart(eSingParam);
        cryptoKey.setCryptoYPart(ySignParam);

        LOGGER.info(String.format("Successfully encrypted message %s with key %s", message, cryptoKey));

        try {
            return SchnorrCryptoKey.addSchnorrKey(cryptoKey, this.controller);
        } catch (Exception e1) {
            LOGGER.fatal("Failed to save crypto key", e1);
        }
        return null;
    }

    /**
     * @see SAlgorithm#sign(String)
     */
    public Integer sign(FileInputStream message) throws IOException, NoSuchAlgorithmException {
        SchnorrCryptoKey cryptoKey = new SchnorrCryptoKey();
        BigInteger privKey, pubKey;
        BigInteger rSignParam, ySignParam, eSingParam, kSignParam;

        // keys
        privKey = new BigInteger(this.keyLength - 1, this.seed);
        pubKey = this.pqa.getA().modPow(privKey.negate(), this.pqa.getP());

        // signing
        kSignParam = new BigInteger(this.keyLength - 1, this.seed);
        rSignParam = MathUtils.powModFast(this.pqa.getA(), kSignParam, this.pqa.getP());    // x = a^r mod p

        // signature
        eSingParam = this.hashCode(message, rSignParam);                                    // H(M, e)
        ySignParam = kSignParam.add(privKey.multiply(eSingParam)).mod(this.pqa.getQ());     // y = (r + s*e) mod q

        // saving
        cryptoKey.setPrivateKey(privKey);
        cryptoKey.setPublicKey(pubKey);
        cryptoKey.setCryptoEPart(eSingParam);
        cryptoKey.setCryptoYPart(ySignParam);

        LOGGER.info(String.format("Successfully encrypted message %s with key %s", message, cryptoKey));

        try {
            return SchnorrCryptoKey.addSchnorrKey(cryptoKey, this.controller);
        } catch (Exception e1) {
            LOGGER.fatal("Failed to save crypto key", e1);
        }
        return null;
    }

    /**
     * Method validating inputted message against {@link SchnorrCryptoKey} retrieved by provided recordId
     *
     * @param message  to be verified
     * @param recordId against signature hidden in the record available in db
     *
     * @return true if message is valid
     *
     * @throws NoSuchAlgorithmException
     */
    public boolean verify(String message, Integer recordId) throws NoSuchAlgorithmException {
        try {
            BigInteger x1, e1, tmp;
            SchnorrCryptoKey cryptoKey = SchnorrCryptoKey.getSchnorrKey(recordId, this.controller);

            // x1 = ((a^y)*(v^e)) mod p

            x1 = MathUtils.powModFast(this.pqa.getA(),
                    cryptoKey.getCryptoYPart(),
                    this.pqa.getP());            // a^y mod p
            tmp = MathUtils.powModFast(cryptoKey.getPublicKey(),
                    cryptoKey.getCryptoEPart(),
                    this.pqa.getP());           // v^e mod p
            x1 = x1.multiply(tmp).mod(this.pqa.getP()); // x1 = ((a^y)*(v^e)) mod p

            e1 = this.hashCode(message, x1);             // e1 = H(M,x1)

            return e1.equals(cryptoKey.getCryptoEPart());
        } catch (SQLException e) {
            LOGGER.fatal("Failed to load crypto key", e);
        }
        return false;
    }

    /**
     * @see SAlgorithm#verify(String, Integer)
     */
    public boolean verify(FileInputStream message, Integer recordId) throws NoSuchAlgorithmException, IOException {
        try {
            BigInteger x1, e1, tmp;
            SchnorrCryptoKey cryptoKey = SchnorrCryptoKey.getSchnorrKey(recordId, this.controller);

            // x1 = ((a^y)*(v^e)) mod p

            x1 = MathUtils.powModFast(this.pqa.getA(),
                    cryptoKey.getCryptoYPart(),
                    this.pqa.getP());            // a^y mod p
            tmp = MathUtils.powModFast(cryptoKey.getPublicKey(),
                    cryptoKey.getCryptoEPart(),
                    this.pqa.getP());           // v^e mod p
            x1 = x1.multiply(tmp).mod(this.pqa.getP()); // x1 = ((a^y)*(v^e)) mod p

            e1 = this.hashCode(message, x1);             // e1 = H(M,x1)

            return e1.equals(cryptoKey.getCryptoEPart());
        } catch (SQLException e) {
            LOGGER.fatal("Failed to load crypto key", e);
        }
        return false;
    }

    /**
     * Not overridden method, but simple utility allowing to
     * calculate hashCode
     *
     * @param message   message to calculate hash code
     * @param signParam signing param
     *
     * @return hash result
     */
    public BigInteger hashCode(String message, BigInteger signParam) throws NoSuchAlgorithmException {
        MessageDigest sha512 = MessageDigest.getInstance(SHA_512);

        sha512.update(message.getBytes());
        sha512.update(signParam.toByteArray());

        return new BigInteger(1, sha512.digest());
    }

    /**
     * Similar to the hashCode method getting String on the input. However this
     * method is extended to handle case when there is a strong need to hash an input stream
     *
     * @param message   message to calculate hash code
     * @param signParam signing param
     *
     * @return hash result
     *
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public BigInteger hashCode(FileInputStream message, BigInteger signParam) throws NoSuchAlgorithmException, IOException {
        InputStream in = new BufferedInputStream(message);
        byte[] block = new byte[1024];
        int count;
        MessageDigest sha512 = MessageDigest.getInstance(SHA_512);

        while ((count = in.read(block)) != -1)
            sha512.update(block, 0, count);
        sha512.update(signParam.toByteArray());

        return new BigInteger(1, sha512.digest());
    }
}
