package core.models.actors;

import core.models.base.Employee;


public class Manager extends Employee {
    // Default constructor for Gson
    public Manager() {
        super();
    }

    // Constructor for new managers
    public Manager(String name, String password, String passportId, String phone) {
        super(name, password, passportId, phone);
    }

    // Constructor with existing ID
    public Manager(String id, String name, String password, String passportId, String phone) {
        super(id, name, password, passportId, phone);
    }
}
