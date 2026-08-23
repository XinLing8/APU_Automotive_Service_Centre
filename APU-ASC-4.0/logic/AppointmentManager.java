package logic;

import models.Appointment;
import models.Service;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AppointmentManager {
    private static final String DATA_FILE = "src/data/appointments.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    
    // Working hours: First slot 9:30 AM, Last slot 6:30 PM 
    private static final LocalTime FIRST_SLOT = LocalTime.of(9, 30);
    private static final LocalTime LAST_SLOT = LocalTime.of(18, 30);
    private static final int SLOT_DURATION_MINUTES = 30;
    
    private List<Appointment> appointments;
    
    public AppointmentManager() {
        this.appointments = new ArrayList<>();
        loadAppointments();
    }
    
    private void loadAppointments() {
        appointments.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Appointment apt = parseAppointmentLine(line);
                    if (apt != null) {
                        appointments.add(apt);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Appointments file not found: " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Error loading appointments: " + e.getMessage());
        }
    }
    
    public void refreshAppointments() {
        loadAppointments();
    }
    
    private void saveAppointments() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Appointment apt : appointments) {
                writer.write(apt.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving appointments: " + e.getMessage());
        }
    }
    
    private Appointment parseAppointmentLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length >= 9) {
                return new Appointment(
                        parts[0].trim(), 
                        parts[1].trim(), 
                        parts[2].trim(), 
                        parts[3].trim(), 
                        parts[4].trim(), 
                        parts[5].trim(), 
                        parts[6].trim(), 
                        Double.parseDouble(parts[8].trim()) 
                );
            } 
            else if (parts.length == 8) {
                return new Appointment(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        parts[4].trim(),
                        parts[5].trim(),
                        parts[6].trim(),
                        Double.parseDouble(parts[7].trim())
                );
            }
        } catch (Exception e) {
            System.err.println("Error parsing appointment line: " + line);
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean createAppointment(String customerID, String serviceType, String date, String time, double totalPrice, String technicianID) {
        String appointmentID = generateNextAppointmentID();
        
        if (!validateAppointmentInput(appointmentID, customerID, serviceType, date, time, totalPrice)) {
            return false;
        }
        
        Appointment apt = new Appointment(appointmentID, customerID, technicianID, serviceType, date, time, Appointment.STATUS_PENDING, totalPrice);
        appointments.add(apt);
        saveAppointments();
        
        return true;
    }
    
    public List<Appointment> getAllAppointments() {
        return new ArrayList<>(appointments);
    }
    
    public Appointment getAppointmentById(String appointmentID) {
        for (Appointment apt : appointments) {
            if (apt.getAppointmentID().equals(appointmentID)) {
                return apt;
            }
        }
        return null;
    }
    
    public List<Appointment> getAppointmentsByCustomer(String customerID) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment apt : appointments) {
            if (apt.getCustomerID().equals(customerID)) {
                result.add(apt);
            }
        }
        return result;
    }
    
    public List<Appointment> getAppointmentsByTechnician(String technicianID) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment apt : appointments) {
            if (apt.getTechnicianID().equals(technicianID)) {
                result.add(apt);
            }
        }
        return result;
    }
    
    public boolean updateAppointmentStatus(String appointmentID, String newStatus) {
        Appointment apt = getAppointmentById(appointmentID);
        if (apt == null) {
            System.err.println("Appointment not found: " + appointmentID);
            return false;
        }
        apt.setStatus(newStatus);
        saveAppointments();
        return true;
    }
 
    public boolean saveChanges(Appointment updatedAppointment) {
        if (updatedAppointment == null) {
            System.err.println("Cannot save null appointment");
            return false;
        }
        Appointment existing = getAppointmentById(updatedAppointment.getAppointmentID());
        if (existing == null) {
            System.err.println("Appointment not found: " + updatedAppointment.getAppointmentID());
            return false;
        }
        existing.setCustomerID(updatedAppointment.getCustomerID());
        existing.setTechnicianID(updatedAppointment.getTechnicianID());
        existing.setServiceType(updatedAppointment.getServiceType());
        existing.setDate(updatedAppointment.getDate());
        existing.setTime(updatedAppointment.getTime());
        existing.setStatus(updatedAppointment.getStatus());
        existing.setTotalPrice(updatedAppointment.getTotalPrice());
        
        saveAppointments();
        return true;
    }
    
    public boolean assignTechnician(String appointmentID, String technicianID) {
        Appointment apt = getAppointmentById(appointmentID);
        if (apt == null) {
            System.err.println("Appointment not found: " + appointmentID);
            return false;
        }
        if (!isTechnicianAvailable(technicianID, apt.getDate(), apt.getTime(), apt.getServiceDuration())) {
            System.err.println("Technician " + technicianID + " is not available at " + apt.getTime());
            return false;
        }
        apt.setTechnicianID(technicianID);
        apt.setStatus(Appointment.STATUS_PENDING);
        saveAppointments();
        return true;
    }
    
    public boolean deleteAppointment(String appointmentID) {
        Appointment apt = getAppointmentById(appointmentID);
        if (apt == null) {
            System.err.println("Appointment not found: " + appointmentID);
            return false;
        }
        appointments.remove(apt);
        saveAppointments();
        return true;
    }
    
    public boolean isTechnicianAvailable(String technicianID, String date, String time, int durationHours) {
        try {
            LocalTime startTime = LocalTime.parse(time, TIME_FORMAT);
            LocalTime endTime = startTime.plusHours(durationHours);
            for (Appointment apt : getAppointmentsByTechnician(technicianID)) {
                if (!apt.getDate().equals(date)) {
                    continue; 
                }
                LocalTime aptStartTime = LocalTime.parse(apt.getTime(), TIME_FORMAT);
                LocalTime aptEndTime = aptStartTime.plusHours(apt.getServiceDuration());
                if (timeOverlaps(startTime, endTime, aptStartTime, aptEndTime)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error checking technician availability: " + e.getMessage());
            return false;
        }
    }
    
    private boolean timeOverlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }
    
    public List<String> getAllUniqueTechnicians() {
        Set<String> uniqueTechs = new LinkedHashSet<>();
        for (Appointment apt : appointments) {
            String techID = apt.getTechnicianID();
            if (techID != null && !techID.equals("Unassigned") && !techID.isEmpty()) {
                uniqueTechs.add(techID);
            }
        }
        return new ArrayList<>(uniqueTechs);
    }
    
    public List<String> getAllTechniciansFromFile() {
        List<String> technicians = new ArrayList<>();
        String usersFile = "src/data/users.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;  
                }
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String role = parts[4].trim();
                    if (role.equalsIgnoreCase("Technician")) {
                        String techID = parts[0].trim();
                        if (!techID.isEmpty()) {
                            technicians.add(techID);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Users file not found: " + usersFile);
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }
        return technicians;
    }
    
    public Map<String, String> getTechniciansWithNames() {
        Map<String, String> techniciansMap = new LinkedHashMap<>();
        String usersFile = "src/data/users.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String role = parts[4].trim();
                    if (role.equalsIgnoreCase("Technician")) {
                        String techID = parts[0].trim();
                        String techName = parts[1].trim();
                        if (!techID.isEmpty() && !techName.isEmpty()) {
                            techniciansMap.put(techID, techID + " - " + techName);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Users file not found: " + usersFile);
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }
        return techniciansMap;
    }
    
    public Map<String, String> getCustomersWithNames() {
        Map<String, String> customersMap = new LinkedHashMap<>();
        String usersFile = "src/data/users.txt";
        
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String role = parts[4].trim();
                    if (role.equalsIgnoreCase("Customer")) {
                        String custID = parts[0].trim();
                        String custName = parts[1].trim();
                        if (!custID.isEmpty() && !custName.isEmpty()) {
                            customersMap.put(custID, custID + " - " + custName);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Users file not found: " + usersFile);
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }
        return customersMap;
    }
    
    public Map<String, String> getAvailableTechniciansWithNames(String date, String time, int durationHours) {
        Map<String, String> availableTechsMap = new LinkedHashMap<>();
        List<String> allTechs = getAllTechniciansFromFile();
        Map<String, String> allTechsWithNames = getTechniciansWithNames();
        
        for (String techID : allTechs) {
            if (isTechnicianAvailable(techID, date, time, durationHours)) {
                availableTechsMap.put(techID, allTechsWithNames.get(techID));
            }
        }
        return availableTechsMap;
    }
    
    public List<String> getAvailableTechnicians(String date, String time, int durationHours) {
        List<String> availableTechs = new ArrayList<>();
        List<String> allTechs = getAllTechniciansFromFile();  
        
        for (String techID : allTechs) {
            if (isTechnicianAvailable(techID, date, time, durationHours)) {
                availableTechs.add(techID);
            }
        }
        return availableTechs;
    }
    
    public List<String> getAvailableSlots(String date, String serviceType) {
        List<String> availableSlots = new ArrayList<>();
        int durationHours = (serviceType.equalsIgnoreCase(Appointment.TYPE_NORMAL)) ? 1 : 3;
        
        try {
            LocalTime currentTime = FIRST_SLOT;
            while (currentTime.isBefore(LAST_SLOT)) {
                LocalTime endTime = currentTime.plusHours(durationHours);
                
                if (endTime.isAfter(LAST_SLOT)) {
                    break;
                }
                availableSlots.add(currentTime.format(TIME_FORMAT));
                currentTime = currentTime.plusMinutes(SLOT_DURATION_MINUTES);
            }
        } catch (Exception e) {
            System.err.println("Error generating slots: " + e.getMessage());
        }
        return availableSlots;
    }
    
    public String generateNextAppointmentID() {
        if (appointments.isEmpty()) {
            return "APP0001";
        }
        int maxNumber = 0;
        for (Appointment apt : appointments) {
            String id = apt.getAppointmentID();
            if (id.startsWith("APP")) {
                try {
                    int number = Integer.parseInt(id.substring(3));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return String.format("APP%04d", maxNumber + 1);
    }

    private boolean validateAppointmentInput(String appointmentID, String customerID, String serviceType, String date, String time, double totalPrice) {
        if (appointmentID == null || appointmentID.isEmpty() || customerID == null || customerID.isEmpty() || serviceType == null || serviceType.isEmpty() ||
            date == null || date.isEmpty() || time == null || time.isEmpty()) {
            System.err.println("Please fill up all fields.");
            return false;
        }
        
        if (getAppointmentById(appointmentID) != null) {
            System.err.println("Appointment ID already exists");
            return false;
        }
        
        if (ServiceConfig.getServicePrice(serviceType) <= 0) {
            System.err.println("Invalid service");
            return false;
        }

        try {
            LocalDate appointmentDate = LocalDate.parse(date, DATE_FORMAT);
            LocalDate today = LocalDate.now();

            if (appointmentDate.isBefore(today)) {
                System.err.println("Cannot book appointment in the past");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Invalid date format. Use YYYY-MM-DD");
            return false;
        }
        try {
            LocalTime appointmentTime = LocalTime.parse(time, TIME_FORMAT);
            if (appointmentTime.isBefore(FIRST_SLOT) || appointmentTime.isAfter(LAST_SLOT)) {
                System.err.println("Please select a time within working hours (9:30-18:30)");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Invalid time format. Use HH:mm");
            return false;
        }
        if (totalPrice <= 0) {
            System.err.println("Total price must be greater than 0");
            return false;
        }
        return true;
    }
    
    public void refreshAppointmentPrices(ServiceManager sm) {
        List<Appointment> list = getAllAppointments();
        for (Appointment a : list) {
            Service s = sm.getByName(a.getServiceType());
            if (s != null) {
                a.setTotalPrice(s.getPrice());
            }
        }
        saveAppointments(); 
    }
    
    public void reload() {
        loadAppointments();
    }
}
