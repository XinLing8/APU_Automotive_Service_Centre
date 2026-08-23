package logic;

import models.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
    
public class ManageUser {
    private static final String FILE_PATH = System.getProperty("user.dir") + "/src/data/users.txt"; 
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        
        File file = new File(FILE_PATH);
        
        if (!file.exists()) {
            System.out.println("users.txt not found. Creating new file...");
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1);
                
                if (data.length < 5) continue;
                
                String userID = data[0];
                
                String fullName = data[1].trim();
                int lastSpace = fullName.lastIndexOf(" ");
                
                String firstName;
                String lastName;
                
                if (lastSpace == -1) {
                    firstName = fullName;
                    lastName = "";
                } else {
                    firstName = fullName.substring(0, lastSpace);
                    lastName = fullName.substring(lastSpace + 1);
                }

                String email = data[2];
                String password = data[3];
                String role = data[4];
                
                users.add(createUserObject(userID, firstName, lastName, email, password, role));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    private User createUserObject(String userID, String firstName, String lastName, String email, String password, String role) {
        switch(role) {
            case "Manager": 
                return new Manager(userID, firstName, lastName, email, password);
            case "Technician": 
                return new Technician(userID, firstName, lastName, email, password);
            case "Counter Staff": 
                return new CounterStaff(userID, firstName, lastName, email, password);
            default:
                return new Customer(userID, firstName, lastName, email, password);
        }
    }
    
    public String generateUserID(String role, List<User> users) {
        int max = 0;
        String prefix = "";
        
        switch (role) {
            case "Customer": 
                prefix = "CUST";
                break;
            case "Technician": 
                prefix = "TECH";
                break;
            case "Counter Staff": 
                prefix = "STAFF";
                break;
            case "Manager": 
                prefix = "MGR";
                break;
        }
        
        Set<String> allIDs = getAllUsedIDs();
        for (String id : allIDs) {
            if (id.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(id.replace(prefix, ""));
                    if (num > max) max = num;
                } catch (Exception e) {
                    continue;
                }
            }
        }
        
        return prefix + String.format("%03d", max + 1);
    }
    
    public void addUser(User user) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(user.toFileString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void updateUser(User updatedUser) {
        List<User> users = getAllUsers();
        
        for (User u : users) {
            if (!u.getUserID().equals(updatedUser.getUserID()) &&
                u.getEmail().equalsIgnoreCase(updatedUser.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User u : users) {
                if (u.getUserID().equals(updatedUser.getUserID())) {
                    bw.write(updatedUser.toFileString());
                } else {
                    bw.write(u.toFileString());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteUser(String userID) {
        List<User> users = getAllUsers();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User u : users) {
                if (!u.getUserID().equals(userID)) {
                    bw.write(u.toFileString());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isEmailExists(String email) {
        List<User> users = getAllUsers();
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean customerExists(String customerID) {
        if (customerID == null || customerID.trim().isEmpty()) {
            return false;
        }

        for (User u : getAllUsers()) {
            if (u instanceof Customer && u.getUserID().equalsIgnoreCase(customerID.trim())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean registerUser(User user) {
        if (user == null || user.getEmail() == null) return false;
        
        if (isEmailExists(user.getEmail())) {
            return false;
        }

        addUser(user);
        return true;
    }
    
    public List<User> searchUsers(String keyword) {
        List<User> result = new ArrayList<>();
        List<User> users = getAllUsers();

        for (User u : users) {
            if (u.getUserID().toLowerCase().contains(keyword.toLowerCase()) ||
                u.getEmail().toLowerCase().contains(keyword.toLowerCase()) ||
                u.getFullName().toLowerCase().contains(keyword.toLowerCase())) {

                result.add(u);
            }
        }
        return result;
    }
    
    public void logDeletedUser(User user, String reason) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(System.getProperty("user.dir") + "/src/data/deleted_users.txt", true))) {

            bw.write(user.toFileString() + 
                    ",DELETED_AT=" + new Date() + 
                    ",Reason=" + reason);
            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Set<String> getAllUsedIDs() {
        Set<String> ids = new HashSet<>();

        for (User u : getAllUsers()) {
            ids.add(u.getUserID());
        }

        File file = new File(System.getProperty("user.dir") + "/src/data/deleted_users.txt");

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length > 0) {
                        ids.add(data[0]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ids;
    }
}
