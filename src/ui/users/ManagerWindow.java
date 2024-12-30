package ui.users;

import core.services.core.ClientService;
import core.services.core.ManagerService;
import core.services.core.SubscriptionService;
import core.services.core.TrainingClassService;
import core.services.storage.*;
import ui.users.base.BaseWindow;

import javax.swing.*;
import java.awt.*;

public class ManagerWindow extends BaseWindow {
    private final ManagerService managerService;
    private final JTabbedPane tabbedPane;

    public ManagerWindow(String managerId) {
        super("Manager Dashboard", managerId);

        this.managerService = new ManagerService(
                new ManagerStorageService(),
                new TrainerStorageService(),
                new ClientService(
                        new ClientStorageService(),
                        new SubscriptionStorageService(),
                        new TrainingClassService(
                                new GroupTrainingStorageService(),
                                new SoloTrainingStorageService(),
                                new TrainerStorageService()
                        )
                ),
                new TrainingClassService(
                        new GroupTrainingStorageService(),
                        new SoloTrainingStorageService(),
                        new TrainerStorageService()
                ),
                new SubscriptionService(
                        new SubscriptionStorageService(),
                        new ClientStorageService()
                )
        );

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Clients", createClientsPanel());
        tabbedPane.addTab("Trainers", createTrainersPanel());
        tabbedPane.addTab("Classes", createClassesPanel());
        tabbedPane.addTab("Subscriptions", createSubscriptionsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // Implementation of panels will be provided in the next message due to length
    private JPanel createClientsPanel() {
        // TODO: Implement CRUD operations for clients
        return new JPanel();
    }

    private JPanel createTrainersPanel() {
        // TODO: Implement CRUD operations for trainers
        return new JPanel();
    }

    private JPanel createClassesPanel() {
        // TODO: Implement class management
        return new JPanel();
    }

    private JPanel createSubscriptionsPanel() {
        // TODO: Implement subscription management
        return new JPanel();
    }
}
