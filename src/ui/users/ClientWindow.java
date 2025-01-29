package ui.users;

import core.models.Subscription;
import core.models.actors.Trainer;
import core.models.base.TrainingClass;
import core.services.core.*;
import core.services.storage.*;
import ui.users.base.BaseWindow;
import ui.utils.EntityAwareTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ClientWindow extends BaseWindow {
    private final ClientService clientService;
    private final TrainerService trainerService;
    private final JTabbedPane tabbedPane;

    public ClientWindow(String clientId) {
        super("Client Dashboard", clientId);
        SubscriptionStorageService subscriptionStorageService = new SubscriptionStorageService();
        ClientStorageService clientStorageService = new ClientStorageService();
        TrainerStorageService trainerStorageService= new TrainerStorageService();
        TrainingClassStorageService trainingClassStorageService = new TrainingClassStorageService();
        PassportStorageService passportStorageService = new PassportStorageService();

        var subscriptionService = new SubscriptionService(
                subscriptionStorageService,
                clientStorageService
        );

        var trainingClassService = new TrainingClassService(
                trainerStorageService,
                trainingClassStorageService,
                subscriptionService
        );

        var passportService = new PassportService(passportStorageService);

        this.trainerService = new TrainerService(
                trainerStorageService,
                trainingClassService,
                passportService
        );

        this.clientService = new ClientService(
                clientStorageService,
                subscriptionStorageService,
                trainingClassService,
                this.trainerService,
                passportService
        );

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Subscriptions", createSubscriptionsPanel());
        tabbedPane.addTab("My Classes", createClassesPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createSubscriptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns
        String[] realColumns = new String[]{"ID", "Start Date", "End Date", "Paid"};
        String[] displayColumns = new String[]{"Start Date", "End Date", "Paid"};
        EntityAwareTableModel<Subscription> tableModel = new EntityAwareTableModel<>(realColumns, displayColumns);

        // Set up column formatters
        tableModel.setColumnFormatter(0, subscription ->
                subscription.getStartDate().toString());
        tableModel.setColumnFormatter(1, subscription ->
                subscription.getEndDate().toString());
        tableModel.setColumnFormatter(2, subscription ->
                subscription.isPaid() ? "Yes" : "No");

        // Create table and add it to a scroll pane
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Load subscriptions
        List<Subscription> subscriptions = clientService.getClientSubscriptions(userId);
        for (Subscription subscription : subscriptions) {
            tableModel.addEntity(subscription, Subscription::getId);
        }

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns
        String[] realColumns = new String[]{"ID", "Dance Type", "Level", "Trainer", "Schedule"};
        String[] displayColumns = new String[]{"Dance Type", "Level", "Trainer", "Schedule"};
        EntityAwareTableModel<TrainingClass> tableModel = new EntityAwareTableModel<>(realColumns, displayColumns);

        // Set up column formatters
        tableModel.setColumnFormatter(0, TrainingClass::getDanceType);
        tableModel.setColumnFormatter(1, trainingClass ->
                trainingClass.getLevel().toString());
        tableModel.setColumnFormatter(2, trainingClass -> {
            Trainer trainer = trainerService.getTrainerById(trainingClass.getTrainerId());
            return trainer != null ? trainer.getName() : "Not Assigned";
        });
        tableModel.setColumnFormatter(3, TrainingClass::getSchedule);

        // Create table and add it to a scroll pane
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Load classes
        List<TrainingClass> classes = clientService.getClientClasses(userId);
        for (TrainingClass trainingClass : classes) {
            tableModel.addEntity(trainingClass, TrainingClass::getId);
        }

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
}