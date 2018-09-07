package com.legalminer.tools;

import org.h2.tools.Server;

import java.sql.*;

public class PostgreSQLTool {

    private Server h2dbEngine;
    private String host = "";
    private String port = "9094";
    private String user = "zhls";
    private String password = "123456";
    private String dbName = "./h2dbEngine/sample";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static final PostgreSQLTool newTool() { return new PostgreSQLTool(); }

    private PostgreSQLTool() {}

    public PostgreSQLTool setHost(String host) {
        this.host = host;
        return this;
    }

    public PostgreSQLTool setPort(String port) {
        this.port = port;
        return this;
    }

    public PostgreSQLTool setUser(String user) {
        this.user = user;
        return this;
    }

    public PostgreSQLTool setPassword(String password) {
        this.password = password;
        return this;
    }

    public PostgreSQLTool setDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/" + dbName, user, password);
        return conn;
    }

    public ResultSet query(Connection connection, String sql) throws SQLException {
        Statement stat = connection.createStatement();
        ResultSet result = stat.executeQuery(sql);
        stat.close();
        return result;
    }

    public int update(Connection connection, String sql) throws SQLException {
        Statement stat = connection.createStatement();
        int result = stat.executeUpdate(sql);
        stat.close();
        return result;
    }

    public void close(Connection connection, ResultSet resultSet) throws SQLException {
        connection.close();
        resultSet.close();
    }
}
