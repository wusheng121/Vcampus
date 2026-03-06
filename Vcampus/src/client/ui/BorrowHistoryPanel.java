package client.ui;

import client.controller.BorrowRecordController;
import common.model.BorrowRecord;
import common.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BorrowHistoryPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private BorrowRecordController recordController = new BorrowRecordController();
    private User currentUser;

    public BorrowHistoryPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());

        // ===== 顶部工具栏 =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.setBackground(new Color(70, 130, 180));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setOpaque(true);
        refreshBtn.addActionListener(e -> refreshTable());
        topPanel.add(refreshBtn);
        add(topPanel, BorderLayout.NORTH);

        // 表头
        String[] columns = {"记录ID", "书籍ID", "书名", "借阅日期", "应还日期", "归还日期", "状态", "罚金"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // 历史记录不可编辑
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(70, 130, 180)); // 钢蓝色
        header.setForeground(Color.WHITE);
        header.setFont(new Font("微软雅黑", Font.BOLD, 14));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // 初始加载数据
        refreshTable();
    }

    private void refreshTable() {
        List<BorrowRecord> records = recordController.getUserRecords(currentUser.getUserId());
        tableModel.setRowCount(0);
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (BorrowRecord r : records) {
            double fine = r.getFine(); // 已归还 -> 数据库里的实际罚金
            if ("借出".equals(r.getStatus()) && r.getDueDate() != null && now.isAfter(r.getDueDate())) {
                long overdueDays = java.time.Duration.between(r.getDueDate(), now).toDays();
                double finePerDay = 1.0;
                fine = overdueDays * finePerDay; // ⚡ 预计罚金
            }
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getBookId(),
                    r.getBookTitle(),
                    r.getBorrowDate() != null ? r.getBorrowDate().format(fmt) : "",
                    r.getDueDate() != null ? r.getDueDate().format(fmt) : "",
                    r.getReturnDate() == null ? "未还" : r.getReturnDate(),
                    r.getStatus(),
                    fine
            });
        }
    }
}
