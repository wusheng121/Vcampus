package client.ui;
import util.EmptyState;
import util.UITheme;

import client.controller.BookController;
import client.controller.BorrowRecordController;
import common.model.Book;
import common.model.User;
import util.ButtonEditor;
import util.ButtonRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BookSearchPanel extends JPanel {
    private final User user;
private DefaultTableModel tableModel;
private JTable table;
private JScrollPane tableScroll;
    private BookController bookController = new BookController();
    private BorrowRecordController borrowRecordController = new BorrowRecordController();

    // 筛选条件
    private JComboBox<String> filterBox;

    public BookSearchPanel(User user) {
        this.user=user;
        setLayout(new BorderLayout());

        // 顶部栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("查询");
        searchBtn.setBackground(UITheme.HEADER); // Bootstrap 风格绿色
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setOpaque(true);


        filterBox = new JComboBox<>(new String[]{
                "默认排序",
                "按编号排序",
                "按借阅人数排序",
                "只看可借"
        });

        topPanel.add(new JLabel("搜索："));
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        topPanel.add(new JLabel("筛选："));
        topPanel.add(filterBox);
        add(topPanel, BorderLayout.NORTH);

        // 表格
        String[] columns = {"ID", "书名", "作者", "数量", "状态", "主题", "借阅人数", "操作"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 7; // 只有操作列可点击
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);
        JTableHeader header = table.getTableHeader();
        header.setBackground(UITheme.HEADER); // 钢蓝色
        header.setForeground(Color.WHITE);
        header.setFont(new Font("微软雅黑", Font.BOLD, 14));

        add((tableScroll = new JScrollPane(table)), BorderLayout.CENTER);

        // 操作列（借书/还书按钮）
        table.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer("借书", UITheme.SUCCESS, Color.WHITE));
        table.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor("借书", table, e -> onBorrow(), UITheme.SUCCESS, Color.WHITE));

        // 按钮事件
        searchBtn.addActionListener(e -> onSearch(searchField.getText().trim()));
        filterBox.addActionListener(e -> refreshTable(searchField.getText().trim()));

        // 初始加载
        refreshTable("");
    }

    private void refreshTable(String keyword) {
        List<Book> books = bookController.getAllBooks();

        // 搜索
        if (!keyword.isEmpty()) {
            books = books.stream()
                    .filter(b -> b.getBookId().contains(keyword)
                            || b.getBookName().contains(keyword)
                            || b.getAuthor().contains(keyword))
                    .collect(Collectors.toList());
        }

        // 筛选排序
        String filter = (String) filterBox.getSelectedItem();
        if ("按编号排序".equals(filter)) {
            books = books.stream()
                    .sorted(Comparator.comparing(Book::getBookId))
                    .collect(Collectors.toList());
        } else if ("按借阅人数排序".equals(filter)) {
            books = books.stream()
                    .sorted(Comparator.comparing(Book::getBorrowers).reversed())
                    .collect(Collectors.toList());
        } else if ("只看可借".equals(filter)) {
            books = books.stream()
                    .filter(b -> "可借".equals(b.getStatus()))
                    .collect(Collectors.toList());
        }

        // 填充表格
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getBookId(), b.getBookName(), b.getAuthor(),
                    b.getNumber(), b.getStatus(), b.getTheme(),
                    b.getBorrowers(),
                    //b.getNumber() > 0 ? "借书" : "还书"
                    "借书"
            });
        }
        EmptyState.updateEmptyState(tableScroll, table, "暂无符合条件的书籍");
    }

    // 借书按钮逻辑
    private void onBorrow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String bookId = (String) tableModel.getValueAt(row, 0);

            boolean success = borrowRecordController.handleBorrow(user.getUserId(), bookId);
            //boolean success1 = borrowRecordController.handleBorrow(userId,bookId)
            JOptionPane.showMessageDialog(null, success ? "借书成功" : "借书失败");

            if (success) {
                refreshTable(""); // 操作后刷新表格
            }
        }
    }


    private void onSearch(String keyword) {
        refreshTable(keyword);
    }
}
