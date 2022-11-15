package com.rookie.stattool;

import com.google.gson.*;
import com.rookie.stattool.db.DbConnection;
import com.rookie.stattool.db.DbQueries;
import com.rookie.stattool.db.DbSchemaUtils;
import com.rookie.stattool.db.DbTestData;
import com.rookie.stattool.json.JsonFileUtils;
import com.rookie.stattool.json.JsonQueries;


public class Main {

    final static String helpMessage = "Db settings are inside db.properties. Test data generation settings are in dbTestData.properties\n" +
            "If file doesnt exist then default values from property file inside jar will be applied\n" +
            "Usage\n" +
            "customerQuery input.json output.json\n" +
            "customerQuery command\n" +
            "--create-db     - drops if exist and creates database \n" +
            "--create-tables - drops and creates tables inside db\n" +
            "--create-all    - executes --create-db and --create-tables\n" +
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
                default:
                    System.out.println(helpMessage);
            }
        } else if (args.length == 2) {
            makeQueries(args[0], args[1]);
        }
        else {
            System.out.println(helpMessage);
        }
        System.out.println("Completed successfully");
    }

    public static void makeQueries (String inputFile, String outputFile) {
        try (DbConnection dbConnection = new DbConnection()) {
            dbConnection.loadProperties();
            dbConnection.connect(dbConnection.getDbName());

            JsonQueries jsonQueries = new JsonQueries(new DbQueries(dbConnection));
            JsonObject jsonObject = JsonFileUtils.readJson(inputFile);
            JsonObject outputJson = jsonQueries.makeQueries(jsonObject);
            JsonFileUtils.writeJson(outputFile, outputJson);
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
