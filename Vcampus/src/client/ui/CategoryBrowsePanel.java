package client.ui;

import client.controller.BookController;
import client.controller.BorrowRecordController;
import common.model.Book;
import common.model.User;
import util.ButtonEditor;
import util.ButtonRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CategoryBrowsePanel extends JPanel {
    private final User user;
    private final BookController bookController = new BookController();
    private final BorrowRecordController borrowRecordController = new BorrowRecordController();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTree categoryTree;

    // 缓存所有书籍，避免每次点分类都重新查库
    private List<Book> allBooks;

    public CategoryBrowsePanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());

        // ===== 左边分类树 =====
        categoryTree = new JTree(new DefaultMutableTreeNode("正在加载..."));
        categoryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // ===== 右边表格 =====
        String[] columns = {"书籍ID", "书名", "作者", "库存", "状态", "借阅人数", "操作"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 6; // 只有操作列可点击
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);

        JScrollPane treeScroll = new JScrollPane(categoryTree);
        JScrollPane tableScroll = new JScrollPane(table);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, tableScroll);
        splitPane.setDividerLocation(200);
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(70, 130, 180)); // 钢蓝色
        header.setForeground(Color.WHITE);
        header.setFont(new Font("微软雅黑", Font.BOLD, 14));
        add(splitPane, BorderLayout.CENTER);

        // 操作列（借书按钮）
        table.getColumnModel().getColumn(6).setCellRenderer(
                new ButtonRenderer("借书", new Color(40, 167, 69), Color.WHITE));
        table.getColumnModel().getColumn(6).setCellEditor(
                new ButtonEditor("借书", table, e -> onBorrow(), new Color(40, 167, 69), Color.WHITE));

        // ===== 初始化数据（从数据库获取所有书籍）=====
        loadBooksAndInitUI();

        // 监听分类选择
        categoryTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
            if (node != null) {
                String category = node.toString();
                refreshTable(category);
            }
        });
    }

    /** 第一次加载书籍并构造分类树 */
    private void loadBooksAndInitUI() {
        allBooks = bookController.getAllBooks();
        if (allBooks == null) allBooks = List.of();

        // 构造分类树
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("全部图书");
        List<String> categories = allBooks.stream()
                .map(Book::getTheme)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        for (String c : categories) {
            root.add(new DefaultMutableTreeNode(c));
        }

        categoryTree.setModel(new javax.swing.tree.DefaultTreeModel(root));
        categoryTree.setSelectionRow(0); // 默认选中 "全部图书"

        // 默认表格加载所有图书
        refreshTable("全部图书");
    }

    /** 刷新表格内容（从缓存 allBooks 里筛选） */
    private void refreshTable(String category) {
        List<Book> books;
        if ("全部图书".equals(category)) {
            books = allBooks;
        } else {
            books = allBooks.stream()
                    .filter(b -> category.equals(b.getTheme()))
                    .collect(Collectors.toList());
        }

        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getBookId(),
                    b.getBookName(),
                    b.getAuthor(),
                    b.getNumber(),
                    b.getStatus(),
                    b.getBorrowers(),
                    "借书"
            });
        }
    }

    /** 借书按钮逻辑 */
    private void onBorrow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String bookId = (String) tableModel.getValueAt(row, 0);
            boolean success = borrowRecordController.handleBorrow(user.getUserId(), bookId);

            JOptionPane.showMessageDialog(null, success ? "借书成功" : "借书失败");

            if (success) {
                // 更新缓存 & 刷新表格
                allBooks = bookController.getAllBooks();
                refreshTable("全部图书");
            }
        }
    }
}
