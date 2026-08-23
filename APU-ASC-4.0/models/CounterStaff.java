package models;

public class CounterStaff extends User{
    public CounterStaff(String userID, String firstName, String lastName, String email, String password) {
        super(userID, firstName, lastName, email, password, "Counter Staff");
    }
    
    @Override
    public String toFileString() {
        return super.toFileString();
    }
}
