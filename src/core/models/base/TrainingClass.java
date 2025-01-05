package core.models.base;

import core.models.enums.TrainingLevel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrainingClass {
    private String id;
    private String danceType;
    private TrainingLevel level;
    private String trainerId;
    private String clientId;
    private List<LocalDate> schedule;

    // Default constructor for Gson
    public TrainingClass() {
        this.schedule = new ArrayList<>();
    }

    // Constructor for new training classes
    public TrainingClass(String danceType, TrainingLevel level, String trainerId, String clientId) {
        this.id = UUID.randomUUID().toString();
        this.danceType = danceType;
        this.level = level;
        this.trainerId = trainerId;
        this.clientId = clientId;
        this.schedule = new ArrayList<>();
    }

    // Constructor with existing ID
    public TrainingClass(String id, String danceType, TrainingLevel level, String trainerId, String clientId) {
        this.id = id;
        this.danceType = danceType;
        this.level = level;
        this.trainerId = trainerId;
        this.clientId = clientId;
        this.schedule = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDanceType() {
        return danceType;
    }

    public void setDanceType(String danceType) {
        this.danceType = danceType;
    }

    public TrainingLevel getLevel() {
        return level;
    }

    public void setLevel(TrainingLevel level) {
        this.level = level;
    }

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        this.trainerId = trainerId;
    }

    public List<LocalDate> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<LocalDate> schedule) {
        this.schedule = schedule;
    }

    public void addScheduleDate(LocalDate date) {
        this.schedule.add(date);
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}


