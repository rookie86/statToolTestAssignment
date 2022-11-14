package com.rookie.customerQuery.db;

import net.datafaker.Faker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DbTestData {

    private int customers;
    private int products;
    private int productsPriceMin;
    private int productsPriceMax;
    private int orders;
    private int ordersItemsMin;
    private int ordersItemsMax;
    private String ordersDateStart;
    private String ordersDateEnd;
    private final DbConnection dbConnection;

    public DbTestData(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    private void setProperties(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        try {
            setCustomers(Integer.parseInt(properties.getProperty("customers")));
            setProducts(Integer.parseInt(properties.getProperty("products")));
            setProductsPriceMin(Integer.parseInt(properties.getProperty("products.price.min")));
            setProductsPriceMax(Integer.parseInt(properties.getProperty("products.price.max")));
            setOrders(Integer.parseInt(properties.getProperty("orders")));
            setOrdersItemsMin(Integer.parseInt(properties.getProperty("orders.items.min")));
            setOrdersItemsMax(Integer.parseInt(properties.getProperty("orders.items.max")));
            setOrdersDateStart(properties.getProperty("orders.date.start"));
            setOrdersDateEnd(properties.getProperty("orders.date.end"));
        } catch (Exception e) {
            System.out.println("Error parsing properties file values");
            e.printStackTrace();
        }
    }

    public void loadProperties() {
        File dbPropertiesFile = new File("dbTestData.properties");
        if (dbPropertiesFile.isFile()) {
            try (InputStream inputStream = Files.newInputStream(Paths.get("dbTestData.properties"))) {
                setProperties(inputStream);
            } catch (IOException e) {
                System.out.println("Error reading dbTestData.properties");
                e.printStackTrace();
            }
        } else {
            try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("dbTestData.properties")) {
                setProperties(inputStream);
            } catch (IOException e) {
                System.out.println("Error reading db.properties inside .jar file");
                e.printStackTrace();
            }
        }
    }

    public void generate() {
        try {

            System.out.println("Generating test data. It may take some time...");
            Faker faker = new Faker(new Locale("ru"));
            Random r = new Random();

            PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(
                    "INSERT INTO customer (id, last_name, first_name)" +
                         "VALUES (?, ?, ?)");

            for (int i = 0; i < getCustomers(); i++) {
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, faker.name().lastName());
                preparedStatement.setString(3, faker.name().firstName());
                preparedStatement.executeUpdate();
            }

            preparedStatement.close();
            System.out.println("Created customer test data");



            List<Integer> productPrices = new ArrayList<>(getProducts());
            HashSet<String> productNames = new HashSet<>();
            preparedStatement = dbConnection.getConnection().prepareStatement(
                    "INSERT INTO product (id, name, price)" +
                         "VALUES (?, ?, ?)");
            String productName;
            for (int i = 0; i < getProducts(); i++) {
                preparedStatement.setInt(1, i);

                //check if this product name wasn't used before, as this column is unique
                do {
                    productName = faker.commerce().productName();
                } while(productNames.contains(productName));
                productNames.add(productName);
                preparedStatement.setString(2, productName);

                int price = getProductsPriceMin() + r.nextInt(getProductsPriceMax() - getProductsPriceMin());
                preparedStatement.setLong(3, price);
                productPrices.add(price);

                preparedStatement.executeUpdate();
            }
            preparedStatement.close();
            System.out.println("Created product test data");

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");

            preparedStatement = dbConnection.getConnection().prepareStatement(
                    "INSERT INTO order_item (id, order_id, product_id)" +
                    "VALUES (?, ?, ?)");
            PreparedStatement preparedStatement_order = dbConnection.getConnection().prepareStatement(
                    "INSERT INTO \"order\" (id, customer_id, total, date)" +
                            "VALUES (?, ?, ?, ?)");
            int orderItemId = 0;

            for (int i = 0; i < getOrders(); i++) {
                long total = 0;
                int numOfItems = getOrdersItemsMin() + r.nextInt(getOrdersItemsMax() - getOrdersItemsMin());
                for (int j = 0; j < numOfItems; j++) {
                    int productId = r.nextInt(getProducts());
                    total += productPrices.get(productId);
                    preparedStatement.setInt(1, orderItemId);
                    preparedStatement.setInt(2, i);
                    preparedStatement.setInt(3, productId);
                    orderItemId++;
                }

                Timestamp orderDate = faker.date().between(new Timestamp(formatter.parse(getOrdersDateStart()).getTime()),
                        new Timestamp(formatter.parse(getOrdersDateEnd()).getTime()));

                preparedStatement_order.setInt(1, i);
                preparedStatement_order.setInt(2, r.nextInt(getCustomers()));
                preparedStatement_order.setLong(3, total);
                preparedStatement_order.setTimestamp(4, orderDate);
                //execute order statement first due to order_item foreign key constraint
                preparedStatement_order.executeUpdate();
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();
            preparedStatement_order.close();
            System.out.println("Created orders and order_items");

            System.out.println("Test data created");
        } catch (ParseException e) {
            System.out.println("Error parsing date");
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getCustomers() {
        return customers;
    }

    public void setCustomers(int customers) {
        this.customers = customers;
    }

    public int getProducts() {
        return products;
    }

    public void setProducts(int products) {
        this.products = products;
    }

    public int getProductsPriceMin() {
        return productsPriceMin;
    }

    public void setProductsPriceMin(int productsPriceMin) {
        this.productsPriceMin = productsPriceMin;
    }

    public int getProductsPriceMax() {
        return productsPriceMax;
    }

    public void setProductsPriceMax(int productsPriceMax) {
        this.productsPriceMax = productsPriceMax;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public int getOrdersItemsMin() {
        return ordersItemsMin;
    }

    public void setOrdersItemsMin(int ordersItemsMin) {
        this.ordersItemsMin = ordersItemsMin;
    }

    public int getOrdersItemsMax() {
        return ordersItemsMax;
    }

    public void setOrdersItemsMax(int ordersItemsMax) {
        this.ordersItemsMax = ordersItemsMax;
    }

    public String getOrdersDateStart() {
        return ordersDateStart;
    }

    public void setOrdersDateStart(String ordersDateStart) {
        this.ordersDateStart = ordersDateStart;
    }

    public String getOrdersDateEnd() {
        return ordersDateEnd;
    }

    public void setOrdersDateEnd(String ordersDateEnd) {
        this.ordersDateEnd = ordersDateEnd;
    }
}
