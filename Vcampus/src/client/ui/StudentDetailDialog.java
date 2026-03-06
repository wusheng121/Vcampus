package client.ui;

import client.controller.StudentController;
import common.model.Student;
import common.model.StudentPersonal;

import javax.swing.*;
import java.awt.*;

/**
 * 学生详情面板（管理员/学生双击查看用）
 * 只读信息 + 管理员可编辑基本信息
 */
public class StudentDetailDialog extends JPanel {

    private final Student student;
    private final Runnable parentRefresh; // 回调：关闭后刷新主表
    private final Runnable onClose;       // 回调：关闭面板

    public StudentDetailDialog(Student stu, Runnable parentRefresh, Runnable onClose) {
        this.student = stu;
        this.parentRefresh = parentRefresh;
        this.onClose = onClose;
        setLayout(new BorderLayout());
        initView();
    }

    private void initView() {
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        /* 学籍信息 */
        p.add(new JLabel("学号"));      p.add(new JLabel(student.getStudentId()));
        p.add(new JLabel("姓名"));      p.add(new JLabel(student.getStudentName()));
        p.add(new JLabel("性别"));      p.add(new JLabel(student.getSex()));
        p.add(new JLabel("身份证号"));  p.add(new JLabel(student.getIdentity()));
        p.add(new JLabel("一卡通号"));  p.add(new JLabel(student.getUserId()));

        /* 联系方式 */
        StudentPersonal sp = student.getPersonal();
        if (sp != null) {
            p.add(new JLabel("电话")); p.add(new JLabel(sp.getPhone()));
            p.add(new JLabel("邮箱")); p.add(new JLabel(sp.getEmail()));
            p.add(new JLabel("地址")); p.add(new JLabel(sp.getAddress()));
        } else {
            p.add(new JLabel("电话")); p.add(new JLabel("未填写"));
            p.add(new JLabel("邮箱")); p.add(new JLabel("未填写"));
            p.add(new JLabel("地址")); p.add(new JLabel("未填写"));
        }

        /* 关闭按钮 */
        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> {
            if (onClose != null) onClose.run();
        });

        /* 管理员可编辑 */
        boolean isAdmin = true; // 实际应按登录用户角色判断
        JPanel south = new JPanel();
        if (isAdmin) {
            JButton editBtn = new JButton("编辑基本信息");
            editBtn.addActionListener(e -> openEdit());
            south.add(editBtn);
        }
        south.add(closeBtn);

        add(p, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    /* ---------------------- 管理员编辑 ---------------------- */
    private void openEdit() {
        StudentPersonal old = student.getPersonal();
        if (old == null) old = new StudentPersonal(student.getStudentId(), "", "", "");

        StudentPersonal finalOld = old;
        new EditPersonalDialog(SwingUtilities.getWindowAncestor(this), finalOld, ok -> {
            if (ok) {
                // 重新查询最新数据并刷新本窗口
                Student fresh = new StudentController().getStudent(student.getStudentId());
                if (fresh != null) {
                    removeAll();
                    revalidate();
                    repaint();
                    new StudentDetailDialog(fresh, parentRefresh, onClose)
                            .renderInto(this);
                    if (parentRefresh != null) parentRefresh.run(); // 刷新主表
                }
            }
        }).setVisible(true);
    }

    /**
     * 把新的 StudentDetailPanel 渲染到当前面板（避免外部重复写 add/remove）
     */
    private void renderInto(JPanel target) {
        target.setLayout(new BorderLayout());
        target.add(this, BorderLayout.CENTER);
    }

    /* ====================================================================== */
    /*                    编辑联系方式子窗口（管理员用）                         */
    /* ====================================================================== */
    private static class EditPersonalDialog extends JDialog {
        private final JTextField tfPhone = new JTextField(15);
        private final JTextField tfEmail = new JTextField(15);
        private final JTextField tfAddr  = new JTextField(15);

        EditPersonalDialog(Window owner, StudentPersonal old, java.util.function.Consumer<Boolean> callback) {
            super(owner, "编辑基本信息", ModalityType.APPLICATION_MODAL);
            setSize(350, 200);
            setLocationRelativeTo(owner);

            tfPhone.setText(old.getPhone());
            tfEmail.setText(old.getEmail());
            tfAddr.setText(old.getAddress());

            JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
            form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            form.add(new JLabel("电话")); form.add(tfPhone);
            form.add(new JLabel("邮箱")); form.add(tfEmail);
            form.add(new JLabel("地址")); form.add(tfAddr);

            JButton btnSave   = new JButton("保存");
            JButton btnCancel = new JButton("取消");
            btnSave.addActionListener(e -> {
                String phone = tfPhone.getText().trim();
                String email = tfEmail.getText().trim();
                String addr  = tfAddr.getText().trim();
                if (phone.isEmpty() || email.isEmpty() || addr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "三项均不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                StudentPersonal sp = new StudentPersonal(old.getStudentId(), phone, email, addr);
                boolean ok = new StudentController().saveOrUpdatePersonal(sp);
                if (ok) {
                    dispose();
                    callback.accept(true);
                } else {
                    JOptionPane.showMessageDialog(this, "保存失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            btnCancel.addActionListener(e -> {
                dispose();
                callback.accept(false);
            });

            JPanel south = new JPanel();
            south.add(btnSave);
            south.add(btnCancel);
            add(form, BorderLayout.CENTER);
            add(south, BorderLayout.SOUTH);
        }
    }
}
