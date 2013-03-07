package org.kornicameister.crypto.sqlite;

import org.apache.log4j.Logger;
import org.kornicameister.crypto.schnorr.SchnorrCryptoKey;
import org.kornicameister.crypto.sqlite.annotations.Column;
import org.kornicameister.crypto.sqlite.annotations.Id;
import org.kornicameister.crypto.sqlite.annotations.Table;
import org.kornicameister.crypto.utils.Pair;
import org.kornicameister.crypto.utils.TimeUtils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Master of Mongo for JSchnorr.
 * Init arguments:
 * <ul>
 * <li>database - must be</li>
 * <li>host - must be</li>
 * <li>port - may be</li>
 * </ul>
 *
 * @author kornicameister
 * @since 0.0.1
 */
// @TODO IO in db is working but it is not optimized, to be fixed..caching fields and so on..providing general methods
public class SQLiteController {
    private final static Logger LOGGER = Logger.getLogger(SQLiteController.class);
    private final static String INSERT = "insert into %name% ( %fields% ) values ( %values% )";
    private final static String SELECT = "select %fields% from %name%";
    private final static String SELECT_WHERE = "select %fields% from %name% where %where%";
    private static SQLiteController CONTROLLER;
    private Connection connectionPool;

    public static SQLiteController getConnection(String... data) throws ClassNotFoundException, SQLException {
        if (SQLiteController.CONTROLLER == null) {
            long startTime = System.nanoTime();

            SQLiteController.CONTROLLER = new SQLiteController();

            try {
                Class.forName("org.sqlite.JDBC");
                String url = "jdbc:sqlite:".concat(data[0]);
                SQLiteController.CONTROLLER.connectionPool = DriverManager.getConnection(url);
            } catch (ClassNotFoundException cate) {
                LOGGER.fatal("SQLiteController could not have been initialised due to missing driver", cate);
                throw cate;
            } catch (SQLException sable) {
                LOGGER.fatal("SQLiteController failed to open the connection", sable);
                throw sable;
            }

            LOGGER.info(String.format("SQLiteController is ready and is waiting, ctrl=%s, time=%d",
                    SQLiteController.CONTROLLER,
                    TimeUtils.elapsedFromTime(startTime)));
        }

        return SQLiteController.CONTROLLER;
    }

    public boolean isConnected() {
        return CONTROLLER.connectionPool != null;
    }

    public void close() {
        try {
            CONTROLLER.connectionPool.close();
        } catch (SQLException e) {
            LOGGER.warn("Failed to close the connection", e);
        }
    }


    public Integer add(SchnorrCryptoKey data) throws Exception {
        long startTime = System.nanoTime();
        Table table = data.getClass().getAnnotation(Table.class);
        Column column;
        StringBuilder fields = new StringBuilder();
        List<Pair<Column.Types, Object>> dataList = new ArrayList<>();

        if (table == null) {
            throw new Exception(String.format("%s is not persistent", data.getClass().getSimpleName()));
        }

        /**
         * Reading  stuff from the passed object
         */
        String tableName = table.name();
        final Field[] declaredFields = data.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if ((column = field.getAnnotation(Column.class)) != null) {
                field.setAccessible(true);
                {
                    fields.append(column.name());
                    fields.append(',');
                    switch (column.type()) {
                        case INTEGER:
                            try {
                                dataList.add(new Pair<>(column.type(), field.get(data)));
                            } catch (IllegalAccessException e) {
                                LOGGER.warn(String.format("Could not read data from %s", field.getName()));
                                return null;
                            }
                            break;
                        case BLOB:
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
                            outputStream.writeObject(field.get(data));
                            byte[] rawBytes = byteArrayOutputStream.toByteArray();
                            dataList.add(new Pair<Column.Types, Object>(column.type(), rawBytes));
                        default:
                            break;
                    }
                    LOGGER.info(String.format("Saved %s with values %s", field.getName(), field.get(data)));
                }
                field.setAccessible(false);
            }
        }

        /**
         * Building query
         */
        String query = INSERT;
        for (int step = 0; step < 3; step++) {
            String text;
            switch (step) {
                case 0:
                    query = query.replaceFirst("%name%", tableName);
                    LOGGER.info(String.format("Replaced table name with %s", tableName));
                    break;
                case 1:
                    text = fields.deleteCharAt(fields.length() - 1).toString();
                    query = query.replaceFirst("%fields%", text);
                    LOGGER.info(String.format("Replaced fields with %s", text));
                    break;
                case 2: {
                    StringBuilder questionMarkBuilder = new StringBuilder();
                    for (int i = 0; i < dataList.size(); i++) {
                        questionMarkBuilder.append('?');
                        if (i != dataList.size() - 1) {
                            questionMarkBuilder.append(',');
                        }
                    }
                    text = questionMarkBuilder.toString();
                    query = query.replaceFirst("%values%", text);
                    LOGGER.info(String.format("Replaced values with %s", text));
                    break;
                }
            }
        }

