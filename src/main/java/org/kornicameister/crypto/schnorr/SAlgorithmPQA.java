package org.kornicameister.crypto.schnorr;

import org.apache.log4j.Logger;
import org.kornicameister.crypto.schnorr.exception.PQAGenerationException;
import org.kornicameister.crypto.utils.MathUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * SAlgorithmPQA is the utility-like class that can be used
 * in any other DSA-based algorithm. It does compute, store
 * and retrieve following parameters:
 * <dl>
 * <dt>Param <strong>p</strong></dt>
 * <dd>large primary number</dd>
 * <dt>Param <strong>q</strong></dt>
 * <dd>Smaller prime number such that <verb>(p - 1) mod q = 0</verb></dd>
 * <dt>Param <strong>a</strong></dt>
 * <dd>Number that multiplicative order modulo p is q, which means <verb>a^q = 1 mod p</verb></dd>
 * </dl>
 *
 * @author kornicameister
 */
public class SAlgorithmPQA {
    private final static Logger LOGGER = Logger.getLogger(SAlgorithmPQA.class);
    private static final int RADIX = 16;
    private static final int MAX_STEPS = 4096;
    private final Random randomSeed;
    private final SComplexity complexity;
    private BigInteger p, q, a;
    private int certainty;

    private SAlgorithmPQA(SComplexity complexity) {
        this.p = null;
        this.q = null;
        this.a = null;

        this.complexity = complexity;
        this.certainty = 10;

        this.randomSeed = new SecureRandom();

        LOGGER.info(String.format("%s init variables pBitLength=%d, qBitLength=%d, certainty=%d",
                SAlgorithmPQA.class.getSimpleName(),
                this.complexity.getPBitLength(),
                this.complexity.getQBitLength(),
                this.certainty)
        );
    }

    /**
     * Method that takes generates entry points for DSA::Schnorr algorithm
     * based on given {@link SComplexity}. Keys lengths varies significantly
     * between each level, therefore computation times increases with each
     * one of them
     *
     * @param complexity for generator
     * @return fully initialized SAlgorithmPQA object with p,q and a parameters set
     * @throws PQAGenerationException
     */
    public static SAlgorithmPQA generate(SComplexity complexity) throws PQAGenerationException {
        SAlgorithmPQA sAlgorithm = new SAlgorithmPQA(complexity);
        long time = System.nanoTime();

        BigInteger q = generateQFactor(
                sAlgorithm.certainty,
                BigInteger.probablePrime(
                        sAlgorithm.complexity.getQBitLength(),
                        sAlgorithm.randomSeed
                )
        );
        BigInteger p = generatePFactor(
                sAlgorithm.randomSeed,
                sAlgorithm.complexity,
                sAlgorithm.certainty,
                q
        );
        if (p == null) {
            throw new PQAGenerationException("p computation timed off");
        }
        BigInteger a = generateAFactor(
                sAlgorithm.randomSeed,
                p,
                q
        );
        if (a == null) {
            throw new PQAGenerationException("a computation timed off");
        }

        if (q.equals(q.mod(p))) {
            sAlgorithm.q = q;
            sAlgorithm.p = p;
            sAlgorithm.a = a;

            LOGGER.info(String.format("Schnorr algorithm PQA found, took=%dms\np=%s\nq=%s\na=%s",
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time),
                    p.toString(RADIX),
                    q.toString(RADIX),
                    a.toString(RADIX))
            );

            return sAlgorithm;
        }

