package models;

public class User {
    private String userID;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    
    public User(String userID, String firstName, String lastName, String email, String password, String role) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }
    
    public void setUserID(String userID) {
        this.userID = userID;
    }
    
    public String getUserID() {
        return userID;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getRole() {
        return role;
    }
    
    public String toFileString() {
        return userID + "," + getFullName() + "," + email + "," + password + "," + role;
    }
}