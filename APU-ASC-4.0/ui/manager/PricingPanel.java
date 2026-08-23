package ui.manager;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import logic.PriceConfig;
import ui.common.StyleConfig;

public class PricingPanel extends JPanel {

    private JComboBox<String> serviceTypeBox;
    private JComboBox<String> serviceBox;
    private JTextField servicePriceField;
    private Map<String, String> serviceTypeMap = new LinkedHashMap<>();
    
    private static final Color HEADER_BG = new Color(60, 72, 107);
    private static final Color PANEL_BG = new Color(245, 247, 250);
    private static final Color FORM_BG = Color.WHITE;
    private static final Color BUTTON_COLOR = StyleConfig.BLUE;
    private static final Color BUTTON_TEXT_COLOR = StyleConfig.WHITE;
    
    public PricingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        initializeServicesFile();
        loadServicesFromFile();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setBackground(HEADER_BG);

        JLabel title = new JLabel("Set Service Prices");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        panel.add(title);
        return panel;
    }

    private JPanel createForm() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBackground(FORM_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panel.add(new JLabel("Service Type:"));
        serviceTypeBox = new JComboBox<>(new String[]{"Normal", "Major"});
        serviceTypeBox.addActionListener(e -> loadServicesByType());
        panel.add(serviceTypeBox);

        panel.add(new JLabel("Service Name:"));
        serviceBox = new JComboBox<>();
        serviceBox.addItem("Others...");
        panel.add(serviceBox);

        panel.add(new JLabel("Service Price (RM):"));
        servicePriceField = new JTextField();
        panel.add(servicePriceField);

        serviceBox.addActionListener(e -> {
            String selected = (String) serviceBox.getSelectedItem();

            if (selected == null) {
                return;
            }

            if (selected.equals("Others...")) {
                SwingUtilities.invokeLater(() -> openAddServiceDialog());
            } else {
                loadSelectedServicePrice();
            }
        });
        
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BG);

        JButton saveBtn = new JButton("Save");
        styleButton(saveBtn);
        saveBtn.addActionListener(e -> savePrices());

        JButton refreshBtn = new JButton("Refresh");
        styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadCurrentPrices());

        JButton updateServiceBtn = new JButton("Update Service Price");
        styleButton(updateServiceBtn);
        updateServiceBtn.addActionListener(e -> updateServicePrice());
        
        panel.add(saveBtn);
        panel.add(refreshBtn);
        panel.add(updateServiceBtn);

        return panel;
    }

    private void styleButton(JButton btn) {
        btn.setBackground(BUTTON_COLOR);
        btn.setForeground(BUTTON_TEXT_COLOR);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }


    private void savePrices() {
        try {
            double price = Double.parseDouble(servicePriceField.getText().trim());

            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be > 0");
                return;
            }
            JOptionPane.showMessageDialog(this, "Prices updated successfully");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format");
        }
    }
    
    private void loadServicesFromFile() {
        serviceTypeMap.clear();
        serviceBox.removeAllItems();
        File file = new File("src/data/services.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 3) {
                    String service = p[0];
                    String type = p[1];
                    serviceTypeMap.put(service, type);
                }
            }
            loadServicesByType();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeServicesFile() {
        File serviceFile = new File("src/data/services.txt");
        if (serviceFile.exists() && serviceFile.length() > 0) return;
        Map<String, String> addedServices = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/data/appointments.txt")); PrintWriter pw = new PrintWriter(new FileWriter(serviceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 9) {
                    String service = p[3];
                    double price = Double.parseDouble(p[8]);
                    String type = (service.contains("Oil") || service.contains("Car") || service.contains("Tire")) ? "Normal" : "Major";
                    if (!addedServices.containsKey(service)) {
                        addedServices.put(service, type);
                        pw.println(service + "," + type + "," + price);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadServicesByType() {
        String type = (String) serviceTypeBox.getSelectedItem();
        serviceBox.removeAllItems();
        for (Map.Entry<String, String> entry : serviceTypeMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(type)) serviceBox.addItem(entry.getKey());
        }
        serviceBox.addItem("Others...");
        loadSelectedServicePrice();
    }

    private void loadCurrentPrices() {
        servicePriceField.setText("");
    }
    
    private void loadSelectedServicePrice() {
        String service = (String) serviceBox.getSelectedItem();
        if (service == null || service.equals("Others...")) {
            servicePriceField.setText("");
            return;
        }
        Double price = getServicePriceFromFile(service);
        servicePriceField.setText(price > 0 ? String.valueOf(price) : "");
    }

    private double getServicePriceFromFile(String service) {
        File file = new File("src/data/services.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 3 && p[0].equals(service)) return Double.parseDouble(p[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private void updateServicePrice() {
        try {
            String service = (String) serviceBox.getSelectedItem();
            if (service == null || "Others...".equals(service)) {
                JOptionPane.showMessageDialog(this, "Please select a valid service");
                return;
            }

            String type = (String) serviceTypeBox.getSelectedItem();
            double price = Double.parseDouble(servicePriceField.getText().trim());
            if (type == null || price <= 0) {
                JOptionPane.showMessageDialog(this, "Invalid input");
                return;
            }

            updateAppointmentFile(service, price);
            updateServicePriceFile(service, type, price);
            
            JOptionPane.showMessageDialog(this, "Service price updated");
            initializeServicesFile();
            loadServicesFromFile();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating service");
        }
    }

    private void updateAppointmentFile(String service, double newPrice) {
        File file = new File("src/data/appointments.txt");
        List<String> updated = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 9 && p[3].equals(service)) {
                    p[8] = String.valueOf(newPrice);
                    line = String.join(",", p);
                }
                updated.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String l : updated) pw.println(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateServicePriceFile(String service, String type, double newPrice) {
        File file = new File("src/data/services.txt");
        List<String> updated = new ArrayList<>();
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 3 && p[0].equals(service)) {
                    p[1] = type;
                    p[2] = String.valueOf(newPrice);
                    line = String.join(",", p);
                    found = true;
                }
                updated.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!found) {
            updated.add(service + "," + type + "," + newPrice);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (String l : updated) pw.println(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void openAddServiceDialog() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Service Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Price (RM):"));
        panel.add(priceField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Service", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String type = (String) serviceTypeBox.getSelectedItem();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Service name cannot be empty");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price");
            return;
        }

        if (price <= 0) {
            JOptionPane.showMessageDialog(this, "Invalid price");
            return;
        }
        if (serviceTypeMap.containsKey(name)) {
            JOptionPane.showMessageDialog(this, "Service already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        serviceTypeMap.put(name, type);
        saveNewServiceToFile(name, type, price);
        loadServicesByType();
        serviceBox.setSelectedItem(name);
        JOptionPane.showMessageDialog(this, "Service added successfully!");
        initializeServicesFile();
        loadServicesFromFile();
    }
    
    private void saveNewServiceToFile(String name, String type, double price) {
        File file = new File("src/data/services.txt");
        try (FileWriter fw = new FileWriter(file, true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter pw = new PrintWriter(bw)) {
            pw.println(name + "," + type + "," + price);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
