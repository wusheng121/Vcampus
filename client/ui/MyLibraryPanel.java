package client.ui;

import common.model.User;

import javax.swing.*;
import java.awt.*;

public class MyLibraryPanel extends JPanel {
    public MyLibraryPanel(User user) {
        setLayout(new BorderLayout());

        // 左侧菜单
        JPanel leftMenu = new JPanel();
        leftMenu.setLayout(new BoxLayout(leftMenu, BoxLayout.Y_AXIS));
        leftMenu.setPreferredSize(new Dimension(200, 0));

        // 菜单项
        String[] menuItems = {"当前借阅", "借阅历史"};

        // 右侧内容区（使用 CardLayout 切换）
        JPanel rightContentPanel = new JPanel(new CardLayout());
        CardLayout rightCard = (CardLayout) rightContentPanel.getLayout();

        for (String menu : menuItems) {
            JButton btn = new JButton(menu);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            leftMenu.add(btn);
            leftMenu.add(Box.createVerticalStrut(10));

            JPanel content;
            switch (menu) {
//                case "我的首页":
//                    content = new LibraryHomePanel(user);
//                    break;
                case "当前借阅":
                    content = new CurrentBorrowPanel(user);
                    break;
                case "借阅历史":
                    content = new BorrowHistoryPanel(user);
                    break;
//                case "检索历史":
//                    content = new SearchHistoryPanel(user);
//                    break;
                default:
                    content = new JPanel(new BorderLayout());
                    content.add(new JLabel(menu + " 内容区", JLabel.CENTER), BorderLayout.CENTER);
            }

            rightContentPanel.add(content, menu);

            btn.addActionListener(e -> rightCard.show(rightContentPanel, menu));
        }

        // 分隔布局
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftMenu, rightContentPanel);
        splitPane.setDividerLocation(200);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
    }
}
