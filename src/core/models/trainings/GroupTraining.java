package core.models.trainings;

import core.models.base.TrainingClass;
import core.models.enums.AgeGroup;
import core.models.enums.TrainingLevel;

import java.util.ArrayList;
import java.util.List;

public class GroupTraining extends TrainingClass {
    private AgeGroup ageGroup;
    private List<String> clientIds;

    // Default constructor for Gson
    public GroupTraining() {
        super();
        this.clientIds = new ArrayList<>();
    }

    // Constructor for new group trainings
    public GroupTraining(String danceType, TrainingLevel level, String trainerId, AgeGroup ageGroup) {
        super(danceType, level, trainerId);
        this.ageGroup = ageGroup;
        this.clientIds = new ArrayList<>();
    }

    // Constructor with existing ID
    public GroupTraining(String id, String danceType, TrainingLevel level, String trainerId, AgeGroup ageGroup) {
        super(id, danceType, level, trainerId);
        this.ageGroup = ageGroup;
        this.clientIds = new ArrayList<>();
    }

    // Getters and Setters
    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(AgeGroup ageGroup) {
        this.ageGroup = ageGroup;
    }

    public List<String> getClientIds() {
        return clientIds;
    }

    public void setClientIds(List<String> clientIds) {
        this.clientIds = clientIds;
    }

    public void addClient(String clientId) {
        this.clientIds.add(clientId);
    }
}


