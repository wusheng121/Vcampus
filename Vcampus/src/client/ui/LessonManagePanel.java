package client.ui;

import client.controller.CourseController;
import common.model.Course;
import common.model.Lesson;
import common.model.LessonTime;
import common.model.Teacher;
import common.net.Message;
import util.EmptyState;
import util.LessonTimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LessonManagePanel extends JPanel {
    private final CourseController controller = new CourseController();

    // private final DefaultTableModel model = new DefaultTableModel(
    //         new Object[]{"开课ID","课程ID","课程名称","容量","开放","教室","备注"}, 0) {
    private final DefaultTableModel model = new DefaultTableModel(
        new Object[] {"开课ID","课程ID","课程名称","容量","开放","教室","上课时间","备注"}, 0) {

        @Override
        public boolean isCellEditable(int r,int c){
            return c == 4; // 仅允许“开放”列可点击编辑
        }

        @Override
        public Class<?> getColumnClass(int columnIndex){
            return switch (columnIndex) {
                case 3 -> Integer.class; // 容量
                case 4 -> Boolean.class; // 开放
                default -> Object.class;
            };
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            super.setValueAt(aValue, row, column);
            if (column == 4) { // 点了“开放”
                try {
                    int modelRow = table.convertRowIndexToModel(row);
                    Lesson l = buildLessonFromTableRow(modelRow);
                    l.setOpen(Boolean.TRUE.equals(aValue));
                    Message resp = controller.updateLesson(l);
                    boolean ok = "success".equalsIgnoreCase(String.valueOf(resp.getStatus()));
                    JOptionPane.showMessageDialog(LessonManagePanel.this,
                            (ok ? "已更新：" : "更新失败：") + resp.getMsg());
                    if (!ok) super.setValueAt(!Boolean.TRUE.equals(aValue), row, column);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LessonManagePanel.this,
                            "更新失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    super.setValueAt(!Boolean.TRUE.equals(aValue), row, column);
                }
            }
        }
    };

    private final JTable table = new JTable(model);
    private final JScrollPane tableScroll = new JScrollPane(table);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    public LessonManagePanel() {
        setLayout(new BorderLayout());

        // 工具栏
        JToolBar bar = new JToolBar(); bar.setFloatable(false);
        JButton addBtn=new JButton("新增开课"), btnEdit=new JButton("编辑课程"),
                btnDel=new JButton("删除开课"), btnRef=new JButton("刷新");

        // 搜索控件
        JTextField tfLessonId = new JTextField(6);
        JTextField tfCourseId = new JTextField(6);
        JComboBox<String> cbOpenFilter = new JComboBox<>(new String[]{"全部","仅开放","仅未开放"});
        JButton btnSearch = new JButton("搜索");
        JButton btnClear  = new JButton("清空");

        bar.add(addBtn); bar.add(btnEdit); bar.add(btnDel); bar.addSeparator(); bar.add(btnRef);
        bar.addSeparator();
        bar.add(new JLabel("开课ID:"));  bar.add(tfLessonId);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(new JLabel("课程ID:"));  bar.add(tfCourseId);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(new JLabel("开放:"));    bar.add(cbOpenFilter);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(btnSearch); bar.add(btnClear);
        add(bar, BorderLayout.NORTH);

        // 表格
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);
        add(tableScroll, BorderLayout.CENTER);
        hideIdColumn(); // 隐藏0号列（开课ID）

        // 事件
        tfLessonId.addActionListener(e -> btnSearch.doClick());
        tfCourseId.addActionListener(e -> btnSearch.doClick());
        btnSearch.addActionListener(e -> applyFilter(
                tfLessonId.getText().trim(),
                tfCourseId.getText().trim(),
                (String) cbOpenFilter.getSelectedItem()));
        btnClear.addActionListener(e -> {
            tfLessonId.setText("");
            tfCourseId.setText("");
            cbOpenFilter.setSelectedIndex(0);
            sorter.setRowFilter(null);
        });
        btnRef.addActionListener(e -> refreshTable());
        addBtn.addActionListener(e -> onCreate());
        btnEdit.addActionListener(e -> onEdit());
        btnDel.addActionListener(e -> onDelete());

        refreshTable();
    }

    private void refreshTable() {
        model.setRowCount(0);
        List<Lesson> lessons = controller.listLessons();
        // 批量预加载课程名与上课时间，消除逐课 N+1
        java.util.Map<Integer, Course> courseMap = new java.util.HashMap<>();
        for (Course c : controller.listCourses()) courseMap.put(c.getCourseId(), c);
        java.util.Map<Integer, java.util.List<LessonTime>> timesByLesson = new java.util.HashMap<>();
        for (LessonTime t : controller.listAllLessonTimes()) {
            timesByLesson.computeIfAbsent(t.getLessonId(), k -> new java.util.ArrayList<>()).add(t);
        }
        for (Lesson lesson : lessons) {
            Course c = courseMap.get(lesson.getCourseId());
            String courseName = c != null ? c.getCourseName() : "";
            List<LessonTime> times = timesByLesson.getOrDefault(lesson.getLessonId(), java.util.Collections.emptyList());
            String timeStr = LessonTimeUtil.formatTimes(times);
            model.addRow(new Object[]{
                    lesson.getLessonId(),            // 0 (隐藏)
                    lesson.getCourseId(),            // 1
                    courseName,                      // 2
                    lesson.getCapacity(),            // 3
                    lesson.isOpen(),                 // 4
                    lesson.getClassroom(),           // 5
                    timeStr,                         // 6 新增：上课时间
                    lesson.getRemark()               // 7
            });
        }
        EmptyState.updateEmptyState(tableScroll, table, "暂无排课");
    }

    private void onCreate(){
        List<Course> courses = controller.listCourses();
        List<Teacher> teachers = controller.listTeachers();
        LessonDialog dlg = new LessonDialog(null, courses, teachers);
        if (dlg.show(getParent())) {
            Object[] row = dlg.toRow();
            Lesson l = buildLessonFromDialogRow(row, null);
            Message resp = controller.createLesson(l);
            JOptionPane.showMessageDialog(this, resp.getMsg());
            if ("success".equalsIgnoreCase(String.valueOf(resp.getStatus()))) {
                // 新增 lesson_time（如果用户填了上课时间）
                 String timesStr = (row.length > 8) ? String.valueOf(row[8]).trim() : "";
                int newLessonId = l.getLessonId();
                if (newLessonId > 0 && !timesStr.isEmpty()) {
                    List<LessonTime> toCreate = parseTimes(timesStr);
                    for (LessonTime t : toCreate) {
                        t.setLessonId(newLessonId);
                        controller.createLessonTime(t);
                    }
                }                
                refreshTable();
            }
        }
    }

    private void onEdit(){
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int r = table.convertRowIndexToModel(viewRow);

        // 取完整 lesson（用于预填选课时间/教师）；上课时间直接用工具类格式化
        int lessonId = ((Number)model.getValueAt(r,0)).intValue();
        Lesson lesson = controller.getLessonById(lessonId);
        String timesStr = LessonTimeUtil.formatTimes(controller.listLessonTimes(lessonId));

        Object[] preset = new Object[9];
        preset[0] = model.getValueAt(r,0); // lessonId（隐藏列）
        preset[1] = model.getValueAt(r,1); // 课程ID
        preset[2] = model.getValueAt(r,2); // 课程名(占位)
        preset[3] = model.getValueAt(r,3); // 容量
        preset[4] = model.getValueAt(r,4); // 开放
        preset[5] = model.getValueAt(r,5); // 教室
        preset[6] = model.getValueAt(r,7); // 备注（注意：6是上课时间）
        preset[7] = lesson != null ? lesson.getTeacherId() : null;         // 教师
        preset[8] = timesStr; // 上课时间字符串（预填）

        List<Course> courses = controller.listCourses();
        List<Teacher> teachers = controller.listTeachers();
        LessonDialog dlg = new LessonDialog(preset, courses, teachers);
        if (dlg.show(getParent())) {
            Object[] row = dlg.toRow();
            int id = ((Number)preset[0]).intValue();

            Lesson l = buildLessonFromDialogRow(row, id);
            Message resp = controller.updateLesson(l);
            // 覆盖更新 lesson_time：删旧建新
            String newTimesStr = (row.length > 8) ? String.valueOf(row[8]).trim() : "";
            List<LessonTime> old = controller.listLessonTimes(id);
            if (old != null) {
                for (LessonTime t : old) {
                    controller.deleteLessonTime(t.getTimeId());
                }
            }
            if (!newTimesStr.isEmpty()) {
                List<LessonTime> toCreate = parseTimes(newTimesStr);
                for (LessonTime t : toCreate) {
                    t.setLessonId(id);
                    controller.createLessonTime(t);
                }
            }            

            JOptionPane.showMessageDialog(this, resp.getMsg());
            if ("success".equalsIgnoreCase(String.valueOf(resp.getStatus()))) {
                refreshTable();
            }
        }
    }

    private void onDelete(){
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int r = table.convertRowIndexToModel(viewRow);

        int id = ((Number)model.getValueAt(r,0)).intValue();
        if (JOptionPane.showConfirmDialog(this, "确认删除开课 " + id + " ？",
                "确认", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            Message resp = controller.deleteLesson(id);
            JOptionPane.showMessageDialog(this, resp.getMsg());
            if ("success".equalsIgnoreCase(String.valueOf(resp.getStatus()))) {
                refreshTable();
            }
        }
    }

    private Lesson buildLessonFromDialogRow(Object[] row, Integer keepIdOrNull){
        Lesson l = new Lesson();
        if (keepIdOrNull != null) l.setLessonId(keepIdOrNull);
        l.setCourseId(((Number)row[1]).intValue());
        l.setCapacity(((Number)row[3]).intValue());
        l.setOpen((Boolean) row[4]);
        l.setClassroom((String) row[5]);
        l.setRemark((String) row[6]); // 这里row[6]来自对话框（备注字段保持不变）
        if (row.length > 7) l.setTeacherId((String)row[7]);
        return l;
    }

    private Lesson buildLessonFromTableRow(int r) {
        Lesson l = new Lesson();
        l.setLessonId(((Number)model.getValueAt(r,0)).intValue());
        l.setCourseId(((Number)model.getValueAt(r,1)).intValue());
        l.setCapacity(((Number)model.getValueAt(r,3)).intValue());
        l.setOpen((Boolean) model.getValueAt(r,4));
        l.setClassroom((String) model.getValueAt(r,5));
        l.setRemark((String) model.getValueAt(r,7));
        return l;
    }

    private void applyFilter(String lessonIdText, String courseIdText, String openChoice) {
        List<RowFilter<Object,Object>> filters = new ArrayList<>();

        if (!lessonIdText.isEmpty()) {
            filters.add(new RowFilter<>() {
                @Override public boolean include(Entry<?,?> e) {
                    Object v = e.getValue(0);
                    return v != null && v.toString().equals(lessonIdText);
                }
            });
        }
        if (!courseIdText.isEmpty()) {
            filters.add(new RowFilter<>() {
                @Override public boolean include(Entry<?,?> e) {
                    Object v = e.getValue(1);
                    return v != null && v.toString().equals(courseIdText);
                }
            });
        }
        if (!"全部".equals(openChoice)) {
            final boolean wantOpen = "仅开放".equals(openChoice);
            filters.add(new RowFilter<>() {
                @Override public boolean include(Entry<?,?> e) {
                    Object v = e.getValue(4);
                    return v instanceof Boolean && ((Boolean)v) == wantOpen;
                }
            });
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    // 把第0列(开课ID)隐藏
    private void hideIdColumn() {
        var col = table.getColumnModel().getColumn(0);
        col.setMinWidth(0);
        col.setMaxWidth(0);
        col.setPreferredWidth(0);
        col.setResizable(false);
    }
    /** 开课表单对话框（模版） */
    static class LessonDialog {
        private final JComboBox<CourseItem> cbCourse = new JComboBox<>();
        private final JSpinner spCourseId = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        private final JSpinner spCapacity = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        private final JCheckBox cbOpen    = new JCheckBox("开放选课");
        private final JTextField tfRoom   = new JTextField(16);
        private final JTextArea  taRemark = new JTextArea(3, 20);
        private final Integer idOrNull;
        private final JComboBox<TeacherItem> cbTeacher = new JComboBox<>();
        private final JTextField tfTimes = new JTextField(24); // 新增：上课时间（文本，示例：周三 2-7；周五 6-8 或 3 2-7;5 6-8）
        LessonDialog(Object[] presetRowOrNull, List<Course> courses, List<Teacher> teachers){
        // ...课程下拉...
        if (teachers != null) {
            for (Teacher t : teachers) {
                cbTeacher.addItem(new TeacherItem(t.getTeacherId(), t.getTeacherName()));
            }
        }
        // ...existing code...
        if (presetRowOrNull != null && presetRowOrNull.length > 7) {
            String presetTeacherId = (String)presetRowOrNull[7];
            for (int i=0;i<cbTeacher.getItemCount();i++){
                if (cbTeacher.getItemAt(i).id.equals(presetTeacherId)) {
                    cbTeacher.setSelectedIndex(i);
                    break;
                }
            }
        }            
            // 塞课程到下拉框
            if (courses != null && !courses.isEmpty()) {
                for (Course c : courses) {
                    cbCourse.addItem(new CourseItem(c.getCourseId(), c.getCourseCode(), c.getCourseName()));
                }
                cbCourse.setEnabled(true);
                spCourseId.setEnabled(false);
            } else {
                cbCourse.setEnabled(false);
                spCourseId.setEnabled(true);
            }

            if (presetRowOrNull != null) {
                idOrNull = ((Number)presetRowOrNull[0]).intValue();

                int presetCourseId = ((Number)presetRowOrNull[1]).intValue();
                boolean matched = false;
                for (int i=0;i<cbCourse.getItemCount();i++){
                    if (cbCourse.getItemAt(i).id == presetCourseId) {
                        cbCourse.setSelectedIndex(i);
                        matched = true;
                        break;
                    }
                }
                if (!matched) spCourseId.setValue(presetCourseId);

                spCapacity.setValue(((Number)presetRowOrNull[3]).intValue());
                cbOpen.setSelected((Boolean)presetRowOrNull[4]);
                tfRoom.setText(String.valueOf(presetRowOrNull[5]));
                taRemark.setText(String.valueOf(presetRowOrNull[6]));
                // 预填“上课时间”字符串
                if (presetRowOrNull.length > 8 && presetRowOrNull[8] != null) tfTimes.setText(String.valueOf(presetRowOrNull[8]));

            } else {
                idOrNull = null;
            }
        }

        boolean show(Component parent){
            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4,6,4,6);

            c.gridx=0;c.gridy=0;c.anchor=GridBagConstraints.EAST; p.add(new JLabel("课程："),c);
            c.gridy++; p.add(new JLabel("容量："),c);
            c.gridy++; p.add(new JLabel("是否开放："),c);
            c.gridy++; p.add(new JLabel("教室："),c);
            c.gridy++; p.add(new JLabel("备注："),c);
            c.gridy++; p.add(new JLabel("教师："),c);
            c.gridy++; p.add(new JLabel("上课时间："),c);

            c.gridx=1;c.gridy=0;c.anchor=GridBagConstraints.WEST;
            JPanel coursePicker = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
            coursePicker.add(cbCourse);
            if (spCourseId.isEnabled()) { 
                coursePicker.add(Box.createHorizontalStrut(8));
                coursePicker.add(new JLabel("或手动ID："));
                coursePicker.add(spCourseId);
            }
            p.add(coursePicker,c);

            c.gridy++; p.add(spCapacity,c);
            c.gridy++; p.add(cbOpen,c);
            c.gridy++; p.add(tfRoom,c);
            c.gridy++; p.add(new JScrollPane(taRemark),c);
            c.gridy++; p.add(cbTeacher,c);
            c.gridy++;
            JPanel timesLine = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
            timesLine.add(tfTimes);
            timesLine.add(Box.createHorizontalStrut(8));
            timesLine.add(new JLabel("示例：周三 2-7；周五 6-8 或 3 2-7;5 6-8"));
            p.add(timesLine,c);            

            String title = idOrNull==null ? "新增开课" : ("编辑开课 - ID " + idOrNull);
            int opt = JOptionPane.showConfirmDialog(parent, p, title, JOptionPane.OK_CANCEL_OPTION);
            return opt == JOptionPane.OK_OPTION;
        }

        Object[] toRow(){
            int courseId;
            if (cbCourse.isEnabled() && cbCourse.getSelectedItem() instanceof CourseItem sel) {
                courseId = sel.id;
            } else {
                courseId = ((Number)spCourseId.getValue()).intValue();
            }
            String teacherId = cbTeacher.getSelectedItem() instanceof TeacherItem sel ? sel.id : null;
            return new Object[]{
                0, courseId, "", // 课程名这一列在 refreshTable() 填充，这里空
                ((Number)spCapacity.getValue()).intValue(),
                cbOpen.isSelected(),
                tfRoom.getText().trim(),
                taRemark.getText(),
                teacherId,
                tfTimes.getText().trim() // 上课时间字串 
            };
        }
    static class TeacherItem {
        final String id; final String name;
        TeacherItem(String id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return id + " - " + name; }
    }
        static class CourseItem {
            final int id; final String code; final String name;
            CourseItem(int id, String code, String name) {
                this.id = id; this.code = code; this.name = name;
            }
            @Override public String toString() {
                return id + " - " + (code==null?"":code) + " " + (name==null?"":name);
            }
        }
    }
    // ====== 解析“上课时间”文字为 LessonTime 列表 ======
    // 支持：周三 2-7；周五 6-8 或 3 2-7;5 6-8（分隔符支持；；，;）
    private List<LessonTime> parseTimes(String input) {
        List<LessonTime> list = new ArrayList<>();
        if (input == null || input.isBlank()) return list;
        String[] segs = input.replace('，',';').replace('；',';').split(";+");
        for (String seg : segs) {
            String s = seg.trim();
            if (s.isEmpty()) continue;
            s = s.replaceAll("\\s+", " ").replace('~','-');
            String[] parts = s.split(" ");
            if (parts.length < 2) continue;

            int day = parseDay(parts[0]); // “周三/3”等
            String[] se = parts[1].split("-");
            if (day < 1 || day > 7 || se.length != 2) continue;
            try {
                int st = Integer.parseInt(se[0].trim());
                int ed = Integer.parseInt(se[1].trim());
                if (st <= 0 || ed <= 0 || st > ed) continue;

                LessonTime t = new LessonTime();
                t.setDayOfWeek(day);
                t.setStartSec(st);
                t.setEndSec(ed);
                list.add(t);
            } catch (NumberFormatException ignore) {}
        }
        return list;
    }

    private int parseDay(String token) {
        token = token.trim();
        switch (token) {
            case "周一": case "星期一": case "一": return 1;
            case "周二": case "星期二": case "二": return 2;
            case "周三": case "星期三": case "三": return 3;
            case "周四": case "星期四": case "四": return 4;
            case "周五": case "星期五": case "五": return 5;
            case "周六": case "星期六": case "六": return 6;
            case "周日": case "星期日": case "日":
            case "周天": case "星期天": case "天": return 7;
            default:
                try { return Integer.parseInt(token); } catch (Exception e) { return -1; }
        }
    }

}
