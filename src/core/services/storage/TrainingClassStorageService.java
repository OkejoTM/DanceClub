package core.services.storage;

import core.models.base.TrainingClass;
import core.services.base.JsonStorageService;

public class TrainingClassStorageService extends JsonStorageService<TrainingClass> {

    public TrainingClassStorageService() {
        super(TrainingClass.class, "files/trainings");;
    }

    @Override
    protected String getId(TrainingClass entity) {
        return "";
    }
}
