package core.services.core;

import core.models.Subscription;
import core.models.actors.Client;
import core.models.base.TrainingClass;
import core.services.storage.ClientStorageService;
import core.services.storage.SubscriptionStorageService;

import java.util.List;
import java.util.Objects;

public class ClientService {
    private final ClientStorageService clientStorage;
    private final SubscriptionStorageService subscriptionStorage;
    private final TrainingClassService trainingClassService;

    public ClientService(
            ClientStorageService clientStorage,
            SubscriptionStorageService subscriptionStorage,
            TrainingClassService trainingClassService) {
        this.clientStorage = clientStorage;
        this.subscriptionStorage = subscriptionStorage;
        this.trainingClassService = trainingClassService;
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
        // First delete all client's subscriptions
        List<Subscription> subscriptions = getClientSubscriptions(client.getId());
        for (Subscription subscription : subscriptions) {
            subscriptionStorage.delete(subscription.getId());
        }

        clientStorage.delete(client.getId());
    }

    public List<Client> getAllClients() {
        return clientStorage.getAll();
    }

    public Client getClientById(String clientId) {
        return clientStorage.getById(clientId);
    }
}