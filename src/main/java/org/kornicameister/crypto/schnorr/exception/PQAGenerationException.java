package org.kornicameister.crypto.schnorr.exception;

/**
 * Exception is thrown if any part of the entry parameters (p,q,a)
 * could not have been generated. Most common reason for it is
 * computation timed off.
 *
 * @author kornicameister
 * @since 0.0.1
 */
public class PQAGenerationException extends RuntimeException {
    public PQAGenerationException(String s) {
        super(s);
    }
}
