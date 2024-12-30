package core.services.storage;

import core.models.actors.Manager;
import core.services.base.JsonStorageService;

public class ManagerStorageService extends JsonStorageService<Manager> {
    public ManagerStorageService() {
        super(Manager.class, "files/managers");
    }

    @Override
    protected String getId(Manager manager) {
        return manager.getId();
    }
}
