package logic;

import models.Service;
import java.io.*;
import java.util.*;

public class ServiceManager {

    private static final String FILE_PATH = "src/data/services.txt";
    private List<Service> services = new ArrayList<>();

    public ServiceManager() {
        loadServices();
    }

    public final void loadServices() {
        services.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                services.add(new Service(
                        p[0],
                        p[1],
                        Double.parseDouble(p[2])
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<Service> getAllServices() {
        return services;
    }

    public List<Service> getByType(String type) {
        List<Service> list = new ArrayList<>();
        for (Service s : services) {
            if (s.getType().equalsIgnoreCase(type)) {
                list.add(s);
            }
        }
        return list;
    }

    public Service getByName(String name) {
        for (Service s : services) {
            if (s.getName().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    public void updatePrice(String name, double newPrice) {
        Service s = getByName(name);
        if (s != null) {
            s.setPrice(newPrice);
            saveToFile();
        }
    }

    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Service s : services) {
                pw.println(s.getName() + "," + s.getType() + "," + s.getPrice());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}