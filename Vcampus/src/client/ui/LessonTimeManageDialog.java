package client.ui;

import client.controller.CourseController;
import common.model.LessonTime;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

class LessonTimeManageDialog extends JDialog {
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"时间ID","星期","开始节次","结束节次","教室"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            return switch (c) {
                case 0,1,2,3 -> Integer.class;
                default -> Object.class;
            };
        }
    };
    private final JTable table = new JTable(model);
    private final CourseController controller = new CourseController();
    private final int lessonId;

    LessonTimeManageDialog(Window owner, int lessonId) {
        super(owner, "管理上课时间 - Lesson " + lessonId, ModalityType.APPLICATION_MODAL);
        this.lessonId = lessonId;
        setLayout(new BorderLayout());

        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JToolBar bar = new JToolBar(); bar.setFloatable(false);
        JButton btnAdd = new JButton("新增"), btnEdit = new JButton("编辑"),
                btnDel = new JButton("删除"), btnClose = new JButton("关闭");
        bar.add(btnAdd); bar.add(btnEdit); bar.add(btnDel); bar.addSeparator(); bar.add(btnClose);
        add(bar, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> dispose());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDel.addActionListener(e -> onDelete());

        setSize(600, 360);
        setLocationRelativeTo(owner);
        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        List<LessonTime> times = controller.listLessonTimes(lessonId);
        for (LessonTime t : times) {
            model.addRow(new Object[]{ t.getTimeId(), t.getDayOfWeek(), t.getStartSec(), t.getEndSec(), t.getLocation() });
        }
    }

    private void onAdd() {
        LessonTime t = editTime(null);
        if (t != null) {
            t.setLessonId(lessonId);
            var resp = controller.createLessonTime(t);
            JOptionPane.showMessageDialog(this, resp.getMsg());
            if ("success".equalsIgnoreCase(String.valueOf(resp.getStatus()))) refresh();
        }
    }

    private void onEdit() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        int idx = table.convertRowIndexToModel(r);

        LessonTime origin = new LessonTime();
        origin.setTimeId((Integer) model.getValueAt(idx, 0));
        origin.setLessonId(lessonId);
        origin.setDayOfWeek((Integer) model.getValueAt(idx, 1));
        origin.setStartSec((Integer) model.getValueAt(idx, 2));
        origin.setEndSec((Integer) model.getValueAt(idx, 3));
        origin.setLocation((String) model.getValueAt(idx, 4));

        LessonTime edited = editTime(origin);
        if (edited != null) {
            edited.setTimeId(origin.getTimeId());
            edited.setLessonId(lessonId);
            var resp = controller.updateLessonTime(edited);
            JOptionPane.showMessageDialog(this, resp.getMsg());
            if ("success".equalsIgnoreCase(String.valueOf(resp.getStatus()))) refresh();
        }
    }

    private void onDelete() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        int idx = table.convertRowIndexToModel(r);
        int timeId = (Integer) model.getValueAt(idx, 0);
        if (JOptionPane.showConfirmDialog(this, "确定删除时间片 ID="+timeId+" ?", "确认",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            var resp = controller.deleteLessonTime(timeId);
            JOptionPane.showMessageDialog(this, resp.getMsg());
            if ("success".equalsIgnoreCase(String.valueOf(resp.getStatus()))) refresh();
        }
    }

    private LessonTime editTime(LessonTime preset) {
        JSpinner spDay   = new JSpinner(new SpinnerNumberModel(preset==null?1:preset.getDayOfWeek(), 1, 7, 1));
        JSpinner spStart = new JSpinner(new SpinnerNumberModel(preset==null?1:preset.getStartSec(), 1, 13, 1));
        JSpinner spEnd   = new JSpinner(new SpinnerNumberModel(preset==null?2:preset.getEndSec(),   1, 13, 1));
        JTextField tfLoc = new JTextField(preset==null?"":preset.getLocation(), 12);

        JPanel p = new JPanel(new GridLayout(4,2,6,6));
        p.add(new JLabel("星期(1-7)：")); p.add(spDay);
        p.add(new JLabel("开始节次："));   p.add(spStart);
        p.add(new JLabel("结束节次："));   p.add(spEnd);
        p.add(new JLabel("上课地点："));   p.add(tfLoc);

        if (JOptionPane.showConfirmDialog(this, p, (preset==null?"新增":"编辑")+"上课时间",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            int s = (Integer) spStart.getValue(), e = (Integer) spEnd.getValue();
            if (s > e) { JOptionPane.showMessageDialog(this, "开始节次不可大于结束节次"); return null; }
            LessonTime t = new LessonTime();
            t.setDayOfWeek((Integer) spDay.getValue());
            t.setStartSec(s);
            t.setEndSec(e);
            t.setLocation(tfLoc.getText().trim());
            return t;
        }
        return null;
    }
}
