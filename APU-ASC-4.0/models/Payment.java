package models;

public class Payment extends Records{
    private String paymentID;
    private String appointmentID;
    private double amount;
    private String method; 

    public static final String STATUS_COMPLETED = "Paid";
    public static final String STATUS_PENDING = "Pending";
    
    public Payment(String paymentID, String appointmentID, String customerID, double amount, String status, String date, String method) {
        super(customerID, date, status);
        
        this.paymentID = paymentID;
        this.appointmentID = appointmentID;
        this.amount = amount;
        this.method = method;
    }

    public String getPaymentID() { 
        return paymentID; 
    }
    
    public void setPaymentID(String paymentID){
        this.paymentID = paymentID;
    }
    
    public String getAppointmentID() { 
        return appointmentID; 
    }
   
    public void setAppointmentID(String appointmentID){
        this.appointmentID = appointmentID;
    }
    
    public double getAmount() { 
        return amount; 
    }
    
    public void setAmount(double amount){
        this.amount = amount;
    }    
    
    public String getMethod() { 
        return method; 
    }
    
    public void setMethod(String method) { 
        this.method = method; 
    }
    
    @Override
    public String toString() {
        return paymentID + "," + appointmentID + "," + customerID + "," +
               amount + "," + status + "," + date + "," + method;
    }
}
