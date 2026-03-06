/**
 * 可选课程对应实体课程
 */
package common.model;
import java.io.Serializable;
import java.util.Date;

public class Lesson implements Serializable {
    private static final long serialVersionUID = 1L;

    private int lessonId;        // lesson.lesson_id
    private int courseId;        // lesson.course_id
    private int capacity;        // lesson.capacity
    private boolean open;        // lesson.is_open
    private Date enrollStart;    // lesson.enroll_start
    private Date enrollEnd;      // lesson.enroll_end
    private String classroom;    // lesson.classroom
    private String remark;       // lesson.remark
    private String teacherUserId;   // 目前表里注释掉了，先留可空
    // TODO: 老师信息
    public Lesson() {};

    public Lesson(int lessonId, int courseId, int capacity, boolean open,
                  Date enrollStart, Date enrollEnd, String classroom, String remark) {
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.capacity = capacity;
        this.open = open;
        this.enrollStart = enrollStart;
        this.enrollEnd = enrollEnd;
        this.classroom = classroom;
        this.remark = remark;
    }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public Date getEnrollStart() { return enrollStart; }
    public void setEnrollStart(Date enrollStart) { this.enrollStart = enrollStart; }

    public Date getEnrollEnd() { return enrollEnd; }
    public void setEnrollEnd(Date enrollEnd) { this.enrollEnd = enrollEnd; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getTeacherId() { return teacherUserId; }
    public void setTeacherId(String teacherId) { this.teacherUserId = teacherId;}
 
    @Override public String toString() {
        return "Lesson{lessonId=" + lessonId +
               ", courseId=" + courseId +
               ", capacity=" + capacity +
               ", open=" + open +
               ", classroom='" + classroom + '\'' + '}';
    }
}
