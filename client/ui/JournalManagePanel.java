// src/client/ui/JournalManagePanel.java
package client.ui;

import client.controller.JournalController;
import common.model.Journal;
import util.ButtonEditor;
import util.ButtonRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class JournalManagePanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JournalController controller = new JournalController();

    public JournalManagePanel() {
        setLayout(new BorderLayout());

        // 顶部栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("新增");
        addBtn.setBackground(new Color(70, 130, 180));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setOpaque(true);
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("查询");
        searchBtn.setBackground(new Color(70, 130, 180));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setOpaque(true);
        topPanel.add(addBtn);
        topPanel.add(new JLabel("搜索："));
        topPanel.add(searchField);
        topPanel.add(searchBtn);

        add(topPanel, BorderLayout.NORTH);

        // 表格
        String[] columns = {"编号", "名称", "类别", "出版日期", "出版社", "简介", "编辑", "删除"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 6 || col == 7;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(70, 130, 180)); // 钢蓝色
        header.setForeground(Color.WHITE);
        header.setFont(new Font("微软雅黑", Font.BOLD, 14));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 按钮渲染器和编辑器
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer("编辑", new Color(40,167,69), Color.WHITE));
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor("编辑", table, e -> onEdit(), new Color(40,167,69), Color.WHITE));
        table.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer("删除", new Color(220,53,69), Color.WHITE));
        table.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor("删除", table, e -> onDelete(), new Color(220,53,69), Color.WHITE));

        // 按钮事件
        addBtn.addActionListener(e -> onAdd());
        searchBtn.addActionListener(e -> onSearch(searchField.getText().trim()));

        refreshTable();
    }

    private void refreshTable() {
        List<Journal> list = controller.getAllJournals();
        tableModel.setRowCount(0);
        for (Journal j : list) {
            tableModel.addRow(new Object[]{
                    j.getJournalId(), j.getName(), j.getCategory(),
                    j.getPublishDate(), j.getPublisher(), j.getDescription(),
                    "编辑", "删除"
            });
        }
    }

    private void onAdd() {
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField nameField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField publisherField = new JTextField();
        JTextArea descArea = new JTextArea(3, 20);
        form.add(new JLabel("名称:")); form.add(nameField);
        form.add(new JLabel("类别:")); form.add(categoryField);
        form.add(new JLabel("出版日期(yyyy-MM-dd):")); form.add(dateField);
        form.add(new JLabel("出版社:")); form.add(publisherField);
        form.add(new JLabel("简介:")); form.add(new JScrollPane(descArea));

        int result = JOptionPane.showConfirmDialog(null, form, "新增期刊",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Journal j = new Journal();
                j.setName(nameField.getText());
                j.setCategory(categoryField.getText());
                j.setPublishDate(java.sql.Date.valueOf(dateField.getText()));
                j.setPublisher(publisherField.getText());
                j.setDescription(descArea.getText());
                boolean ok = controller.addJournal(j);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, ok ? "新增成功！" : "新增失败！");
                    refreshTable();
                });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "日期格式错误");
            }
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            JTextField nameField = new JTextField((String) tableModel.getValueAt(row, 1));
            JTextField categoryField = new JTextField((String) tableModel.getValueAt(row, 2));
            JTextField dateField = new JTextField(tableModel.getValueAt(row, 3).toString());
            JTextField publisherField = new JTextField((String) tableModel.getValueAt(row, 4));
            JTextArea descArea = new JTextArea(tableModel.getValueAt(row, 5).toString(), 3, 20);

            JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
            form.add(new JLabel("名称:")); form.add(nameField);
            form.add(new JLabel("类别:")); form.add(categoryField);
            form.add(new JLabel("出版日期(yyyy-MM-dd):")); form.add(dateField);
            form.add(new JLabel("出版社:")); form.add(publisherField);
            form.add(new JLabel("简介:")); form.add(new JScrollPane(descArea));

            int result = JOptionPane.showConfirmDialog(null, form, "编辑期刊",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    Journal j = new Journal();
                    j.setJournalId((int) tableModel.getValueAt(row, 0));
                    j.setName(nameField.getText());
                    j.setCategory(categoryField.getText());
                    j.setPublishDate(java.sql.Date.valueOf(dateField.getText()));
                    j.setPublisher(publisherField.getText());
                    j.setDescription(descArea.getText());
                    boolean ok = controller.updateJournal(j);
                    JOptionPane.showMessageDialog(null, ok ? "修改成功" : "修改失败");
                    refreshTable();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "日期格式错误");
                }
            }
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int journalId = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(null,
                    "确认删除期刊 编号=" + journalId + " ?", "确认", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = controller.deleteJournal(journalId);
                JOptionPane.showMessageDialog(null, ok ? "删除成功" : "删除失败");
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                refreshTable();
            }
        }
    }

    private void onSearch(String keyword) {
        List<Journal> list = controller.getAllJournals();
        tableModel.setRowCount(0);
        for (Journal j : list) {
            if (String.valueOf(j.getJournalId()).contains(keyword) ||
                    j.getName().contains(keyword) ||
                    j.getCategory().contains(keyword)) {
                tableModel.addRow(new Object[]{
                        j.getJournalId(), j.getName(), j.getCategory(),
                        j.getPublishDate(), j.getPublisher(), j.getDescription(),
                        "编辑", "删除"
                });
            }
        }
    }
}
