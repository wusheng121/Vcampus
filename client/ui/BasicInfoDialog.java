package client.ui;

import client.controller.StudentController;
import common.model.StudentPersonal;

import javax.swing.*;
import java.awt.*;

/**
 * 基本信息弹出窗口
 * 学号、姓名只读；电话、邮箱、地址可编辑；保存即update/insert
 */
public class BasicInfoDialog extends JDialog {

    private final String studentId;
    private final String studentName;
    private final StudentController controller = new StudentController();

    private final JTextField txtPhone = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtAddr  = new JTextField(20);

    public BasicInfoDialog(Window owner, String studentId, String studentName) {
        super(owner, "基本信息 - " + studentName, ModalityType.APPLICATION_MODAL);
        this.studentId   = studentId;
        this.studentName = studentName;

        /* 回显旧数据 */
        StudentPersonal old = controller.getPersonal(studentId);
        txtPhone.setText(old.getPhone());
        txtEmail.setText(old.getEmail());
        txtAddr.setText(old.getAddress());

        /* 只读信息 */
        JPanel north = new JPanel(new GridLayout(2, 2, 5, 5));
        north.add(new JLabel("学号")); north.add(new JLabel(studentId));
        north.add(new JLabel("姓名")); north.add(new JLabel(studentName));

        /* 可编辑信息 */
        JPanel center = new JPanel(new GridLayout(3, 2, 5, 5));
        center.add(new JLabel("电话")); center.add(txtPhone);
        center.add(new JLabel("邮箱")); center.add(txtEmail);
        center.add(new JLabel("地址")); center.add(txtAddr);

        /* 保存按钮 */
        JButton btnSave = new JButton("保存");
        btnSave.addActionListener(e -> {
            StudentPersonal sp = new StudentPersonal(
                    studentId,
                    txtPhone.getText().trim(),
                    txtEmail.getText().trim(),
                    txtAddr.getText().trim());
            boolean ok = controller.savePersonal(sp);
            JOptionPane.showMessageDialog(this, ok ? "保存成功！" : "保存失败！");
            if (ok) dispose();
        });

        JPanel south = new JPanel(); south.add(btnSave);

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        setSize(400, 220);
        setLocationRelativeTo(owner);
    }
}