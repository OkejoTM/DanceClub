package core.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Subscription {
    private String id;
    private String clientId;
    private String trainingClassId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isPaid;
    private List<LocalDate> attendanceDates;

    // Default constructor for Gson
    public Subscription() {
        this.attendanceDates = new ArrayList<>();
    }

    // Constructor for new subscriptions
    public Subscription(String clientId, String trainingClassId, LocalDate startDate, LocalDate endDate, boolean isPaid) {
        this.id = UUID.randomUUID().toString();
        this.clientId = clientId;
        this.trainingClassId = trainingClassId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isPaid = isPaid;
        this.attendanceDates = new ArrayList<>();
    }

    // Constructor with existing ID
    public Subscription(String id, String clientId, String trainingClassId, LocalDate startDate, LocalDate endDate, boolean isPaid) {
        this.id = id;
        this.clientId = clientId;
        this.trainingClassId = trainingClassId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isPaid = isPaid;
        this.attendanceDates = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTrainingClassId() {
        return trainingClassId;
    }

    public void setTrainingClassId(String trainingClassId) {
        this.trainingClassId = trainingClassId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public List<LocalDate> getAttendanceDates() {
        return attendanceDates;
    }

    public void setAttendanceDates(List<LocalDate> attendanceDates) {
        this.attendanceDates = attendanceDates;
    }

    public void addAttendanceDate(LocalDate date) {
        this.attendanceDates.add(date);
    }
}
