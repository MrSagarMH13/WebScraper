package com.webscraper;

import com.webscraper.dto.EventPOJO;

import java.sql.*;
import java.util.List;

public class MySQLConnection {

    private Connection _conn;

    // init
    MySQLConnection(String url, String user, String pass) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        this._conn = DriverManager.getConnection(url, user, pass);
        System.out.println("Database connection established.");
    }

    public void saveData(List<EventPOJO> events) throws SQLException {
        Statement stmt = null;
        try {
            stmt = _conn.createStatement();
            _conn.setAutoCommit(false);
            for (EventPOJO eventPOJO : events) {
                String query = "insert into Event(eventName,eventDate,eventLocation)" +
                    " values ( '" + eventPOJO.getName().replace("'","''") + "','" + eventPOJO.getEventDate() + "','" + eventPOJO.getLocation() + "')";
                //System.out.println(query);
                stmt.addBatch(query);
            }
            int[] result = stmt.executeBatch();
            System.out.println("The number of rows inserted: " + result.length);
            _conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            _conn.rollback();
        } finally {
            if (stmt != null)
                stmt.close();
            if (_conn != null)
                _conn.close();
        }
    }

}
