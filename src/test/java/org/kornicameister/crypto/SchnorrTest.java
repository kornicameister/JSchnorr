package org.kornicameister.crypto;

import org.junit.After;
import org.junit.Before;
import org.kornicameister.crypto.schnorr.exception.PQAGenerationException;
import org.kornicameister.crypto.sqlite.SQLiteController;

import java.io.File;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class SchnorrTest {
    protected static final String RESOURCES_MONGO_PROPERTIES = "/home/kornicameister/Dropbox/STUDIA/INFORMATYKA/SEMESTR6/PK/laboratoria/JSchnorr/src/main/resources/schnorr.properties";
    protected static final String RESOURCES_MONGO_PROPERTIES_2 = "D:\\Dropbox\\STUDIA\\INFORMATYKA\\SEMESTR6\\PK\\laboratoria\\JSchnorr\\src\\main\\resources\\schnorr.properties";
    protected SQLiteController sqlitecontroller;

    @Before
    public void setUp() throws Exception, PQAGenerationException {
        this.sqlitecontroller = sqlitecontroller.getConnection(new File(RESOURCES_MONGO_PROPERTIES));
    }

    @After
    public void tearDown() throws Exception {
        if (sqlitecontroller != null) {
            sqlitecontroller.close();
        }
    }
}
