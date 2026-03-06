package client.ui;

import client.controller.StudentController;
import common.model.StudentPersonal;

import javax.swing.*;
import java.awt.*;

/**
 * 只读基本信息查看对话框
 * 下方“编辑”按钮打开可编辑窗口
 */
public class BasicInfoViewDialog extends JDialog {

    private final String studentId;
    private final StudentController controller = new StudentController();

    public BasicInfoViewDialog(Window owner, String studentId, String studentName) {
        super(owner, "基本信息 - " + studentName, ModalityType.APPLICATION_MODAL);
        this.studentId = studentId;

        StudentPersonal sp = controller.getPersonal(studentId);

        /* 只读文本 */
        JTextField tfId    = createReadOnly(sp.getStudentId());
        JTextField tfName  = createReadOnly(studentName);
        JTextField tfPhone = createReadOnly(sp.getPhone());
        JTextField tfEmail = createReadOnly(sp.getEmail());
        JTextField tfAddr  = createReadOnly(sp.getAddress());

        JPanel center = new JPanel(new GridLayout(5, 2, 5, 5));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        center.add(new JLabel("学号")); center.add(tfId);
        center.add(new JLabel("姓名")); center.add(tfName);
        center.add(new JLabel("电话")); center.add(tfPhone);
        center.add(new JLabel("邮箱")); center.add(tfEmail);
        center.add(new JLabel("地址")); center.add(tfAddr);

        /* 编辑按钮 */
        JButton btnEdit = new JButton("编辑");
        btnEdit.addActionListener(e -> {
            /* 关闭只读窗口，打开编辑窗口 */
            dispose();
            new BasicInfoDialog(owner, studentId, studentName).setVisible(true);
        });

        JPanel south = new JPanel(); south.add(btnEdit);

        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        setSize(400, 220);
        setLocationRelativeTo(owner);
    }

    private static JTextField createReadOnly(String text) {
        JTextField tf = new JTextField(text);
        tf.setEditable(false);
        tf.setBorder(BorderFactory.createEmptyBorder());
        tf.setBackground(UIManager.getColor("Label.background"));
        return tf;
    }
}