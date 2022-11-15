package com.rookie.customerQuery.db;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DbQueries {

    private final DbConnection dbConnection;

    public DbQueries(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public static final long DAY_IN_MS = 60 * 60 * 24 * 1000;

    public JsonObject statsPeriod(String beginDate, String endDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Timestamp beginTimestamp;
        Timestamp endTimestamp;

        beginTimestamp = new Timestamp(formatter.parse(beginDate).getTime());
        // Added one day to endTimestamp, because by design end date should be included
        endTimestamp = new Timestamp(formatter.parse(endDate).getTime() + DAY_IN_MS);


        String sqlQuery = "SELECT c.id, c.last_name, c.first_name, p.name, SUM(p.price) " +
                "FROM customer AS c " +
                "JOIN \"order\" AS o " +
                "ON c.id = o.customer_id " + "JOIN order_item AS o_i " +
                "ON o.id = o_i.order_id " +
                "JOIN product AS p " +
                "ON o_i.product_id = p.id " +
                "WHERE (o.date > ?) AND (o.date < ?) " +
                "GROUP BY c.id, p.id ORDER BY c.id, SUM (p.price) DESC;";

        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sqlQuery)) {
            preparedStatement.setTimestamp(1, beginTimestamp);
            preparedStatement.setTimestamp(2, endTimestamp);


            ResultSet rs = preparedStatement.executeQuery();

            JsonObject jsonOutput = new JsonObject();

            jsonOutput.addProperty("type", "stat");

            long totalDays = (endTimestamp.getTime() - beginTimestamp.getTime()) / DAY_IN_MS;
            jsonOutput.addProperty("totalDays", String.valueOf(totalDays));

            JsonObject jsonCustomerData = new JsonObject();
            JsonArray jsonPurchases = new JsonArray();
            JsonArray jsonCustomers = new JsonArray();
            long lastId = -1;
            long id;
            int customerCount = 0;
            BigInteger totalAllCustomers = BigInteger.valueOf(0);
            BigInteger total = BigInteger.valueOf(0);

            while (rs.next()) {
                id = rs.getLong(1);
                if (lastId != id) {
                    if (lastId != -1) {
                        jsonCustomerData.add("purchases", jsonPurchases);
                        jsonCustomers.add(jsonCustomerData);
                        jsonCustomerData.addProperty("totalExpenses", total.toString());
                        jsonCustomerData = new JsonObject();
                        jsonPurchases = new JsonArray();
                    }
                    customerCount++;
                    String name = rs.getString(2) + " " + rs.getString(3);
                    jsonCustomerData.addProperty("name", name);
                    //System.out.println(jsonCustomerData);
                    totalAllCustomers = totalAllCustomers.add(total);
                    total = BigInteger.valueOf(0);
                }
                long expenses = rs.getLong(5);
                total = total.add(BigInteger.valueOf(expenses));
                JsonObject productExpenses = new JsonObject();
                productExpenses.addProperty("name", rs.getString(4));
                productExpenses.addProperty("expenses", expenses);
                jsonPurchases.add(productExpenses);
                lastId = id;
            }

            totalAllCustomers = totalAllCustomers.add(total);
            jsonOutput.add("customers", jsonCustomers);
            jsonOutput.addProperty("totalExpenses", totalAllCustomers.toString());
            BigDecimal avgExpenses = new BigDecimal(totalAllCustomers);
            avgExpenses = avgExpenses.divide(BigDecimal.valueOf(customerCount), 2, RoundingMode.HALF_UP);
            jsonOutput.addProperty("avgExpenses", avgExpenses.toString());
            //System.out.println(jsonOutput);
            return jsonOutput;
        } catch (SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
            return null;
        }
    }

    public JsonObject searchBadCustomers(int amount) {

        String sqlQuery = "SELECT c.last_name, c.first_name " +
                "FROM customer AS c " +
                "JOIN \"order\" AS o " +
                "ON c.id = o.customer_id " +
                "JOIN order_item AS o_i " +
                "ON o.id = o_i.order_id " +
                "GROUP BY c.id " +
                "ORDER BY COUNT (o_i.id) ASC LIMIT ?;";

        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sqlQuery)) {
            preparedStatement.setLong(1, amount);

            ResultSet rs = preparedStatement.executeQuery();

            JsonObject jsonOutput = new JsonObject();
            JsonObject jsonCriteria = new JsonObject();
            jsonCriteria.addProperty("badCustomers", amount);
            jsonOutput.add("criteria", jsonCriteria);
            JsonArray jsonResults = new JsonArray();
            while (rs.next()) {
                JsonObject jsonCustomer = new JsonObject();
                jsonCustomer.addProperty("lastName", rs.getString(1));
                jsonCustomer.addProperty("firstName", rs.getString(2));
                jsonResults.add(jsonCustomer);
            }
            jsonOutput.add("results", jsonResults);
            //System.out.println(jsonOutput);
            return jsonOutput;
        } catch (SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
            return null;
        }
    }

    public JsonObject searchByExpenses(long minExpenses, long maxExpenses) {

        String sqlQuery = "SELECT c.last_name, c.first_name " +
                " FROM customer AS c " +
                "JOIN \"order\" AS o " +
                "ON c.id = o.customer_id " +
                "WHERE (o.total > ?) AND (o.total < ?);";

        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sqlQuery)) {
            preparedStatement.setLong(1, minExpenses);
            preparedStatement.setLong(2, maxExpenses);

            ResultSet rs = preparedStatement.executeQuery();

            JsonObject jsonOutput = new JsonObject();
            JsonObject jsonCriteria = new JsonObject();
            jsonCriteria.addProperty("minExpenses", minExpenses);
            jsonCriteria.addProperty("maxExpenses", maxExpenses);
            jsonOutput.add("criteria", jsonCriteria);
            JsonArray jsonResults = new JsonArray();

            while (rs.next()) {
                JsonObject jsonCustomer = new JsonObject();
                jsonCustomer.addProperty("lastName", rs.getString(1));
                jsonCustomer.addProperty("firstName", rs.getString(2));
                jsonResults.add(jsonCustomer);
            }
            jsonOutput.add("results", jsonResults);
            //System.out.println(jsonOutput);
            return jsonOutput;
        } catch (SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
            return null;
        }
    }

    public JsonObject searchByProductName(String productName, int minTimes) {

        String sqlQuery = "SELECT c.last_name, c.first_name " +
                "FROM customer AS c " +
                "JOIN \"order\" AS o " +
                "ON c.id = o.customer_id " +
                "JOIN order_item AS o_i " +
                "ON o.id = o_i.order_id " +
                "WHERE o_i.product_id = (SELECT id FROM product WHERE name = ?) " +
                "GROUP BY c.id HAVING COUNT (c.id) > ?;";

        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, productName);
            preparedStatement.setInt(2, minTimes);

            ResultSet rs = preparedStatement.executeQuery();

            JsonObject jsonOutput = new JsonObject();
            JsonObject jsonCriteria = new JsonObject();
            jsonCriteria.addProperty("productName", productName);
            jsonCriteria.addProperty("minTimes", minTimes);
            jsonOutput.add("criteria", jsonCriteria);
            JsonArray jsonResults = new JsonArray();

            while (rs.next()) {
                JsonObject jsonCustomer = new JsonObject();
                jsonCustomer.addProperty("lastName", rs.getString(1));
                jsonCustomer.addProperty("firstName", rs.getString(2));
                jsonResults.add(jsonCustomer);
            }
            jsonOutput.add("results", jsonResults);
            //System.out.println(jsonOutput);
            return jsonOutput;
        } catch (SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
            return null;
        }
    }

    public JsonObject searchByLastName(String lastName) {
        String sqlQuery = "SELECT last_name, first_name " +
                "FROM customer " +
                "WHERE last_name = ?";

        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, lastName);
            ResultSet rs = preparedStatement.executeQuery();

            JsonObject jsonOutput = new JsonObject();
            JsonObject jsonCriteria = new JsonObject();
            jsonCriteria.addProperty("lastName", lastName);
            jsonOutput.add("criteria", jsonCriteria);
            JsonArray jsonResults = new JsonArray();
            while (rs.next()) {
                JsonObject jsonCustomer = new JsonObject();
                jsonCustomer.addProperty("lastName", rs.getString(1));
                jsonCustomer.addProperty("firstName", rs.getString(2));
                jsonResults.add(jsonCustomer);
            }
            jsonOutput.add("results", jsonResults);
            //System.out.println(jsonOutput);
            return jsonOutput;
        } catch (SQLException e) {
            System.out.println("Error executing query");
            e.printStackTrace();
            return null;
        }
    }
}
