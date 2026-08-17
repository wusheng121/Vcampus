import client.ui.LoginFrame;
import util.UITheme;

import javax.swing.*;

/**
 * 客户端启动入口。
 */
public class App {
    public static void main(String[] args) {
        // 全局安装 FlatLightLaf + 统一 UI 默认值（整个 JVM 生命周期只需一次）
        UITheme.setup();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
