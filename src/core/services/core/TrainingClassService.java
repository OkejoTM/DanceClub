package core.services.core;

import core.models.actors.Trainer;
import core.models.base.TrainingClass;
import core.models.enums.AgeGroup;
import core.models.enums.TrainingLevel;
import core.models.trainings.GroupTraining;
import core.models.trainings.SoloTraining;
import core.services.storage.GroupTrainingStorageService;
import core.services.storage.SoloTrainingStorageService;
import core.services.storage.TrainerStorageService;

import java.util.ArrayList;
import java.util.List;

public class TrainingClassService {
    private final GroupTrainingStorageService groupTrainingStorage;
    private final SoloTrainingStorageService soloTrainingStorage;
    private final TrainerStorageService trainerStorage;

    public TrainingClassService(
            GroupTrainingStorageService groupTrainingStorage,
            SoloTrainingStorageService soloTrainingStorage,
            TrainerStorageService trainerStorage) {
        this.groupTrainingStorage = groupTrainingStorage;
        this.soloTrainingStorage = soloTrainingStorage;
        this.trainerStorage = trainerStorage;
    }

    public TrainingClass getTrainingClass(String classId) {
        TrainingClass training = groupTrainingStorage.getById(classId);
        if (training == null) {
            training = soloTrainingStorage.getById(classId);
        }
        return training;
    }

    public List<TrainingClass> getTrainerClasses(String trainerId) {
        List<TrainingClass> allClasses = new ArrayList<>();

        allClasses.addAll(groupTrainingStorage.getAll().stream()
                .filter(training -> training.getTrainerId().equals(trainerId))
                .toList());

        allClasses.addAll(soloTrainingStorage.getAll().stream()
                .filter(training -> training.getTrainerId().equals(trainerId))
                .toList());

        return allClasses;
    }

    public GroupTraining createGroupTraining(String danceType, TrainingLevel level, String trainerId, AgeGroup ageGroup) {
        GroupTraining training = new GroupTraining(danceType, level, trainerId, ageGroup);
        groupTrainingStorage.save(training);

        // Update trainer's training list
        Trainer trainer = trainerStorage.getById(trainerId);
        if (trainer != null) {
            trainer.addTrainingClass(training.getId());
            trainerStorage.save(trainer);
        }

        return training;
    }

    public SoloTraining createSoloTraining(String danceType, TrainingLevel level, String trainerId, String clientId) {
        SoloTraining training = new SoloTraining(danceType, level, trainerId, clientId);
        soloTrainingStorage.save(training);

        // Update trainer's training list
        Trainer trainer = trainerStorage.getById(trainerId);
        if (trainer != null) {
            trainer.addTrainingClass(training.getId());
            trainerStorage.save(trainer);
        }

        return training;
    }

    public void assignTrainerToClass(String classId, String trainerId) {
        TrainingClass training = getTrainingClass(classId);
        if (training instanceof GroupTraining) {
            GroupTraining groupTraining = (GroupTraining) training;
            groupTraining.setTrainerId(trainerId);
            groupTrainingStorage.save(groupTraining);
        } else if (training instanceof SoloTraining) {
            SoloTraining soloTraining = (SoloTraining) training;
            soloTraining.setTrainerId(trainerId);
            soloTrainingStorage.save(soloTraining);
        }
    }
}