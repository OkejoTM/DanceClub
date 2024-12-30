package core.models.trainings;

import core.models.base.TrainingClass;
import core.models.enums.TrainingLevel;


public class SoloTraining extends TrainingClass {
    private String clientId;

    // Default constructor for Gson
    public SoloTraining() {
        super();
    }

    // Constructor for new solo trainings
    public SoloTraining(String danceType, TrainingLevel level, String trainerId, String clientId) {
        super(danceType, level, trainerId);
        this.clientId = clientId;
    }

    // Constructor with existing ID
    public SoloTraining(String id, String danceType, TrainingLevel level, String trainerId, String clientId) {
        super(id, danceType, level, trainerId);
        this.clientId = clientId;
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}

