package core.models.base;

import core.models.enums.TrainingLevel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class TrainingClass {
    private String id;
    private String danceType;
    private TrainingLevel level;
    private String trainerId;
    private List<LocalDate> schedule;

    // Default constructor for Gson
    protected TrainingClass() {
        this.schedule = new ArrayList<>();
    }

    // Constructor for new training classes
    protected TrainingClass(String danceType, TrainingLevel level, String trainerId) {
        this.id = UUID.randomUUID().toString();
        this.danceType = danceType;
        this.level = level;
        this.trainerId = trainerId;
        this.schedule = new ArrayList<>();
    }

    // Constructor with existing ID
    protected TrainingClass(String id, String danceType, TrainingLevel level, String trainerId) {
        this.id = id;
        this.danceType = danceType;
        this.level = level;
        this.trainerId = trainerId;
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
}


