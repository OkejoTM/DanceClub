package ui.users;

import core.models.base.TrainingClass;
import core.services.core.TrainingClassService;
import core.services.storage.TrainerStorageService;
import core.services.storage.TrainingClassStorageService;
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
                new TrainerStorageService(),
                new TrainingClassStorageService()
        );

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Classes", createClassesPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model for classes
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Dance Type");
        model.addColumn("Level");
        model.addColumn("Schedule");

        JTable table = new JTable(model);

        // Load trainer's classes
        List<TrainingClass> classes = trainingClassService.getTrainerClasses(userId);
        for (TrainingClass cls : classes) {
            model.addRow(new Object[]{
                    cls.getDanceType(),
                    cls.getLevel(),
                    cls.getSchedule().toString()
            });
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}

