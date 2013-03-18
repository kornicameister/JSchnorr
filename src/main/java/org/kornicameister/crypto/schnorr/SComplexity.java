package org.kornicameister.crypto.schnorr;

/**
 * Pretty handy enum that describes DSA (Schnorr) complexity.
 * Contains two numbers where first one describes bit-length of
 * <b>p</b> param and the second one is related to the <b>q's</q>
 * bit-length.
 * 
 * @author kornicameister
 * @since 0.0.1
 */
public enum SComplexity {
    S_320(1024, 160),
    S_448(2048, 224),
    S_512(3072, 256);

    private final int pBitLength;
    private final int qBitLength;

    SComplexity(int i, int i1) {
        this.pBitLength = i;
        this.qBitLength = i1;
    }

    public int getPBitLength() {
        return pBitLength;
    }

    public int getQBitLength() {
        return qBitLength;
    }
}
