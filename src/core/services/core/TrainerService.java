package core.services.core;

import core.models.actors.Trainer;
import core.services.storage.TrainerStorageService;

import java.util.List;

public class TrainerService {

    private final TrainerStorageService trainerStorage;

    public TrainerService(TrainerStorageService trainerStorage) {
        this.trainerStorage = trainerStorage;
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
        trainerStorage.delete(trainerId);
    }

    public List<Trainer> getAllTrainers() {
        return trainerStorage.getAll();
    }

    public Trainer getTrainerById(String id)
    {
        return trainerStorage.getById(id);
    }

}
