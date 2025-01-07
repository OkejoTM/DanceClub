package ui.users;

import core.models.Passport;
import core.models.Subscription;
import core.models.actors.Client;
import core.models.actors.Trainer;
import core.models.base.Employee;
import core.models.base.TrainingClass;
import core.models.enums.TrainingLevel;
import core.services.core.*;
import core.services.storage.*;
import ui.users.base.BaseWindow;
import ui.utils.EntityAwareTableModel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ManagerWindow extends BaseWindow {
    private final TrainerService trainerService;
    private final ClientService clientService;
    private final TrainingClassService trainingClassService;
    private final SubscriptionService subscriptionService;
    private final PassportService passportService;
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
        this.passportService = new PassportService(new PassportStorageService());

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
        String[] realColumns = new String[]{"ID", "Name", "Passport Details"};
        String[] displayColumns = new String[]{"Name", "Passport Details"};
        EntityAwareTableModel<Client> tableModel = new EntityAwareTableModel<>(realColumns, displayColumns);

        tableModel.setColumnFormatter(0, Client::getName);
        tableModel.setColumnFormatter(1, client -> {
            Passport passport = passportService.getPassportById(client.getPassportId());
            return String.format("%s %s",
                    passport.getSeries(),
                    passport.getNumber());
        });

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
                    Client client = tableModel.getEntityForRow(selectedRow);
                    clientService.deleteClient(client.getId());
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

    private void showAddClientDialog(EntityAwareTableModel<Client> tableModel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Client", true);
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        List<Passport> passports = passportService.getAllPassports();
        JComboBox<Passport> passportCombo = new JComboBox<>(passports.toArray(new Passport[0]));

        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Passport Details:"));
        form.add(passportCombo);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            Passport passport = (Passport) passportCombo.getSelectedItem();

            if (!name.isEmpty() && passport != null) {
                Client client = clientService.createClient(name, passport.getId());
                tableModel.addEntity(client, Client::getId);
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

    private void showEditClientDialog(EntityAwareTableModel<Client> tableModel, int row) {
        Client client = tableModel.getEntityForRow(row);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Client", true);
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(client.getName());

        List<Passport> passports = passportService.getAllPassports();
        JComboBox<Passport> passportCombo = new JComboBox<>(passports.toArray(new Passport[0]));

        // Find and select the current passport
        Passport currentPassport = passportService.getPassportById(client.getPassportId());

        if (currentPassport != null) {
            passportCombo.setSelectedItem(currentPassport);
        }

        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Passport Details:"));
        form.add(passportCombo);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            Passport passport = (Passport) passportCombo.getSelectedItem();

            if (!name.isEmpty() && passport != null) {
                Client updatedClient = new Client(name, passport.getId());
                updatedClient.setId(client.getId());
                updatedClient.setSubscriptionIds(client.getSubscriptionIds());
                clientService.updateClient(updatedClient);

                // Refresh the entire row
                tableModel.removeRow(row);
                tableModel.addEntity(updatedClient, Client::getId);

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

    private void refreshClientTable(EntityAwareTableModel<Client> tableModel) {
        tableModel.setRowCount(0);
        List<Client> clients = clientService.getAllClients();
        for (Client client : clients) {
            tableModel.addEntity(client, Client::getId);
        }
    }

    private JPanel createTrainersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns
        String[] realColumns = new String[]{"ID", "Name", "Passport Details", "Phone"};
        String[] displayColumns = new String[]{"Name", "Passport Details", "Phone"};
        EntityAwareTableModel<Trainer> tableModel = new EntityAwareTableModel<>(realColumns, displayColumns);

        tableModel.setColumnFormatter(0, Employee::getName);
        tableModel.setColumnFormatter(1, trainer -> {
            Passport passport = passportService.getPassportById(trainer.getPassportId());
            return String.format("%s %s",
                    passport.getSeries(),
                    passport.getNumber());
        });
        tableModel.setColumnFormatter(2, Employee::getPhone);

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
                    Trainer trainer = tableModel.getEntityForRow(selectedRow);
                    trainerService.deleteTrainer(trainer.getId());
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

    private void showAddTrainerDialog(EntityAwareTableModel<Trainer> tableModel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Trainer", true);
        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField phoneField = new JTextField();

        List<Passport> passports = passportService.getAllPassports();
        JComboBox<Passport> passportCombo = new JComboBox<>(passports.toArray(new Passport[0]));

        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        form.add(new JLabel("Passports:"));
        form.add(passportCombo);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            Passport passport = (Passport) passportCombo.getSelectedItem();
            String phone = phoneField.getText().trim();

            if (!name.isEmpty() && !password.isEmpty() && passport != null && !phone.isEmpty()) {
                Trainer trainer = trainerService.createTrainer(name, password, passport.getId(), phone);

                tableModel.addEntity(trainer, Trainer::getId);
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

    private void showEditTrainerDialog(EntityAwareTableModel<Trainer> tableModel, int row) {
        Trainer trainer = tableModel.getEntityForRow(row);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Trainer", true);
        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(trainer.getName());
        JTextField phoneField = new JTextField(trainer.getPhone());

        // Get all passports and create combo box
        List<Passport> passports = passportService.getAllPassports();
        JComboBox<Passport> passportCombo = new JComboBox<>(passports.toArray(new Passport[0]));

        // Find and select the current passport
        Passport currentPassport = passportService.getPassportById(trainer.getPassportId());

        if (currentPassport != null) {
            passportCombo.setSelectedItem(currentPassport);
        }

        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Passport:"));
        form.add(passportCombo);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            Passport passport = (Passport) passportCombo.getSelectedItem();
            String phone = phoneField.getText().trim();

            if (!name.isEmpty() && passport != null && !phone.isEmpty()) {
                // Create new trainer instance with updated values
                Trainer updatedTrainer = new Trainer(name, passport.getId(), phone);
                updatedTrainer.setId(trainer.getId());

                // Copy over values that shouldn't change
                updatedTrainer.setTrainingClassIds(trainer.getTrainingClassIds());
                updatedTrainer.setPassword(trainer.getPassword());

                // Update in service
                trainerService.updateTrainer(updatedTrainer);

                // Update table - remove and add to refresh all formatted values
                tableModel.removeRow(row);
                tableModel.addEntity(updatedTrainer, Trainer::getId);

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


    private void refreshTrainerTable(EntityAwareTableModel<Trainer> tableModel) {
        tableModel.setRowCount(0);
        List<Trainer> trainers = trainerService.getAllTrainers();
        for (Trainer trainer : trainers) {
            tableModel.addEntity(trainer, Trainer::getId);
        }
    }

    private JPanel createClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns
        String[] realColumns = new String[]{"ID", "Dance Type", "Level", "Trainer", "Client"};
        String[] displayColumns = new String[]{"Dance Type", "Level", "Trainer", "Client"};
        EntityAwareTableModel<TrainingClass> tableModel = new EntityAwareTableModel<>(realColumns, displayColumns);

        tableModel.setColumnFormatter(0, TrainingClass::getDanceType);
        tableModel.setColumnFormatter(1, trainingClass -> trainingClass.getLevel().toString());
        tableModel.setColumnFormatter(2, trainingClass -> {
            Trainer trainer = trainerService.getTrainerById(trainingClass.getTrainerId());
            return trainer.toString();
        });
        tableModel.setColumnFormatter(3, trainingClass -> {
            Client trainer = clientService.getClientById(trainingClass.getClientId());
            return trainer.toString();
        });

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

    private void refreshClassesTable(EntityAwareTableModel<TrainingClass> tableModel) {
        tableModel.setRowCount(0);
        var classes = trainingClassService.getAllClasses();
        for (TrainingClass trainingClass : classes) {
            tableModel.addEntity(trainingClass, TrainingClass::getId);
        }
    }


    private void showAddClassDialog(EntityAwareTableModel<TrainingClass> tableModel) {
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

                tableModel.addEntity(training, TrainingClass::getId);
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

    private void showAssignTrainerDialog(EntityAwareTableModel<TrainingClass> tableModel, int row) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Assign Trainer", true);
        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        TrainingClass training = tableModel.getEntityForRow(row);

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
                trainingClassService.assignTrainerToClass(training.getId(), selectedTrainer.getId());

                tableModel.removeRow(row);
                tableModel.addEntity(training, TrainingClass::getId);

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
        String[] realColumns = new String[]{"ID", "Client", "Class", "Start Date", "End Date", "Paid"};
        String[] displayColumns = new String[]{"Client", "Class", "Start Date", "End Date", "Paid"};
        EntityAwareTableModel<Subscription> tableModel = new EntityAwareTableModel<>(realColumns, displayColumns);

        tableModel.setColumnFormatter(0, subscription -> {
            Client client = clientService.getClientById(subscription.getClientId());
            return client.getName();
        });

        tableModel.setColumnFormatter(1, subscription -> {
            TrainingClass trainingClass = trainingClassService.getTrainingClass(subscription.getTrainingClassId());
            return trainingClass.getDanceType();
        });

        tableModel.setColumnFormatter(2, subscription ->
                subscription.getStartDate().toString());

        tableModel.setColumnFormatter(3, subscription ->
                subscription.getEndDate().toString());

        tableModel.setColumnFormatter(4, subscription ->
                Boolean.toString(subscription.isPaid()));


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
                Subscription subscription = tableModel.getEntityForRow(selectedRow);
                subscription.setPaid(!subscription.isPaid());
                subscriptionService.updateSubscription(subscription);

                // Refresh the row
                tableModel.removeRow(selectedRow);
                tableModel.addEntity(subscription, Subscription::getId);
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

    private void refreshSubscriptionTable(EntityAwareTableModel<Subscription> tableModel) {
        tableModel.setRowCount(0);
        List<Subscription> subscriptions = subscriptionService.getAll();
        for (Subscription subscription : subscriptions) {
            tableModel.addEntity(subscription, Subscription::getId);
        }
    }

    private void showAddSubscriptionDialog(EntityAwareTableModel<Subscription> tableModel) {
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

                    tableModel.addEntity(subscription, Subscription::getId);

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
