package ui.users;

import core.models.Subscription;
import core.models.base.TrainingClass;
import core.models.trainings.GroupTraining;
import core.services.core.ClientService;
import core.services.core.TrainingClassService;
import core.services.storage.*;
import ui.users.base.BaseWindow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientWindow extends BaseWindow {
    private final ClientService clientService;
    private final JTabbedPane tabbedPane;

    public ClientWindow(String clientId) {
        super("Client Dashboard", clientId);

        this.clientService = new ClientService(
                new ClientStorageService(),
                new SubscriptionStorageService(),
                new TrainingClassService(
                        new GroupTrainingStorageService(),
                        new SoloTrainingStorageService(),
                        new TrainerStorageService()
                )
        );

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Subscriptions", createSubscriptionsPanel());
        tabbedPane.addTab("My Classes", createClassesPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createSubscriptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model for subscriptions
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Start Date");
        model.addColumn("End Date");
        model.addColumn("Paid");

        JTable table = new JTable(model);

        // Load subscriptions
        List<Subscription> subscriptions = clientService.getClientSubscriptions(userId);
        for (Subscription sub : subscriptions) {
            model.addRow(new Object[]{
                    sub.getId(),
                    sub.getStartDate(),
                    sub.getEndDate(),
                    sub.isPaid() ? "Yes" : "No"
            });
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model for classes
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Type");
        model.addColumn("Dance Type");
        model.addColumn("Level");

        JTable table = new JTable(model);

        // Load classes
        List<TrainingClass> classes = clientService.getClientClasses(userId);
        for (TrainingClass cls : classes) {
            model.addRow(new Object[]{
                    cls instanceof GroupTraining ? "Group" : "Solo",
                    cls.getDanceType(),
                    cls.getLevel()
            });
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}

