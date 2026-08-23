package models;

public class Feedback {
    private String date;
    private String time;
    private String appointmentId;
    private String userId;
    private String writtenBy;
    private String feedback;
    
    public Feedback(String date, String time, String appointmentId, String userId, String writtenBy, String feedback) {
        this.date = date;
        this.time = time;
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.writtenBy = writtenBy;
        this.feedback = feedback;
    }
    
    public String getDate() {
        return date;
    }
    
    public String getTime() {
        return time;
    }
    
    public String getAppointmentId() {
        return appointmentId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getWrittenBy() {
        return writtenBy;
    }
    
    public String getFeedback() {
        return feedback;
    }
}
