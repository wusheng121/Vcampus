/**
 * 存放课老师信息
 */
package common.model;
import java.io.Serializable;

public class Teacher implements Serializable {
    private static final long serialVersionUID = 1L;

    private String teacher_id;
    private String teacher_name;
    private String sex;
    private String subject;
    private String userId;
    private String phone;
    private String email;
    private String address;
    /* 以及对应的 getter/setter */

    public Teacher() {};

    public String getTeacher_id() {
        return teacher_id;
    }

    public void setTeacher_id(String teacher_id) {
        this.teacher_id = teacher_id;
    }

    public String getTeacher_name() {
        return teacher_name;
    }

    public void setTeacher_name(String teacher_name) {
        this.teacher_name = teacher_name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Teacher(String teacher_id, String teacher_name) {
        this.teacher_id = teacher_id;
        this.teacher_name = teacher_name;
    }

    // getters/setters
    public String getTeacherId() { return teacher_id; }
    public void setTeacherId(String teacher_id) { this.teacher_id = teacher_id; }

    public String getTeacherName() { return teacher_name; }
    public void setTeacherName(String teacher_name) { this.teacher_name = teacher_name; }

    @Override public String toString() {
        return "Teacher{teacherId=" + teacher_id +
                ", name='" + teacher_name + '}';
    }
}

