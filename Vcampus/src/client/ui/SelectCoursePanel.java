package client.ui;

import client.controller.CourseController;
import common.model.*;
import common.net.Message;
import util.EmptyState;
import util.LessonTimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

public class SelectCoursePanel extends JPanel {
    // 列索引常量
    private static final int COL_LESSON_ID = 0; 
    private static final int COL_STATUS    = 8; // 最后一列“状态”

    private final User user;
    private final CourseController controller = new CourseController();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final javax.swing.JScrollPane tableScroll;
    private final Runnable onEnrollmentChanged;
    private JTextField searchField; // 搜寻栏
    private TableRowSorter<DefaultTableModel> sorter;
    private java.util.Map<Integer, Course> courseMap = new java.util.HashMap<>();
    private java.util.Map<String, String> teacherMap = new java.util.HashMap<>();
    private java.util.Set<Integer> enrolledLessonIds = new java.util.HashSet<>();

    public SelectCoursePanel(User user, Runnable onEnrollmentChanged) {
        this.user = user;
        this.onEnrollmentChanged = onEnrollmentChanged;
        setLayout(new BorderLayout());

        // 顶部搜寻栏
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        searchField = new JTextField(24);
        installPlaceholder(searchField, "输入课程编号/名称/教师；为空=刷新");
        JButton searchBtn = new JButton("查询");
        JButton clearBtn  = new JButton("清除");
        searchPanel.add(new JLabel("搜寻："));
        JButton refreshBtn = new JButton("刷新"); 
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);
        searchPanel.add(refreshBtn);
        add(searchPanel, BorderLayout.NORTH);

