package org.kornicameister.crypto.schnorr;

import java.math.BigInteger;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class SAlgorithm {

    public SAlgorithm() {
    }

    /**
     * Method to sing message
     *
     * @param message message
     */
    public void sign(final String message) {
    }

    public void verify() {

    }

    /**
     * Not overriden method, but simple utility allowing to
     * calculate hashCode
     *
     * @param message
     *
     * @return
     */
    public BigInteger hashCode(String message) {
        return null;
    }

    private class SAlgorithmPQA {

    }
}
