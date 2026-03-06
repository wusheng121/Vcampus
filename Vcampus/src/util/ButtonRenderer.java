package util;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonRenderer extends JButton implements TableCellRenderer {
    private final String defaultText;
    private final Color bg;
    private final Color fg;

    public ButtonRenderer(String text, Color bg, Color fg) {
        this.defaultText = text;
        this.bg = bg;
        this.fg = fg;
        setOpaque(true);
        setFocusPainted(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int col) {
        setText(defaultText);
        setBackground(bg);
        setForeground(fg);
        return this;
    }
}
