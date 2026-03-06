package util;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
    private final JButton button;
    private final String label;
    private final Color bg;
    private final Color fg;
    private final ActionListener actionListener;

    public ButtonEditor(String text, JTable table, ActionListener actionListener,
                        Color bg, Color fg) {
        this.label = text;
        this.bg = bg;
        this.fg = fg;
        this.actionListener = actionListener;

        button = new JButton(label);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(fg);

        button.addActionListener(e -> {
            fireEditingStopped(); // 结束编辑
            actionListener.actionPerformed(
                    new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "")
            );
        });
    }

    @Override
    public Object getCellEditorValue() {
        return null; // 不写回模型
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int col) {
        button.setText(label);
        button.setBackground(bg);
        button.setForeground(fg);
        return button;
    }
}
