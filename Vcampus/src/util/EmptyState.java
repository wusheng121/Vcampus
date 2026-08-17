package util;

import javax.swing.*;
import java.awt.*;

/**
 * 表格空状态提示：返回一个统一样式的"暂无数据"标签，
 * 或在数据为空时把 JScrollPane 的视口切换为空状态标签。
 */
public final class EmptyState {
    public static final String DEFAULT_TEXT = "暂无数据";

    private EmptyState() {
    }

    public static JLabel label() {
        return label(DEFAULT_TEXT);
    }

    public static JLabel label(String message) {
        JLabel label = new JLabel(message == null ? DEFAULT_TEXT : message, SwingConstants.CENTER);
        label.setFont(UITheme.BODY);
        label.setForeground(UITheme.MUTED_TEXT);
        label.setBackground(UITheme.CARD_BG);
        label.setOpaque(true);
        return label;
    }

    /**
     * 在 refreshTable 之后调用：表格为空则把 scroll 的视口切为"暂无数据"标签，
     * 非空则切回表格（JTable 的 addNotify/removeNotify 会自动重配表头）。
     */
    public static void updateEmptyState(JScrollPane scroll, JTable table, String text) {
        if (scroll == null || table == null || table.getModel() == null) return;
        boolean empty = table.getModel().getRowCount() == 0;
        Component current = scroll.getViewport().getView();
        if (empty && !(current instanceof JLabel)) {
            scroll.setViewportView(label(text));
        } else if (!empty && current instanceof JLabel) {
            scroll.setViewportView(table);
        }
    }

    public static void updateEmptyState(JScrollPane scroll, JTable table) {
        updateEmptyState(scroll, table, DEFAULT_TEXT);
    }
}

