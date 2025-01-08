package core.services.management;

import core.models.Subscription;
import core.models.actors.Client;
import core.models.base.TrainingClass;
import core.services.core.ClientService;
import core.services.core.SubscriptionService;
import core.services.core.TrainingClassService;

import java.util.List;

public class ClientManagementService {

    private final ClientService clientService;
    private final SubscriptionService subscriptionService;
    private final TrainingClassService trainingClassService;

    public ClientManagementService(ClientService clientService, SubscriptionService subscriptionService, TrainingClassService trainingClassService) {
        this.clientService = clientService;
        this.subscriptionService = subscriptionService;
        this.trainingClassService = trainingClassService;
    }

//    public void deleteIfFree(Client client){
//        if (clientIsNotFree(client))
//        {
//            throw new IllegalArgumentException("Delete subscription and training class before deleting client");
//        }
//        clientService.deleteClient(client);
//    }
//
//    private boolean clientIsNotFree(Client client){
//        return trainingClassService.getAllClasses().stream()
//                .anyMatch(trainingClass -> trainingClass.getClientId().equals(client.getId()))
//                ||
//                subscriptionService.getAll().stream()
//                .anyMatch(subscription -> subscription.getClientId().equals(client.getId()));
//    }

    public void deleteClient(Client client){
        List<Subscription> subscriptions = clientService.getClientSubscriptions(client.getId());
        for (Subscription subscription : subscriptions) {
            subscriptionService.deleteSubscription(subscription);
        }

        List<TrainingClass> classes = clientService.getClientClasses(client.getId());
        for (TrainingClass trainingClass : classes)
        {
            trainingClassService.deleteTrainingClass(trainingClass);
        }

        


    }




}
