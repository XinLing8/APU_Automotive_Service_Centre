package logic;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailUtil {
    private static final String MY_EMAIL = "yingx2lim0522@gmail.com";
    private static final String MY_PASSWORD = "rzum gxmx exie wnho";

    public static void sendAppointmentConfirmation(String toEmail, String customerID, String customerName, String date, String time, String type, String price) {
        String subject = "APU Automotive Service Centre - Appointment Confirmation";
        String body = "Dear Customer (" + customerID + " - " + customerName + "),\n\n" +
                        "Your appointment has been successfully booked!\n\n" +
                        "Appointment Details:\n" +
                        "Service Type  : " + type + "\n" +
                        "Date  : " + date + "\n" +
                        "Time  : " + time + "\n" +
                        "Price : RM " + price + "\n\n" +
                        "Please reach out to the email above if you need further assistance.\n\n" +
                        "Thank you!";
        sendEmail(toEmail, subject, body);
    }
    
    public static void sendEmailOTP(String toEmail, String otp) {
        String subject = "APU Automotive Service Centre - OTP Code";
        String body = "Your OTP code is: " + otp + "\n\n" +
                        "This code will be expired in 3 minutes. \n\n" +
                        "Please reach out to the email above if you need further assistance.\n\n" +
                        "Thank you!";
        sendEmail(toEmail, subject, body);
    }
    
    public static void sendPaymentConfirmation(String toEmail, String customerId, String customerName, String receipt) {
        String subject = "APU Automotive Service Centre - Payment Confirmation";
        String body = "Dear Customer (" + customerId + " - " + customerName + "),\n\n" +
                        "Your appointment has been successfully paid!\n\n" +
                        receipt;
        sendEmail(toEmail, subject, body);
    }
    
    public static void sendAccUpdateConfirmation(String toEmail, String customerID, String customerName) {
        String subject = "APU Automotive Service Centre - Account Update Confirmation";
        String body = "Dear Customer (" + customerID + " - " + customerName + "),\n\n" +
                      "Your account details have been updated by an administrator.\n" +
                      "If you did not request this change, please reach out to the email above immediately.";
        sendEmail(toEmail, subject, body);
    }

    private static void sendEmail(String to, String subject, String body) {
        Session session = Session.getInstance(new Properties() {{
            put("mail.smtp.host", "smtp.gmail.com");
            put("mail.smtp.port", "587");
            put("mail.smtp.auth", "true");
            put("mail.smtp.starttls.enable", "true");
        }}, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MY_EMAIL, MY_PASSWORD);
            }
        });
        
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(MY_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setText(body);
            Transport.send(msg);
        } catch (MessagingException e) {
            System.out.print(e);
        }
    }
}
