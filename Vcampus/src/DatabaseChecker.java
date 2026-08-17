import util.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseChecker {
    public static void main(String[] args) {
        // 数据库连接信息（从 config.properties 读取）
        String url = Config.get("db.url", "jdbc:mysql://localhost:3306/vcampus");
        String user = Config.get("db.user", "root");
        String password = Config.get("db.password", "");

        try {
            // 加载MySQL JDBC驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 建立数据库连接
            Connection conn = DriverManager.getConnection(url, user, password);

            // 创建Statement对象
            Statement stmt = conn.createStatement();

            // 执行查询
            String sql = "SELECT * FROM user";
            ResultSet rs = stmt.executeQuery(sql);

            // 输出查询结果
            while (rs.next()) {
                System.out.println("user_id: " + rs.getString("user_id"));
                System.out.println("password: " + rs.getString("password"));
                System.out.println("role: " + rs.getString("role"));
                System.out.println("name: " + rs.getString("name"));
                System.out.println("email: " + rs.getString("email"));
                System.out.println("--------------------");
            }

            // 关闭资源
            rs.close();
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC驱动未找到");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("数据库连接或查询失败");
            e.printStackTrace();
        }
    }
}