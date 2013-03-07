package org.kornicameister.crypto.sqlite;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public enum SQLiteControllerStatics {
    DB_NAME_INDEX(0),
    HOST_INDEX(1),
    PORT_INDEX(2);

    private int index;

    SQLiteControllerStatics(int i) {
        this.index = i;
    }

    public int getIndex() {
        return this.index;
    }
}
