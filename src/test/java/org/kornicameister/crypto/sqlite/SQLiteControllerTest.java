package org.kornicameister.crypto.sqlite;

import org.junit.Test;
import org.kornicameister.crypto.SchnorrTest;

import static org.junit.Assert.assertTrue;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class SQLiteControllerTest extends SchnorrTest {
    @Test
    public void testIsConnected() throws Exception {
        assertTrue("Mongo is not connected", this.SQLiteController.isConnected());
    }
}
