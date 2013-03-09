package org.kornicameister.crypto.schnorr;

import org.junit.Before;
import org.junit.Test;
import org.kornicameister.crypto.SchnorrTest;

import java.io.FileInputStream;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author kornicameister
 */
public class SAlgorithmPQATest extends SchnorrTest {

    private int certainty;
    private SAlgorithmPQA pqa;

    @Before
    public void setUp() throws Exception {
        this.certainty = 10;
        this.pqa = SAlgorithmPQA.generate();
    }

    @Test
    public void testGenerate() throws Exception {
        assertNotNull("SAlgorithmPQA is null, that is bad", pqa);

        assertTrue(String.format("P is not prime on given certainty = %d", this.certainty),
                pqa.getP().isProbablePrime(this.certainty));
        assertTrue(String.format("Q is not prime on given certainty = %d", this.certainty),
                pqa.getQ().isProbablePrime(this.certainty));
    }

    @Test
    public void testGenerateSave() throws Exception {
        this.pqa.toProperties(SAlgorithmPQATest.RESOURCES_MONGO_PROPERTIES);

        Properties properties = new Properties();
        properties.load(new FileInputStream(SAlgorithmPQATest.RESOURCES_MONGO_PROPERTIES));

        assertNotEquals("Failed to save pNumber", properties.getProperty("pNumber"), "");
        assertNotEquals("Failed to save qNumber", properties.getProperty("qNumber"), "");
        assertNotEquals("Failed to save aNumber", properties.getProperty("aNumber"), "");
    }

    @Test
    public void testGenerateLoad() throws Exception {
        this.pqa.fromProperties(SAlgorithmPQATest.RESOURCES_MONGO_PROPERTIES);

        assertNotNull("Retrieved p is null", this.pqa.getP() != null);
        assertNotNull("Retrieved q is null", this.pqa.getQ() != null);

        assertTrue(String.format("P is not prime on given certainty = %d", this.certainty),
                pqa.getP().isProbablePrime(this.certainty));
        assertTrue(String.format("Q is not prime on given certainty = %d", this.certainty),
                pqa.getQ().isProbablePrime(this.certainty));

    }
}
