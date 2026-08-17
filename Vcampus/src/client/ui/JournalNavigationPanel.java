// src/client/ui/JournalNavigationPanel.java
package client.ui;
import util.UITheme;

import client.controller.JournalController;
import common.model.Journal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class JournalNavigationPanel extends JPanel {
    private final JournalController controller = new JournalController();
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<String> categoryBox;
    private JLabel statusLabel;

    // 缓存
    private List<Journal> currentList = Collections.emptyList(); // 全部文献
    private List<Journal> displayList = Collections.emptyList(); // 当前显示的过滤结果

    public JournalNavigationPanel() {
        setLayout(new BorderLayout());

        // 顶部栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("查询");
        searchBtn.setBackground(UITheme.HEADER);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setOpaque(true);

        categoryBox = new JComboBox<>();
        categoryBox.addItem("全部");

        statusLabel = new JLabel("正在加载文献...");
        statusLabel.setForeground(Color.DARK_GRAY);

        topPanel.add(new JLabel("分类:"));
        topPanel.add(categoryBox);
        topPanel.add(Box.createHorizontalStrut(8));
        topPanel.add(new JLabel("关键词:"));
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        topPanel.add(Box.createHorizontalStrut(12));
        topPanel.add(statusLabel);
        add(topPanel, BorderLayout.NORTH);

        // 表格
        String[] columns = {"编号", "名称", "类别", "出版日期", "出版社"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);
        JTableHeader header = table.getTableHeader();
        header.setBackground(UITheme.HEADER);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("微软雅黑", Font.BOLD, 14));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 搜索 + 分类过滤
        searchBtn.addActionListener(e -> filterAndRefreshFromCache());
        categoryBox.addActionListener(e -> filterAndRefreshFromCache());

        // 双击详情：直接从 displayList 取
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && row < displayList.size()) {
                        Journal j = displayList.get(row);
                        showDetailDialog(j);
                    }
                }
            }
        });

        // 后台加载
        loadAllDataAsync();
    }

    /** 异步加载所有文献 */
    private void loadAllDataAsync() {
        setLoadingState(true, "正在加载本地 + arXiv 文献...");
        SwingWorker<List<Journal>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Journal> doInBackground() {
                return controller.getAllJournalsCombined(""); // 一次性加载
            }

            @Override
            protected void done() {
                try {
                    List<Journal> list = get();
                    if (list == null) list = Collections.emptyList();
                    currentList = list;

                    populateCategoryBox(list);
                    filterAndRefreshFromCache();

                    setLoadingState(false, "共加载 " + list.size() + " 篇文献");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    currentList = Collections.emptyList();
                    displayList = Collections.emptyList();
                    setLoadingState(false, "加载失败");
                    JOptionPane.showMessageDialog(JournalNavigationPanel.this,
                            "加载期刊数据失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /** 更新类别下拉框 */
    private void populateCategoryBox(List<Journal> list) {
        List<String> cats = list.stream()
                .map(Journal::getCategory)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        SwingUtilities.invokeLater(() -> {
            categoryBox.removeAllItems();
            categoryBox.addItem("全部");
            for (String c : cats) categoryBox.addItem(c);
        });
    }

    /** 从缓存过滤并刷新表格 */
    private void filterAndRefreshFromCache() {
        if (currentList == null || currentList.isEmpty()) {
            setLoadingState(true, "正在加载，请稍候...");
            return;
        }
        String category = (String) categoryBox.getSelectedItem();
        if (category == null) category = "全部";
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        String finalCategory = category;
        displayList = currentList.stream()
                .filter(j -> {
                    boolean okCat = "全部".equals(finalCategory) || finalCategory.equals(j.getCategory());
                    if (!okCat) return false;
                    if (keyword.isEmpty()) return true;
                    return String.valueOf(j.getJournalId()).contains(keyword)
                            || (j.getName() != null && j.getName().toLowerCase().contains(keyword))
                            || (j.getCategory() != null && j.getCategory().toLowerCase().contains(keyword))
                            || (j.getPublisher() != null && j.getPublisher().toLowerCase().contains(keyword));
                })
                .collect(Collectors.toList());

        // 更新表格
        model.setRowCount(0);
        for (Journal j : displayList) {
            model.addRow(new Object[]{
                    j.getJournalId(),
                    j.getName(),
                    j.getCategory(),
                    j.getPublishDate(),
                    j.getPublisher()
            });
        }

        statusLabel.setText("当前显示 " + displayList.size() + " 篇");
    }

    private void setLoadingState(boolean loading, String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            searchField.setEnabled(!loading);
            categoryBox.setEnabled(!loading);
        });
    }

    private void showDetailDialog(Journal j) {
        String html = "<html><body style='font-family:微软雅黑; font-size:12pt;'>" +
                "<h2>" + escapeHtml(j.getName()) + "</h2>" +
                "<p><b>类别:</b> " + escapeHtml(j.getCategory()) + "<br>" +
                "<b>出版日期:</b> " + (j.getPublishDate() == null ? "" : j.getPublishDate()) + "<br>" +
                "<b>出版社:</b> " + escapeHtml(j.getPublisher()) + "<br>" +
                "<b>简介:</b> " + escapeHtml(j.getDescription()) + "<br>" +
                "<b>链接:</b> <a href='" + escapeHtml(j.getLink()) + "'>" + escapeHtml(j.getLink()) + "</a></p>" +
                "</body></html>";

        JEditorPane editorPane = new JEditorPane("text/html", html);
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(620, 460));
        JOptionPane.showMessageDialog(this, scrollPane, "期刊详情", JOptionPane.INFORMATION_MESSAGE);
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>");
    }
}
