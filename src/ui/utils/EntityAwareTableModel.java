package ui.utils;

import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EntityAwareTableModel<T> extends DefaultTableModel {
    private final Map<Integer, T> rowToEntityMap = new HashMap<>(); // Для того чтобы хранить данные в строках
    private final Map<Integer, Function<T, String>> columnFormatters = new HashMap<>(); // Форматеры для столбцов
    private final String[] realColumns;
    private final String[] displayColumns;

    public EntityAwareTableModel(String[] realColumns, String[] displayColumns) {
        super(displayColumns, 0);
        this.realColumns = realColumns;
        this.displayColumns = displayColumns;
    }

    public void setColumnFormatter(int columnIndex, Function<T, String> formatter) {
        columnFormatters.put(columnIndex, formatter);
    }

    public void addEntity(T entity, Function<T, String> idExtractor) {
        Object[] displayData = new Object[displayColumns.length];

        // Format each column using registered formatters
        for (int i = 0; i < displayColumns.length; i++) {
            Function<T, String> formatter = columnFormatters.get(i);
            if (formatter != null) {
                displayData[i] = formatter.apply(entity);
            }
        }

        super.addRow(displayData);
        rowToEntityMap.put(getRowCount() - 1, entity);
    }

    public T getEntityForRow(int row) {
        return rowToEntityMap.get(row);
    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        // Rebuild the map to maintain correct row-to-entity mapping
        Map<Integer, T> newMap = new HashMap<>();
        for (Map.Entry<Integer, T> entry : rowToEntityMap.entrySet()) {
            if (entry.getKey() < row) {
                newMap.put(entry.getKey(), entry.getValue());
            } else if (entry.getKey() > row) {
                newMap.put(entry.getKey() - 1, entry.getValue());
            }
        }
        rowToEntityMap.clear();
        rowToEntityMap.putAll(newMap);
    }

//    @Override
//    public void setValueAt(Object value, int row, int column) {
//        super.setValueAt(value, row, column);
//        // Optionally update the entity if needed
//        T entity = rowToEntityMap.get(row);
//        if (entity != null) {
//            // The actual update of the entity should be handled by the service layer
//        }
//    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}