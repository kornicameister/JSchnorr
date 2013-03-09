package org.kornicameister.crypto.sqlite.enums;

import java.math.BigInteger;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public enum ColumnType {
    INTEGER(Integer.class),
    BIG_INTEGER(BigInteger.class),
    STRING(String.class);

    private final Class<?> clazz;

    ColumnType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}