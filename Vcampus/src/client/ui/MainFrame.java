package client.ui;

import client.net.ClientSocket;
import common.model.Student;
import common.model.User;
import common.net.Message;
import common.net.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class MainFrame extends JFrame {
    protected User user;
    protected Student student; // 当前登录学生对象
    protected JPanel mainPanel;      // 用于存放模块
    protected CardLayout cardLayout; // 控制模块切换
    protected JPanel sideBar;        // 左侧侧边栏
    protected Map<String, JButton> moduleButtons = new LinkedHashMap<>(); // 模块按钮
    private JLabel userLabel; // 用户ID标签
    private JLabel avatarLabel; // 头像标签
    private boolean sidebarExpanded = true; // 初始展开
    // 用于存放所有侧边栏按钮（固定按钮 + 模块按钮）
    protected Map<String, JButton> allButtons = new LinkedHashMap<>();
    // 存储模块工厂（延迟创建）
    private Map<String, Supplier<JPanel>> moduleSuppliers = new HashMap<>();
    private Map<String, JPanel> loadedModules = new HashMap<>();


    public MainFrame(User user) {
        this.user = user;
        setTitle("虚拟校园 - " + user.getType() + " 界面");
        setSize(1280, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 获取学生信息
        if ("student".equalsIgnoreCase(user.getType())) {
            String studentId = findStudentIdByUserId(user.getUserId());
            if (studentId != null) this.student = findStudentById(studentId);
        }

        // ================= 左侧侧边栏 =================
        sideBar = new JPanel();
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setBackground(new Color(84, 113, 232));
        sideBar.setPreferredSize(new Dimension(60, getHeight()));
        sideBar.addMouseListener(sidebarHoverListener);

        // 头像
        ImageIcon avatarIcon = new ImageIcon(getClass().getResource("/pictures/东大2.jpg"));
        Image avatarImg = avatarIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        avatarLabel = new JLabel(new ImageIcon(avatarImg));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 5, 0));
        sideBar.add(avatarLabel);

        // 用户ID
        userLabel = new JLabel(user.getUserId());
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        userLabel.setVisible(false);
        sideBar.add(userLabel);

        // 固定按钮
        JButton homeBtn = createSideButton("首页", "/pictures/首页.png");
        homeBtn.addActionListener(e -> showModule("Welcome"));
        registerFixedButton("首页", homeBtn);

        JButton infoBtn = createSideButton("个人信息", "/pictures/个人.png");
        infoBtn.addActionListener(e -> showPersonalInfo());
        registerFixedButton("个人信息", infoBtn);

        sideBar.add(Box.createVerticalStrut(20));

        // ================= 主内容面板 =================
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel, BorderLayout.CENTER);

        // 欢迎页
        JPanel homePanel = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("欢迎你, " + user.getUserId(), JLabel.CENTER);
        welcome.setFont(new Font("微软雅黑", Font.BOLD, 28));
        homePanel.add(welcome, BorderLayout.NORTH);
        ImageCarouselPanel carousel = new ImageCarouselPanel();
        homePanel.add(carousel, BorderLayout.CENTER);
        mainPanel.add(homePanel, "Welcome");

        // 功能模块按钮
        addModuleButtons();

        // 底部退出按钮
        sideBar.add(Box.createVerticalGlue());
        JButton logoutBtn = createSideButton("退出登录", "/pictures/退出.png");
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setOpaque(true);
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { logoutBtn.setBackground(new Color(200, 40, 55)); }
            @Override
            public void mouseExited(MouseEvent e) { logoutBtn.setBackground(new Color(220, 53, 69)); }
        });
        logoutBtn.addActionListener(e -> logout());
        registerFixedButton("退出登录", logoutBtn);

        add(sideBar, BorderLayout.WEST);
    }


    /** 工具方法：统一按钮风格 */
    private JButton createSideButton(String text, String iconPath) {
        JButton btn = new JButton(text, loadIcon(iconPath));
        //btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btn.setBackground(new Color(84, 113, 232));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(new Color(100, 130, 240));
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(new Color(84, 113, 232));
            }
        });

        return btn;
    }


    /** 子类实现：添加功能模块按钮 */
    protected abstract void addModuleButtons();

    /** 子类实现：添加功能模块菜单项 */
    protected abstract void addModuleMenu(JMenu systemMenu);

    /** 注册一个功能模块（支持收起/展开时隐藏文字） */


    /** 新版：延迟加载模块 */
    protected void registerModule(String name, Supplier<JPanel> panelSupplier, String iconPath) {
        moduleSuppliers.put(name, panelSupplier);

        // 先放一个占位空面板（CardLayout 需要有名字，不然 showModule 会报错）
        JPanel placeholder = new JPanel();
        mainPanel.add(placeholder, name);

        JButton btn = createSideButton(name, iconPath);
        btn.setActionCommand(name);
        btn.addActionListener(e -> showModule(name));

        moduleButtons.put(name, btn);
        allButtons.put(name, btn);

        // 给模块按钮也加悬停展开事件
        btn.addMouseListener(sidebarHoverListener);

        sideBar.add(btn);
        sideBar.add(Box.createVerticalStrut(10));
    }

    /** 兼容旧调用，不传 icon 的情况 */
    protected void registerModule(String name, Supplier<JPanel> panelSupplier) {
        registerModule(name, panelSupplier, null);
    }

    /** 懒加载切换模块 */
    //@Override
    protected void showModule(String name) {
        // 如果还没真正加载，就创建一次
        if (!loadedModules.containsKey(name)) {
            Supplier<JPanel> supplier = moduleSuppliers.get(name);
            if (supplier != null) {
                JPanel panel = supplier.get();
                loadedModules.put(name, panel);
                mainPanel.add(panel, name); // 替换 placeholder
            }
        }

        cardLayout.show(mainPanel, name);
        highlightButton(name);
    }

    /** 高亮当前按钮 */
    private void highlightButton(String activeName) {
        for (Map.Entry<String, JButton> entry : moduleButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(activeName)) {
                btn.setBackground(new Color(100, 149, 237));
            } else {
                btn.setBackground(new Color(84, 113, 232));
            }
        }
    }

    protected void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void logout() {
        dispose();
        new LoginFrame().setVisible(true); // 返回登录界面
    }

    protected void showPersonalInfo() {
        if (student != null) {
            JDialog dialog = new JDialog(this, "个人信息", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setSize(480, 340);
            dialog.setLocationRelativeTo(this);

            StudentInfoPanel infoPanel = new StudentInfoPanel(student, dialog::dispose);
            dialog.setContentPane(infoPanel);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "未能加载学生信息！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }


    // 查找学号
    protected String findStudentIdByUserId(String userId) {
        try {
            Message req = new Message(MessageType.STUDENT_ID_BY_USERID, userId);
            Message resp = new ClientSocket().sendRequest(req);
            if ("success".equals(resp.getStatus())) {
                return (String) resp.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 查找学生对象
    protected Student findStudentById(String studentId) {
        try {
            Message req = new Message(MessageType.STUDENT_GET, studentId);
            Message resp = new ClientSocket().sendRequest(req);
            if ("success".equals(resp.getStatus())) {
                return (Student) resp.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

private void toggleSidebar(boolean expand) {
    int targetWidth = expand ? 180 : 60;
    int step = 5;
    Timer timer = new Timer(15, null);

    timer.addActionListener(e -> {
        int currentWidth = sideBar.getWidth();
        boolean done = false;
        if (expand) {
            currentWidth += step;
            if (currentWidth >= targetWidth) {
                currentWidth = targetWidth;
                done = true;
            }
        } else {
            currentWidth -= step;
            if (currentWidth <= targetWidth) {
                currentWidth = targetWidth;
                done = true;
            }
        }

        sideBar.setPreferredSize(new Dimension(currentWidth, sideBar.getHeight()));
        sideBar.revalidate(); // 只更新 sideBar 自身即可

        if (done) {
            timer.stop();
            updateButtonLabels(expand);
            sidebarExpanded = expand;
        }
    });
    timer.start();
}


    private void updateButtonLabels(boolean expanded) {
        for (Map.Entry<String, JButton> entry : allButtons.entrySet()) {
            JButton btn = entry.getValue();
            btn.setText(expanded ? entry.getKey() : ""); // 收起时清空文字
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

    private void registerFixedButton(String key, JButton btn) {
        allButtons.put(key, btn);

        // 给按钮也加悬停展开事件
        btn.addMouseListener(sidebarHoverListener);

        sideBar.add(btn);
        sideBar.add(Box.createVerticalStrut(10));
    }

    private MouseAdapter sidebarHoverListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            if (!sidebarExpanded) {
                toggleSidebar(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // 检查鼠标是否真的移出了 sideBar
            Point mousePos = sideBar.getMousePosition();
            if (sidebarExpanded && mousePos == null) {
                toggleSidebar(false);
            }
        }
    };


}
