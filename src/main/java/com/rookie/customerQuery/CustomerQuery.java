package com.rookie.customerQuery;

import com.rookie.customerQuery.db.DbConnection;
import com.rookie.customerQuery.db.DbQueries;
import com.rookie.customerQuery.db.DbSchemaUtils;
import com.rookie.customerQuery.db.DbTestData;


public class CustomerQuery {

    final static String helpMessage = "Db settings are inside db.properties. Test data generation settings are in dbTestData.properties\n" +
            "If file doesnt exist then default values from property file inside jar will be applied\n" +
            "--create-db - drops if exist and creates database \n" +
            "--create-tables - drops and creates tables inside db\n" +
            "--create-all - executes --create-db and --create-tables\n" +
            "--generate-data - drops tables, generates test data and inserts it into database";

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
                case "--generate-data":
                    generateData();
                    break;
                case "--test-queries":
                    testQueries();
                    break;
                default:
                    System.out.println(helpMessage);
            }
        } else {
            System.out.println(helpMessage);
        }
    }

        public static void testQueries() {
        try (DbConnection dbConnection = new DbConnection()) {
            dbConnection.loadProperties();
            dbConnection.connect(dbConnection.getDbName());
            DbQueries dbQueries = new DbQueries(dbConnection);
            dbQueries.searchByLastName("Белоусов");
            dbQueries.searchByProductName("Прочный Бетонный Бумажник", 2);
            dbQueries.searchByExpenses(2000, 2500);
            dbQueries.searchBadCustomers(3);
            dbQueries.statsPeriod("2020-01-16", "2020-01-16");
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

    public static void generateData() {
        try (DbConnection dbConnection = new DbConnection()) {
            dbConnection.loadProperties();
            dbConnection.connect(dbConnection.getDbName());

            DbSchemaUtils dbSchemaUtils = new DbSchemaUtils(dbConnection);
            dbSchemaUtils.createTables();

            DbTestData dbTestData = new DbTestData(dbConnection);
            dbTestData.loadProperties();
            dbTestData.generate();
        }
    }
}
