package core.models.actors;

import core.models.base.Employee;

import java.util.ArrayList;
import java.util.List;

public class Trainer extends Employee {
    private List<String> trainingClassIds;

    public Trainer() {
        super();
        this.trainingClassIds = new ArrayList<>();
    }

    public Trainer(String name, String password, String passportId, String phone) {
        super(name, password, passportId, phone);
        this.trainingClassIds = new ArrayList<>();
    }

    public Trainer(String name, String passportId, String phone) {
        super(name, passportId, phone);
        this.trainingClassIds = new ArrayList<>();
    }

    // Getters and Setters
    public List<String> getTrainingClassIds() {
        return trainingClassIds;
    }

    public void setTrainingClassIds(List<String> trainingClassIds) {
        this.trainingClassIds = trainingClassIds;
    }

    public void addTrainingClass(String trainingClassId) {
        this.trainingClassIds.add(trainingClassId);
    }

    public void removeTrainingClass(String trainingClassId){
        this.trainingClassIds.remove(trainingClassId);
    }

    @Override
    public String toString() {
        return getName();
    }
}



