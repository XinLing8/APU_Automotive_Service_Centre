package ui.common;

public interface IPayment {
    
    String createPayment(String appointmentID, String customerID, double amount, String method);
    boolean isAlreadyPaid(String appointmentID);
    
}
