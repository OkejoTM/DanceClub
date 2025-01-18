package core.services.core;

import core.models.actors.Trainer;
import core.models.base.TrainingClass;
import core.services.storage.TrainerStorageService;
import java.util.List;

public class TrainerService {

    private final TrainerStorageService trainerStorage;
    private final TrainingClassService trainingClassService;
    private final PassportService passportService;

    public TrainerService(
            TrainerStorageService trainerStorage,
            TrainingClassService trainingClassService,
            PassportService passportService) {
        this.trainerStorage = trainerStorage;
        this.trainingClassService = trainingClassService;
        this.passportService = passportService;
    }

    // Trainer management
    public Trainer createTrainer(String name, String password, String passportId, String phone) {
        Trainer trainer = new Trainer(name, password, passportId, phone);
        trainerStorage.save(trainer);
        return trainer;
    }

    public void updateTrainer(Trainer trainer) {
        trainerStorage.save(trainer);
    }

    public void deleteTrainer(String trainerId) {
        Trainer trainer = getTrainerById(trainerId);
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer not found");
        }

        // Get all training classes for this trainer
        List<TrainingClass> trainerClasses = trainingClassService.getTrainerClasses(trainerId);

        // Delete each training class (this will also handle subscriptions)
        for (TrainingClass trainingClass : trainerClasses) {
            trainingClassService.deleteTrainingClass(trainingClass);
        }

        passportService.deletePassport(passportService.getPassportById(trainer.getPassportId()));

        // Finally delete the trainer
        trainerStorage.delete(trainerId);
    }

    public List<Trainer> getAllTrainers() {
        return trainerStorage.getAll();
    }

    public Trainer getTrainerById(String id) {
        return trainerStorage.getById(id);
    }
}