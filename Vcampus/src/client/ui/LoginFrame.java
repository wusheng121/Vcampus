package client.ui;

import client.controller.UserController;
import common.model.User;
import common.net.Message;
import util.Router;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField txtUserId;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private JLabel lblMessage;

    public LoginFrame() {
        setTitle("虚拟校园 - 用户登录");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        // 主面板
        JPanel panel = new JPanel();
        panel.setLayout(null);
        add(panel);

        // 账号标签与输入框
        JLabel lblUser = new JLabel("账号:");
        lblUser.setBounds(50, 50, 80, 25);
        panel.add(lblUser);

        txtUserId = new JTextField(20);
        txtUserId.setBounds(150, 50, 180, 25);
        panel.add(txtUserId);

        // 密码标签与输入框
        JLabel lblPass = new JLabel("密码:");
        lblPass.setBounds(50, 90, 80, 25);
        panel.add(lblPass);

        txtPassword = new JPasswordField(20);
        txtPassword.setBounds(150, 90, 180, 25);
        panel.add(txtPassword);

        // 提示信息
        lblMessage = new JLabel("");
        lblMessage.setForeground(Color.RED);
        lblMessage.setBounds(50, 130, 280, 25);
        panel.add(lblMessage);

        // 登录按钮
        btnLogin = new JButton("登录");
        btnLogin.setBounds(80, 170, 100, 30);
        panel.add(btnLogin);

        // 退出按钮
        btnExit = new JButton("退出");
        btnExit.setBounds(220, 170, 100, 30);
        panel.add(btnExit);

        // 按钮事件监听
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userId = txtUserId.getText().trim();
                String password = new String(txtPassword.getPassword());
                if (userId.isEmpty() || password.isEmpty()) {
                    lblMessage.setText("账号和密码不能为空！");
                } else {

                    lblMessage.setText("正在验证，请稍候...");

                    UserController controller = new UserController();
                    Message response = controller.login(userId, password);
                    if ("success".equals(response.getStatus())) {
                        User user = (User) response.getData();
                        lblMessage.setText("登录成功，身份：" + ((User)response.getData()).getType());
                        // 后续可根据身份跳转对应主界面
                        Router.route(user);
                        dispose();
                    } else {
                        lblMessage.setText("登录失败：" + response.getMsg());
                    }
                }
            }
        });

        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // 退出程序
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
