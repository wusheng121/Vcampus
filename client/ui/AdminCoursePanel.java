package client.ui;

import client.controller.CourseController;
import common.model.Course;
import common.net.Message;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

/**
 * 管理端：增删改课程、刷新
 */
public class AdminCoursePanel extends JPanel {
    private final CourseController controller = new CourseController();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"课程ID","课程代码","课程名称","学分","描述"}, 0) {
        public boolean isCellEditable(int r,int c){ return false; }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    
    // constructor
    public AdminCoursePanel() {
        
    setLayout(new BorderLayout());
    // 工具栏
    JToolBar bar = new JToolBar(); bar.setFloatable(false);
    JButton btnAdd=new JButton("新增"), btnEdit=new JButton("编辑"),
            btnDel=new JButton("删除"), btnRef=new JButton("刷新");

    // —— 搜索控件
    JTextField tfSearch = new JTextField(16);
    installPlaceholder(tfSearch, "输入课程ID/代码/名称搜索");
    JButton btnSearch = new JButton("搜索");
    JButton btnClear  = new JButton("清空");

    bar.add(btnAdd); bar.add(btnEdit); bar.add(btnDel); bar.addSeparator(); bar.add(btnRef);
    bar.addSeparator();
    bar.add(new JLabel("搜索："));
    bar.add(tfSearch);
    bar.add(btnSearch);
    bar.add(btnClear);
    add(bar, BorderLayout.NORTH);

    // 表格
    table.getTableHeader().setReorderingAllowed(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setRowSorter(sorter);                  // ✅ 开启排序/过滤
    add(new JScrollPane(table), BorderLayout.CENTER);

    // 事件
    btnRef.addActionListener(e -> refreshTable());
    btnAdd.addActionListener(e -> onCreate());
    btnEdit.addActionListener(e -> onEdit());
    btnDel.addActionListener(e -> onDelete());

    // 搜索事件
    btnSearch.addActionListener(e -> applyCourseFilter(tfSearch));
    tfSearch.addActionListener(e -> applyCourseFilter(tfSearch)); // 回车触发
    btnClear.addActionListener(e -> {
        tfSearch.setText("");
        sorter.setRowFilter(null);
    });

    refreshTable();
    }

    private void refreshTable() {
        model.setRowCount(0); // 刷掉旧数据
        List<Course> courses = controller.listCourses();
        if(courses != null)
            for(Course course : courses) {
                model.addRow(new Object[] {
                    course.getCourseId(), course.getCourseCode(), course.getCourseName(), course.getCredit(), course.getDescription()
                });             
            }
    }

    private void onCreate() {
        CourseDialog dlg = new CourseDialog(null);
        if (dlg.show(getParent())) {
            Object[] row = dlg.toRow();
            String courseCode   = (String)  row[1];
            String courseName   = (String)  row[2];
            double courseCredit = ((Number) row[3]).doubleValue();
            String courseDesc   = (String)  row[4];
            Course course = new Course(0, courseCode, courseName, courseCredit, courseDesc);
            
            Message resp = controller.createCourse(course);
            JOptionPane.showMessageDialog(this, resp.getMsg());
            // model.addRow(row);
        }
        refreshTable();
    }

    private void onEdit() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        int id = ((Number)model.getValueAt(r,0)).intValue();
        CourseDialog dlg = new CourseDialog(new Object[]{
                model.getValueAt(r,0),
                model.getValueAt(r,1),
                model.getValueAt(r,2),
                model.getValueAt(r,3),
                model.getValueAt(r,4)
        });
        if (dlg.show(getParent())) {
            Object[] row = dlg.toRow();
            row[0] = id; // 保持原ID
            String courseCode   = (String)  row[1];
            String courseName   = (String)  row[2];
            double courseCredit = ((Number) row[3]).doubleValue();
            String courseDesc   = (String)  row[4];
            Course course = new Course(id, courseCode, courseName, courseCredit, courseDesc);
            Message resp = controller.updateCourse(course);
            JOptionPane.showMessageDialog(this, resp.getMsg());
        }
        refreshTable();
    }

    private void onDelete() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        int id = ((Number)model.getValueAt(r,0)).intValue();
        if (JOptionPane.showConfirmDialog(this, "确认删除课程 "+id+" ？",
                "确认", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            Message resp = controller.deleteCourse(id);
            JOptionPane.showMessageDialog(this, resp.getMsg());
        }
        refreshTable();
    }

    /** 课程表单对话框（模版） */
    static class CourseDialog {
        private final JTextField tfCode = new JTextField(16);
        private final JTextField tfName = new JTextField(16);
        private final JSpinner spCredit = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 99.0, 0.5));
        private final JTextArea taDesc = new JTextArea(4, 20);
        private final Integer idOrNull;

        CourseDialog(Object[] presetRowOrNull){
            if (presetRowOrNull != null) {
                idOrNull = ((Number)presetRowOrNull[0]).intValue();
                tfCode.setText(String.valueOf(presetRowOrNull[1]));
                tfName.setText(String.valueOf(presetRowOrNull[2]));
                spCredit.setValue(Double.parseDouble(String.valueOf(presetRowOrNull[3])));
                taDesc.setText(String.valueOf(presetRowOrNull[4]));
            } else idOrNull = null;
        }

        boolean show(Component parent){
            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets=new Insets(4,6,4,6);
            c.gridx=0;c.gridy=0;c.anchor=GridBagConstraints.EAST; p.add(new JLabel("课程代码："),c);
            c.gridy++; p.add(new JLabel("课程名称："),c);
            c.gridy++; p.add(new JLabel("学分："),c);
            c.gridy++; p.add(new JLabel("描述："),c);

            c.gridx=1;c.gridy=0;c.anchor=GridBagConstraints.WEST; p.add(tfCode,c);
            c.gridy++; p.add(tfName,c);
            c.gridy++; p.add(spCredit,c);
            c.gridy++; p.add(new JScrollPane(taDesc),c);

            String title = idOrNull==null ? "新增课程" : ("编辑课程 - ID " + idOrNull);
            int opt = JOptionPane.showConfirmDialog(parent, p, title, JOptionPane.OK_CANCEL_OPTION);
            return opt == JOptionPane.OK_OPTION;
        }

        Object[] toRow(){
            return new Object[]{
                    0, // 占位，外面会填真实ID
                    tfCode.getText().trim(),
                    tfName.getText().trim(),
                    ((Number)spCredit.getValue()).doubleValue(),
                    taDesc.getText()
            };
        }
    }
