package org.kornicameister.crypto;

import org.junit.After;
import org.junit.Before;
import org.kornicameister.crypto.sqlite.SQLiteController;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class SchnorrTest {
    protected static final String RESOURCES_MONGO_PROPERTIES = "/home/kornicameister/Dropbox/STUDIA/INFORMATYKA/SEMESTR6/PK/laboratoria/JSchnorr/src/main/resources/schnorr.properties";
    protected static final String RESOURCES_MONGO_PROPERTIES_2 = "D:\\Dropbox\\STUDIA\\INFORMATYKA\\SEMESTR6\\PK\\laboratoria\\JSchnorr\\src\\main\\resources\\schnorr.properties";
    protected SQLiteController sqlitecontroller;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(RESOURCES_MONGO_PROPERTIES));
        this.sqlitecontroller = sqlitecontroller.getConnection(
                properties.getProperty("database"),
                properties.getProperty("host"),
                properties.getProperty("port")
        );
    }

    @After
    public void tearDown() throws Exception {
        if (sqlitecontroller != null) {
            sqlitecontroller.close();
        }
    }
}
