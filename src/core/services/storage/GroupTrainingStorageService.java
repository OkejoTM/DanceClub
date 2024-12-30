package core.services.storage;

import core.models.trainings.GroupTraining;
import core.services.base.JsonStorageService;

public class GroupTrainingStorageService extends JsonStorageService<GroupTraining> {
    public GroupTrainingStorageService() {
        super(GroupTraining.class, "files/group-trainings");
    }

    @Override
    protected String getId(GroupTraining training) {
        return training.getId();
    }
}