// 灰色占位提示
private void installPlaceholder(JTextField tf, String hint){
    Color normal = tf.getForeground();
    Color gray   = new Color(150,150,150);

    tf.setForeground(gray);
    tf.setText(hint);

    tf.addFocusListener(new FocusAdapter() {
        @Override public void focusGained(FocusEvent e) {
            if (tf.getForeground().equals(gray)) {
                tf.setText("");
                tf.setForeground(normal);
            }
        }
        @Override public void focusLost(FocusEvent e) {
            if (tf.getText().trim().isEmpty()) {
                tf.setForeground(gray);
                tf.setText(hint);
            }
        }
    });
}

    // 根据课程ID/代码/名称过滤
    private void applyCourseFilter(JTextField tf){
        String raw = tf.getText().trim();
        // 如果现在是占位提示色，视为无输入
        if (!tf.hasFocus() && !raw.isEmpty() && tf.getForeground().getRGB() != (new JTextField().getForeground().getRGB())) {
            sorter.setRowFilter(null);
            return;
        }
        if (raw.isEmpty() || tf.getForeground().getRGB() != (new JTextField().getForeground().getRGB())) {
            sorter.setRowFilter(null);
            return;
        }

        final String q = raw.toLowerCase();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> e) {
                Object idObj   = e.getValue(0); // 课程ID
                Object codeObj = e.getValue(1); // 课程代码
                Object nameObj = e.getValue(2); // 课程名称

                String idStr   = idObj   == null ? "" : idObj.toString();
                String codeStr = codeObj == null ? "" : codeObj.toString().toLowerCase();
                String nameStr = nameObj == null ? "" : nameObj.toString().toLowerCase();

                // 纯数字：优先当作ID精确匹配；否则也允许当作模糊（比如输入“10”匹配 ID=10 或 代码/名称含“10”）
                boolean idMatch = idStr.equals(q);

                boolean textMatch = codeStr.contains(q) || nameStr.contains(q);

                // 如果输入是数字，允许 id 精确 或 code/name 模糊；
                // 如果输入是文本，匹配 code/name 模糊。
                return idMatch || textMatch;
            }
        };
        sorter.setRowFilter(rf);
    }
}