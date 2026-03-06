
package common.model;
import java.io.Serializable;

public class Student implements Serializable {
	 private static final long serialVersionUID = 1L;
    private String studentId;
    private String studentName;
    private String sex;          // 男/女
    private String identity;     // 18位
    private String userId;       // 9位一卡通
    private StudentPersonal personal;
    public Student() {}
    public Student(String studentId, String studentName, String sex, String identity, String userId) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.sex = sex;
        this.identity = identity;
        this.userId = userId;
    }
    private void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException {
        try {
            out.defaultWriteObject();          // 先正常序列化
        } catch (java.io.NotSerializableException e) {
            /* 关键：打印完整“字段路径” */
            System.err.println("▼▼▼ 不可序列化字段路径 ▼▼▼");
            e.printStackTrace();
            System.err.println("▲▲▲ 结束 ▲▲▲");
            throw e;                           // 继续抛出，方便上层感知
        }
    }
    public Student(String studentId, String studentName, String sex, String identity, String userId, StudentPersonal personal) {
        this(studentId, studentName, sex, identity, userId);
        this.personal = personal;
    }
    public boolean hasPersonalInfo() {
        return personal != null;
    }
    public String getStudentId() {
		return studentId;
	}
	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}
	public String getStudentName() {
		return studentName;
	}
	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	
	}
	public StudentPersonal getPersonal() { return personal; }

    public void setPersonal(StudentPersonal personal) { this.personal = personal; }

	// Getter/Setter 省略
    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", studentName='" + studentName + '\'' +
                ", sex='" + sex + '\'' +
                ", identity='" + identity + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
    public StudentPersonal getpersonal() {
        return personal;
    }

    public void setpersonal(StudentPersonal personal) {
        this.personal = personal;
    }
}
