package client.ui;

import client.controller.StudentController;
import common.model.Student;
import common.model.StudentPersonalAudit;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 学籍管理面板（管理员）
 * 功能：表格展示、增删改查、双击查看详情、审核、基本信息维护
 * 原 StudentMgrDialog (JDialog) 已改为 JPanel
 */
public class StudentMgrDialog extends JPanel {

    /* ---------- 控制器 ---------- */
    private final StudentController controller = new StudentController();

    /* ---------- 表格模型 ---------- */
    private final StudentTableModel tableModel = new StudentTableModel();
    private final JTable table = new JTable(tableModel);

    /* ---------- 按钮 ---------- */
    private final JButton btnAdd    = new JButton("增加");
    private final JButton btnDel    = new JButton("删除");
    private final JButton btnUpdate = new JButton("修改");
    private final JButton btnAudit  = new JButton("信息审核");

    /* ---------- 搜索栏 ---------- */
    private final JTextField searchField = new JTextField(15);
    private final JButton    searchBtn   = new JButton("🔍");

    public StudentMgrDialog() {
        setLayout(new BorderLayout());

        initTable();
        initSearchBar();
        initButtons();
        refreshTable(); // 初始加载
    }

    /* ========================== 表格 ========================== */
    private void initTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);

        /* 第6列改成按钮：基本信息 */
        table.getColumnModel().getColumn(5).setCellRenderer(new BasicInfoButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new BasicInfoButtonEditor());

        /* 双击查看详情 */
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
                    if (row != -1) {
                        Student stu = tableModel.getRow(row);

                        JDialog dialog = new JDialog(
                                SwingUtilities.getWindowAncestor(StudentMgrDialog.this),
                                "学生完整信息 - " + stu.getStudentName(),
                                Dialog.ModalityType.APPLICATION_MODAL
                        );
                        dialog.setSize(400, 350);
                        dialog.setLocationRelativeTo(StudentMgrDialog.this);

                        StudentDetailDialog detailPanel = new StudentDetailDialog(
                                stu,
                                () -> refreshTable(),
                                dialog::dispose // 点击关闭按钮时关闭窗口
                        );
                        dialog.add(detailPanel);
                        dialog.setVisible(true);
                    }
                }
            }
        });


        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /* ========================== 搜索栏 ========================== */
    private void initSearchBar() {
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(new JLabel("快速搜索："));
        north.add(searchField);
        north.add(searchBtn);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        searchBtn.addActionListener(e -> filter());

        add(north, BorderLayout.NORTH);
    }

    private void filter() {
        String key = searchField.getText().trim().toLowerCase();
        List<Student> all = controller.getAllStudents();
        if (key.isEmpty()) {
            tableModel.setData(all);
            return;
        }
        List<Student> filtered = new ArrayList<>();
        for (Student s : all) {
            if (s.getStudentId().toLowerCase().contains(key)
                    || s.getStudentName().toLowerCase().contains(key)
                    || s.getIdentity().toLowerCase().contains(key)
                    || s.getUserId().toLowerCase().contains(key)) {
                filtered.add(s);
            }
        }
        tableModel.setData(filtered);
    }

    /* ========================== 按钮条 ========================== */
    private void initButtons() {
        JPanel south = new JPanel();
        south.add(btnAdd);
        south.add(btnDel);
        south.add(btnUpdate);
        south.add(btnAudit);

        btnAdd   .addActionListener(e -> openAdd());
        btnDel   .addActionListener(e -> openDel());
        btnUpdate.addActionListener(e -> openUpdate());
        btnAudit .addActionListener(e -> openAudit());

        add(south, BorderLayout.SOUTH);
    }

    /* ---------------- 增加 ---------------- */
    private void openAdd() {
        new AddStudentDialog(SwingUtilities.getWindowAncestor(this), stu -> {
            boolean ok = controller.addStudent(stu);
            if (ok) {
                JOptionPane.showMessageDialog(this, "保存成功！");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "学号/身份证/一卡通已存在！", "失败", JOptionPane.ERROR_MESSAGE);
            }
        }).setVisible(true);
    }

    /* ---------------- 删除 ---------------- */
    private void openDel() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一行！");
            return;
        }
        String id = tableModel.getRow(row).getStudentId();
        int choose = JOptionPane.showConfirmDialog(this, "确认删除学号 " + id + "？");
        if (choose != JOptionPane.YES_OPTION) return;

        boolean ok = controller.deleteStudent(id);
        if (ok) refreshTable();
        else JOptionPane.showMessageDialog(this, "删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
    }

    /* ---------------- 修改 ---------------- */
    private void openUpdate() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一行！");
            return;
        }
        Student stu = tableModel.getRow(row);
        new UpdateStudentDialog(SwingUtilities.getWindowAncestor(this), stu, updated -> {
            boolean ok = controller.updateStudent(updated);
            if (ok) {
                JOptionPane.showMessageDialog(this, "修改成功！");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "修改失败（身份证/一卡通可能重复）！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }).setVisible(true);
    }

    /* ---------------- 审核 ---------------- */
    private void openAudit() {
        List<StudentPersonalAudit> list = controller.getPendingAudits();
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "暂无待审核记录！");
            return;
        }
        new PersonalAuditDialog(SwingUtilities.getWindowAncestor(this), list, this::refreshTable).setVisible(true);
    }

    /* ---------------- 刷新表格 ---------------- */
    private void refreshTable() {
        tableModel.setData(controller.getAllStudents());
    }

    /* ====================================================================== */
    /*                            表格模型                                     */
    /* ====================================================================== */
    private static class StudentTableModel extends AbstractTableModel {
        private final String[] COL = {"学号", "姓名", "性别", "身份证号", "一卡通号", "基本信息"};
        private List<Student> data = new ArrayList<>();

        void setData(List<Student> list) {
            this.data = list == null ? new ArrayList<>() : list;
            fireTableDataChanged();
        }

        Student getRow(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return COL.length; }
        @Override public String getColumnName(int c) { return COL[c]; }

        @Override public Object getValueAt(int r, int c) {
            Student s = data.get(r);
            return switch (c) {
                case 0 -> s.getStudentId();
                case 1 -> s.getStudentName();
                case 2 -> s.getSex();
                case 3 -> s.getIdentity();
                case 4 -> s.getUserId();
                case 5 -> s.hasPersonalInfo() ? "已填写" : "未填写";
                default -> null;
            };
        }

        @Override public boolean isCellEditable(int r, int c) { return c == 5; }
    }

    /* ====================================================================== */
    /*                     基本信息按钮 渲染器+编辑器                           */
    /* ====================================================================== */
    private static class BasicInfoButtonRenderer extends JButton implements TableCellRenderer {
        BasicInfoButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "未填写" : value.toString());
            return this;
        }
    }

    private class BasicInfoButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton("基本信息");

        BasicInfoButtonEditor() {
            button.addActionListener(e -> {
                int modelRow = table.convertRowIndexToModel(table.getEditingRow());
                Student stu = tableModel.getRow(modelRow);
                new BasicInfoViewDialog(SwingUtilities.getWindowAncestor(StudentMgrDialog.this),
                        stu.getStudentId(), stu.getStudentName()).setVisible(true);
                fireEditingStopped();
            });
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            button.setText(value == null ? "未填写" : value.toString());
            return button;
        }
        @Override public Object getCellEditorValue() { return button.getText(); }
    }

    /* ====================================================================== */
    /*                        新增学生对话框（内部类）                          */
    /* ====================================================================== */
    private static class AddStudentDialog extends JDialog {
        private final JTextField tfId   = new JTextField(8);
        private final JTextField tfName = new JTextField(16);
        private final JComboBox<String> cbSex = new JComboBox<>(new String[]{"男", "女"});
        private final JTextField tfIdentity = new JTextField(18);
        private final JTextField tfUserId   = new JTextField(9);

        AddStudentDialog(Window owner, java.util.function.Consumer<Student> callback) {
            super(owner, "新增学生", ModalityType.APPLICATION_MODAL);
            setSize(350, 250);
            setLocationRelativeTo(owner);
            setResizable(false);

            JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
            form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            form.add(new JLabel("学号："));      form.add(tfId);
            form.add(new JLabel("姓名："));      form.add(tfName);
            form.add(new JLabel("性别："));      form.add(cbSex);
            form.add(new JLabel("身份证号："));  form.add(tfIdentity);
            form.add(new JLabel("一卡通号："));  form.add(tfUserId);

            JButton btnSave   = new JButton("保存");
            JButton btnCancel = new JButton("取消");
            btnSave.addActionListener(e -> {
                Student stu = new Student(
                        tfId.getText().trim(),
                        tfName.getText().trim(),
                        (String) cbSex.getSelectedItem(),
                        tfIdentity.getText().trim(),
                        tfUserId.getText().trim());
                callback.accept(stu);
                dispose();
            });
            btnCancel.addActionListener(e -> dispose());

            JPanel south = new JPanel();
            south.add(btnSave);
            south.add(btnCancel);

            add(form, BorderLayout.CENTER);
            add(south, BorderLayout.SOUTH);
        }
    }

    /* ====================================================================== */
    /*                        修改学生对话框（内部类）                          */
    /* ====================================================================== */
    private static class UpdateStudentDialog extends JDialog {
        private final JTextField tfName = new JTextField(16);
        private final JComboBox<String> cbSex = new JComboBox<>(new String[]{"男", "女"});
        private final JTextField tfIdentity = new JTextField(18);
        private final JTextField tfUserId   = new JTextField(9);

        UpdateStudentDialog(Window owner, Student stu, java.util.function.Consumer<Student> callback) {
            super(owner, "修改学生", ModalityType.APPLICATION_MODAL);
            setSize(350, 220);
            setLocationRelativeTo(owner);
            setResizable(false);

            tfName.setText(stu.getStudentName());
            cbSex.setSelectedItem(stu.getSex());
            tfIdentity.setText(stu.getIdentity());
            tfUserId.setText(stu.getUserId());

            JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
            form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            form.add(new JLabel("姓名："));      form.add(tfName);
            form.add(new JLabel("性别："));      form.add(cbSex);
            form.add(new JLabel("身份证号："));  form.add(tfIdentity);
            form.add(new JLabel("一卡通号："));  form.add(tfUserId);

            JButton btnSave   = new JButton("保存");
            JButton btnCancel = new JButton("取消");
            btnSave.addActionListener(e -> {
                stu.setStudentName(tfName.getText().trim());
                stu.setSex((String) cbSex.getSelectedItem());
                stu.setIdentity(tfIdentity.getText().trim());
                stu.setUserId(tfUserId.getText().trim());
                callback.accept(stu);
                dispose();
            });
            btnCancel.addActionListener(e -> dispose());

            JPanel south = new JPanel();
            south.add(btnSave);
            south.add(btnCancel);

            add(form, BorderLayout.CENTER);
            add(south, BorderLayout.SOUTH);
        }
    }

    /* ====================================================================== */
    /*                        信息审核对话框（内部类）                          */
    /* ====================================================================== */
    private static class PersonalAuditDialog extends JDialog {
        private final JTable auditTable = new JTable(new AuditTableModel());
        private final StudentController controller = new StudentController();

        PersonalAuditDialog(Window owner, List<StudentPersonalAudit> list, Runnable refreshCallback) {
            super(owner, "学生信息审核", ModalityType.APPLICATION_MODAL);
            setSize(700, 400);
            setLocationRelativeTo(owner);

            ((AuditTableModel) auditTable.getModel()).setData(list);
            auditTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JButton btnPass   = new JButton("通过");
            JButton btnReject = new JButton("拒绝");
            btnPass.addActionListener(e -> audit(true, refreshCallback));
            btnReject.addActionListener(e -> audit(false, refreshCallback));

            JPanel south = new JPanel();
            south.add(btnPass);
            south.add(btnReject);

            add(new JScrollPane(auditTable), BorderLayout.CENTER);
            add(south, BorderLayout.SOUTH);
        }

        private void audit(boolean pass, Runnable callback) {
            int row = auditTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "请先选择一条记录！");
                return;
            }
            String studentId = (String) auditTable.getValueAt(row, 0);
            String remark = JOptionPane.showInputDialog(this, pass ? "通过备注（可空）：" : "拒绝原因：");
            boolean ok = controller.audit(studentId, pass, "admin", remark == null ? "" : remark.trim());
            if (ok) {
                JOptionPane.showMessageDialog(this, pass ? "已批准！" : "已拒绝！");
                dispose();
                callback.run();
            } else {
                JOptionPane.showMessageDialog(this, "操作失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }

        /* 审核表模型 */
        private static class AuditTableModel extends AbstractTableModel {
            private final String[] COL = {"学号", "姓名", "电话", "邮箱", "地址", "申请时间"};
            private List<StudentPersonalAudit> data = new ArrayList<>();

            void setData(List<StudentPersonalAudit> list) {
                this.data = list == null ? new ArrayList<>() : list;
                fireTableDataChanged();
            }

            @Override public int getRowCount() { return data.size(); }
            @Override public int getColumnCount() { return COL.length; }
            @Override public String getColumnName(int c) { return COL[c]; }

            @Override public Object getValueAt(int r, int c) {
                StudentPersonalAudit a = data.get(r);
                return switch (c) {
                    case 0 -> a.getStudentId();
                    case 1 -> a.getStudentName();
                    case 2 -> a.getPhone();
                    case 3 -> a.getEmail();
                    case 4 -> a.getAddress();
                    case 5 -> a.getApplyTime();
                    default -> null;
                };
            }
        }
    }
}
