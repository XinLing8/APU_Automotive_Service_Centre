package logic;

import models.Feedback;
import java.util.List;

public interface IFeedback {
    boolean submitFeedback(String appointmentId, String userId, String feedback);
    boolean deleteFeedback(String date, String time, String appointmentId, String userId);
    boolean updateFeedback(String date, String time, String appointmentId, String userId, String newFeedback);
    
    List<Feedback> getFeedbacksByUser(String currentUserId);
    List<Feedback> getFeedbacksByAppointment(String appointmentId);
    List<Feedback> getAllFeedbacks();
}
