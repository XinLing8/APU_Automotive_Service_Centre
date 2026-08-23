package models;

public class Manager extends User {
    public Manager (String userID, String firstName, String lastName, String email, String password) {
        super(userID, firstName, lastName, email, password, "Manager");
    }
    
    @Override
    public String toFileString() {
        return super.toFileString();
    }
}
