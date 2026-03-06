/**
 * 课程时间
 */
package common.model;
import java.io.Serializable;

public class LessonTime implements Serializable {
    private static final long serialVersionUID = 1L;

    private int timeId;        // lesson_time.time_id
    private int lessonId;      // lesson_time.lesson_id
    private int dayOfWeek;     // 1=周一 ... 7=周日
    private int startSec;      // 第几节开始
    private int endSec;        // 第几节结束
    private String location;   // 上课地点（可空）

    public LessonTime() {}

    public LessonTime(int timeId, int lessonId, int dayOfWeek, int startSec, int endSec, String location) {
        this.timeId = timeId;
        this.lessonId = lessonId;
        this.dayOfWeek = dayOfWeek;
        this.startSec = startSec;
        this.endSec = endSec;
        this.location = location;
    }

    public int getTimeId() { return timeId; }
    public void setTimeId(int timeId) { this.timeId = timeId; }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getStartSec() { return startSec; }
    public void setStartSec(int startSec) { this.startSec = startSec; }

    public int getEndSec() { return endSec; }
    public void setEndSec(int endSec) { this.endSec = endSec; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override public String toString() {
        return "LessonTime{lessonId=" + lessonId +
               ", dayOfWeek=" + dayOfWeek +
               ", " + startSec + "-" + endSec +
               ", location='" + location + '\'' + '}';
    }  
}
