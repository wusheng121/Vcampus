package common.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;       // 用户类型: student/teacher/admin
    private String userId;     // 用户账号
    private String password;   // 密码
    private String name;       // 姓名
    private String email;      // 邮箱
    private Timestamp createdAt;  // 创建时间（可选，用于显示）
    private String phone;   // 电话
    private String address;

    public User() {}

    public User(String type, String userId, String password) {
        this.type = type;
        this.userId = userId;
        this.password = password;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return "User{type='" + type + "', userId='" + userId + "', name='" + name + "'}";
    }
}
