package logic;

import models.Appointment;

public class PriceConfig {
    
    private static double normalServicePrice = 150.00;  
    private static double majorServicePrice = 350.00;   
    
    public static double getPrice(String serviceType) {
        if (serviceType == null) {
            return 0.0;
        }
        
        if (serviceType.equalsIgnoreCase(Appointment.TYPE_NORMAL)) {
            return normalServicePrice;
        } else if (serviceType.equalsIgnoreCase(Appointment.TYPE_MAJOR)) {
            return majorServicePrice;
        }
        
        return 0.0;
    }
    
    public static double getNormalServicePrice() {
        return normalServicePrice;
    }

    public static double getMajorServicePrice() {
        return majorServicePrice;
    }

    public static void setNormalServicePrice(double price) {
        if (price > 0) {
            normalServicePrice = price;
        }
    }

    public static void setMajorServicePrice(double price) {
        if (price > 0) {
            majorServicePrice = price;
        }
    }

    public static String getPriceFormatted(String serviceType) {
        return String.format("RM %.2f", getPrice(serviceType));
    }
}
