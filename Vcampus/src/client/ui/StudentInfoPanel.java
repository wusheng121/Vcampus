package client.ui;

import client.controller.StudentController;
import common.model.Student;
import common.model.StudentPersonal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class StudentInfoPanel extends JPanel {

    private final Student student;
    private final StudentController controller = new StudentController();
    private final Runnable onBack;
    /* 添加在类最上面，和其他静态常量放一起 */
    private static final Set<String> notifiedSet = new HashSet<>();
    /* 统一配色 */
    private static final Color BG_GLOBAL   = new Color(0xFAFAFA);
    private static final Color BG_CARD     = new Color(0xFFFFFF);
    private static final Color BG_SUB      = new Color(0xF5F7FA);
    private static final Color COLOR_THEME = new Color(0x3874F7);
    private static final Color COLOR_GRAY  = new Color(0x8C8C8C);

    /* 只读字段 */
    private final JTextField tfStudentId   = createUnderLineField();
    private final JTextField tfStudentName = createUnderLineField();
    private final JTextField tfSex         = createUnderLineField();
    private final JTextField tfIdentity    = createUnderLineField();
    private final JTextField tfUserId      = createUnderLineField();

    /* 可编辑字段 */
    private final JTextField tfPhone = createUnderLineField();
    private final JTextField tfEmail = createUnderLineField();
    private final JTextField tfAddr  = createUnderLineField();

    /* 按钮 */
    private final JButton btnEdit = createFlatButton("编辑", COLOR_THEME, Color.WHITE);
    private final JButton btnBack = createFlatButton("返回", COLOR_GRAY,  Color.WHITE);

    public StudentInfoPanel(Student stu, Runnable onBack) {
        this.student = stu;
        this.onBack  = onBack;
        setBackground(BG_GLOBAL);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initView();
        loadData();
        setEditable(false);
        checkAuditStatus();
    }

    /* ====================== 布局入口 ====================== */
    private void initView() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 20, 0);

        /* 学籍信息 – 65 % */
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0.65;
        add(createTopCard(), gbc);

        /* 联系信息 – 35 % */
        gbc.gridy = 1; gbc.weighty = 0.35;
        add(createCenterCard(), gbc);

        /* 按钮区 – 0 % */
        gbc.gridy = 2; gbc.weighty = 0; gbc.insets = new Insets(0, 0, 0, 0);
        add(createSouthPanel(), gbc);
    }

    /* -------------------- 卡片工厂 -------------------- */
    private JPanel createTopCard() {
        JPanel p = new JPanel(new GridLayout(5, 2, 20, 12));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        p.add(createLabel("学号"));      p.add(tfStudentId);
        p.add(createLabel("姓名"));      p.add(tfStudentName);
        p.add(createLabel("性别"));      p.add(tfSex);
        p.add(createLabel("身份证号"));  p.add(tfIdentity);
        p.add(createLabel("一卡通号"));  p.add(tfUserId);
        return wrapCard(p, "学籍信息（只读）");
    }

    private JPanel createCenterCard() {
        JPanel p = new JPanel(new GridLayout(3, 2, 20, 10));
        p.setBackground(BG_SUB);
        p.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        p.add(createLabel("电话")); p.add(tfPhone);
        p.add(createLabel("邮箱")); p.add(tfEmail);
        p.add(createLabel("地址")); p.add(tfAddr);
        return wrapCard(p, "联系信息（审核通过后生效）");
    }

    private JPanel createSouthPanel() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        south.setBackground(BG_GLOBAL);

        JButton btnChangePwd = createFlatButton("修改密码", COLOR_THEME, Color.WHITE);
        btnChangePwd.addActionListener(e ->
                new ChangePasswordDialog(SwingUtilities.getWindowAncestor(this), student.getUserId()).setVisible(true));

        btnEdit.addActionListener(e -> openEditDialog());
        btnBack.addActionListener(e -> { if (onBack != null) onBack.run(); });

        south.add(btnChangePwd);   // 先加改密
        south.add(btnEdit);
        south.add(btnBack);
        return south;
    }

    private JPanel wrapCard(JPanel content, String title) {
        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setBackground(BG_GLOBAL);
        JLabel lbTitle = new JLabel(title);
        lbTitle.setFont(lbTitle.getFont().deriveFont(Font.BOLD, 14));
        lbTitle.setForeground(COLOR_GRAY);
        wrap.add(lbTitle, BorderLayout.NORTH);
        wrap.add(content, BorderLayout.CENTER);
        return wrap;
    }

    /* -------------------- 组件工厂 -------------------- */
    private JLabel createLabel(String txt) {
        JLabel lb = new JLabel(txt);
        lb.setForeground(COLOR_GRAY);
        return lb;
    }

    private JTextField createUnderLineField() {
        JTextField f = new JTextField();
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_GRAY),
                BorderFactory.createEmptyBorder(2, 0, 2, 0)));
        f.setBackground(null);
        f.setEditable(false);
        return f;
    }

    private static JButton createFlatButton(String txt, Color bg, Color fg) {
        JButton b = new JButton(txt);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(90, 32));
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 14));
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder());
        return b;
    }

    /* -------------------- 数据与交互 -------------------- */
    private void loadData() {
        tfStudentId.setText(student.getStudentId());
        tfStudentName.setText(student.getStudentName());
        tfSex.setText(student.getSex());
        tfIdentity.setText(student.getIdentity());
        tfUserId.setText(student.getUserId());

        StudentPersonal p = student.getPersonal();
        tfPhone.setText(p == null || p.getPhone() == null ? "" : p.getPhone());
        tfEmail.setText(p == null || p.getEmail() == null ? "" : p.getEmail());
        tfAddr.setText(p == null || p.getAddress() == null ? "" : p.getAddress());
    }

    private void setEditable(boolean editable) {
        tfPhone.setEditable(editable);
        tfEmail.setEditable(editable);
        tfAddr.setEditable(editable);
    }

    private void openEditDialog() {
        new EditDialog(SwingUtilities.getWindowAncestor(this), sp -> {
            boolean ok = controller.submitPersonalAudit(sp);
            if (ok) {
                JOptionPane.showMessageDialog(this, "修改已提交，待管理员审核！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "提交失败，请稍后重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }).setVisible(true);
    }

    private void checkAuditStatus() {
        String status = controller.getPersonalAuditStatus(student.getStudentId());
        // 如果已经提示过，直接返回
        if (notifiedSet.contains(student.getStudentId())) {
            return;
        }

        switch (status) {
            case "APPROVED":
                JOptionPane.showMessageDialog(this, "您的联系信息已审核通过！", "提示", JOptionPane.INFORMATION_MESSAGE);
                notifiedSet.add(student.getStudentId());
                break;
            case "REJECTED":
                String reason = controller.getPersonalAuditRemark(student.getStudentId());
                JOptionPane.showMessageDialog(this, "审核未通过：\n" + reason, "提示", JOptionPane.WARNING_MESSAGE);
                notifiedSet.add(student.getStudentId());
                break;
            // 如果你也想提示“待审核”，也可以加 case "PENDING"
        }
    }

    /* -------------------- 编辑弹窗 -------------------- */
    private final class EditDialog extends JDialog {
        private final JTextField tfPhone = createUnderLineField();
        private final JTextField tfEmail = createUnderLineField();
        private final JTextField tfAddr  = createUnderLineField();
        private final java.util.function.Consumer<StudentPersonal> callback;

        EditDialog(Window owner, java.util.function.Consumer<StudentPersonal> callback) {
            super(owner, "编辑联系信息", ModalityType.APPLICATION_MODAL);
            this.callback = callback;
            setSize(400, 240);
            setLocationRelativeTo(owner);
            setResizable(false);
            getContentPane().setBackground(BG_GLOBAL);

            StudentPersonal old = student.getPersonal();
            tfPhone.setText(old == null ? "" : old.getPhone());
            tfEmail.setText(old == null ? "" : old.getEmail());
            tfAddr.setText(old == null ? "" : old.getAddress());
            tfPhone.setEditable(true);
            tfEmail.setEditable(true);
            tfAddr.setEditable(true);
            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
            form.setBackground(BG_GLOBAL);
            form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            form.add(createLabel("电话 *")); form.add(tfPhone);
            form.add(createLabel("邮箱 *")); form.add(tfEmail);
            form.add(createLabel("地址 *")); form.add(tfAddr);

            JButton btnSave   = createFlatButton("保存", COLOR_THEME, Color.WHITE);
            JButton btnCancel = createFlatButton("取消", COLOR_GRAY,  Color.WHITE);
            btnSave.addActionListener(this::onSave);
            btnCancel.addActionListener(e -> dispose());

            JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            south.setBackground(BG_GLOBAL);
            south.add(btnSave);
            south.add(btnCancel);

            add(form, BorderLayout.CENTER);
            add(south, BorderLayout.SOUTH);
        }

        private void onSave(ActionEvent e) {
            String phone = tfPhone.getText().trim();
            String email = tfEmail.getText().trim();
            String addr  = tfAddr.getText().trim();
            if (phone.isEmpty() || email.isEmpty() || addr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "电话、邮箱、地址都不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            callback.accept(new StudentPersonal(student.getStudentId(), phone, email, addr));
            dispose();
        }
    }
}