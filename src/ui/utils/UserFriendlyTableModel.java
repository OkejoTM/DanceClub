package ui.utils;

import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;

public class UserFriendlyTableModel extends DefaultTableModel {
    private final Map<Integer, String> rowToIdMap = new HashMap<>();
    private final String[] realColumns;
    private final String[] displayColumns;
    private int autoIncrementCounter = 1;

    public UserFriendlyTableModel(String[] realColumns, String[] displayColumns) {
        super(displayColumns, 0);
        this.realColumns = realColumns;
        this.displayColumns = displayColumns;
    }

    public void addRowWithId(Object[] rowData) {
        // Store the ID in our map
        String id = (String) rowData[0];
        // Replace ID with row number for display
        Object[] displayData = new Object[displayColumns.length];
        System.arraycopy(rowData, 1, displayData, 0, displayData.length);

        super.addRow(displayData);
        rowToIdMap.put(getRowCount() - 1, id);
    }

    public String getIdForRow(int row) {
        return rowToIdMap.get(row);
    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        // Rebuild the map to maintain correct row-to-id mapping
        Map<Integer, String> newMap = new HashMap<>();
        for (Map.Entry<Integer, String> entry : rowToIdMap.entrySet()) {
            if (entry.getKey() < row) {
                newMap.put(entry.getKey(), entry.getValue());
            } else if (entry.getKey() > row) {
                newMap.put(entry.getKey() - 1, entry.getValue());
            }
        }
        rowToIdMap.clear();
        rowToIdMap.putAll(newMap);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}