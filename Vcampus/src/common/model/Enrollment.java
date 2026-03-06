/**
 * 选课记录
 */
package common.model;
import java.io.Serializable;
import java.util.Date;

public class Enrollment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String STATUS_ENROLLED = "enrolled";
    public static final String STATUS_DROPPED  = "dropped";
    
    private int enrollId;        // enrollment.enroll_id
    private String studentId;    // enrollment.student_id (CHAR(8))
    private int lessonId;        // enrollment.lesson_id
    private String status;       // 'enrolled' / 'dropped'
    private Date createdAt;      // enrollment.created_at

    public Enrollment() {};
    
    public Enrollment(int enrollId, String studentId, int lessonId, String status, Date createdAt) {
        this.enrollId = enrollId;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getEnrollId() { return enrollId; }
    public void setEnrollId(int enrollId) { this.enrollId = enrollId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Override public String toString() {
        return "Enrollment{enrollId=" + enrollId +
               ", studentId='" + studentId + '\'' +
               ", lessonId=" + lessonId +
               ", status='" + status + '\'' + '}';
    }
}
