package com.gymora.util;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DBInsertTest {

    public static void main(String[] args) {

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try {
            Connection connection = DB_Connection.getConnection();

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, "testuser");
            statement.setString(2, "12345");

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Data inserted successfully!");
            }

            statement.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}