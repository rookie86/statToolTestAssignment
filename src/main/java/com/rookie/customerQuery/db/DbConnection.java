package com.rookie.customerQuery.db;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

public class DbConnection implements Closeable {
    private String dbAddress = "127.0.0.1";
    private String dbPort = "5432";
    private String dbName = "customer_query_app";
    private String dbUsername = "postgres";
    private String dbPass = "changeme";
    private Connection connection;

    private void setProperties(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        setDbAddress(properties.getProperty("db_address"));
        setDbPort(properties.getProperty("db_port"));
        setDbName(properties.getProperty("db_name"));
        setDbUsername(properties.getProperty("db_username"));
        setDbPass(properties.getProperty("db_password"));
    }

    public void loadProperties() {
        File dbPropertiesFile = new File("db.properties");
        if (dbPropertiesFile.isFile()) {
            try (InputStream inputStream = Files.newInputStream(Paths.get("db.properties"))) {
                setProperties(inputStream);
            } catch(IOException e) {
                System.out.println("Error reading db.properties");
                e.printStackTrace();
            }
        } else {
            try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("db.properties")) {
                setProperties(inputStream);
            } catch(IOException e) {
                System.out.println("Error reading db.properties inside .jar file");
                e.printStackTrace();
            }
        }
    }

    public void connect(String dbName) {
        // "jdbc:postgresql://127.0.0.1:5432/customer_query_app"
        String connectionString = "jdbc:postgresql://" + getDbAddress() + ":"
                + getDbPort() + "/" + dbName;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        //System.out.println("PostgreSQL JDBC Driver successfully connected");
        Connection connection = null;

        try {
            connection = DriverManager
                    .getConnection(connectionString, dbUsername, dbPass);

        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
        }

        this.connection = connection;
    }

    public void close()  {
        try {
            this.connection.close();
        } catch (SQLException e) {
            System.out.println("Error closing connection");
            e.printStackTrace();
        }
    }

    public String getDbAddress() {
        return dbAddress;
    }

    public void setDbAddress(String dbAddress) {
        this.dbAddress = dbAddress;
    }

    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPass() {
        return dbPass;
    }

    public void setDbPass(String dbPass) {
        this.dbPass = dbPass;
    }

    public Connection getConnection() {
        return connection;
    }
}

