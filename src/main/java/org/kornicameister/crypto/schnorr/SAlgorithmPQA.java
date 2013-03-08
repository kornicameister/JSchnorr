package org.kornicameister.crypto.schnorr;

import org.apache.log4j.Logger;
import org.kornicameister.crypto.utils.MathExponentiationUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;
import java.util.Random;

/**
 * @author kornicameister
 */
public class SAlgorithmPQA {
    private final static Logger LOGGER = Logger.getLogger(SAlgorithmPQA.class);
    private final int pBitLength;
    private final int qBitLength;
    private final Random randomSeed;
    private BigInteger p, q, a;
    private int certainty;

    private SAlgorithmPQA() {
        this.p = null;
        this.q = null;
        this.a = null;

        this.pBitLength = 1024;
        this.qBitLength = 160;
        this.certainty = 10;

        this.randomSeed = new Random(System.nanoTime());

        LOGGER.info(String.format("%s init variables pBitLength=%d, qBitLength=%d, certainty=%d",
                SAlgorithmPQA.class.getSimpleName(),
                this.pBitLength,
                this.qBitLength,
                this.certainty)
        );
    }

    public static SAlgorithmPQA generate() {
        SAlgorithmPQA sAlgorithm = new SAlgorithmPQA();
        BigInteger q = new BigInteger(sAlgorithm.qBitLength, sAlgorithm.randomSeed);

        LOGGER.info(String.format("Initial q = %s", q.toString(16)));

        while (!q.isProbablePrime(sAlgorithm.certainty)) {
            q = q.add(MathExponentiationUtils.INTEGER_1);
            LOGGER.info(String.format("Looking for q being prime, q=%s", q.toString(16)));
        }

        LOGGER.info(String.format("Found q being prime, q=%s", q.toString(16)));

        BigInteger m, mR, p;
        for (int i = 0; i < 4096; i++) {
            m = new BigInteger(sAlgorithm.pBitLength, sAlgorithm.randomSeed);
            mR = m.mod(q.multiply(MathExponentiationUtils.INTEGER_2));
            p = m.subtract(mR).subtract(MathExponentiationUtils.INTEGER_1);

            LOGGER.info(String.format("Prime checkup for p=%s", p.toString(16)));

            if (p.isProbablePrime(sAlgorithm.certainty)) {
                LOGGER.info(String.format("Primers [p,q]=[%s,%s]", p.toString(16), q.toString(16)));
                sAlgorithm.p = p;
                sAlgorithm.q = q;
                return sAlgorithm;
            }
        }
        return null;
    }

    public void toProperties(String pFile) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(pFile));

        LOGGER.info("Saving [p,g] to properties");

        properties.setProperty("qNumber", this.q.toString(16));
        properties.setProperty("pNumber", this.p.toString(16));

        properties.store(new FileOutputStream(pFile), "Prime numbers generated");
    }

    public void fromPropertes(String pFile) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(pFile));

        LOGGER.info("Retrieving [p,g] from properties");

        this.q = new BigInteger(properties.getProperty("qNumber"), 16);
        this.p = new BigInteger(properties.getProperty("pNumber"), 16);

        LOGGER.info(String.format("Loaded [p,q]=[%s,%s]", p.toString(16), q.toString(16)));
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getQ() {
        return q;
    }

    public BigInteger getA() {
        return a;
    }
}
