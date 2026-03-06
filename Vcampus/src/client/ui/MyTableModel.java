package client.ui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class MyTableModel<T> extends AbstractTableModel {
    private List<T> data = new ArrayList<>();
    private final String[] columnNames;
    private final BiFunction<T, Integer, Object> valueGetter;

    public MyTableModel(String[] cols, BiFunction<T, Integer, Object> getter) {
        this.columnNames = cols;
        this.valueGetter = getter;
    }

    public void setData(List<T> list) {
        this.data = list;
        fireTableDataChanged();
    }

    public List<T> getData() { return data; }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return columnNames.length; }
    @Override public String getColumnName(int c) { return columnNames[c]; }
    @Override public Object getValueAt(int r, int c) { return valueGetter.apply(data.get(r), c); }
}