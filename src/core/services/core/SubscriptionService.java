package core.services.core;

import core.models.Subscription;
import core.models.actors.Client;
import core.services.storage.ClientStorageService;
import core.services.storage.SubscriptionStorageService;

import java.time.LocalDate;
import java.util.List;

public class SubscriptionService {
    private final SubscriptionStorageService subscriptionStorage;
    private final ClientStorageService clientStorage;

    public SubscriptionService(
            SubscriptionStorageService subscriptionStorage,
            ClientStorageService clientStorage) {
        this.subscriptionStorage = subscriptionStorage;
        this.clientStorage = clientStorage;
    }

    public Subscription createSubscription(
            String clientId,
            String trainingClassId,
            LocalDate startDate,
            LocalDate endDate,
            boolean isPaid) {

        Subscription subscription = new Subscription(clientId, trainingClassId, startDate, endDate, isPaid);
        subscriptionStorage.save(subscription);

        // Update client's subscription list
        Client client = clientStorage.getById(clientId);
        if (client != null) {
            client.addSubscription(subscription.getId());
            clientStorage.save(client);
        }

        return subscription;
    }

    public void updateSubscription(Subscription subscription) {
        subscriptionStorage.save(subscription);
    }

    public void deleteSubscription(Subscription subscription) {
        if (subscription != null) {
            // Remove subscription from client's list
            Client client = clientStorage.getById(subscription.getClientId());
            if (client != null) {
                client.getSubscriptionIds().remove(subscription.getId());
                clientStorage.save(client);
            }
            subscriptionStorage.delete(subscription.getId());
        }
    }

    public List<Subscription> getAll(){
        return subscriptionStorage.getAll();
    }

    public Subscription getSubscriptionByClientIdAndTrainingClassId(String clientId, String trainingClassId){
        return subscriptionStorage.getAll().stream()
                .filter(subscription -> subscription.getClientId().equals(clientId) &&
                        subscription.getTrainingClassId().equals(trainingClassId))
                .findFirst()
                .orElse(null);
    }

}
