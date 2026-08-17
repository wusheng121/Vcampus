package client.ui;
import util.UITheme;

import client.net.ClientSocket;
import common.model.Teacher;
import common.net.Message;
import common.net.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TeacherInfoPanel extends JPanel {

    private  Teacher teacher;
    private final Runnable onBack;

    /* 统一配色 */
    private static final Color BG_GLOBAL   = new Color(0xFAFAFA);
    private static final Color BG_CARD     = new Color(0xFFFFFF);
    private static final Color BG_SUB      = new Color(0xF5F7FA);
    private static final Color COLOR_THEME = UITheme.PRIMARY;
    private static final Color COLOR_GRAY  = new Color(0x8C8C8C);

    /* 只读文本框 */
    private final JTextField tfTeacherId   = createUnderLineField();
    private final JTextField tfTeacherName = createUnderLineField();
    private final JTextField tfSex         = createUnderLineField();
    private final JTextField tfSubject     = createUnderLineField();
    private final JTextField tfUserId      = createUnderLineField();
    private final JTextField tfPhone       = createUnderLineField();
    private final JTextField tfEmail       = createUnderLineField();
    private final JTextField tfAddr        = createUnderLineField();

    /* 按钮 */
    private final JButton btnEdit = createFlatButton("编辑", COLOR_THEME, Color.WHITE);
    private final JButton btnBack = createFlatButton("返回", COLOR_GRAY,  Color.WHITE);

    public TeacherInfoPanel(Teacher teacher, Runnable onBack) {
        this.teacher = teacher;
        this.onBack  = onBack;
        setBackground(BG_GLOBAL);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initView();
        loadData();
        setEditable(false);   // 只读
    }

    /* ====================== 布局入口 ====================== */
    private void initView() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 20, 0);

        /* 教师信息 – 65 % */
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
        p.add(createLabel("教工号"));      p.add(tfTeacherId);
        p.add(createLabel("姓名"));        p.add(tfTeacherName);
        p.add(createLabel("性别"));        p.add(tfSex);
        p.add(createLabel("任教科目"));    p.add(tfSubject);
        p.add(createLabel("用户号"));      p.add(tfUserId);
        return wrapCard(p, "教师信息");
    }

    private JPanel createCenterCard() {
        JPanel p = new JPanel(new GridLayout(3, 2, 20, 10));
        p.setBackground(BG_SUB);
        p.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        p.add(createLabel("电话")); p.add(tfPhone);
        p.add(createLabel("邮箱")); p.add(tfEmail);
        p.add(createLabel("地址")); p.add(tfAddr);
        return wrapCard(p, "联系信息");
    }

    private JPanel createSouthPanel() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        south.setBackground(BG_GLOBAL);

        JButton btnChangePwd = createFlatButton("修改密码", COLOR_THEME, Color.WHITE);
        btnChangePwd.addActionListener(e ->
                new ChangePasswordDialog(SwingUtilities.getWindowAncestor(this), teacher.getUserId()).setVisible(true));

        btnEdit.addActionListener(e -> openEditDialog());
        btnBack.addActionListener(e -> { if (onBack != null) onBack.run(); });

        south.add(btnChangePwd);
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

    /* -------------------- 数据操作 -------------------- */
    private void loadData() {
        tfTeacherId.setText(teacher.getTeacherId());
        tfTeacherName.setText(teacher.getTeacherName());
        tfSex.setText(teacher.getSex());
        tfSubject.setText(teacher.getSubject());
        tfUserId.setText(teacher.getUserId());

        tfPhone.setText(teacher.getPhone() == null ? "" : teacher.getPhone());
        tfEmail.setText(teacher.getEmail() == null ? "" : teacher.getEmail());
        tfAddr.setText(teacher.getAddress() == null ? "" : teacher.getAddress());
    }

    private void setEditable(boolean editable) {
        tfTeacherId.setEditable(false);
        tfTeacherName.setEditable(false);
        tfSex.setEditable(false);
        tfSubject.setEditable(false);
        tfUserId.setEditable(false);
        tfPhone.setEditable(editable);
        tfEmail.setEditable(editable);
        tfAddr.setEditable(editable);
    }

    /* -------------------- 弹窗编辑 -------------------- */
    private void openEditDialog() {
        new EditDialog(SwingUtilities.getWindowAncestor(this), updated -> {
            if (updated != null) {
                this.teacher = updated;   // 更新内存对象
                loadData();               // 刷新只读面板
                JOptionPane.showMessageDialog(this, "保存成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            }
        }).setVisible(true);
    }
    private final class EditDialog extends JDialog {
        private final JTextField tfPhone = createUnderLineField();
        private final JTextField tfEmail = createUnderLineField();
        private final JTextField tfAddr  = createUnderLineField();
        private final java.util.function.Consumer<Teacher> callback;

        EditDialog(Window owner, java.util.function.Consumer<Teacher> callback) {
            super(owner, "编辑联系信息", ModalityType.APPLICATION_MODAL);
            this.callback = callback;
            setSize(400, 240);
            setLocationRelativeTo(owner);
            setResizable(false);
            getContentPane().setBackground(BG_GLOBAL);

            /* 回显旧值 */
            tfPhone.setText(teacher.getPhone() == null ? "" : teacher.getPhone());
            tfEmail.setText(teacher.getEmail() == null ? "" : teacher.getEmail());
            tfAddr.setText(teacher.getAddress() == null ? "" : teacher.getAddress());

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

            /* 直接写回对象 */
            teacher.setPhone(phone);
            teacher.setEmail(email);
            teacher.setAddress(addr);

            /* 落库 */
            boolean ok = saveTeacher(teacher);
            if (ok) {
                callback.accept(teacher);   // 通知外层刷新
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "保存失败，请稍后重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        private boolean saveTeacher(Teacher teacher) {
            try {
                Message req = new Message(MessageType.TEACHER_UPDATE, teacher);
                Message resp = ClientSocket.getInstance().sendRequest(req);
                return "success".equals(resp.getStatus());
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }
}