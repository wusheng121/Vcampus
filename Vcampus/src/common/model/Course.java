/**
 * 存放课程信息用来显示在学生选课页面
 */
package common.model;
import java.io.Serializable;

public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private int courseId;         // course.course_id
    private String courseCode;    // course.course_code
    private String courseName;    // course.course_name
    private double credit;        // course.credit
    private String description;   // course.description

    public Course() {};

    public Course(int courseId, String courseCode, String courseName, double credit, String description) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credit = credit;
        this.description = description;        
    }

    // getters/setters
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public double getCredit() { return credit; }
    public void setCredit(double credit) { this.credit = credit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override public String toString() {
        return "Course{courseId=" + courseId +
               ", code='" + courseCode + '\'' +
               ", name='" + courseName + '\'' +
               ", credit=" + credit + '}';
    }
}
