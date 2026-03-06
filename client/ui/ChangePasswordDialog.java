package client.ui;

import client.controller.UserController;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private final String userId;
    private final UserController controller = new UserController();

    private final JPasswordField tfOld = new JPasswordField(15);
    private final JPasswordField tfNew = new JPasswordField(15);
    private final JPasswordField tfConfirm = new JPasswordField(15);

    public ChangePasswordDialog(Window owner, String userId) {
        super(owner, "修改密码", ModalityType.APPLICATION_MODAL);
        this.userId = userId;
        setSize(360, 220);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        form.add(new JLabel("原密码:")); form.add(tfOld);
        form.add(new JLabel("新密码:")); form.add(tfNew);
        form.add(new JLabel("确认密码:")); form.add(tfConfirm);

        JButton btnSave = new JButton("保存");
        JButton btnCancel = new JButton("取消");
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        south.add(btnSave);
        south.add(btnCancel);

        add(form, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void onSave() {
        String oldPwd = new String(tfOld.getPassword()).trim();
        String newPwd = new String(tfNew.getPassword()).trim();
        String confirm = new String(tfConfirm.getPassword()).trim();

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "三项均不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPwd.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "新密码与确认密码不一致！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = controller.updatePassword(userId, oldPwd, newPwd);
        if (ok) {
            JOptionPane.showMessageDialog(this, "密码修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "修改失败，请检查原密码或网络！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
