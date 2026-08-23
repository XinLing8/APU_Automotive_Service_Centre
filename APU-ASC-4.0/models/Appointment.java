package models;

import logic.ServiceConfig;

public class Appointment extends Records{
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String TYPE_NORMAL = "Normal";
    public static final String TYPE_MAJOR = "Major";

    private String appointmentID; 
    private String technicianID;  
    private String serviceType;   
    private String time;
    private double totalPrice;    

    public int getServiceDuration() {
        return ServiceConfig.getServiceDuration(serviceType);
    }

    public Appointment(String appointmentID, String customerID, String technicianID, String serviceType, String date, String time, String status, double totalPrice) {
        super(customerID, date, status);
        
        this.appointmentID = appointmentID;
        this.technicianID = technicianID;
        this.serviceType = serviceType;
        this.time = time;
        this.totalPrice = totalPrice;
    }

    public String getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(String appointmentID) {
        this.appointmentID = appointmentID;
    }

    public String getTechnicianID() {
        return technicianID;
    }

    public void setTechnicianID(String technicianID) {
        this.technicianID = technicianID;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return appointmentID + "," + customerID + "," + technicianID + "," + serviceType + "," + date + "," + time + "," + status  + "," + totalPrice;
    }
}