        // 表头
        String[] columeNames = {"课程编号", "课程名称", "教师", "学分","课容量","已选人数", "时间", "地点", "状态"};
        tableModel = new DefaultTableModel(columeNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column){
                // 全表不可编辑
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 表格单选
        table.getTableHeader().setReorderingAllowed(false); // 禁止拖动列换顺序

        // 排序/过滤支持
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        add((tableScroll = new JScrollPane(table)), BorderLayout.CENTER);

        // 按钮区域
        JButton selectBtn = new JButton("选课");
        JButton dropBtn   = new JButton("退课");
        JPanel btnPanel   = new JPanel();
        btnPanel.add(selectBtn);
        btnPanel.add(dropBtn);
        add(btnPanel, BorderLayout.SOUTH);
        
        selectBtn.addActionListener(e -> enrollSelected());
        dropBtn.addActionListener(e -> dropSelected());

        // 事件
        searchBtn.addActionListener(e -> applyFilterOrRefresh());          // 改成“过滤或刷新”
        clearBtn.addActionListener(e -> { searchField.setText(""); applyFilterOrRefresh(); });
        refreshBtn.addActionListener(e -> refreshTable());                 // 独立刷新按钮
        searchField.addActionListener(e -> applyFilterOrRefresh()); 

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Lesson> lessons = controller.listLessons();
        List<Enrollment> enrollments = controller.listMyEnrollments(user.getUserId());
        List<Course> courses = controller.listCourses();
        List<Teacher> teachers = controller.listTeachers();
        courseMap.clear();
        enrolledLessonIds.clear();
        teacherMap.clear();

        for(Course c : courses) courseMap.put(c.getCourseId(), c);
        if (teachers != null) for(Teacher t : teachers) {
            if (t.getTeacherId() != null) teacherMap.put(t.getTeacherId(), t.getTeacherName());
        }
        for(Enrollment e : enrollments){
            if("enrolled".equals(e.getStatus())){
                enrolledLessonIds.add(e.getLessonId());
            }
        }
        // 批量预加载上课时间与已选人数，消除逐课 N+1 往返
        java.util.Map<Integer, java.util.List<LessonTime>> timesByLesson = new java.util.HashMap<>();
        java.util.Map<Integer, Integer> enrolledCountMap = controller.countEnrolledForAll();
        for (LessonTime t : controller.listAllLessonTimes()) {
            timesByLesson.computeIfAbsent(t.getLessonId(), k -> new java.util.ArrayList<>()).add(t);
        }
        if(lessons != null)
            for(Lesson l : lessons) {
                Course c = courseMap.get(l.getCourseId());
                String courseName = c != null ? c.getCourseName() : String.valueOf(l.getCourseId());
                boolean selected = enrolledLessonIds.contains(l.getLessonId());
                List<LessonTime> lessonTimes = timesByLesson.getOrDefault(l.getLessonId(), java.util.Collections.emptyList());
                int enrolledCnt = enrolledCountMap.getOrDefault(l.getLessonId(), 0);
                int capacity = l.getCapacity();
                String timeStr = LessonTimeUtil.formatTimes(lessonTimes);
                tableModel.addRow(new Object[]{
                    l.getLessonId(),
                    courseName,
                    teacherMap.getOrDefault(l.getTeacherId(), "—"),
                    c.getCredit(),
                    capacity,
                    enrolledCnt,
                    timeStr,
                    l.getClassroom(),
                    l.isOpen() ? selected ? "已选" : "未选" : "未开放"
                });
            }
        EmptyState.updateEmptyState(tableScroll, table, "暂无可选课程");
        // 设置渲染器(标出选择的课程)
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // 转模型行
                // int modelRow = table.convertRowIndexToModel(row);
                // String status = (String) table.getModel().getValueAt(modelRow, COL_STATUS);
                // 未选中时按状态上色；选中时以选中高亮为准
                // if (!isSelected) {
                //     if ("已选".equals(status)) {
                //         c.setBackground(new Color(173, 216, 230)); // 浅蓝
                //     } else {
                //         c.setBackground(Color.WHITE);
                //     }
                //     c.setForeground(Color.BLACK);
                // } else {
                //     c.setBackground(table.getSelectionBackground());
                //     c.setForeground(table.getSelectionForeground());
                // }
                return c;                
            }
        }) ;
    }
    // 灰色占位提示
    private void installPlaceholder(JTextField tf, String hint){
        Color normal = tf.getForeground();
        Color gray   = new Color(150,150,150);

        tf.putClientProperty("placeholderActive", true);
        tf.setForeground(gray);
        tf.setText(hint);

        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                Boolean ph = (Boolean) tf.getClientProperty("placeholderActive");
                if (Boolean.TRUE.equals(ph)) {
                    tf.setText("");
                    tf.setForeground(normal);
                    tf.putClientProperty("placeholderActive", false);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (tf.getText().trim().isEmpty()) {
                    tf.setForeground(gray);
                    tf.setText(hint);
                    tf.putClientProperty("placeholderActive", true);
                }
            }
        });
    }

    private void applyFilterOrRefresh() {
        boolean placeholder = Boolean.TRUE.equals(searchField.getClientProperty("placeholderActive"));
        String raw = searchField.getText() == null ? "" : searchField.getText().trim();

        if (placeholder || raw.isEmpty()) {
            sorter.setRowFilter(null);
            refreshTable(); // ✅ 空输入 = 刷新（重新拉后端数据）
            return;
        }

        final String keyword = raw.toLowerCase();
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                // 只检查 0=课程编号, 1=课程名称, 2=教师
                for (int col : new int[]{0, 1, 2}) {
                    Object value = entry.getValue(col);
                    if (value != null && value.toString().toLowerCase().contains(keyword)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void enrollSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 ) return;
        int modelRow = table.convertRowIndexToModel(viewRow); // 视图行 -> 模型行
        int lessonId = (int) tableModel.getValueAt(modelRow, 0);
        if(enrolledLessonIds.contains(lessonId) == true){ // 已选过了
            // System.out.println();
            JOptionPane.showMessageDialog(this, "请勿重复选课");
            return;
        }
        Message resp = controller.enroll(user.getUserId(), lessonId);
        JOptionPane.showMessageDialog(this, resp.getMsg());
        refreshTable();
        // 只有成功时才通知课表刷新
        if (resp != null && "success".equalsIgnoreCase(String.valueOf(resp.getStatus()))) {
            if(onEnrollmentChanged != null)onEnrollmentChanged.run();
        }
    }

    private void dropSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 ) return;
        int modelRow = table.convertRowIndexToModel(viewRow); // 视图 -> 模型
        int lessonId = (int) tableModel.getValueAt(modelRow, 0);
        if(enrolledLessonIds.contains(lessonId) == false){ // 本来就不在
            // System.out.println();
            JOptionPane.showMessageDialog(this, "未选择课程");
            return;
        }        
        Message resp = controller.drop(user.getUserId(), lessonId);
        JOptionPane.showMessageDialog(this, resp.getMsg());
        refreshTable();
        // 只有成功时才通知课表刷新
        if (resp != null && "success".equalsIgnoreCase(String.valueOf(resp.getStatus()))) {
            if(onEnrollmentChanged != null)onEnrollmentChanged.run();
        }
    }
}
