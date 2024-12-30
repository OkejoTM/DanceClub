package core.services.storage;

import core.models.actors.Trainer;
import core.services.base.JsonStorageService;

public class TrainerStorageService extends JsonStorageService<Trainer> {
    public TrainerStorageService() {
        super(Trainer.class, "files/trainers");
    }

    @Override
    protected String getId(Trainer trainer) {
        return trainer.getId();
    }
}