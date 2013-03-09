package org.kornicameister.crypto.sqlite;

import org.apache.log4j.Logger;
import org.kornicameister.crypto.schnorr.SchnorrCryptoKey;
import org.kornicameister.crypto.sqlite.annotations.Column;
import org.kornicameister.crypto.sqlite.annotations.Id;
import org.kornicameister.crypto.sqlite.annotations.Table;
import org.kornicameister.crypto.utils.TimeUtils;

import java.lang.reflect.Field;
import java.math.BigInteger;
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
        if (CONTROLLER.connectionPool == null) {
            LOGGER.warn("Connection pool has been initialized...");
            return;
        }
        try {
            CONTROLLER.connectionPool.close();
        } catch (SQLException e) {
            LOGGER.warn("Failed to close the connection", e);
        }
    }

    public Integer saveObject(SchnorrCryptoKey data) throws SQLException {
        PreparedStatement pStatement = CONTROLLER
                .connectionPool
                .prepareStatement(this.getInsertQuery(data.getClass()));
        List<java.lang.Object> objectList = this.getQueryData(data);

        LOGGER.info(String.format("Ready to use query %s", pStatement));

        for (int i = 0; i < objectList.size(); i++) {
            pStatement.setObject(i + 1, objectList.get(i));
        }

        pStatement.execute();

        ResultSet result = pStatement.getGeneratedKeys();
        result.next();
        Integer idKey = Integer.valueOf(result.getString(1));
        data.setModelId(idKey);
        pStatement.close();

        return data.getModelId();
    }


    public Object getObject(Integer id, Class clazz) throws SQLException {
        String query = this.getSelectQuery(clazz, id);
        PreparedStatement statement = CONTROLLER.connectionPool.prepareStatement(query);

        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        Object cryptoKey = this.setObjectFromQueryResult(clazz, resultSet);
        statement.close();

        return cryptoKey;
    }

    private Object setObjectFromQueryResult(Class clazz, ResultSet resultSet) throws SQLException {
        Object cryptoKey = null;
        Id id;
        Column column;
        try {
            cryptoKey = clazz.newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                if ((id = field.getAnnotation(Id.class)) != null) {
                    column = id.column();
                } else {
                    column = field.getAnnotation(Column.class);
                }
                if (column != null) {
                    field.setAccessible(true);
                    switch (column.type()) {
                        case INTEGER:
                            field.set(cryptoKey, resultSet.getInt(column.name()));
                            break;
                        case BIG_INTEGER:
                            byte[] bytes = resultSet.getBytes(column.name());
                            field.set(cryptoKey, new BigInteger(bytes));
                            break;
                    }
                    field.setAccessible(false);
                }
            }
        } catch (InstantiationException e) {
            LOGGER.fatal("Failed to instance an object", e);
        } catch (IllegalAccessException e) {
            LOGGER.fatal("Failed to access class constructor", e);
        }
        return cryptoKey;
    }

    private String getSelectQuery(Class clazz, Integer idWhere) {
        String fields = this.getQueryColumns(clazz, idWhere).toString();
        Table table = (Table) clazz.getAnnotation(Table.class);
        Id id;
        Column column = null;
        for (Field field : clazz.getDeclaredFields()) {
            if ((id = field.getAnnotation(Id.class)) != null) {
                column = id.column();
                break;
            }
        }

        if (column != null) {
            String query = SELECT_WHERE.replaceFirst("%fields%", fields).replaceFirst("%name%", table.name());
            query = query.replaceFirst("%where%", String.format("%s=%d", column.name(), idWhere));
            return query;
        }

        return null;
    }

    private List<java.lang.Object> getQueryData(Object data) {
        Field[] declaredFields = data.getClass().getDeclaredFields();
        List<java.lang.Object> objects = new ArrayList<>();

        Column column;
        try {
            for (Field field : declaredFields) {
                if ((column = field.getAnnotation(Column.class)) != null && field.getAnnotation(Id.class) == null) {
                    field.setAccessible(true);
                    switch (column.type()) {
                        case INTEGER:
                            objects.add(field.getInt(data));
                            break;
                        case BIG_INTEGER:
                            BigInteger bi = (BigInteger) field.get(data);
                            objects.add(bi.toByteArray());
                            break;
                    }
                    field.setAccessible(false);
                }
            }
        } catch (IllegalAccessException iae) {
            LOGGER.fatal("Failed to retrieved query values by reflection", iae);
            return null;
        }

        return objects;
    }

    private String getInsertQuery(Class clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        StringBuilder columns = this.getQueryColumns(clazz, null), fields = new StringBuilder();
        String tableName = ((Table) clazz.getAnnotation(Table.class)).name();

        for (Field field : declaredFields) {
            if (field.getAnnotation(Column.class) != null && field.getAnnotation(Id.class) == null) {
                {
                    fields.append("?,");
                }
            }
        }
        String query = INSERT.replaceFirst("%name%", tableName);
        query = query.replaceFirst("%fields%", columns.toString());
        query = query.replaceFirst("%values%", fields.deleteCharAt(fields.length() - 1).toString());

        return query;
    }

    private StringBuilder getQueryColumns(Class clazz, Integer idWhere) {
        Field[] declaredFields = clazz.getDeclaredFields();
        Column column;
        Id id;
        StringBuilder columns = new StringBuilder();
        for (Field field : declaredFields) {
            if (idWhere != null && (id = field.getAnnotation(Id.class)) != null) {
                column = id.column();
                columns.append(column.name() + ",");
                LOGGER.info(String.format("Saved %s with values", field.getName()));
            } else if ((column = field.getAnnotation(Column.class)) != null && field.getAnnotation(Id.class) == null) {
                {
                    columns.append(column.name() + ",");
                    LOGGER.info(String.format("Saved %s with values", field.getName()));
                }
            }
        }
        return columns.deleteCharAt(columns.length() - 1);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SQLiteController{");
        sb.append("connectionPool=").append(CONTROLLER.connectionPool);
        sb.append('}');
        return sb.toString();
    }


}
