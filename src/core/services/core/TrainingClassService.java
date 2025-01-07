package core.services.core;

import core.models.actors.Trainer;
import core.models.base.TrainingClass;
import core.models.enums.TrainingLevel;
import core.services.storage.TrainerStorageService;
import core.services.storage.TrainingClassStorageService;

import java.util.ArrayList;
import java.util.List;

public class TrainingClassService {
    private final TrainingClassStorageService trainingClassStorageService;
    private final TrainerStorageService trainerStorage;

    public TrainingClassService(
            TrainerStorageService trainerStorage,
            TrainingClassStorageService trainingClassStorageService) {
        this.trainingClassStorageService = trainingClassStorageService;
        this.trainerStorage = trainerStorage;
    }

    public TrainingClass getTrainingClass(String classId) {
        return trainingClassStorageService.getById(classId);
    }

    public List<TrainingClass> getTrainerClasses(String trainerId) {
        return new ArrayList<>(trainingClassStorageService.getAll().stream()
                .filter(training -> training.getTrainerId().equals(trainerId))
                .toList());
    }

    public List<TrainingClass> getAllClasses() {

        return new ArrayList<>(trainingClassStorageService.getAll());
    }

    public TrainingClass createTraining(String danceType, TrainingLevel level, String trainerId, String clientId) {
        TrainingClass training = new TrainingClass(danceType, level, trainerId, clientId);
        trainingClassStorageService.save(training);

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
        training.setTrainerId(trainerId);

        Trainer trainer = trainerStorage.getById(trainerId);
        trainer.addTrainingClass(training.getId());
        
        trainerStorage.save(trainer);
        trainingClassStorageService.save(training);
    }
}