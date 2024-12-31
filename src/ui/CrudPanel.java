package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.Consumer;

public abstract class CrudPanel<T> extends JPanel {
    protected final DefaultTableModel tableModel;
    protected final JTable table;
    protected final JPanel buttonPanel;

    public CrudPanel(String[] columnNames) {
        setLayout(new BorderLayout());

        // Create table
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Create button panel
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        addButton.addActionListener(e -> handleAdd());
        editButton.addActionListener(e -> handleEdit());
        deleteButton.addActionListener(e -> handleDelete());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // Add components to panel
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    protected abstract void handleAdd();
    protected abstract void handleEdit();
    protected abstract void handleDelete();
    protected abstract void refreshTable();
    protected abstract Object[] getRowData(T item);

    protected void showEditDialog(String title, T item, Consumer<T> onSave) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        JPanel form = createForm(item);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            T result = getFormData(form);
            if (result != null) {
                onSave.accept(result);
                dialog.dispose();
                refreshTable();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    protected abstract JPanel createForm(T item);
    protected abstract T getFormData(JPanel form);
}
