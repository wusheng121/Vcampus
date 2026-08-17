package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/vcampus" +
                    "?useSSL=false" +
                    "&characterEncoding=utf8" +
                    "&serverTimezone=UTC" +
                    "&allowPublicKeyRetrieval=true";

    static {
        URL = Config.get("db.url", DEFAULT_URL);
        USER = Config.get("db.user", "root");
        PASSWORD = Config.get("db.password", "");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL8.x 驱动
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 辅助关闭资源
    public static void close(AutoCloseable ac) {
        if (ac != null) {
            try { ac.close(); } catch (Exception ignore) {}
        }
    }
}
