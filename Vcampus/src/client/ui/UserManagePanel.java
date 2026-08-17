package client.ui;
import util.EmptyState;
import util.UITheme;
import util.Validators;

import client.controller.UserController;
import common.model.User;
import util.ButtonEditor;
import util.ButtonRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class UserManagePanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JScrollPane tableScroll;
    private UserController userController = new UserController();

    public UserManagePanel(User user) {
        setLayout(new BorderLayout());

        // 顶部栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("新增");
        addBtn.setBackground(UITheme.HEADER); // Bootstrap 风格绿色
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setOpaque(true);
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("查询");
        searchBtn.setBackground(UITheme.HEADER); // Bootstrap 风格绿色
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setOpaque(true);

        // 角色筛选
        JComboBox<String> roleFilter = new JComboBox<>(new String[]{"全部", "student", "teacher", "admin"});

        topPanel.add(addBtn);
        topPanel.add(new JLabel("搜索："));
        topPanel.add(searchField);
        topPanel.add(new JLabel("角色："));
        topPanel.add(roleFilter);
        topPanel.add(searchBtn);
        add(topPanel, BorderLayout.NORTH);


        // 表格
        String[] columns = {"用户ID", "姓名", "邮箱", "身份", "创建时间", "修改", "删除"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 5 || col == 6; // 只允许操作列点击
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

        // 渲染器 & 编辑器
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("修改", new Color(40,167,69), Color.WHITE));
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor("修改", table, e -> onEdit(), new Color(40,167,69), Color.WHITE));
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer("删除", new Color(220,53,69), Color.WHITE));
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor("删除", table, e -> onDelete(), new Color(220,53,69), Color.WHITE));

        // 事件
        addBtn.addActionListener(e -> onAdd());
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            String role = (String) roleFilter.getSelectedItem();
            refreshTable(keyword, role);
        });

        roleFilter.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            String role = (String) roleFilter.getSelectedItem();
            refreshTable(keyword, role);
        });


        refreshTable();
    }

    private void refreshTable() {
        refreshTable(null, "全部");
    }

    private void refreshTable(String keyword, String role) {
        List<User> users;
        if (keyword != null && !keyword.isEmpty()) {
            users = userController.searchUsers(keyword);
        } else {
            users = userController.getAllUsers();
        }

        // 如果选定了角色，过滤掉不符合的
        if (role != null && !"全部".equals(role)) {
            users.removeIf(u -> !role.equals(u.getType()));
        }

        tableModel.setRowCount(0);
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    u.getUserId(), u.getName(), u.getEmail(),
                    u.getType(), u.getCreatedAt(),
                    "修改", "删除"
            });
        }
        EmptyState.updateEmptyState(tableScroll, table);
    }


    private void onAdd() {
        JTextField userIdField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"student", "teacher", "admin"});

        Object[] message = {
                "用户ID:", userIdField,
                "姓名:", nameField,
                "邮箱:", emailField,
                "密码:", passwordField,
                "角色:", roleBox
        };

        while (true) {
            int option = JOptionPane.showConfirmDialog(this, message, "新增用户", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) return;

            String uid = userIdField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pwd = new String(passwordField.getPassword());

            if (!Validators.nonEmpty(uid) || !Validators.nonEmpty(name) || !Validators.nonEmpty(pwd)) {
                JOptionPane.showMessageDialog(this, "用户ID/姓名/密码不能为空", "提示", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if (!email.isEmpty() && !Validators.isEmail(email)) {
                JOptionPane.showMessageDialog(this, "邮箱格式不正确", "提示", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            User newUser = new User();
            newUser.setUserId(uid);
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPassword(pwd);
            newUser.setType((String) roleBox.getSelectedItem());

            boolean success = userController.addUser(newUser);
            if (!success) {
                JOptionPane.showMessageDialog(this, "新增用户失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
            refreshTable();
            return;
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String userId = (String) tableModel.getValueAt(row, 0);
        String oldName = (String) tableModel.getValueAt(row, 1);
        String oldEmail = (String) tableModel.getValueAt(row, 2);
        String oldRole = (String) tableModel.getValueAt(row, 3);

        JTextField nameField = new JTextField(oldName);
        JTextField emailField = new JTextField(oldEmail);
        JPasswordField passwordField = new JPasswordField(); // 可选重置密码
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"student", "teacher", "admin"});
        roleBox.setSelectedItem(oldRole);

        Object[] message = {
                "姓名:", nameField,
                "邮箱:", emailField,
                "新密码(可选):", passwordField,
                "角色:", roleBox
        };

        int option = JOptionPane.showConfirmDialog(this, message, "修改用户信息", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            User updatedUser = new User();
            updatedUser.setUserId(userId);
            updatedUser.setName(nameField.getText().trim());
            updatedUser.setEmail(emailField.getText().trim());
            updatedUser.setType((String) roleBox.getSelectedItem());

            String newPwd = new String(passwordField.getPassword());
            if (!newPwd.isEmpty()) {
                updatedUser.setPassword(newPwd);
            }

            boolean success = userController.updateUser(updatedUser);
            if (!success) {
                JOptionPane.showMessageDialog(this, "修改失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
            refreshTable();
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String userId = (String) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(null,
                    "确认删除用户 ID=" + userId + " ?", "确认", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                userController.deleteUser(userId);
                refreshTable();
            }
        }
    }

    private void onSearch(String keyword) {
        List<User> users = userController.searchUsers(keyword);
        tableModel.setRowCount(0);
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    u.getUserId(), u.getName(), u.getEmail(),
                    u.getType(), u.getCreatedAt(),
                    "修改", "删除"
            });
        }
    }
}
