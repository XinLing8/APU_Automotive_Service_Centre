package logic;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class ValidationHelper {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$"); 
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Z]{4}\\d{3,6}$");
    
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (isNullOrEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
    
    public static boolean isValidDateFormat(String date) {
        if (isNullOrEmpty(date)) {
            return false;
        }
        try {
            LocalDate.parse(date, DATE_FORMAT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isFutureDate(String date) {
        if (!isValidDateFormat(date)) {
            return false;
        }
        try {
            LocalDate appointmentDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate today = LocalDate.now();
            return appointmentDate.isAfter(today);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isTodayOrLater(String date) {
        if (!isValidDateFormat(date)) {
            return false;
        }
        try {
            LocalDate appointmentDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate today = LocalDate.now();
            return appointmentDate.isEqual(today) || appointmentDate.isAfter(today);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidTimeFormat(String time) {
        if (isNullOrEmpty(time)) {
            return false;
        }
        try {
            LocalTime.parse(time, TIME_FORMAT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isValidTimeRange(String time) {
        if (!isValidTimeFormat(time)) {
            return false;
        }
        try {
            LocalTime appointmentTime = LocalTime.parse(time, TIME_FORMAT);
            LocalTime firstSlot = LocalTime.of(9, 30);
            LocalTime lastSlot = LocalTime.of(18, 30);
            return !appointmentTime.isBefore(firstSlot) && !appointmentTime.isAfter(lastSlot);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isValidPositiveNumber(String number) {
        if (isNullOrEmpty(number)) {
            return false;
        }
        try {
            double value = Double.parseDouble(number);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidPositiveInteger(String number) {
        if (isNullOrEmpty(number)) {
            return false;
        }
        try {
            int value = Integer.parseInt(number);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidUserID(String userID) {
        if (isNullOrEmpty(userID)) {
            return false;
        }
        return ID_PATTERN.matcher(userID).matches();
    }
    
    public static boolean isAlphanumeric(String value) {
        if (isNullOrEmpty(value)) {
            return false;
        }
        return value.matches("^[a-zA-Z0-9 ]+$");
    }
    
    public static boolean isValidPassword(String password) {
        if (isNullOrEmpty(password)) {
            return false;
        }
        return password.length() >= 6;
    }
    
    public static boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$");
    }
    
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return minLength == 0;
        }
        int length = value.length();
        return length >= minLength && length <= maxLength;
    }
    
    public static boolean isValidID(String id) {
        return isValidUserID(id);
    }

    public static boolean isValidDate(String date) {
        return isTodayOrLater(date);
    }
    
    public static boolean isValidTime(String time) {
        return isValidTimeFormat(time);
    }
    
    public static boolean isValidPrice(String price) {
        return isValidPositiveNumber(price);
    }
    
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
