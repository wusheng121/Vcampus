package util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 * 表格工厂：统一创建带排序、统一样式与行高的 JTable，
 * 替代各面板各自 new DefaultTableModel + 手设表头色的重复样板。
 */
public final class Tables {
    public static final int ROW_HEIGHT = 28;

    private Tables() {
    }

    /** 建一个不可编辑、带行排序、表头钢蓝、统一行高的表格。 */
    public static JTable newTable(String[] columnNames) {
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(ROW_HEIGHT);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setRowSorter(new TableRowSorter<>(model));
        table.setFont(UITheme.BODY);
        table.setSelectionBackground(UITheme.TABLE_SELECTION);
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setBackground(UITheme.HEADER);
        header.setForeground(Color.WHITE);
        header.setFont(UITheme.BOLD);
        return table;
    }

    /** 为已存在的表格补挂 TableRowSorter（用于改造旧表格）。 */
    public static void attachSorter(JTable table) {
        if (!(table.getModel() instanceof DefaultTableModel)) return;
        table.setRowHeight(ROW_HEIGHT);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setRowSorter(new TableRowSorter<>((DefaultTableModel) table.getModel()));
        JTableHeader header = table.getTableHeader();
        header.setBackground(UITheme.HEADER);
        header.setForeground(Color.WHITE);
        header.setFont(UITheme.BOLD);
        table.setFont(UITheme.BODY);
    }
}
