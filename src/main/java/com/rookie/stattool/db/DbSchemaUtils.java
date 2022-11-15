package com.rookie.stattool.db;

import java.sql.*;

public class DbSchemaUtils {

    private final DbConnection dbConnection;

    public DbSchemaUtils(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public void createDb() {
        try (Statement statement = dbConnection.getConnection().createStatement()) {

            // disconnect all users from database
            String sqlQuery = "SELECT pg_terminate_backend(pg_stat_activity.pid) " +
                              "FROM pg_stat_activity " +
                              "WHERE pg_stat_activity.datname = '" + dbConnection.getDbName() + "'" +
                              "AND pid <> pg_backend_pid();";
            statement.execute(sqlQuery);

            sqlQuery = "DROP DATABASE IF EXISTS " + dbConnection.getDbName();
            statement.execute(sqlQuery);
            System.out.println("Dropped database " + dbConnection.getDbName());

            sqlQuery = "CREATE DATABASE " + dbConnection.getDbName();
            statement.execute(sqlQuery);
            System.out.println("Created new database " + dbConnection.getDbName());
        } catch (SQLException e) {
            System.out.println("Error creating database");
            e.printStackTrace();
        }
    }

    public void dropTables() {
        try (Statement statement = dbConnection.getConnection().createStatement()) {
            String sqlQuery = "DROP TABLE IF EXISTS order_item";
            statement.execute(sqlQuery);
            sqlQuery = "DROP TABLE IF EXISTS\"order\"";
            statement.execute(sqlQuery);
            sqlQuery = "DROP TABLE IF EXISTS customer";
            statement.execute(sqlQuery);
            sqlQuery = "DROP TABLE IF EXISTS product";
            statement.execute(sqlQuery);
        } catch (SQLException e) {
            System.out.println("Error while dropping tables");
            e.printStackTrace();
        }
    }

    public void createTables() {
        try (Statement statement = dbConnection.getConnection().createStatement()) {

            dropTables();

            String sqlQuery;
            sqlQuery = "DROP TABLE IF EXISTS order_item";
            statement.execute(sqlQuery);
            sqlQuery = "DROP TABLE IF EXISTS\"order\"";
            statement.execute(sqlQuery);
            sqlQuery = "DROP TABLE IF EXISTS customer";
            statement.execute(sqlQuery);
            sqlQuery = "DROP TABLE IF EXISTS product";
            statement.execute(sqlQuery);

            sqlQuery =
                    "CREATE TABLE customer (" +
                            "id	            BIGSERIAL PRIMARY KEY, " +
                            "last_name      VARCHAR(60) NOT NULL, " +
                            "first_name 	VARCHAR(60) NOT NULL" +
                    ");";
            statement.execute(sqlQuery);

            sqlQuery =
                    "CREATE TABLE product (" +
                            "id	    BIGSERIAL PRIMARY KEY, " +
                            "name   VARCHAR(200) NOT NULL UNIQUE, " +
                            "price  BIGINT NOT NULL" +
                    ");";
            statement.execute(sqlQuery);

            sqlQuery =
                    "CREATE TABLE \"order\" (" +
                            "id	            BIGSERIAL PRIMARY KEY, " +
                            "customer_id    BIGINT NOT NULL REFERENCES customer (id), " +
                            "total          BIGINT NOT NULL," +
                            "date           TIMESTAMP" +
                    ");";
            statement.execute(sqlQuery);

            sqlQuery =
                    "CREATE TABLE order_item (" +
                            "id	            BIGSERIAL PRIMARY KEY, " +
                            "order_id       BIGINT NOT NULL REFERENCES \"order\" (id), " +
                            "product_id     BIGINT NOT NULL REFERENCES product (id)" +
                    ");";
            statement.execute(sqlQuery);

            System.out.println("Created tables in " + dbConnection.getDbName());
        } catch (SQLException e) {
            System.out.println("Error creating tables");
            e.printStackTrace();
        }
    }
}
