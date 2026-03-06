package client.ui;

import client.controller.UserController;
import com.formdev.flatlaf.FlatLightLaf;
import common.model.User;
import common.net.Message;
import util.Router;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUserId;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private JLabel lblMessage;

    public LoginFrame() {
        FlatLightLaf.setup();

        // 全局 UI 调整
        UIManager.put("Button.arc", 10);                // 按钮圆角
        UIManager.put("Component.focusWidth", 2);       // 焦点边框
        UIManager.put("Table.showGrid", true);          // 表格显示网格
        UIManager.put("Table.gridColor", new Color(220,220,220));
        UIManager.put("Table.selectionBackground", new Color(0,120,215)); // 选中颜色
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TextComponent.arc", 5);          // 文本框圆角
        UIManager.put("ScrollBar.showButtons", true);   // 滚动条按钮

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
        btnLogin = new JButton("登录");
        btnLogin.setBackground(new Color(84, 113, 232));
        btnLogin.setForeground(Color.WHITE);

        btnExit = new JButton("退出");
//        btnExit.setBackground(new Color(220, 53, 69));
//        btnExit.setForeground(Color.WHITE);

        Dimension btnSize = new Dimension(100, 35);
        btnLogin.setPreferredSize(btnSize);
        btnExit.setPreferredSize(btnSize);

        btnPanel.add(btnLogin);
        btnPanel.add(btnExit);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        // ====== 按钮事件 ======
        btnLogin.addActionListener(e -> handleLogin());
        btnExit.addActionListener(e -> System.exit(0));
    }

    private void handleLogin() {
        String userId = txtUserId.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (userId.isEmpty() || password.isEmpty()) {
            lblMessage.setText("账号和密码不能为空！");
            return;
        }

        lblMessage.setText("正在验证，请稍候...");

        UserController controller = new UserController();
        Message response = controller.login(userId, password);
        if ("success".equals(response.getStatus())) {
            User user = (User) response.getData();
            lblMessage.setText("登录成功，身份：" + user.getType());
            Router.route(user);
            dispose();
        } else {
            lblMessage.setText("登录失败：" + response.getMsg());
        }
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