        return null;
    }

    /**
     * Builds <strong>a</strong> parameters based on given p and q params.
     * <strong>a</strong> must meet following requirement:
     * <i>number whose multiplicative order modulo p is q.</i>
     * <p/>
     * This method builds such a number by:
     * <ul>
     * <li>calculating <b>h</b> which is randomly chosen number <verb>1 < h < p - 1</verb></li>
     * <li>calculating a as follow <verb>a = h^((p-1)/q) mod p</verb></li>
     * <li>checking above result against being equal to 1, if a != 1 than algorithm finishes</li>
     * <li>otherwise h is incremented with one</li>
     * </ul>
     *
     * @param randomSeed seed used throughout whole PQA generation process
     * @param p          already calculated p
     * @param q          already calculated q
     * @return calculated a
     * @see SAlgorithmPQA#generatePFactor(java.util.Random, SComplexity, int, java.math.BigInteger)
     * @see SAlgorithmPQA#generateQFactor(int, java.math.BigInteger)
     */
    private static BigInteger generateAFactor(Random randomSeed, BigInteger p, BigInteger q) {
        long time = System.nanoTime();
        BigInteger a = null, h = MathUtils.random(p.subtract(BigInteger.ONE), randomSeed);
        int step = 0;
        while ((step++) < MAX_STEPS) {
            a = MathUtils.powModFast(h, p.subtract(BigInteger.ONE).divide(q), p);
            if (a.compareTo(BigInteger.ONE) != 0) {
                LOGGER.info(
                        String.format(
                                "Generated a number, took=%dms\nq=%s",
                                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time),
                                a.toString(RADIX)
                        )
                );
                break;
            }
            h = h.add(BigInteger.ONE);
        }
        return a;
    }

    /**
     * Complex method that is used to generate <strong>p</strong> parameter
     * for Schnorr. When computation is started, the method goes by following
     * algorithm:
     * <ol>
     * <li>generating random <i>M</i> that bitLength reaches up to given in complexity for p param</li>
     * <li>calculates M_r as <verb>M_r = M mod 2q</verb></li>
     * <li>calculates p as <verb>p = (M - Mr) + 1</verb></li>
     * <li>checks for p being prime on given certainty</li>
     * <li>if p is prime than algorithm is finised, otherwise all steps must be taken again</li>
     * </ol>
     *
     * @param randomSeed seed used throughout whole generator
     * @param complexity of the generator, determines p's bitLength
     * @param certainty  against which prime test is begin taken
     * @param q          already calculated q
     * @return nice and fresh <p>p</p>
     * @see SComplexity
     */
    private static BigInteger generatePFactor(
            Random randomSeed,
            SComplexity complexity,
            int certainty,
            BigInteger q) {
        long time = System.nanoTime();
        BigInteger m;
        BigInteger mR;
        BigInteger p = null;
        int step = 0;
        while ((step++) < MAX_STEPS) {
            m = new BigInteger(complexity.getPBitLength(), randomSeed);
            mR = m.mod(q.multiply(MathUtils.INTEGER_2));
            p = m.subtract(mR).add(BigInteger.ONE);
            if (p.isProbablePrime(certainty)) {
                LOGGER.info(
                        String.format(
                                "Generated p number, took=%dms\nq=%s",
                                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time),
                                p.toString(RADIX)
                        )
                );
                break;
            }
        }
        return p;
    }

    /**
     * Method calculates <strong>q</strong> param for Schnorr. It is quite simple
     * if compared to <b>p</b> generating method. It does nothing but recomputes
     * provided q value if such does not meet prime condition.
     *
     * @param certainty used to check whether or not q is prime
     * @param q         initial value of q
     * @return calculate q
     */
    private static BigInteger generateQFactor(int certainty, BigInteger q) {
        long time = System.nanoTime();
        while (!q.isProbablePrime(certainty)) {
            q = q.add(BigInteger.ONE);
        }
        LOGGER.info(
                String.format(
                        "Generated q number, took=%dms\nq=%s",
                        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time),
                        q.toString(RADIX)
                )
        );
        return q;
    }

    /**
     * Method to store generated [p,q,a] in {@link Properties}
     * file available under provided path
     *
     * @param pFile path to properties file
     * @throws IOException
     */
    public void toProperties(String pFile) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(pFile));

        LOGGER.info("Saving [p,g,a] to properties");

        properties.setProperty("qNumber", this.q.toString(RADIX));
        properties.setProperty("pNumber", this.p.toString(RADIX));
        properties.setProperty("aNumber", this.a.toString(RADIX));

        properties.store(new FileOutputStream(pFile), "Prime numbers generated");
    }

    /**
     * Method to retrieve generated [p,q,a] stored in some
     * {@link Properties} file.
     *
     * @param pFile path to properties file
     * @throws IOException
     */
    public void fromProperties(String pFile) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(pFile));

        LOGGER.info("Retrieving [p,g] from properties");

        this.q = new BigInteger(properties.getProperty("qNumber"), RADIX);
        this.p = new BigInteger(properties.getProperty("pNumber"), RADIX);
        this.a = new BigInteger(properties.getProperty("aNumber"), RADIX);

        LOGGER.info(String.format("Loaded [p,q,a]=[%s,%s,%s]",
                p.toString(RADIX),
                q.toString(RADIX),
                a.toString(RADIX))
        );
    }

    /**
     * Static method similar to {@link SAlgorithmPQA#generate(SComplexity)} with
     * the difference on the computation, as this method does not actually do them.
     *
     * @param complexity of the algorithm
     * @param pFile      path to properties file
     * @return nice and fresh object of this class
     * @throws IOException
     */
    public static SAlgorithmPQA loadFromProperties(SComplexity complexity, String pFile) throws IOException {
        SAlgorithmPQA pqa = new SAlgorithmPQA(complexity);
        pqa.fromProperties(pFile);
        return pqa;
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

    public SComplexity getComplexity() {
        return complexity;
    }
}
