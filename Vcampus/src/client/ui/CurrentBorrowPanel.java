package client.ui;

import client.controller.BorrowRecordController;
import common.model.BorrowRecord;
import common.model.User;
import util.ButtonEditor;
import util.ButtonRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CurrentBorrowPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private BorrowRecordController recordController = new BorrowRecordController();
    private User user;

    public CurrentBorrowPanel(User user) {
        this.user = user;
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

        // ===== 表头 =====
        String[] columns = {"记录ID", "书名", "借阅日期", "到期日期", "状态", "罚金", "操作"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 6; // 只允许“操作”列可以点击
            }
        };

        // ===== 表格 =====
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(70, 130, 180)); // 钢蓝色
        header.setForeground(Color.WHITE);
        header.setFont(new Font("微软雅黑", Font.BOLD, 14));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== 操作列按钮 =====
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer("还书", new Color(40, 167, 69), Color.WHITE));
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor("还书", table, e -> onReturnBook(), new Color(40, 167, 69), Color.WHITE));

        // 初始加载
        refreshTable();
    }

    // 刷新表格
    // 刷新表格
    private void refreshTable() {
        List<BorrowRecord> records = recordController.getUserRecords(user.getUserId());
        if (records == null) return;

        // 只显示未归还的
        List<BorrowRecord> currentRecords = records.stream()
                .filter(r -> "借出".equals(r.getStatus()))
                .collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (BorrowRecord r : currentRecords) {
            double fine = 0;
            LocalDateTime now = LocalDateTime.now();
            if (r.getDueDate() != null && now.isAfter(r.getDueDate())) {
                long overdueDays = java.time.Duration.between(r.getDueDate(), now).toDays();
                double finePerDay = 1.0; // 每天罚款 1 元（可配置）
                fine = overdueDays * finePerDay;
            }

            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getBookTitle(),
                    r.getBorrowDate(),
                    r.getDueDate(),
                    r.getStatus(),
                    fine,   // ⚡ 动态计算罚金
                    "还书"
            });
        }
    }


    private void onReturnBook() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        int recordId = (int) tableModel.getValueAt(row, 0);
        String bookTitle = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "确认归还《" + bookTitle + "》吗？",
                "确认还书", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = recordController.handleReturn(recordId);
            if (success) {
                JOptionPane.showMessageDialog(this, "归还成功！");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "归还失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
