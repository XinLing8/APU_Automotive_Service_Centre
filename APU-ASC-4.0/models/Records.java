package models;

public class Records {
    protected String customerID;
    protected String date;
    protected String status;
    
    public Records(String customerID, String date, String status){
        this.customerID = customerID;
        this.date = date;
        this.status = status;
    }
    
    public String getCustomerID(){
        return customerID;
    }
    
    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }
    
    public String getDate(){
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getStatus(){
        return status;
    }
    
    public void setStatus(String status){
        this.status = status;
    }
}
