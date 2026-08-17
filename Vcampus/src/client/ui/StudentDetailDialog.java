package client.ui;

import client.controller.StudentController;
import common.model.Student;
import common.model.StudentPersonal;
import common.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * 学生详情面板（管理员/学生双击查看用）
 * 只读信息 + 管理员可编辑基本信息
 */
public class StudentDetailDialog extends JPanel {

    private Student student;
    private final User user;
    private final Runnable parentRefresh; // 回调：关闭后刷新主表
    private final Runnable onClose;       // 回调：关闭面板

    // 值标签，保存编辑后可就地刷新，无需重建整个面板
    private JLabel lblStudentId, lblName, lblSex, lblIdentity, lblUserId;
    private JLabel lblPhone, lblEmail, lblAddress;

    public StudentDetailDialog(Student stu, User user, Runnable parentRefresh, Runnable onClose) {
        this.student = stu;
        this.user = user;
        this.parentRefresh = parentRefresh;
        this.onClose = onClose;
        setLayout(new BorderLayout());
        initView();
    }

    private void initView() {
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        /* 学籍信息 */
        lblStudentId = new JLabel(student.getStudentId());
        lblName = new JLabel(student.getStudentName());
        lblSex = new JLabel(student.getSex());
        lblIdentity = new JLabel(student.getIdentity());
        lblUserId = new JLabel(student.getUserId());
        p.add(new JLabel("学号"));      p.add(lblStudentId);
        p.add(new JLabel("姓名"));      p.add(lblName);
        p.add(new JLabel("性别"));      p.add(lblSex);
        p.add(new JLabel("身份证号"));  p.add(lblIdentity);
        p.add(new JLabel("一卡通号"));  p.add(lblUserId);

        /* 联系方式 */
        StudentPersonal sp = student.getPersonal();
        String phone = sp != null ? nz(sp.getPhone()) : "未填写";
        String email = sp != null ? nz(sp.getEmail()) : "未填写";
        String address = sp != null ? nz(sp.getAddress()) : "未填写";
        lblPhone = new JLabel(phone);
        lblEmail = new JLabel(email);
        lblAddress = new JLabel(address);
        p.add(new JLabel("电话")); p.add(lblPhone);
        p.add(new JLabel("邮箱")); p.add(lblEmail);
        p.add(new JLabel("地址")); p.add(lblAddress);

        /* 关闭按钮 */
        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> {
            if (onClose != null) onClose.run();
        });

        /* 管理员可编辑（按当前登录用户角色，非硬编码） */
        boolean isAdmin = user != null && "admin".equalsIgnoreCase(user.getType());
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

    /** 保存后就地刷新字段值，避免重建整个面板（含 controller）。 */
    private void refreshView(Student fresh) {
        this.student = fresh;
        lblStudentId.setText(fresh.getStudentId());
        lblName.setText(fresh.getStudentName());
        lblSex.setText(fresh.getSex());
        lblIdentity.setText(fresh.getIdentity());
        lblUserId.setText(fresh.getUserId());
        StudentPersonal sp = fresh.getPersonal();
        lblPhone.setText(sp != null ? nz(sp.getPhone()) : "未填写");
        lblEmail.setText(sp != null ? nz(sp.getEmail()) : "未填写");
        lblAddress.setText(sp != null ? nz(sp.getAddress()) : "未填写");
        revalidate();
        repaint();
    }

    private static String nz(String s) { return s == null ? "" : s; }

    /* ---------------------- 管理员编辑 ---------------------- */
    private void openEdit() {
        StudentPersonal old = student.getPersonal();
        if (old == null) old = new StudentPersonal(student.getStudentId(), "", "", "");

        StudentPersonal finalOld = old;
        new EditPersonalDialog(SwingUtilities.getWindowAncestor(this), finalOld, ok -> {
            if (ok) {
                // 重新查询最新数据并就地刷新本面板（不再重建整个面板与 controller）
                Student fresh = new StudentController().getStudent(student.getStudentId());
                if (fresh != null) {
                    refreshView(fresh);
                    if (parentRefresh != null) parentRefresh.run(); // 刷新主表
                }
            }
        }).setVisible(true);
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
