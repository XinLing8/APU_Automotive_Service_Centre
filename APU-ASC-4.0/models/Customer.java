package models;

public class Customer extends User{
    public Customer(String userID, String firstName, String lastName, String email, String password) {
        super(userID, firstName, lastName, email, password, "Customer");
    }
    
    @Override
    public String toFileString() {
        return super.toFileString();
    }
}
