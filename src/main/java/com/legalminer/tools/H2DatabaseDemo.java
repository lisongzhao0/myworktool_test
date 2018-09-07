package com.legalminer.tools;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.Server;


public class H2DatabaseDemo {

    private Server h2dbEngine;
    private String port = "9094";
    private String dbDir = "./h2dbEngine/sample";
    private String user = "zhls";
    private String password = "123456";

    public void startDB() {
        try {
            System.out.println("正在启动h2...");
            h2dbEngine = Server.createTcpServer(new String[]{"-tcpPort", port}).start();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("启动h2出错：" + e.toString());
            throw new RuntimeException(e);
        }
    }

    public void stopDB() {
        if (h2dbEngine !=null) {
            System.out.println("正在关闭h2...");
            h2dbEngine.stop();
            System.out.println("关闭成功.");
            h2dbEngine = null;
        }
    }


    public void useH2() {
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:" + dbDir, user, password);
            Statement stat = conn.createStatement();
            // insert data
            stat.execute("CREATE TABLE TEST(NAME VARCHAR)");
            stat.execute("INSERT INTO TEST VALUES('Hello World')");

            // use data
            ResultSet result = stat.executeQuery("select name from test ");
            int i = 1;
            while (result.next()) {
                System.out.println(i++ + ":" + result.getString("name"));
            }
            result.close();
            stat.close();
            conn.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
