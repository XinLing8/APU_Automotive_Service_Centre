package logic;

import models.Feedback;
import java.io.*;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalTime;

public class FeedbackManager implements IFeedback {
    private static final String FEEDBACKS_FILE = "src/data/feedbacks.txt";
    private static final String APPOINTMENTS_FILE = "src/data/appointments.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public FeedbackManager() {
        
    }
    
    @Override
    public boolean submitFeedback(String appointmentID, String userID, String feedback) {
        if (appointmentID == null || appointmentID.isEmpty() || userID == null || userID.isEmpty()) {
            System.err.println("FeedbackManager: Missing AppointmentId or CustomerId when submitting feedback.");
            return false;
        }
        
        if (!isAppointmentCompleted(appointmentID)) {
            System.err.println(
                    "FeedbackManager: Appointment " + appointmentID + " is not completed yet.");
            return false;
        }
        
        if (userID.startsWith("CUST")) {
            if (!isUserLinkedToAppointment(appointmentID, userID)) {
                System.err.println(
                        "FeedbackManager: Appointment " + appointmentID + " does not belong to customer " + userID);
                return false;
            }
        } else if (userID.startsWith("TECH")) {
            if (!isUserLinkedToAppointment(appointmentID, userID)) {
                System.err.println(
                        "FeedbackManager: Appointment " + appointmentID + " is not assigned to technician " + userID);
                return false;
            }
        } else {
            System.err.println("FeedbackManager: Unrecognized user ID - " + userID);
            return false;
        }
        
        String date = LocalDate.now().format(DATE_FORMAT);
        String time = LocalTime.now().format(TIME_FORMAT);
        String newRecord = date + ", " + time + ", " + appointmentID + ", " + userID + ", " + feedback.trim();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FEEDBACKS_FILE, true))) {
            writer.write(newRecord);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("FeedbackManager: Error in writing feedback: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteFeedback(String date, String time, String appointmentID, String userID) {
        File feedbackFile = new File(FEEDBACKS_FILE);
        List<String> lines = new ArrayList<>();
        
        boolean feedbackFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(feedbackFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                
                if (parts.length >= 4 &&
                    parts[0].trim().equals(date) &&
                    parts[1].trim().equals(time) &&
                    parts[2].trim().equals(appointmentID) &&
                    parts[3].trim().equals(userID)) {
                    feedbackFound = true;
                    
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (!feedbackFound) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(feedbackFile))) {
            for (String ln : lines) {
                writer.write(ln);
                writer.newLine();
            }
            
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean updateFeedback(String date, String time, String appointmentID, String userID, String newFeedback) {
        File feedbackFile = new File(FEEDBACKS_FILE);
        List<String> lines = new ArrayList<>();
        
        boolean feedbackFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(feedbackFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                
                if (parts.length >= 4 &&
                    parts[0].trim().equals(date) &&
                    parts[1].trim().equals(time) &&
                    parts[2].trim().equals(appointmentID) &&
                    parts[3].trim().equals(userID)) {
                    lines.add(date + ", " + time + ", " + appointmentID + ", " + userID + ", " + newFeedback.trim());
                    feedbackFound = true;
                    
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (!feedbackFound) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(feedbackFile))) {
            for (String ln : lines) {
                writer.write(ln);
                writer.newLine();
            }
            
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
    @Override
    public List<Feedback> getFeedbacksByUser(String currentUserID) {
        List<Feedback> matchedFeedbacks = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(FEEDBACKS_FILE))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String appointmentID = parts[2].trim();
                    String userID = parts[3].trim();

                    String userRole;
                    if (userID.startsWith("CUST")) {
                        userRole = "Customer";
                    } else if (userID.startsWith("TECH")) {
                        userRole = "Technician";
                    } else {
                        userRole = "Unknown User";
                    }

                    StringBuilder feedbackBuilder = new StringBuilder();
                    for (int i = 4; i < parts.length; i++) {
                        if (i > 4) feedbackBuilder.append(",");
                        feedbackBuilder.append(parts[i]);
                    }
                    String feedback = feedbackBuilder.toString().trim();
                    boolean hasFeedback = !feedback.equalsIgnoreCase("No feedback yet") && !feedback.isEmpty();

                    boolean isCurrentUserFeedback = userID.equals(currentUserID);
                    boolean isOtherPartyFeedback;

                    if (currentUserID.startsWith("CUST")) {
                        isOtherPartyFeedback = userRole.equals("Technician") && isUserLinkedToAppointment(appointmentID, currentUserID);
                    } else if (currentUserID.startsWith("TECH")) {
                        isOtherPartyFeedback = userRole.equals("Customer") && isUserLinkedToAppointment(appointmentID, currentUserID);
                    } else {
                        isOtherPartyFeedback = false;
                    }

                    if ((isCurrentUserFeedback || isOtherPartyFeedback) && hasFeedback) {
                        matchedFeedbacks.add(buildFeedback(line));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Feedback Manager: Error Reading File: " + e.getMessage());
        }
        
        return matchedFeedbacks;
    }
    
    @Override
    public List<Feedback> getFeedbacksByAppointment(String appointmentID) {
        List<Feedback> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FEEDBACKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[2].trim().equals(appointmentID)) {
                    result.add(buildFeedback(line));
                }
            }
        } catch (FileNotFoundException e) {
            return result;
        } catch (IOException e) {
            System.err.println("FeedbackManager: Error reading feedbacks: " + e.getMessage());
        }
        return result;
    }
    
    @Override
    public List<Feedback> getAllFeedbacks() {
        List<Feedback> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FEEDBACKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result.add(buildFeedback(line));
                }
            }
        } catch (FileNotFoundException e) {
            return result;
        } catch (IOException e) {
            System.err.println("FeedbackManager: Error reading feedbacks: " + e.getMessage());
        }
        return result;
    }
    
    public void deleteFeedbacksByAppointment(String appointmentID) {
        File file = new File(FEEDBACKS_FILE);
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && !parts[2].trim().equals(appointmentID)) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("FeedbackManager: Error reading feedbacks: " + e.getMessage());
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String ln : lines) {
                writer.write(ln);
                writer.newLine();
            }
            
        } catch (IOException e) {
            System.err.println("FeedbackManager: Error writing feedbacks: " + e.getMessage());
        }
    }
    
    private boolean isAppointmentCompleted(String appointmentID) {
        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String parts[] = line.split(",");
                if (parts.length >= 7 && parts[0].trim().equals(appointmentID)) {
                    String apptStatus = parts[6].trim();
                    return apptStatus.equalsIgnoreCase("Completed");
                }
            }
        } catch (IOException e) {
            System.err.println("FeedbackManager: Error in reading appointment: " + e.getMessage());
        }
        return false;
    }
    
    private boolean isUserLinkedToAppointment(String appointmentID, String userID) {
        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].trim().equals(appointmentID)) {
                    if (userID.startsWith("CUST")) {
                        return parts[1].trim().equals(userID);
                    } else if (userID.startsWith("TECH")) {
                        return parts[2].trim().equals(userID);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("FeedbackManager: Error Reading Appointments: " + e.getMessage());
        }
        return false;
    }
    
    private Feedback buildFeedback(String line) {
        String[] parts = line.split(",");

        String date = parts[0].trim();
        String time = parts[1].trim();
        String appointmentID = parts[2].trim();
        String userID = parts[3].trim();

        String userRole;
        if (userID.startsWith("CUST")) {
            userRole = "Customer";
        } else if (userID.startsWith("TECH")) {
            userRole = "Technician";
        } else {
            userRole = "Unknown";
        }

        // Feedback text is everything from index 4 onward
        StringBuilder feedbackBuilder = new StringBuilder();
        for (int i = 4; i < parts.length; i++) {
            if (i > 4) feedbackBuilder.append(",");
            feedbackBuilder.append(parts[i]);
        }
        String feedback = feedbackBuilder.toString().trim();

        return new Feedback(date, time, appointmentID, userID, userRole, feedback);
    }
}