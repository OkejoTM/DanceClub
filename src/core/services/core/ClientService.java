package core.services.core;

import core.models.Subscription;
import core.models.actors.Client;
import core.models.actors.Trainer;
import core.models.base.TrainingClass;
import core.services.storage.ClientStorageService;
import core.services.storage.SubscriptionStorageService;

import java.util.List;
import java.util.Objects;

public class ClientService {
    private final ClientStorageService clientStorage;
    private final SubscriptionStorageService subscriptionStorage;
    private final TrainingClassService trainingClassService;
    private final TrainerService trainerService;
    private final PassportService passportService;

    public ClientService(
            ClientStorageService clientStorage,
            SubscriptionStorageService subscriptionStorage,
            TrainingClassService trainingClassService, TrainerService trainerService, PassportService passportService) {
        this.clientStorage = clientStorage;
        this.subscriptionStorage = subscriptionStorage;
        this.trainingClassService = trainingClassService;
        this.trainerService = trainerService;
        this.passportService = passportService;
    }

    // Methods for Clients to view their data
    public List<Subscription> getClientSubscriptions(String clientId) {
        return subscriptionStorage.getAll().stream()
                .filter(sub -> sub.getClientId().equals(clientId))
                .toList();
    }

    public List<TrainingClass> getClientClasses(String clientId) {
        List<String> classIds = getClientSubscriptions(clientId).stream()
                .map(Subscription::getTrainingClassId)
                .toList();

        return classIds.stream()
                .map(trainingClassService::getTrainingClass)
                .filter(Objects::nonNull)
                .toList();
    }

    // Methods for Managers to manage clients
    public Client createClient(String name, String passportId) {
        Client client = new Client(name, passportId);
        clientStorage.save(client);
        return client;
    }

    public void updateClient(Client client) {
        clientStorage.save(client);
    }

    public void deleteClient(Client client) {
        List<Subscription> subscriptions = getClientSubscriptions(client.getId());
        List<TrainingClass> classes = getClientClasses(client.getId());

        for (TrainingClass trainingClass : classes) {
            // Remove class from trainer's list
            Trainer trainer = trainerService.getTrainerById(trainingClass.getTrainerId());
            if (trainer != null) {
                trainer.removeTrainingClass(trainingClass.getId());
                trainerService.updateTrainer(trainer);
            }
            trainingClassService.deleteTrainingClass(trainingClass);
        }

        for (Subscription subscription : subscriptions) {
            subscriptionStorage.delete(subscription.getId());
        }

        passportService.deletePassport(passportService.getPassportById(client.getPassportId()));

        clientStorage.delete(client.getId());
    }

    public List<Client> getAllClients() {
        return clientStorage.getAll();
    }

    public Client getClientById(String clientId) {
        return clientStorage.getById(clientId);
    }
}