package client.ui;
import util.EmptyState;
import util.UITheme;

import client.controller.BorrowRecordController;
import common.model.BorrowRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class BorrowRecordsPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JScrollPane tableScroll;
    private BorrowRecordController recordController = new BorrowRecordController();

    public BorrowRecordsPanel() {
        setLayout(new BorderLayout());

        // 顶部：搜索框（支持按用户ID、书籍ID查找）
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("查询");
        searchBtn.setBackground(UITheme.HEADER);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setOpaque(true);
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setBackground(UITheme.HEADER);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setOpaque(true);
        topPanel.add(new JLabel("搜索（用户ID/书籍ID）："));
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        topPanel.add(refreshBtn);
        add(topPanel, BorderLayout.NORTH);

        // 表格
        String[] columns = {
                "记录ID", "用户ID", "书籍ID", "书名", "借书时间",
                "应还时间", "还书时间", "状态", "罚款"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            //@Override
//            public boolean isCellEditable(int row, int col) {
//                // 只允许点击最后的 "删除" 按钮
//                return col == 8;
//            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);
        JTableHeader header = table.getTableHeader();
        header.setBackground(UITheme.HEADER); // 钢蓝色
        header.setForeground(Color.WHITE);
        header.setFont(new Font("微软雅黑", Font.BOLD, 14));
        add((tableScroll = new JScrollPane(table)), BorderLayout.CENTER);

        // 加载数据
        refreshTable();

        // 查询功能
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            refreshTable(keyword);
        });

        // 刷新功能
        refreshBtn.addActionListener(e -> refreshTable());
    }

    // 默认刷新所有记录
    private void refreshTable() {
        refreshTable("");
    }

    // 带搜索的刷新
    private void refreshTable(String keyword) {
        List<BorrowRecord> records = recordController.getAllRecords();
        tableModel.setRowCount(0);

        for (BorrowRecord r : records) {
            if (keyword.isEmpty()
                    || r.getUserId().contains(keyword)
                    || r.getBookId().contains(keyword)) {
                tableModel.addRow(new Object[]{
                        r.getId(),
                        r.getUserId(),
                        r.getBookId(),
                        r.getBookTitle(),
                        r.getBorrowDate(),
                        r.getDueDate(),
                        r.getReturnDate() == null ? "未还" : r.getReturnDate(),
                        r.getStatus(),
                        r.getFine(),
                        //"删除"
                });
            }
        }
        EmptyState.updateEmptyState(tableScroll, table, "暂无借阅记录");
    }
}
