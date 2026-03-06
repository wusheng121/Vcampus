package client.ai;

import client.controller.CourseController;
import common.model.*;

import java.util.*;

/** 选课系统专用的 Hook，实现 AgentHooks 抽象 */
public class CourseAgentHooks implements AgentHooks {
    private final User user;
    private final CourseController controller = new CourseController();

    // 这些数据只在 hooks 内部维护
    private List<Lesson> lessons = Collections.emptyList();
    private List<LessonTime> lessonTimes = new ArrayList<>();
    private List<Course> courses = Collections.emptyList();
    private List<Enrollment> myEnrollments = Collections.emptyList();

    public CourseAgentHooks(User user) {
        this.user = user;
    }

    @Override
    public String systemPrompt() {
        return AgentPrompts.courseSelectionSystemPrompt();
    }

    @Override
    public void preload() {
        // 拉取所需数据
        myEnrollments = controller.listMyEnrollments(user.getUserId());
        lessons = controller.listLessons();
        courses = controller.listCourses();

        lessonTimes = new ArrayList<>();
        if (lessons != null && !lessons.isEmpty()) {
            for (Lesson l : lessons) {
                List<LessonTime> ts = controller.listLessonTimes(l.getLessonId());
                if (ts != null) lessonTimes.addAll(ts);
            }
        }
    }

    @Override
    public String buildDataset() {
        Map<Integer, Integer> enrolledMap = new HashMap<>();
        if (lessons != null) {
            for (Lesson l : lessons) {
                enrolledMap.put(l.getLessonId(), controller.countEnrolled(l.getLessonId()));
            }
        }
        return CourseSelectionContext.build(courses, lessons, myEnrollments, lessonTimes, enrolledMap);
    }

    @Override
    public String helpText() {
        return CourseSelectionContext.HELP_TEXT;
    }
}
