package core.services.storage;

import core.models.trainings.SoloTraining;
import core.services.base.JsonStorageService;

public class SoloTrainingStorageService extends JsonStorageService<SoloTraining> {
    public SoloTrainingStorageService() {
        super(SoloTraining.class, "files/solo-trainings");
    }

    @Override
    protected String getId(SoloTraining training) {
        return training.getId();
    }
}