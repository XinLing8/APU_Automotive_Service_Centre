package logic;

import models.User;
import java.util.List;

public class UserAuthentication {
    private ManageUser manageUser;
    private String currentOTP;
    private long otpExpiryTime;
    
    public UserAuthentication() {
        this.manageUser = new ManageUser();
    }

    public User login(String userID, String password) {
        List<User> users = manageUser.getAllUsers();
        
        String hashedInput = HashUtil.hash(password);

        for (User u : users) {
            if (u.getUserID().equalsIgnoreCase(userID.trim()) &&
                    u.getPassword().equals(hashedInput)) {
                return u;
            }
        }
        return null;
    }

    public String generateOTP() {
        currentOTP = String.valueOf((int)(Math.random() * 900000) + 100000);
        otpExpiryTime = System.currentTimeMillis() + (3 * 60 * 1000);
        return currentOTP;
    }
    
    public boolean verifyOTP(String inputOTP) {
        if (currentOTP == null) {
            return false;
        }
        if (System.currentTimeMillis() > otpExpiryTime) {
            currentOTP = null;
            return false;
        }
        return currentOTP.equals(inputOTP.trim());
    }
    
    public void sendOTP(String toEmail) {
        String otp = generateOTP();
        System.out.println(otp);
//        new Thread(() -> 
//                EmailUtil.sendEmailOTP(toEmail, otp)).start();
    }
    
    public boolean isOTPExpired() {
        return System.currentTimeMillis() > otpExpiryTime;
    }
}