package client.ui;

import client.controller.UserController;
import util.AsyncRunner;
import util.UITheme;
import util.Validators;

import javax.swing.*;
import java.awt.*;

/**
 * 找回密码对话框：输入账号/邮箱 → 发送验证码 → 填验证码+新密码 → 重置。
 * 网络调用经 AsyncRunner 走后台线程，避免界面冻结。
 */
public class ForgotPasswordDialog extends JDialog {
    private final UserController controller = new UserController();

    private final JTextField tfAccount = new JTextField(20);
    private final JTextField tfCode = new JTextField(8);
    private final JPasswordField tfNew = new JPasswordField(16);
    private final JPasswordField tfConfirm = new JPasswordField(16);
    private final JLabel lblStatus = new JLabel(" ");
    private final JButton btnSend = new JButton("发送验证码");
    private final JButton btnReset = new JButton("重置密码");

    public ForgotPasswordDialog(Window owner) {
        super(owner, "找回密码", ModalityType.APPLICATION_MODAL);
        setSize(440, 300);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        form.add(new JLabel("账号或邮箱：")); form.add(tfAccount);
        form.add(new JLabel("验证码："));     form.add(tfCode);
        form.add(new JLabel("新密码："));     form.add(tfNew);
        form.add(new JLabel("确认密码："));   form.add(tfConfirm);
        add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        south.add(btnSend);
        south.add(btnReset);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(south, BorderLayout.NORTH);
        lblStatus.setForeground(UITheme.DANGER);
        JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusWrap.add(lblStatus);
        bottom.add(statusWrap, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        btnSend.addActionListener(e -> onSend());
        btnReset.addActionListener(e -> onReset());

        // Enter 提交：账号框 Enter → 发码；验证码/密码框 Enter → 重置
        tfAccount.addActionListener(e -> onSend());
        tfCode.addActionListener(e -> onReset());
        tfConfirm.addActionListener(e -> onReset());
    }

    private void onSend() {
        String acct = tfAccount.getText().trim();
        if (acct.isEmpty()) {
            status("请输入账号或邮箱");
            return;
        }
        setBusy(true, "正在发送验证码…");
        AsyncRunner.run(
                () -> controller.requestPasswordReset(acct),
                msg -> {
                    status(msg);
                    setBusy(false, null);
                });
    }

    private void onReset() {
        String acct = tfAccount.getText().trim();
        String code = tfCode.getText().trim();
        String np = new String(tfNew.getPassword());
        String cf = new String(tfConfirm.getPassword());
        if (acct.isEmpty() || code.isEmpty() || np.isEmpty()) {
            status("账号/验证码/新密码不能为空");
            return;
        }
        if (!np.equals(cf)) {
            status("两次输入的新密码不一致");
            return;
        }
        if (!Validators.passwordStrong(np)) {
            status("新密码强度不足：需至少 8 位且含字母与数字");
            return;
        }
        setBusy(true, "正在重置…");
        AsyncRunner.run(
                () -> controller.resetPassword(acct, code, np),
                result -> {
                    if ("success".equals(result)) {
                        JOptionPane.showMessageDialog(this, "密码重置成功，请用新密码登录。", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        status(result);
                    }
                    setBusy(false, null);
                });
    }

    private void setBusy(boolean busy, String busyMsg) {
        btnSend.setEnabled(!busy);
        btnReset.setEnabled(!busy);
        tfAccount.setEditable(!busy);
        tfCode.setEditable(!busy);
        tfNew.setEditable(!busy);
        tfConfirm.setEditable(!busy);
        if (busy && busyMsg != null) status(busyMsg);
    }

    private void status(String s) {
        lblStatus.setText(s == null || s.isEmpty() ? " " : s);
    }
}
