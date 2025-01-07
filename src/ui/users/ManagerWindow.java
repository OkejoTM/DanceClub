package ui.users;

import core.models.Subscription;
import core.models.actors.Client;
import core.models.actors.Trainer;
import core.models.base.TrainingClass;
import core.models.enums.TrainingLevel;
import core.services.core.*;
import core.services.storage.*;
import ui.users.base.BaseWindow;
import ui.utils.UserFriendlyTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ManagerWindow extends BaseWindow {
    private final TrainerService trainerService;
    private final ClientService clientService;
    private final TrainingClassService trainingClassService;
    private final SubscriptionService subscriptionService;
    private final JTabbedPane tabbedPane;

    public ManagerWindow(String managerId) {
        super("Manager Dashboard", managerId);
        this.subscriptionService = new SubscriptionService(
                new SubscriptionStorageService(),
                new ClientStorageService()
        );
        this.trainerService = new TrainerService(
                new TrainerStorageService()
        );
        this.trainingClassService = new TrainingClassService(
                new TrainerStorageService(),
                new TrainingClassStorageService()
        );
        this.clientService = new ClientService(
                new ClientStorageService(),
                new SubscriptionStorageService(),
                new TrainingClassService(
                        new TrainerStorageService(),
                        new TrainingClassStorageService()
                )
        );

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Clients", createClientsPanel());
        tabbedPane.addTab("Trainers", createTrainersPanel());
        tabbedPane.addTab("Classes", createClassesPanel());
        tabbedPane.addTab("Subscriptions", createSubscriptionsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createClientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns
        String[] realColumns = new String[]{"ID", "Name", "Passport ID"};
        String[] displayColumns = new String[]{"Name", "Passport ID"};
        UserFriendlyTableModel tableModel = new UserFriendlyTableModel(realColumns, displayColumns);

        // Create table and add it to a scroll pane
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Client");
        JButton editButton = new JButton("Edit Client");
        JButton deleteButton = new JButton("Delete Client");

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);

        // Add action listeners to buttons
        addButton.addActionListener(e -> showAddClientDialog(tableModel));
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                showEditClientDialog(tableModel, selectedRow);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a client to edit");
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure you want to delete this client?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    // Use getIdForRow instead of getValueAt
                    String clientId = tableModel.getIdForRow(selectedRow);
                    clientService.deleteClient(clientId);
                    tableModel.removeRow(selectedRow);
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a client to delete");
            }
        });

        // Add components to panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshClientTable(tableModel);

        return panel;
    }

    private void showAddClientDialog(UserFriendlyTableModel tableModel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Client", true);
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        JTextField passportField = new JTextField();

        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Passport ID:"));
        form.add(passportField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String passportId = passportField.getText().trim();

            if (!name.isEmpty() && !passportId.isEmpty()) {
                Client client = clientService.createClient(name, passportId);
                // Use addRowWithId instead of addRow
                tableModel.addRowWithId(new Object[]{
                        client.getId(),
                        client.getName(),
                        client.getPassportId()
                });
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditClientDialog(UserFriendlyTableModel tableModel, int row) {
        String clientId = tableModel.getIdForRow(row);
        String currentName = (String) tableModel.getValueAt(row, 0);
        String currentPassport = (String) tableModel.getValueAt(row, 1);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Client", true);
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(currentName);
        JTextField passportField = new JTextField(currentPassport);

        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Passport ID:"));
        form.add(passportField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String passportId = passportField.getText().trim();

            if (!name.isEmpty() && !passportId.isEmpty()) {
                Client oldClient = clientService.getClientById(clientId);
                Client client = new Client(name, passportId);
                client.setId(clientId);
                client.setSubscriptionIds(oldClient.getSubscriptionIds());
                clientService.updateClient(client);

                tableModel.setValueAt(name, row, 0);
                tableModel.setValueAt(passportId, row, 1);

                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void refreshClientTable(UserFriendlyTableModel tableModel) {
        tableModel.setRowCount(0);
        List<Client> clients = clientService.getAllClients();
        for (Client client : clients) {
            tableModel.addRowWithId(new Object[]{
                    client.getId(),
                    client.getName(),
                    client.getPassportId()
            });
        }
    }

    private JPanel createTrainersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns
        String[] realColumns = new String[]{"ID", "Name", "Passport ID", "Phone"};
        String[] displayColumns = new String[]{"Name", "Passport ID", "Phone"};
        UserFriendlyTableModel tableModel = new UserFriendlyTableModel(realColumns, displayColumns);

        // Create table and add it to a scroll pane
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Trainer");
        JButton editButton = new JButton("Edit Trainer");
        JButton deleteButton = new JButton("Delete Trainer");

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);

        // Add action listeners
        addButton.addActionListener(e -> showAddTrainerDialog(tableModel));
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                showEditTrainerDialog(tableModel, selectedRow);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a trainer to edit");
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure you want to delete this trainer?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    String trainerId = tableModel.getIdForRow(selectedRow);
                    trainerService.deleteTrainer(trainerId);
                    tableModel.removeRow(selectedRow);
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a trainer to delete");
            }
        });

        // Add components to panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshTrainerTable(tableModel);

        return panel;
    }

    private void showAddTrainerDialog(UserFriendlyTableModel tableModel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Trainer", true);
        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField passportField = new JTextField();
        JTextField phoneField = new JTextField();

        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        form.add(new JLabel("Passport ID:"));
        form.add(passportField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String passportId = passportField.getText().trim();
            String phone = phoneField.getText().trim();

            if (!name.isEmpty() && !password.isEmpty() && !passportId.isEmpty() && !phone.isEmpty()) {
                Trainer trainer = trainerService.createTrainer(name, password, passportId, phone);
                tableModel.addRowWithId(new Object[]{
                        trainer.getId(),
                        trainer.getName(),
                        trainer.getPassportId(),
                        trainer.getPhone()
                });
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields");
            }

        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditTrainerDialog(UserFriendlyTableModel  tableModel, int row) {
        String trainerId = tableModel.getIdForRow(row);
        String currentName = (String) tableModel.getValueAt(row, 0);
        String currentPassport = (String) tableModel.getValueAt(row, 1);
        String currentPhone = (String) tableModel.getValueAt(row, 2);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Trainer", true);
        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(currentName);
        JTextField passportField = new JTextField(currentPassport);
        JTextField phoneField = new JTextField(currentPhone);

        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Passport ID:"));
        form.add(passportField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String passportId = passportField.getText().trim();
            String phone = phoneField.getText().trim();

            if (!name.isEmpty() && !passportId.isEmpty() && !phone.isEmpty()) {
                Trainer oldTrainer = trainerService.getTrainerById(trainerId);
                Trainer trainer = new Trainer(name, passportId, phone);
                trainer.setId(trainerId);
                trainer.setTrainingClassIds(oldTrainer.getTrainingClassIds());
                trainer.setPassword(oldTrainer.getPassword());
                trainerService.updateTrainer(trainer);

                tableModel.setValueAt(name, row, 0);
                tableModel.setValueAt(passportId, row, 1);
                tableModel.setValueAt(phone, row, 2);

                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void refreshTrainerTable(UserFriendlyTableModel tableModel) {
        tableModel.setRowCount(0);
        List<Trainer> trainers = trainerService.getAllTrainers();
        for (Trainer trainer : trainers) {
            tableModel.addRowWithId(new Object[]{
                    trainer.getId(),
                    trainer.getName(),
                    trainer.getPassportId(),
                    trainer.getPhone()
            });
        }
    }

    private JPanel createClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"ID", "Dance Type", "Level", "Trainer", "Client"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create table and add it to a scroll pane
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addClass = new JButton("Add Class");
        JButton assignTrainerButton = new JButton("Assign Trainer");

        buttonsPanel.add(addClass);
        buttonsPanel.add(assignTrainerButton);

        // Add action listeners
        addClass.addActionListener(e -> showAddClassDialog(tableModel));

        assignTrainerButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                showAssignTrainerDialog(tableModel, selectedRow);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a class");
            }
        });

        // Add components to panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        refreshClassesTable(tableModel);

        return panel;
    }

    private void refreshClassesTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        var classes = trainingClassService.getAllClasses();
        for (TrainingClass trainingClass : classes) {
            tableModel.addRow(new Object[]{
                    trainingClass.getId(),
                    trainingClass.getDanceType(),
                    trainingClass.getLevel(),
                    trainingClass.getTrainerId(),
                    trainingClass.getClientId()
            });
        }
    }


    private void showAddClassDialog(DefaultTableModel tableModel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Class", true);
        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField danceTypeField = new JTextField();
        JComboBox<TrainingLevel> levelCombo = new JComboBox<>(TrainingLevel.values());

        List<Trainer> trainers = trainerService.getAllTrainers();
        JComboBox<Trainer> trainerCombo = new JComboBox<>(trainers.toArray(new Trainer[0]));

        List<Client> clients = clientService.getAllClients();
        JComboBox<Client> clientCombo = new JComboBox<>(clients.toArray(new Client[0]));

        form.add(new JLabel("Dance Type:"));
        form.add(danceTypeField);
        form.add(new JLabel("Level:"));
        form.add(levelCombo);
        form.add(new JLabel("Trainer:"));
        form.add(trainerCombo);
        form.add(new JLabel("Clients:"));
        form.add(clientCombo);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            String danceType = danceTypeField.getText().trim();
            TrainingLevel level = (TrainingLevel) levelCombo.getSelectedItem();
            Trainer trainer = (Trainer) trainerCombo.getSelectedItem();
            Client client = (Client) clientCombo.getSelectedItem();

            if (!danceType.isEmpty() && level != null && trainer != null && client != null) {
                TrainingClass training = trainingClassService.createTraining(
                        danceType, level, trainer.getId(), client.getId());

                tableModel.addRow(new Object[]{
                        training.getId(),
                        training.getDanceType(),
                        training.getLevel(),
                        trainer.getName(),
                        client.getName()
                });
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAssignTrainerDialog(DefaultTableModel tableModel, int row) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Assign Trainer", true);
        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String classId = (String) tableModel.getValueAt(row, 0);

        // Get trainers and create combo box
        List<Trainer> trainers = trainerService.getAllTrainers();
        JComboBox<Trainer> trainerCombo = new JComboBox<>(trainers.toArray(new Trainer[0]));

        form.add(new JLabel("Trainer:"));
        form.add(trainerCombo);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            Trainer selectedTrainer = (Trainer) trainerCombo.getSelectedItem();
            if (selectedTrainer != null) {
                trainingClassService.assignTrainerToClass(classId, selectedTrainer.getId());
                tableModel.setValueAt(selectedTrainer.getName(), row, 4); // Update trainer name in table
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a trainer");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createSubscriptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"ID", "Client", "Class", "Start Date", "End Date", "Paid"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create table and add it to a scroll pane
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Subscription");
        JButton togglePaidButton = new JButton("Toggle Paid Status");

        buttonsPanel.add(addButton);
        buttonsPanel.add(togglePaidButton);

        // Add action listeners
        addButton.addActionListener(e -> showAddSubscriptionDialog(tableModel));
        togglePaidButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                boolean currentPaidStatus = (boolean) tableModel.getValueAt(selectedRow, 5);
                tableModel.setValueAt(!currentPaidStatus, selectedRow, 5);
                String subscriptionId = (String) tableModel.getValueAt(selectedRow, 0);
                Subscription subscription = subscriptionService.getById(subscriptionId);
                subscription.setPaid(!currentPaidStatus);
                subscriptionService.updateSubscription(subscription);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a subscription");
            }
        });

        // Add components to panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        refreshSubscriptionTable(tableModel);

        return panel;
    }

    private void refreshSubscriptionTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        var subscriptions = subscriptionService.getAll();
        for (Subscription subscription : subscriptions) {
            tableModel.addRow(new Object[]{
                    subscription.getId(),
                    subscription.getClientId(),
                    subscription.getTrainingClassId(),
                    subscription.getStartDate(),
                    subscription.getEndDate(),
                    subscription.isPaid()
            });
        }
    }

    private void showAddSubscriptionDialog(DefaultTableModel tableModel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Subscription", true);
        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Get clients and create combo box
        List<Client> clients = clientService.getAllClients();
        JComboBox<Client> clientCombo = new JComboBox<>(clients.toArray(new Client[0]));

        // Add date pickers
        JTextField startDateField = new JTextField("YYYY-MM-DD");
        JTextField endDateField = new JTextField("YYYY-MM-DD");
        JCheckBox paidCheckBox = new JCheckBox();

        // Training class selection
        List<TrainingClass> classes = trainingClassService.getAllClasses();
        JComboBox<TrainingClass> classesCombo = new JComboBox<>(classes.toArray(new TrainingClass[0]));

        form.add(new JLabel("Client:"));
        form.add(clientCombo);
        form.add(new JLabel("Class:"));
        form.add(classesCombo);
        form.add(new JLabel("Start Date:"));
        form.add(startDateField);
        form.add(new JLabel("End Date:"));
        form.add(endDateField);
        form.add(new JLabel("Paid:"));
        form.add(paidCheckBox);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            Client client = (Client) clientCombo.getSelectedItem();
            String startDateStr = startDateField.getText().trim();
            String endDateStr = endDateField.getText().trim();
            TrainingClass trainingClass = (TrainingClass) classesCombo.getSelectedItem();
            boolean isPaid = paidCheckBox.isSelected();

            if (client != null && !startDateStr.isEmpty() && !endDateStr.isEmpty() && trainingClass != null) {
                try {
                    LocalDate startDate = LocalDate.parse(startDateStr);
                    LocalDate endDate = LocalDate.parse(endDateStr);

                    Subscription subscription = subscriptionService.createSubscription(
                            client.getId(),
                            trainingClass.getId(),
                            startDate,
                            endDate,
                            isPaid
                    );

                    tableModel.addRow(new Object[]{
                            subscription.getId(),
                            client.getName(),
                            trainingClass.getDanceType(),
                            startDate.toString(),
                            endDate.toString(),
                            isPaid
                    });

                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Invalid date format. Please use YYYY-MM-DD format.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Please fill in all fields",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
