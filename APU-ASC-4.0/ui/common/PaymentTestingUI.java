// for Counter Staff Payment
package ui.common;

import logic.AppointmentManager;
import models.Appointment;
import logic.PaymentManager;
import logic.ManageUser;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import logic.EmailUtil;
import logic.ServiceConfig;
import models.User;

public class PaymentTestingUI extends JPanel {

    private static final Color HEADER_BG = new Color(60, 72, 107);

    private JComboBox<String> appointmentBox;
    private JLabel amountLabel;
    private JComboBox<String> methodBox;
    private JComboBox<String> customerBox;
    private AppointmentManager appointmentManager;
    private PaymentManager paymentManager;
    private List<Appointment> completedAppointments;
    private Appointment selectedAppointment;
    private ManageUser manageUser;

    public PaymentTestingUI() {

        appointmentManager = new AppointmentManager();
        paymentManager = new PaymentManager();
        manageUser = new ManageUser();

        setLayout(new BorderLayout(10, 10));
        setBackground(StyleConfig.GREY);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadCustomers();

        if (customerBox.getItemCount() > 0) {
            customerBox.setSelectedIndex(0);
            loadAppointmentsByCustomer();
        }
    }

    private JPanel createHeader() {

        JPanel panel = new JPanel();
        panel.setBackground(HEADER_BG);

        JLabel title = new JLabel("Payment");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        panel.add(title);

        return panel;
    }

    private JPanel createForm() {

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Customer:"));
        customerBox = new JComboBox<>();
        customerBox.addActionListener(e -> loadAppointmentsByCustomer());
        panel.add(customerBox);

        panel.add(new JLabel("Appointment:"));
        appointmentBox = new JComboBox<>();
        appointmentBox.addActionListener(e -> updateDetails());
        panel.add(appointmentBox);
        
        panel.add(new JLabel("Service:"));
        amountLabel = new JLabel("-");
        panel.add(amountLabel);

        panel.add(new JLabel("Payment Method:"));
        methodBox = new JComboBox<>(new String[]{
            "Cash",
            "Credit Card"
        });

        panel.add(methodBox);

        return panel;
    }

    private JPanel createButtonPanel() {

        JPanel panel = new JPanel();
        panel.setBackground(StyleConfig.GREY);

        JButton payButton = createStyledButton("Make Payment");
        payButton.addActionListener(e -> processPayment());

        JButton refreshButton = createStyledButton("Refresh");
        refreshButton.addActionListener(e -> {

            loadCustomers();

            if (customerBox.getItemCount() > 0) {
                customerBox.setSelectedIndex(0);
                loadAppointmentsByCustomer();
            }
        });
        panel.add(payButton);
        panel.add(refreshButton);

        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(StyleConfig.BLUE);
        button.setForeground(StyleConfig.WHITE);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        return button;
    }

    private void loadCustomers() {
        customerBox.removeAllItems();

        appointmentManager.refreshAppointments();
        List<Appointment> all = appointmentManager.getAllAppointments();

        Set<String> customerSet = new HashSet<>();

        for (Appointment a : all) {
            if (a.getStatus() != null
                    && a.getStatus().trim().equalsIgnoreCase("Completed")
                    && manageUser.customerExists(a.getCustomerID())){
                customerSet.add(a.getCustomerID());
            }
        }

        for (String cust : customerSet) {
            customerBox.addItem(cust);
        }
    }

    private void loadAppointmentsByCustomer() {
        appointmentBox.removeAllItems();

        String selectedCustomer = (String) customerBox.getSelectedItem();

        if (selectedCustomer == null || selectedCustomer.isEmpty()) {
            amountLabel.setText("-");
            completedAppointments = List.of();
            return;
        }

        List<Appointment> all = appointmentManager.getAllAppointments();

        completedAppointments = all.stream()
                .filter(a -> a.getStatus() != null)
                .filter(a -> a.getStatus()
                .trim()
                .equalsIgnoreCase("Completed"))
                .filter(a -> a.getCustomerID()
                .equals(selectedCustomer))
                .filter(a -> !paymentManager.isAlreadyPaid(a.getAppointmentID()))
                .toList();

        if (completedAppointments.isEmpty()) {
            amountLabel.setText("-");
            return;
        }

        for (Appointment a : completedAppointments) {
            appointmentBox.addItem(a.getAppointmentID() + " - " + a.getServiceType());
        }

        appointmentBox.setSelectedIndex(0);
        updateDetails();
    }
    
    private void updateDetails() {
        String selected = (String) appointmentBox.getSelectedItem();

        if (selected == null) {
            selectedAppointment = null;
            amountLabel.setText("-");
            return;
        }
        
        String selectedID = selected.split(" - ")[0];
       
        for (Appointment a : completedAppointments) {
            if (a.getAppointmentID().equals(selectedID)) {
                selectedAppointment = a;
                amountLabel.setText("RM " + a.getTotalPrice());
                return;
            }
        }
        amountLabel.setText("-");
    }

    private void processPayment() {
        String selected = (String) appointmentBox.getSelectedItem();

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "No appointment selected");
            return;
        }
        String appointmentID = selected.split(" - ")[0];

        Appointment found = null;

        for (Appointment a : completedAppointments) {
            if (a.getAppointmentID().equals(appointmentID)) {
                found = a;
                break;
            }
        }

        if (found == null) {
            JOptionPane.showMessageDialog(this, "Appointment not found");
            return;
        }
        String method = methodBox.getSelectedItem().toString();
        String paymentID = paymentManager.createPayment(
                found.getAppointmentID(),
                found.getCustomerID(),
                found.getTotalPrice(),
                method
        );

        if (paymentID != null) {
            appointmentManager.refreshAppointments();
            loadCustomers();
            loadAppointmentsByCustomer();
            updateDetails();

            String receipt
                    = "------- RECEIPT ---------\n"
                    + "Payment ID: " + paymentID + "\n"
                    + "Appointment: " + found.getAppointmentID() + "\n"
                    + "Customer: " + found.getCustomerID() + "\n"
                    + "Service: " + found.getServiceType() + "\n"
                    + "Amount: RM " + found.getTotalPrice() + "\n"
                    + "Method: " + method + "\n"
                    + "Date: " + java.time.LocalDate.now() + "\n"
                    + "------------------------";

            JOptionPane.showMessageDialog(this, receipt);
            
            String customerName = "";
            String customerEmail = "";

            for (User user: manageUser.getAllUsers()) {
                if (user.getUserID().equals(found.getCustomerID())) {
                    customerName = user.getFullName();
                    customerEmail = user.getEmail();
                    break;
                }
            }
                    
            if (!customerEmail.isEmpty()) {
                final String tempCustomerEmail = customerEmail;
                final String tempCustomerID = found.getCustomerID();
                final String tempCustomerName = customerName;
                final String tempReceipt = receipt;
                
                new Thread(() -> {
                    EmailUtil.sendPaymentConfirmation(
                        tempCustomerEmail, 
                        tempCustomerID,
                        tempCustomerName,
                        tempReceipt);
                }).start();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Payment Failed");
        }
    }
}
