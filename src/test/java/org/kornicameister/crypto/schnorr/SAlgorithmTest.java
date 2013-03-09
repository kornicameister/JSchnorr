package org.kornicameister.crypto.schnorr;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kornicameister.crypto.SchnorrTest;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class SAlgorithmTest extends SchnorrTest {
    private SAlgorithm schnorr;
    private String message = "test";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.schnorr = new SAlgorithm(
                SAlgorithmPQA.loadFromProperties(
                        SComplexity.S_320,
                        RESOURCES_MONGO_PROPERTIES
                ),
                this.sqlitecontroller
        );
    }

    @Test
    public void testSignVerify() throws Exception {
        Integer recordId = this.schnorr.sign(this.message);
        Assert.assertNotNull(recordId);

        Assert.assertTrue(this.schnorr.verify(this.message, recordId));
    }
}
