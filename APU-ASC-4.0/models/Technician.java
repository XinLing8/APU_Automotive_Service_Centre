package models;

public class Technician extends User{
    public Technician(String userID, String firstName, String lastName, String email, String password) {
        super(userID, firstName, lastName, email, password, "Technician");
    }
    
    @Override
    public String toFileString() {
        return super.toFileString();
    }
}

