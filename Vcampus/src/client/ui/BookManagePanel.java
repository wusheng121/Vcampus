package client.ui;

import client.controller.BookController;
import common.model.Book;
import util.ButtonEditor;
import util.ButtonRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class BookManagePanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private BookController bookController = new BookController();

    public BookManagePanel() {
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
        String[] columns = {"ID", "书名", "作者", "数量", "状态", "主题", "借阅人数", "修改", "删除"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 7 || col == 8;
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

        // 渲染器 & 编辑器
        table.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer("修改", new Color(40, 167, 69), Color.WHITE));
        table.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor("修改", table, e -> onEdit(), new Color(40, 167, 69), Color.WHITE));
        table.getColumnModel().getColumn(8).setCellRenderer(new ButtonRenderer("删除", new Color(220, 53, 69), Color.WHITE));
        table.getColumnModel().getColumn(8).setCellEditor(new ButtonEditor("删除", table, e -> onDelete(), new Color(220, 53, 69), Color.WHITE));

        // 按钮事件
        addBtn.addActionListener(e -> onAdd());
        searchBtn.addActionListener(e -> onSearch(searchField.getText().trim()));

        refreshTable();
    }

    private void refreshTable() {
        List<Book> books = bookController.getAllBooks();
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getBookId(), b.getBookName(), b.getAuthor(),
                    b.getNumber(), b.getStatus(), b.getTheme(),
                    b.getBorrowers(), "修改", "删除"
            });
        }
    }

    private void onAdd() {
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField numberField = new JTextField("1");
        JTextField statusField = new JTextField("可借");
        JTextField themeField = new JTextField();
        form.add(new JLabel("ID:")); form.add(idField);
        form.add(new JLabel("书名:")); form.add(nameField);
        form.add(new JLabel("作者:")); form.add(authorField);
        form.add(new JLabel("数量:")); form.add(numberField);
        form.add(new JLabel("状态:")); form.add(statusField);
        form.add(new JLabel("主题:")); form.add(themeField);

        int result = JOptionPane.showConfirmDialog(null, form, "新增书籍",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Book newBook = new Book();
                newBook.setBookId(idField.getText());
                newBook.setBookName(nameField.getText());
                newBook.setAuthor(authorField.getText());
                newBook.setNumber(Integer.parseInt(numberField.getText()));
                newBook.setStatus(statusField.getText());
                newBook.setTheme(themeField.getText());
                newBook.setBorrowers(0);

                boolean ok = bookController.addBook(newBook);
                SwingUtilities.invokeLater(() -> {
                    if (ok) JOptionPane.showMessageDialog(null, "新增成功！");
                    else JOptionPane.showMessageDialog(null, "新增失败！");
                    refreshTable();
                });
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "数量必须为数字");
            }
        }
    }

    private void onSearch(String keyword) {
        List<Book> books = bookController.getAllBooks();
        tableModel.setRowCount(0);
        for (Book b : books) {
            if (b.getBookId().contains(keyword) ||
                    b.getBookName().contains(keyword) ||
                    b.getAuthor().contains(keyword)) {
                tableModel.addRow(new Object[]{
                        b.getBookId(), b.getBookName(), b.getAuthor(),
                        b.getNumber(), b.getStatus(), b.getTheme(),
                        b.getBorrowers(), "修改", "删除"
                });
            }
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            JTextField idField = new JTextField((String) tableModel.getValueAt(row, 0));
            idField.setEditable(false);
            JTextField nameField = new JTextField((String) tableModel.getValueAt(row, 1));
            JTextField authorField = new JTextField((String) tableModel.getValueAt(row, 2));
            JTextField numberField = new JTextField(tableModel.getValueAt(row, 3).toString());
            JTextField statusField = new JTextField((String) tableModel.getValueAt(row, 4));
            JTextField themeField = new JTextField((String) tableModel.getValueAt(row, 5));
            JTextField borrowersField = new JTextField(tableModel.getValueAt(row, 6).toString());

            JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
            form.add(new JLabel("ID:")); form.add(idField);
            form.add(new JLabel("书名:")); form.add(nameField);
            form.add(new JLabel("作者:")); form.add(authorField);
            form.add(new JLabel("数量:")); form.add(numberField);
            form.add(new JLabel("状态:")); form.add(statusField);
            form.add(new JLabel("主题:")); form.add(themeField);
            form.add(new JLabel("借阅人数:")); form.add(borrowersField);

            int result = JOptionPane.showConfirmDialog(null, form, "修改书籍",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    Book updatedBook = new Book();
                    updatedBook.setBookId(idField.getText());
                    updatedBook.setBookName(nameField.getText());
                    updatedBook.setAuthor(authorField.getText());
                    updatedBook.setNumber(Integer.parseInt(numberField.getText()));
                    updatedBook.setStatus(statusField.getText());
                    updatedBook.setTheme(themeField.getText());
                    updatedBook.setBorrowers(Integer.parseInt(borrowersField.getText()));

                    if (bookController.updateBook(updatedBook.getBookId(), updatedBook)) {
                        JOptionPane.showMessageDialog(null, "修改成功");
                        refreshTable();
                    } else {
                        JOptionPane.showMessageDialog(null, "修改失败");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "数量/借阅人数必须是数字！");
                }
            }
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String bookId = (String) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(null,
                    "确认删除书籍 ID=" + bookId + " ?", "确认", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (bookController.deleteBook(bookId)) {
                    JOptionPane.showMessageDialog(null, "删除成功");
                    if (table.isEditing()) {
                        table.getCellEditor().stopCellEditing();
                    }
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(null, "删除失败");
                }
            }
        }
    }
}
