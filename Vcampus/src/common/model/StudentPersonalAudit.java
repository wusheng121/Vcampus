package common.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class StudentPersonalAudit implements Serializable {
    private static final long serialVersionUID = 1L;
    private String studentId;
    private String studentName;   // 联查用，非表字段
    private String phone;
    private String email;
    private String address;
    private Timestamp applyTime;
    private String status;   // 审核状态：PENDING / APPROVED / REJECTED

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    

    /* 下面全是 Getter/Setter，Alt+Insert 一键生成 */
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Timestamp getApplyTime() { return applyTime; }
    public void setApplyTime(Timestamp applyTime) { this.applyTime = applyTime; }
}