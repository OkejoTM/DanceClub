package core.models.base;

import java.util.UUID;

public abstract class Employee {
    private String id;
    private String name;
    private String password;
    private String passportId;
    private String phone;

    protected Employee() {
    }

    protected Employee(String name, String password, String passportId, String phone) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.password = password;
        this.passportId = passportId;
        this.phone = phone;
    }

    protected Employee(String id, String name, String password, String passportId, String phone) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.passportId = passportId;
        this.phone = phone;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassportId() {
        return passportId;
    }

    public void setPassportId(String passportId) {
        this.passportId = passportId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

