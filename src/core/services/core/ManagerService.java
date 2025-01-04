package core.services.core;

import core.models.Subscription;
import core.models.actors.Client;
import core.models.actors.Trainer;
import core.models.enums.AgeGroup;
import core.models.enums.TrainingLevel;
import core.models.trainings.GroupTraining;
import core.models.trainings.SoloTraining;
import core.services.storage.ManagerStorageService;
import core.services.storage.TrainerStorageService;

import java.time.LocalDate;
import java.util.List;

public class ManagerService {
    private final ManagerStorageService managerStorage;
    private final ClientService clientService;
    private final TrainingClassService trainingClassService;
    private final SubscriptionService subscriptionService;

    public ManagerService(
            ManagerStorageService managerStorage,
            ClientService clientService,
            TrainingClassService trainingClassService,
            SubscriptionService subscriptionService) {
        this.managerStorage = managerStorage;
        this.clientService = clientService;
        this.trainingClassService = trainingClassService;
        this.subscriptionService = subscriptionService;
    }

    // Client management methods (delegating to ClientService)
    public Client createClient(String name, String passportId) {
        return clientService.createClient(name, passportId);
    }

    public void updateClient(Client client) {
        clientService.updateClient(client);
    }

    public void deleteClient(String clientId) {
        clientService.deleteClient(clientId);
    }

    public List<Client> getAllClients(){
        return clientService.getAllClients();
    }

    // Subscription management
    public Subscription createSubscription(
            String clientId,
            String trainingClassId,
            LocalDate startDate,
            LocalDate endDate,
            boolean isPaid) {
        return subscriptionService.createSubscription(
                clientId, trainingClassId, startDate, endDate, isPaid);
    }

    // Training class management
    public GroupTraining createGroupTraining(
            String danceType,
            TrainingLevel level,
            String trainerId,
            AgeGroup ageGroup) {
        return trainingClassService.createGroupTraining(danceType, level, trainerId, ageGroup);
    }

    public SoloTraining createSoloTraining(
            String danceType,
            TrainingLevel level,
            String trainerId,
            String clientId) {
        return trainingClassService.createSoloTraining(danceType, level, trainerId, clientId);
    }

    public void assignTrainerToClass(String classId, String trainerId) {
        trainingClassService.assignTrainerToClass(classId, trainerId);
    }
}