package client.ui;

import client.controller.UserController;
import common.model.User;
import util.AsyncRunner;
import util.Router;
import util.UITheme;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUserId;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private JLabel lblMessage;

    public LoginFrame() {
        setTitle("虚拟校园 - 用户登录");
        setSize(1280, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        // ====== 背景面板（绘制背景图） ======
        // 背景面板
        JPanel background = new JPanel(new BorderLayout()) {
            private Image bg = new ImageIcon(
                    LoginFrame.class.getResource("/pictures/东大4.jpg")
            ).getImage();


            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bg != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f)); // 背景透明度
                    g2d.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                    g2d.dispose();
                }
            }
        };
        background.setOpaque(false);
        add(background);

        // ====== 居中容器 ======
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false); // 透明，显示背景
        background.add(centerPanel, BorderLayout.CENTER);

        // ====== 登录卡片 ======
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(255, 255, 255, 100)); // 半透明白色
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        centerPanel.add(formPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel lblTitle = new JLabel("虚拟校园登录", JLabel.CENTER);
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(lblTitle, gbc);

        // 账号
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("账号:"), gbc);

        txtUserId = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(txtUserId, gbc);

        // ====== 密码 ======
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("密码:"), gbc);

// 密码输入面板（带小眼睛按钮）
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setOpaque(false); // 透明背景

        txtPassword = new JPasswordField(20);
        passwordPanel.add(txtPassword, BorderLayout.CENTER);

// 小眼睛按钮
        JButton eyeButton = new JButton(loadIcon("/pictures/不显示密码.png"));
        eyeButton.setBorder(BorderFactory.createEmptyBorder());
        eyeButton.setContentAreaFilled(false);
        eyeButton.setFocusPainted(false);
        eyeButton.setOpaque(false);
        passwordPanel.add(eyeButton, BorderLayout.EAST);

// 点击切换显示/隐藏密码
        eyeButton.addActionListener(e -> {
            if (txtPassword.getEchoChar() == (char)0) {
                txtPassword.setEchoChar('•'); // 隐藏
                eyeButton.setIcon(loadIcon("/pictures/不显示密码.png"));
            } else {
                txtPassword.setEchoChar((char)0); // 显示
                eyeButton.setIcon(loadIcon("/pictures/显示密码.png"));
            }
        });

// 添加到表单
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(passwordPanel, gbc);


        // 提示信息
        lblMessage = new JLabel("");
        lblMessage.setForeground(Color.RED);
        lblMessage.setOpaque(true); // ✅ 开启不透明背景
        lblMessage.setBackground(new Color(255, 255, 255, 180)); // 半透明白底
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        //lblMessage.setPreferredSize(new Dimension(200, 25));
        formPanel.add(lblMessage, gbc);

        // 按钮区域
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false); // 背景透明
        btnLogin = new JButton("登录(L)");
        btnLogin.setMnemonic('L');
        btnLogin.setToolTipText("登录虚拟校园");
        btnLogin.setBackground(UITheme.PRIMARY);
        btnLogin.setForeground(Color.WHITE);

        btnExit = new JButton("退出(X)");
        btnExit.setMnemonic('X');
        btnExit.setToolTipText("退出程序");

        Dimension btnSize = new Dimension(100, 35);
        btnLogin.setPreferredSize(btnSize);
        btnExit.setPreferredSize(btnSize);

        btnPanel.add(btnLogin);
        btnPanel.add(btnExit);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        // 忘记密码链接
        JButton btnForgot = new JButton("忘记密码？");
        btnForgot.setBorderPainted(false);
        btnForgot.setContentAreaFilled(false);
        btnForgot.setOpaque(false);
        btnForgot.setForeground(UITheme.PRIMARY);
        btnForgot.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnForgot.setToolTipText("通过注册邮箱找回密码");
        btnForgot.addActionListener(e -> new ForgotPasswordDialog(this).setVisible(true));
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        forgotPanel.setOpaque(false);
        forgotPanel.add(btnForgot);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        formPanel.add(forgotPanel, gbc);

        // ====== 按钮事件 ======
        btnLogin.addActionListener(e -> handleLogin());
        btnExit.addActionListener(e -> System.exit(0));
        // Enter 提交：在账号/密码框按 Enter 直接登录
        txtUserId.addActionListener(e -> handleLogin());
        txtPassword.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String userId = txtUserId.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (userId.isEmpty() || password.isEmpty()) {
            lblMessage.setText("账号和密码不能为空！");
            return;
        }

        UserController controller = new UserController();
        AsyncRunner.run(
                () -> controller.login(userId, password),
                response -> {
                    if ("success".equals(response.getStatus())) {
                        User user = (User) response.getData();
                        util.ClientSession.login(user); // 设会话，供后续请求携带 caller 做 RBAC
                        lblMessage.setText("登录成功，身份：" + user.getType());
                        Router.route(user);
                        dispose();
                    } else {
                        lblMessage.setText("登录失败：" + response.getMsg());
                    }
                },
                () -> { // 进入加载态（EDT），"正在验证"现在能真正重绘
                    lblMessage.setText("正在验证，请稍候...");
                    btnLogin.setEnabled(false);
                    btnExit.setEnabled(false);
                    txtUserId.setEditable(false);
                    txtPassword.setEditable(false);
                },
                () -> { // 退出加载态（EDT）
                    btnLogin.setEnabled(true);
                    btnExit.setEnabled(true);
                    txtUserId.setEditable(true);
                    txtPassword.setEditable(true);
                }
        );
    }

    private ImageIcon loadIcon(String resourcePath) {
        java.net.URL imgURL = getClass().getResource(resourcePath);
        if (imgURL != null) {
            Image img = new ImageIcon(imgURL).getImage();
            Image scaled = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
