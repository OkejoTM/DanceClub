package core.models;

import java.time.LocalDate;
import java.util.UUID;

public class Passport {
    private String id;
    private String address;
    private LocalDate birthDate;
    private String number;
    private String series;
    private LocalDate overdue;

    // Default constructor for Gson
    public Passport() {
    }

    // Constructor for new passports
    public Passport(String address, LocalDate birthDate, String number, String series, LocalDate overdue) {
        this.id = UUID.randomUUID().toString();
        this.address = address;
        this.birthDate = birthDate;
        this.number = number;
        this.series = series;
        this.overdue = overdue;
    }

    // Constructor with existing ID
    public Passport(String id, String address, LocalDate birthDate, String number, String series, LocalDate overdue) {
        this.id = id;
        this.address = address;
        this.birthDate = birthDate;
        this.number = number;
        this.series = series;
        this.overdue = overdue;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public LocalDate getOverdue() {
        return overdue;
    }

    public void setOverdue(LocalDate overdue) {
        this.overdue = overdue;
    }
}

