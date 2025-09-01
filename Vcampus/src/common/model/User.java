package common.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L; // 序列化版本号

    private String type;    // 用户类型: student/teacher/admin
    private String userId;  // 用户账号
    private String password;// 密码

    // 无参构造器（序列化/反射需要）
    public User() {}

    // 可选：带参构造器，方便直接创建对象
    public User(String type, String userId, String password) {
        this.type = type;
        this.userId = userId;
        this.password = password;
    }

    // Getter 和 Setter 方法
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    // 可选：便于调试输出
    @Override
    public String toString() {
        return "User{type='" + type + "', userId='" + userId + "'}";
    }
}

