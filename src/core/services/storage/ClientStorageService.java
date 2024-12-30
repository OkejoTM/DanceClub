package core.services.storage;

import core.models.actors.Client;
import core.services.base.JsonStorageService;

public class ClientStorageService extends JsonStorageService<Client> {
    public ClientStorageService() {
        super(Client.class, "files/clients");
    }

    @Override
    protected String getId(Client client) {
        return client.getId();
    }
}