        PreparedStatement pStatement = CONTROLLER.connectionPool.prepareStatement(query);
        LOGGER.info(String.format("Ready to use query %s", pStatement));
        int step = 1;
        for (Pair<Column.Types, Object> pair : dataList) {
            switch (pair.getFirst()) {
                case INTEGER:
                    pStatement.setInt(step++, (Integer) pair.getSecond());
                    break;
                case BLOB:
                    pStatement.setBytes(step++, (byte[]) pair.getSecond());
                    break;
            }
        }

        pStatement.execute();
        pStatement.close();

        LOGGER.info(String.format("SQLiteController :: insert executed, class=%s, fields=%s, time=%d",
                SQLiteController.CONTROLLER,
                fields.toString(),
                TimeUtils.elapsedFromTime(startTime)));

        return data.getModelId();
    }

    public Object get(Integer id, Class<? extends Object> aClass) {
        long startTime = System.nanoTime();
        Table table = aClass.getAnnotation(Table.class);
        final Field[] declaredFields = aClass.getDeclaredFields();
        Column column;
        StringBuilder fields = new StringBuilder();
        String tableName = table.name();

        for (Field field : declaredFields) {
            if ((column = field.getAnnotation(Column.class)) != null) {
                field.setAccessible(true);
                {
                    fields.append(column.name());
                    fields.append(',');
                    LOGGER.info(String.format("Created field with name %s", field.getName()));
                }
                field.setAccessible(false);
            }
        }

        String query = SELECT_WHERE;
        for (int step = 0; step < 3; step++) {
            String text;
            switch (step) {
                case 0:
                    query = query.replaceFirst("%name%", tableName);
                    LOGGER.info(String.format("Replaced table name with %s", tableName));
                    break;
                case 1:
                    text = fields.deleteCharAt(fields.length() - 1).toString();
                    query = query.replaceFirst("%fields%", text);
                    LOGGER.info(String.format("Replaced fields with %s", text));
                    break;
                case 2: {
                    for (Field field : declaredFields) {
                        if (field.getAnnotation(Id.class) != null) {
                            column = field.getAnnotation(Column.class);
                            field.setAccessible(true);
                            {
                                final String format = String.format("%s=%d", column.name(), id);
                                query = query.replaceFirst("%where%", format);
                                LOGGER.info(String.format("Replaced where with %s", format));
                            }
                            field.setAccessible(false);
                        }
                    }
                    break;
                }
            }
        }

        boolean error = false;
        ResultSet rs = null;
        try {
            Statement statement = CONTROLLER.connectionPool.createStatement();
            rs = statement.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.fatal(String.format("Failed to retrieve data by query %s", query), e);
            error = true;
        }

        try {
            if (!error) {
                try {
                    Object aObject = aClass.newInstance();

                    for (Field field : declaredFields) {
                        if ((column = field.getAnnotation(Column.class)) != null) {
                            field.setAccessible(true);
                            {
                                switch (column.type()) {
                                    case INTEGER:
                                        field.set(aObject, rs.getInt(column.name()));
                                        break;
                                    case BLOB:
                                        byte[] rsBytes = rs.getBytes(column.name());
                                        Class<?>[] types = {byte[].class};
                                        Constructor<? extends Object> constructor = field.getType().getConstructor(types);
                                        Object blobObject = constructor.newInstance(rsBytes);
                                        field.set(aObject, blobObject);
                                        break;
                                }
                            }
                            field.setAccessible(false);
                        }
                    }


                    LOGGER.info(String.format("SQLiteController :: select executed, class=%s, fields=%s, time=%d",
                            SQLiteController.CONTROLLER,
                            fields.toString(),
                            TimeUtils.elapsedFromTime(startTime)));
                    rs.close();
                    return aObject;
                } catch (InstantiationException e) {
                    LOGGER.fatal(String.format("Unable to create an object of %s by reflection", aClass.toString()), e);
                } catch (IllegalAccessException e) {
                    LOGGER.fatal(String.format("Unable to access an object's constructor of %s by reflection", aClass.toString()), e);
                } catch (NoSuchMethodException e) {
                    LOGGER.fatal(String.format("No such constructor found for %s", aClass), e);
                } catch (InvocationTargetException e) {
                    LOGGER.fatal(String.format("Failed to invoke constructor for %s", aClass), e);
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            LOGGER.fatal(String.format("Unable to read data from fields for %s", aClass), e);
        }
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SQLiteController{");
        sb.append("connectionPool=").append(CONTROLLER.connectionPool);
        sb.append('}');
        return sb.toString();
    }


}
