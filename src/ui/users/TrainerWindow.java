package ui.users;

import core.models.base.TrainingClass;
import core.services.core.SubscriptionService;
import core.services.core.TrainingClassService;
import core.services.storage.ClientStorageService;
import core.services.storage.SubscriptionStorageService;
import core.services.storage.TrainerStorageService;
import core.services.storage.TrainingClassStorageService;
import ui.users.base.BaseWindow;
import ui.utils.EntityAwareTableModel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TrainerWindow extends BaseWindow {
    private final TrainingClassService trainingClassService;
    private final JTabbedPane tabbedPane;

    public TrainerWindow(String trainerId) {
        super("Trainer Dashboard", trainerId);

        this.trainingClassService = new TrainingClassService(
                new TrainerStorageService(),
                new TrainingClassStorageService(),
                new SubscriptionService(
                        new SubscriptionStorageService(),
                        new ClientStorageService())
        );

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Classes", createClassesPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with columns
        String[] realColumns = new String[]{"ID", "Dance Type", "Level", "Schedule"};
        String[] displayColumns = new String[]{"Dance Type", "Level", "Schedule"};
        EntityAwareTableModel<TrainingClass> tableModel = new EntityAwareTableModel<>(realColumns, displayColumns);

        // Set up column formatters
        tableModel.setColumnFormatter(0, TrainingClass::getDanceType);
        tableModel.setColumnFormatter(1, trainingClass ->
                trainingClass.getLevel().toString());
        tableModel.setColumnFormatter(2, trainingClass -> {
            List<LocalDate> schedule = trainingClass.getSchedule();
            if (schedule == null || schedule.isEmpty()) {
                return "No scheduled dates";
            }
            return schedule.stream()
                    .map(LocalDate::toString)
                    .collect(Collectors.joining(", "));
        });

        // Create table and add it to a scroll pane
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Load trainer's classes
        List<TrainingClass> classes = trainingClassService.getTrainerClasses(userId);
        for (TrainingClass trainingClass : classes) {
            tableModel.addEntity(trainingClass, TrainingClass::getId);
        }

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
}