package ui.common;

import javax.swing.*;
import ui.manager.ManagerDashboard;
import ui.counterstaff.CounterStaffDashboard;
import ui.technician.TechnicianDashboard;
import ui.customer.CustomerDashboard;
import models.*;

public class DashboardDemoApp extends JFrame {
    private JPanel contentPanel;

    public DashboardDemoApp() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("APU Automotive Service Centre - Dashboard Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        contentPanel = new JPanel(new java.awt.GridBagLayout());
        contentPanel.setBackground(new java.awt.Color(245, 247, 250));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        gbc.fill = java.awt.GridBagConstraints.BOTH;

        JLabel titleLabel = new JLabel("Select Dashboard Role");
        titleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        JButton managerButton = createRoleButton("Manager", "");
        managerButton.addActionListener(e -> showManagerDashboard());
        contentPanel.add(managerButton, gbc);

        // Counter Staff Dashboard button
        gbc.gridx = 1;
        gbc.gridy = 1;
        JButton staffButton = createRoleButton("Counter Staff", "");
        staffButton.addActionListener(e -> showCounterStaffDashboard());
        contentPanel.add(staffButton, gbc);

        // Technician Dashboard button
        gbc.gridx = 0;
        gbc.gridy = 2;
        JButton technicianButton = createRoleButton("Technician", "");
        technicianButton.addActionListener(e -> showTechnicianDashboard());
        contentPanel.add(technicianButton, gbc);

        // Customer Dashboard button
        gbc.gridx = 1;
        gbc.gridy = 2;
        JButton customerButton = createRoleButton("Customer", "");
        customerButton.addActionListener(e -> showCustomerDashboard());
        contentPanel.add(customerButton, gbc);
       
        setContentPane(contentPanel);
        setVisible(true);
    }

    private JButton createRoleButton(String title, String icon) {
        JButton button = new JButton("<html><center>" + icon + "<br>" + title + "</center></html>");
        button.setPreferredSize(new java.awt.Dimension(200, 200));
        button.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        button.setBackground(new java.awt.Color(200, 220, 255));
        button.setForeground(new java.awt.Color(50, 50, 50));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new java.awt.Color(100, 141, 174), 2));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new java.awt.Color(170, 200, 255));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new java.awt.Color(200, 220, 255));
            }
        });

        return button;
    }

    private void showManagerDashboard() {
        User manager = new Manager("MGR001", "Admin", "Manager", "admin@email.com", "");
        setContentPane(new ManagerDashboard(manager));
        revalidate();
        repaint();
    }

    private void showCounterStaffDashboard() {
        User staff = new CounterStaff("STAFF001", "Staff", "Counter", "staff@email.com", "");
        setContentPane(new CounterStaffDashboard(staff));
        revalidate();
        repaint();
    }

    private void showTechnicianDashboard() {
        User technician = new Technician("TECH001", "Technician", "Service", "staff@email.com", "");
        setContentPane(new TechnicianDashboard(technician));
        revalidate();
        repaint();
    }

    private void showCustomerDashboard() {
        User customer = new Customer("CUST001", "Customer", "Appointment", "customer@email.com", "");
        setContentPane(new CustomerDashboard(customer));
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardDemoApp());
    }
}
