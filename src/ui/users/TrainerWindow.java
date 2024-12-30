package ui.users;

import core.models.base.TrainingClass;
import core.models.trainings.GroupTraining;
import core.services.core.TrainingClassService;
import core.services.storage.GroupTrainingStorageService;
import core.services.storage.SoloTrainingStorageService;
import core.services.storage.TrainerStorageService;
import ui.users.base.BaseWindow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


public class TrainerWindow extends BaseWindow {
    private final TrainingClassService trainingClassService;
    private final JTabbedPane tabbedPane;

    public TrainerWindow(String trainerId) {
        super("Trainer Dashboard", trainerId);

        this.trainingClassService = new TrainingClassService(
                new GroupTrainingStorageService(),
                new SoloTrainingStorageService(),
                new TrainerStorageService()
        );

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Classes", createClassesPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model for classes
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Type");
        model.addColumn("Dance Type");
        model.addColumn("Level");
        model.addColumn("Schedule");

        JTable table = new JTable(model);

        // Load trainer's classes
        List<TrainingClass> classes = trainingClassService.getTrainerClasses(userId);
        for (TrainingClass cls : classes) {
            model.addRow(new Object[]{
                    cls instanceof GroupTraining ? "Group" : "Solo",
                    cls.getDanceType(),
                    cls.getLevel(),
                    cls.getSchedule().toString()
            });
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}

