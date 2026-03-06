/**
 * 根据已选课程，显示对应课表
 */
package client.ui;

import client.controller.CourseController;
import common.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TimetablePanel extends JPanel {
    private final User user;
    private final CourseController controller = new CourseController();

    // 常量：节次数、星期列（1..7）
    private static final int MAX_SECTIONS = 13;

    private final JPanel grid; // 课表容器（GridBagLayout）
    private final Map<Integer, Course> courseMap = new HashMap<>();
    private final Map<Integer, Lesson> lessonMap = new HashMap<>();

    public TimetablePanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());

        grid = new JPanel(new GridBagLayout());
        add(new JScrollPane(grid), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        if (SwingUtilities.isEventDispatchThread()) {
            buildTimetable();
        } else {
            SwingUtilities.invokeLater(this::buildTimetable);
        }
    }

    private void buildTimetable() {
        grid.removeAll();

        // 占位矩阵：行 0..13（0=标题行），列 0..8（0=时段；1=节次；2..8=星期一..日）
        boolean[][] used = new boolean[MAX_SECTIONS + 1][9];

        // 左上 2 个角
        addCell(titleCell("时段/节次"), 0, 0, 1, 1, used, 0, 0);
        addCell(titleCell(""),        0, 1, 1, 1, used, 0, 0);

        // 顶部星期标题（列 2..8）
        String[] days = {"星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
        for (int d = 1; d <= 7; d++) addCell(titleCell(days[d-1]), 0, d+1, 1, 1, used, 1, 0);

        // 左侧：节次标签（行 1..13，列=1）
        for (int sec = 1; sec <= MAX_SECTIONS; sec++) {
            addCell(titleCell(String.valueOf(sec)), sec, 1, 1, 1, used, 0, 1);
        }

        // 左侧：时段合并（列=0）
        addCell(sectionCell("上午"),  1, 0, 5, 1, used, 0, 5);   // 1-5
        addCell(sectionCell("下午"),  6, 0, 5, 1, used, 0, 5);   // 6-10
        addCell(sectionCell("晚上"), 11, 0, 3, 1, used, 0, 3);   // 11-13

        // ==== 数据 ====
        List<Enrollment> enrollments = controller.listMyEnrollments(user.getUserId());
        List<Course> courses = controller.listCourses();
        List<Lesson> lessons = controller.listLessons();

        courseMap.clear(); lessonMap.clear();
        for (Course c : courses) courseMap.put(c.getCourseId(), c);
        for (Lesson l : lessons) lessonMap.put(l.getLessonId(), l);

        // 稳定配色（同一门课每次颜色一致）
        Map<Integer, Color> colorCache = new HashMap<>();

        // 放课程块（纵向跨格）
        for (Enrollment enr : enrollments) {
            if (!"enrolled".equalsIgnoreCase(enr.getStatus())) continue;

            Lesson lesson = lessonMap.get(enr.getLessonId());
            if (lesson == null) continue;
            Course course = courseMap.get(lesson.getCourseId());
            String courseName = course == null ? ("课程 " + lesson.getCourseId()) : course.getCourseName();

            List<LessonTime> times = controller.listLessonTimes(enr.getLessonId());
            times.sort(Comparator.comparingInt(LessonTime::getDayOfWeek)
                                 .thenComparingInt(LessonTime::getStartSec));

            for (LessonTime t : times) {
                int day   = t.getDayOfWeek();     // 1..7
                int start = Math.max(1, t.getStartSec());
                int end   = Math.min(MAX_SECTIONS, t.getEndSec());
                if (day < 1 || day > 7 || start > end) continue;

            JPanel block = new JPanel(new BorderLayout());
            block.setBorder(BorderFactory.createLineBorder(new Color(210, 214, 230)));
            block.setOpaque(true);
            block.setBackground(colorCache.computeIfAbsent(
                    lesson.getCourseId(), this::pickPastel));

            // 课程节次 + 教室
            String room = lesson.getClassroom();
            String secAndRoom = "第" + start + "-" + end + "节"
                    + (room != null && !room.isBlank() ? "（" + esc(room) + "）" : "");

            String html = "<html><div style='text-align:center; line-height:1.25;'>"
                    + "<b>" + esc(courseName) + "</b><br/>"
                    + "<span style='font-size:10px;color:#6b6b6b;'>" + secAndRoom + "</span>"
                    + "</div></html>";

            JLabel lbl = new JLabel(html, SwingConstants.CENTER);
            block.add(lbl, BorderLayout.CENTER);


                // 列=2..8（星期），所以 day=1..7 -> col = day + 1
                addCell(block, start, day + 1, (end - start + 1), 1, used, 1, (end - start + 1));
            }
        }

        // 补空格子（只补星期区域：列 2..8）
        for (int r = 1; r <= MAX_SECTIONS; r++) {
            for (int c = 2; c <= 8; c++) {
                if (!used[r][c]) {
                    JPanel empty = new JPanel();
                    empty.setBackground(Color.WHITE);
                    empty.setBorder(BorderFactory.createLineBorder(new Color(235, 238, 245)));
                    addCell(empty, r, c, 1, 1, used, 1, 1);
                }
            }
        }

        grid.revalidate();
        grid.repaint();
    }

    /** 带样式的标题格（星期/节次） */
    private JComponent titleCell(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(246, 247, 251));
        lbl.setBorder(BorderFactory.createLineBorder(new Color(220, 223, 235)));
        return lbl;
    }

    /** 合并后的“上午/下午/晚上”单元格 */
    private JComponent sectionCell(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(246, 247, 251));
        lbl.setBorder(BorderFactory.createLineBorder(new Color(220, 223, 235)));
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 12f));
        return lbl;
    }

    /** 添加组件到 (row, col)，跨 rowSpan×colSpan；并标记 used；权重可调 */
    private void addCell(JComponent comp, int row, int col, int rowSpan, int colSpan,
                         boolean[][] used, double wx, double wy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;                // 列（0..8）
        gbc.gridy = row;                // 行（0..13）
        gbc.gridwidth = colSpan;
        gbc.gridheight = rowSpan;
        // 两个左侧标题列不横向拉伸，星期列平均分；标题行不纵向拉伸
        gbc.weightx = (col <= 1 ? 0 : wx);
        gbc.weighty = (row == 0 ? 0 : wy);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);
        grid.add(comp, gbc);

        // 标记被占用
        for (int r = row; r < row + rowSpan && r < used.length; r++) {
            for (int c = col; c < col + colSpan && c < used[r].length; c++) {
                used[r][c] = true;
            }
        }
    }

    /** 稳定的马卡龙色（根据课程ID生成） */
    private Color pickPastel(int key) {
        Random rnd = new Random(key * 2654435761L); // 稳定 seed
        int r = 230 + rnd.nextInt(26);
        int g = 230 + rnd.nextInt(26);
        int b = 200 + rnd.nextInt(56);
        return new Color(r, g, b);
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
