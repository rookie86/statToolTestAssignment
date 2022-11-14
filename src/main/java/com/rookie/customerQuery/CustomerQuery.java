package com.rookie.customerQuery;

import com.rookie.customerQuery.db.DbConnection;
import com.rookie.customerQuery.db.DbSchemaUtils;


public class CustomerQuery {

    final static String helpMessage = "db settings are inside db.properties, if file doesnt exist then default values from proprty file inside jar will be applied\n" +
                                      "--create-db - drops if exist and creates database \n" +
                                      "--create-tables - drops and creates tables inside db\n" +
                                      "--create-all - executes --create-db and --create-tables";

    public static void main(String[] args) {
        if (args.length == 1) {
            switch (args[0]) {
                case "--create-db":
                    createDb();
                    break;
                case "--create-tables":
                    createTables();
                    break;
                case "--create-all":
                    createDb();
                    createTables();
                    break;
                default:
                    System.out.println(helpMessage);
            }
        } else {
            System.out.println(helpMessage);
        }
    }

    public static void createDb() {
        try (DbConnection dbConnection = new DbConnection()) {
            dbConnection.loadProperties();
            dbConnection.connect("");
            DbSchemaUtils dbSchemaUtils = new DbSchemaUtils(dbConnection);
            dbSchemaUtils.createDb();
        }
    }

    public static void createTables() {
        try (DbConnection dbConnection = new DbConnection()) {
            dbConnection.loadProperties();
            dbConnection.connect(dbConnection.getDbName());
            DbSchemaUtils dbSchemaUtils = new DbSchemaUtils(dbConnection);
            dbSchemaUtils.createTables();
        }
    }
}
