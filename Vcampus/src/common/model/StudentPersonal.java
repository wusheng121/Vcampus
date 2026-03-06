package common.model;

import java.io.Serializable;

public class StudentPersonal implements Serializable {
	 private static final long serialVersionUID = 1L;
    private String studentId;
    private String phone;
    private String email;
    private String address;
  
    public StudentPersonal(String studentId, String phone, String email, String address) {
        this.studentId = studentId;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }
    /* getter/setter 省略 */
	public String getStudentId() {
		return studentId;
	}
	public void setStudentId(String studentId) {
		this.studentId = studentId;
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
	
	
}