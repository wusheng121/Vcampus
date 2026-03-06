/*
 * 管理Agent调用「选课系统」数据库资料
 */
package client.ai;

import common.model.Course;
import common.model.Enrollment;
import common.model.Lesson;
import common.model.LessonTime;

import java.util.List;
import java.util.Map;

public class CourseSelectionContext {
    private CourseSelectionContext() {}
    public static String build(List<Course> courses, List<Lesson> lessons, List<Enrollment> myEnrollments, List<LessonTime> lessonTimes, Map<Integer, Integer> enrolledMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("<<DATASET>>\n");
        sb.append("{\n  \"courses\": [\n");
        for(int i = 0;i < courses.size();i++) {
            Course c = courses.get(i);
            sb.append(String.format(
                "    {\"course_id\":%d,\"course_code\":\"%s\",\"course_name\":\"%s\",\"credit\":%.1f,\"description\":\"%s\"}%s\n",
                c.getCourseId(), safe(c.getCourseCode()), safe(c.getCourseName()), c.getCredit(), safe(c.getDescription()),
                (i == courses.size()-1 ? "" : ",")
            ));
        }
        sb.append("  ],\n  \"lessons\": [\n");
        for (int i = 0; i < lessons.size(); i++) {
            Lesson l = lessons.get(i);
            int cur = enrolledMap != null ? enrolledMap.getOrDefault(l.getLessonId(), 0) : 0;

            sb.append(String.format(
                "    {\"lesson_id\":%d,\"course_id\":%d,\"is_open\":%s,\"capacity\":%d,\"current_enrolled\":%d,"
              + "\"classroom\":\"%s\",\"remark\":\"%s\"}%s\n",
                l.getLessonId(), l.getCourseId(), l.isOpen() ? "true" : "false", l.getCapacity(), cur,
                safe(l.getClassroom()), safe(l.getRemark()),            
                (i == lessons.size()-1 ? "" : ",")
            ));            
        }

        sb.append("  ],\n  \"lesson_times\": [\n");
        for (int i = 0; i < lessonTimes.size(); i++) {
            LessonTime t = lessonTimes.get(i);
            sb.append(String.format(
                "    {\"lesson_id\":%d,\"day_of_week\":%d,\"start_sec\":%d,\"end_sec\":%d,\"location\":\"%s\"}%s\n",
                t.getLessonId(), t.getDayOfWeek(), t.getStartSec(), t.getEndSec(), safe(t.getLocation()),
                (i == lessonTimes.size()-1 ? "" : ",")
            ));
        }        

        // 只输出已选(enrolled)，并正确处理逗号
        sb.append("  ],\n  \"my_enrollments\": [\n");
        int enrolledCount = 0;
        for (Enrollment e : myEnrollments) {
            if ("enrolled".equalsIgnoreCase(e.getStatus())) enrolledCount++;
        }
        int printed = 0;
        for (int i = 0; i < myEnrollments.size(); i++) {
            Enrollment e = myEnrollments.get(i);
            if (!"enrolled".equalsIgnoreCase(e.getStatus())) continue;
            printed++;
            sb.append(String.format(
                "    {\"lesson_id\":%d}%s\n",
                e.getLessonId(),
                (printed == enrolledCount ? "" : ",")
            ));
        }

        sb.append("  ]\n}\n");
        sb.append("</DATASET>\n");
        

        return sb.toString();
    }

    // 统一维护的欢迎/帮助文案（AgentPanel 初次进入与 /help 都用它）
    public static final String HELP_TEXT = """
        我是你的选课助手机器人

        可用指令：
        • /help —— 查看帮助
        • /clear —— 清空对话与上下文

        输入提示：
        • Enter 发送，Shift+Enter 换行
        • 可以问：“我选了哪些课？上课时间？”、“能加选【课程名】吗？”
        • 课程冲突依据：已选课程与待选课程时间重叠
        • 可用名额：显示为 “已选人数/容量”
        """;

    // 处理空字串等问题
    private static String safe(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\")
            .replace("\"","\\\"")
            .replace("\n"," ")
            .replace("\r"," ");
    }        
}
