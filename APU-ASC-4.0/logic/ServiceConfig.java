package logic;

import models.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServiceConfig {
    public static final String TYPE_NORMAL = "Normal";
    public static final String TYPE_MAJOR = "Major";
    
    public static final String SERVICE_OIL_CHANGE = "Oil Change";
    public static final String SERVICE_CAR_WASH = "Car Wash";
    public static final String SERVICE_TIRE_CHECK = "Tire Check";
    public static final String SERVICE_ENGINE_REPAIR = "Engine Repair";
    public static final String SERVICE_TRANSMISSION_FIX = "Transmission Fix";
    public static final String SERVICE_FULL_SERVICES = "Full Services";
    
    private static List<Service> loadServices() {
        return new ServiceManager().getAllServices();
    }

    // Dropdown: to group service by normal services first, then sort alphabetically
    private static List<Service> getOrderedServices() {
        List<Service> services = new ArrayList<>(loadServices());
        services.sort(Comparator
                .comparingInt((Service service) -> getTypeOrder(service.getType()))
                .thenComparing(service -> service.getName().toLowerCase()));
        return services;
    }

    private static int getTypeOrder(String type) {
        if (TYPE_NORMAL.equalsIgnoreCase(type)) {
            return 0;
        }
        if (TYPE_MAJOR.equalsIgnoreCase(type)) {
            return 1;
        }
        return 2;
    }
    
    public static List<String> getNormalServices() {
        List<String> services = new ArrayList<>();
        for (Service service : getOrderedServices()) {
            if (service.getType().equalsIgnoreCase(TYPE_NORMAL)) {
                services.add(service.getName() + " (" + service.getType() + ")");
            }
        }
        return services;
    }
    
    public static List<String> getMajorServices() {
        List<String> services = new ArrayList<>();
        for (Service service : getOrderedServices()) {
            if (service.getType().equalsIgnoreCase(TYPE_MAJOR)) {
                services.add(service.getName() + " (" + service.getType() + ")");
            }
        }
        return services;
    }
    
    public static List<String> getAllServices() {
        List<String> services = new ArrayList<>();
        for (Service service : getOrderedServices()) {
            services.add(service.getName() + " (" + service.getType() + ")");
        }
        return services;
    }
    
    public static double getServicePrice(String serviceName) {
        String normalized = serviceName.trim();
        int idx = normalized.lastIndexOf(" (");
        String name = normalized;
        String type = "";
        if (idx > 0 && normalized.endsWith(")")) {
            name = normalized.substring(0, idx).trim();
            type = normalized.substring(idx + 2, normalized.length() - 1).trim();
        }

        for (Service s : loadServices()) {
            if (s.getName().equalsIgnoreCase(name)) {
                if (type.isEmpty() || s.getType().equalsIgnoreCase(type)) {
                    return s.getPrice();
                }
            }
        }
        return 0.0;
    }
    
    public static String getServicePriceFormatted(String serviceName) {
        return String.format("RM %.2f", getServicePrice(serviceName));
    }
    
    public static String getServiceType(String serviceName) {
        String normalized = serviceName.trim();
        int idx = normalized.lastIndexOf(" (");
        String name = normalized;
        String type = "";
        if (idx > 0 && normalized.endsWith(")")) {
            name = normalized.substring(0, idx).trim();
            type = normalized.substring(idx + 2, normalized.length() - 1).trim();
        }

        for (Service s : loadServices()) {
            if (s.getName().equalsIgnoreCase(name)) {
                if (type.isEmpty() || s.getType().equalsIgnoreCase(type)) {
                    return s.getType();
                }
            }
        }

        if (!type.isEmpty()) {
            return type;
        }
        return TYPE_NORMAL;
    }
    
    public static int getServiceDuration(String serviceName) {
        String serviceType = getServiceType(serviceName);
        if (serviceType.equalsIgnoreCase(TYPE_MAJOR)) {
            return 3;  
        } else {
            return 1;  
        }
    }
}
