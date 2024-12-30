package core.services;

import core.models.actors.Client;
import core.models.actors.Manager;
import core.models.actors.Trainer;
import core.models.enums.UserRole;
import core.services.storage.ClientStorageService;
import core.services.storage.ManagerStorageService;
import core.services.storage.TrainerStorageService;

public class AuthService {
    private final ClientStorageService clientStorage;
    private final ManagerStorageService managerStorage;
    private final TrainerStorageService trainerStorage;

    public AuthService(ClientStorageService clientStorage,
            ManagerStorageService managerStorage,
            TrainerStorageService trainerStorage) {
        this.clientStorage = clientStorage;
        this.managerStorage = managerStorage;
        this.trainerStorage = trainerStorage;
    }

    public record AuthResult(boolean success, UserRole role, String userId) {}

    public AuthResult authenticate(String name, String password) {
        // For clients, we only check name as they don't have passwords
        Client client = clientStorage.getAll().stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (client != null) {
            return new AuthResult(true, UserRole.CLIENT, client.getId());
        }

        // Check managers
        Manager manager = managerStorage.getAll().stream()
                .filter(m -> m.getName().equals(name) && m.getPassword().equals(password))
                .findFirst()
                .orElse(null);
        if (manager != null) {
            return new AuthResult(true, UserRole.MANAGER, manager.getId());
        }

        // Check trainers
        Trainer trainer = trainerStorage.getAll().stream()
                .filter(t -> t.getName().equals(name) && t.getPassword().equals(password))
                .findFirst()
                .orElse(null);
        if (trainer != null) {
            return new AuthResult(true, UserRole.TRAINER, trainer.getId());
        }

        return new AuthResult(false, null, null);
    }
}

