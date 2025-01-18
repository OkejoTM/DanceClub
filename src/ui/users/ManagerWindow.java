package ui.users;

import core.models.Passport;
import core.models.Subscription;
import core.models.actors.Client;
import core.models.actors.Trainer;
import core.models.base.Employee;
import core.models.base.TrainingClass;
import core.models.enums.TrainingLevel;
import core.services.core.*;
import core.services.management.PassportManagementService;
import core.services.storage.*;
import ui.users.base.BaseWindow;
import ui.utils.EntityAwareTableModel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ManagerWindow extends BaseWindow {
    private final TrainerService trainerService;
    private final ClientService clientService;
    private final TrainingClassService trainingClassService;
    private final SubscriptionService subscriptionService;
    private final PassportService passportService;
    private final PassportManagementService passportManagementService;
    private final JTabbedPane tabbedPane;
    private EntityAwareTableModel<Client> clientTableModel;
    private EntityAwareTableModel<Subscription> subscriptionTableModel;
    private EntityAwareTableModel<TrainingClass> trainingClassTableModel;
    private EntityAwareTableModel<Trainer> trainerTableModel;

    public ManagerWindow(String managerId) {
        super("Manager Dashboard", managerId);
        SubscriptionStorageService subscriptionStorageService = new SubscriptionStorageService();
        ClientStorageService clientStorageService = new ClientStorageService();
        TrainerStorageService trainerStorageService= new TrainerStorageService();
        TrainingClassStorageService trainingClassStorageService = new TrainingClassStorageService();
        PassportStorageService passportStorageService = new PassportStorageService();

        this.passportService = new PassportService(passportStorageService);

        this.subscriptionService = new SubscriptionService(
                subscriptionStorageService,
                clientStorageService
        );

        TrainingClassService trainingClassService = new TrainingClassService(
                trainerStorageService,
                trainingClassStorageService,
                subscriptionService
        );

        this.trainerService = new TrainerService(
                trainerStorageService,
                trainingClassService,
                passportService
        );

        this.trainingClassService = trainingClassService;

        this.clientService = new ClientService(
                clientStorageService,
                subscriptionStorageService,
                trainingClassService,
                this.trainerService,
                this.passportService
        );

        this.passportManagementService = new PassportManagementService(
                this.clientService,
                this.passportService,
                this.trainerService
        );

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Clients", createClientsPanel());
        tabbedPane.addTab("Trainers", createTrainersPanel());
        tabbedPane.addTab("Classes", createClassesPanel());
        tabbedPane.addTab("Subscriptions", createSubscriptionsPanel());
        tabbedPane.addTab("Passports", createPassportsPanel());

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
        clientTableModel = tableModel;

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
                    clientService.deleteClient(client);
                    tableModel.removeRow(selectedRow);
                    refreshAllTables();
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
        List<Passport> passports = passportManagementService.getAllFreePassports();
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

        List<Passport> passports = passportManagementService.getAllFreePassports();
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
        trainerTableModel = tableModel;

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
                    refreshAllTables();
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

        List<Passport> passports = passportManagementService.getAllFreePassports();
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
        List<Passport> passports = passportManagementService.getAllFreePassports();
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
        trainingClassTableModel = tableModel;

        // Create table and add it to a scroll pane
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addClass = new JButton("Add Class");
        JButton editClass = new JButton("Edit Class");
        JButton deleteClass = new JButton("Delete Class");
        JButton assignTrainerButton = new JButton("Assign Trainer");

        buttonsPanel.add(addClass);
        buttonsPanel.add(editClass);
        buttonsPanel.add(deleteClass);
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

        editClass.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                showEditClassDialog(tableModel, selectedRow);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a class to edit");
            }
        });

        deleteClass.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure you want to delete this class?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    TrainingClass trainingClass = tableModel.getEntityForRow(selectedRow);
                    trainingClassService.deleteTrainingClass(trainingClass);
                    tableModel.removeRow(selectedRow);
                    refreshAllTables();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a class to delete");
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

    private void showEditClassDialog(EntityAwareTableModel<TrainingClass> tableModel, int row) {
        TrainingClass trainingClass = tableModel.getEntityForRow(row);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Class", true);
        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField danceTypeField = new JTextField(trainingClass.getDanceType());
        JComboBox<TrainingLevel> levelCombo = new JComboBox<>(TrainingLevel.values());
        levelCombo.setSelectedItem(trainingClass.getLevel());

        List<Trainer> trainers = trainerService.getAllTrainers();
        JComboBox<Trainer> trainerCombo = new JComboBox<>(trainers.toArray(new Trainer[0]));
        trainerCombo.setSelectedItem(trainerService.getTrainerById(trainingClass.getTrainerId()));

        List<Client> clients = clientService.getAllClients();
        JComboBox<Client> clientCombo = new JComboBox<>(clients.toArray(new Client[0]));
        clientCombo.setSelectedItem(clientService.getClientById(trainingClass.getClientId()));

        form.add(new JLabel("Dance Type:"));
        form.add(danceTypeField);
        form.add(new JLabel("Level:"));
        form.add(levelCombo);
        form.add(new JLabel("Trainer:"));
        form.add(trainerCombo);
        form.add(new JLabel("Client:"));
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
                trainingClass.setDanceType(danceType);
                trainingClass.setLevel(level);
                trainingClass.setTrainerId(trainer.getId());
                trainingClass.setClientId(client.getId());

                trainingClassService.updateTrainingClass(trainingClass);

                tableModel.removeRow(row);
                tableModel.addEntity(trainingClass, TrainingClass::getId);

                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
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

        subscriptionTableModel = tableModel;

        // Create table and add it to a scroll pane
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Subscription");
        JButton togglePaidButton = new JButton("Toggle Paid Status");
        JButton editButton = new JButton("Edit Subscription");
        JButton deleteButton = new JButton("Delete Subscription");

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
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
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                showEditSubscriptionDialog(tableModel, selectedRow);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a subscription to edit");
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure you want to delete this subscription?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    Subscription subscription = tableModel.getEntityForRow(selectedRow);
                    subscriptionService.deleteSubscription(subscription);
                    tableModel.removeRow(selectedRow);
                    refreshAllTables();
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a subscription to delete");
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
                            "Invalid date format. Please use YYYY-MM-DD format." + ex.getMessage(),
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

    private void showEditSubscriptionDialog(EntityAwareTableModel<Subscription> tableModel, int row) {
        Subscription subscription = tableModel.getEntityForRow(row);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Subscription", true);
        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<Client> clients = clientService.getAllClients();
        JComboBox<Client> clientCombo = new JComboBox<>(clients.toArray(new Client[0]));
        clientCombo.setSelectedItem(clientService.getClientById(subscription.getClientId()));

        JTextField startDateField = new JTextField(subscription.getStartDate().toString());
        JTextField endDateField = new JTextField(subscription.getEndDate().toString());
        JCheckBox paidCheckBox = new JCheckBox();
        paidCheckBox.setSelected(subscription.isPaid());

        List<TrainingClass> classes = trainingClassService.getAllClasses();
        JComboBox<TrainingClass> classesCombo = new JComboBox<>(classes.toArray(new TrainingClass[0]));
        classesCombo.setSelectedItem(trainingClassService.getTrainingClass(subscription.getTrainingClassId()));

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

                    subscription.setClientId(client.getId());
                    subscription.setTrainingClassId(trainingClass.getId());
                    subscription.setStartDate(startDate);
                    subscription.setEndDate(endDate);
                    subscription.setPaid(isPaid);

                    subscriptionService.updateSubscription(subscription);

                    // Refresh the row
                    tableModel.removeRow(row);
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

    private JPanel createPassportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns
        String[] realColumns = new String[]{"ID", "Address", "BirthDate", "Number", "Series", "Overdue"};
        String[] displayColumns = new String[]{"Address", "BirthDate", "Number", "Series", "Overdue"};
        EntityAwareTableModel<Passport> tableModel = new EntityAwareTableModel<>(realColumns, displayColumns);

        tableModel.setColumnFormatter(0, Passport::getAddress);

        tableModel.setColumnFormatter(1, passport -> passport.getBirthDate().toString());

        tableModel.setColumnFormatter(2, Passport::getNumber);

        tableModel.setColumnFormatter(3, Passport::getSeries);

        tableModel.setColumnFormatter(4, passport -> passport.getOverdue().toString());

        // Create table and add it to a scroll pane
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Passport");
        JButton editButton = new JButton("Edit Passport");
        JButton deleteButton = new JButton("Delete Passport");

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);

        // Add action listeners
        addButton.addActionListener(e -> showAddPassportDialog(tableModel));
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                showEditPassportDialog(tableModel, selectedRow);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a passport to edit");
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure you want to delete this passport?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    Passport passport = tableModel.getEntityForRow(selectedRow);
                    try {
                        passportManagementService.deleteIfPassportNotOccupied(passport);
                        tableModel.removeRow(selectedRow);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(
                                panel,
                                ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a passport to delete");
            }
        });

        // Add components to panel
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        refreshPassportTable(tableModel);

        return panel;
    }

    private void refreshPassportTable(EntityAwareTableModel<Passport> tableModel) {
        tableModel.setRowCount(0);
        List<Passport> passports = passportService.getAllPassports();
        for (Passport passport : passports) {
            tableModel.addEntity(passport, Passport::getId);
        }
    }

    private void showAddPassportDialog(EntityAwareTableModel<Passport> tableModel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Passport", true);
        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField addressField = new JTextField();
        JTextField birthDateField = new JTextField("YYYY-MM-DD");
        JTextField numberField = new JTextField();
        JTextField seriesField = new JTextField();
        JTextField overdueField = new JTextField("YYYY-MM-DD");

        form.add(new JLabel("Address:"));
        form.add(addressField);
        form.add(new JLabel("Birth Date:"));
        form.add(birthDateField);
        form.add(new JLabel("Number:"));
        form.add(numberField);
        form.add(new JLabel("Series:"));
        form.add(seriesField);
        form.add(new JLabel("Overdue Date:"));
        form.add(overdueField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            String address = addressField.getText().trim();
            String birthDateStr = birthDateField.getText().trim();
            String number = numberField.getText().trim();
            String series = seriesField.getText().trim();
            String overdueStr = overdueField.getText().trim();

            if (!address.isEmpty() && !birthDateStr.isEmpty() && !number.isEmpty()
                    && !series.isEmpty() && !overdueStr.isEmpty()) {
                try {
                    LocalDate birthDate = LocalDate.parse(birthDateStr);
                    LocalDate overdue = LocalDate.parse(overdueStr);
                    Passport passport = new Passport(address,
                            birthDate,
                            number,
                            series,
                            overdue);

                    passportManagementService.createIfPassportNotOccupied(passport);

                    tableModel.addEntity(passport, Passport::getId);
                    dialog.dispose();
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Invalid date format. Please use YYYY-MM-DD format.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (IllegalArgumentException ex){
                    JOptionPane.showMessageDialog(dialog,
                            ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
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

    private void showEditPassportDialog(EntityAwareTableModel<Passport> tableModel, int row) {
        Passport passport = tableModel.getEntityForRow(row);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Passport", true);
        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField addressField = new JTextField(passport.getAddress());
        JTextField birthDateField = new JTextField(passport.getBirthDate().toString());
        JTextField numberField = new JTextField(passport.getNumber());
        JTextField seriesField = new JTextField(passport.getSeries());
        JTextField overdueField = new JTextField(passport.getOverdue().toString());

        form.add(new JLabel("Address:"));
        form.add(addressField);
        form.add(new JLabel("Birth Date:"));
        form.add(birthDateField);
        form.add(new JLabel("Number:"));
        form.add(numberField);
        form.add(new JLabel("Series:"));
        form.add(seriesField);
        form.add(new JLabel("Overdue Date:"));
        form.add(overdueField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        form.add(saveButton);
        form.add(cancelButton);

        saveButton.addActionListener(e -> {
            String address = addressField.getText().trim();
            String birthDateStr = birthDateField.getText().trim();
            String number = numberField.getText().trim();
            String series = seriesField.getText().trim();
            String overdueStr = overdueField.getText().trim();

            if (!address.isEmpty() && !birthDateStr.isEmpty() && !number.isEmpty()
                    && !series.isEmpty() && !overdueStr.isEmpty()) {
                try {
                    LocalDate birthDate = LocalDate.parse(birthDateStr);
                    LocalDate overdue = LocalDate.parse(overdueStr);

                    // Create updated passport with all fields
                    Passport updatedPassport = new Passport(
                            address,
                            birthDate,
                            number,
                            series,
                            overdue
                    );
                    updatedPassport.setId(passport.getId());

                    // Update in service
                    passportManagementService.updateIfPassportNotOccupied(updatedPassport);

                    // Update table
                    tableModel.removeRow(row);
                    tableModel.addEntity(updatedPassport, Passport::getId);

                    dialog.dispose();
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Invalid date format. Please use YYYY-MM-DD format.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (IllegalArgumentException ex){
                    JOptionPane.showMessageDialog(dialog,
                            ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
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

    private void refreshAllTables(){
        refreshClassesTable(trainingClassTableModel);
        refreshTrainerTable(trainerTableModel);
        refreshClientTable(clientTableModel);
        refreshSubscriptionTable(subscriptionTableModel);
    }
}
