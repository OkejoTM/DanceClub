package core.models.actors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Client {
    private String id;
    private String name;
    private String passportId;
    private List<String> subscriptionIds;

    // Default constructor for Gson
    public Client() {
        this.subscriptionIds = new ArrayList<>();
    }

    // Constructor for new clients - automatically generates ID
    public Client(String name, String passportId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.passportId = passportId;
        this.subscriptionIds = new ArrayList<>();
    }

    // Constructor with existing ID
    public Client(String id, String name, String passportId) {
        this.id = id;
        this.name = name;
        this.passportId = passportId;
        this.subscriptionIds = new ArrayList<>();
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

    public String getPassportId() {
        return passportId;
    }

    public void setPassportId(String passportId) {
        this.passportId = passportId;
    }

    public List<String> getSubscriptionIds() {
        return subscriptionIds;
    }

    public void setSubscriptionIds(List<String> subscriptionIds) {
        this.subscriptionIds = subscriptionIds;
    }

    public void addSubscription(String subscriptionId) {
        if (this.subscriptionIds == null) {
            this.subscriptionIds = new ArrayList<>();
        }
        this.subscriptionIds.add(subscriptionId);
    }

    @Override
    public String toString() {
        return getName();
    }
}