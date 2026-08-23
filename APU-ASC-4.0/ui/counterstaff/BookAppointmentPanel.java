package ui.counterstaff;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import logic.AppointmentManager;
import logic.ValidationHelper;

public class BookAppointmentPanel extends JPanel {
    private JTextField customerIdField;
    private JComboBox<String> technicianCombo;
    private JComboBox<String> appointmentTypeCombo;
    private JTextField dateField;
    private JTextField timeField;
    private JTextField priceField;
    private AppointmentManager appointmentManager;

    public BookAppointmentPanel() {
        this.appointmentManager = new AppointmentManager();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel("Book New Appointment");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 247, 250));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(createLabel("Customer ID:"), gbc);
        gbc.gridx = 1;
        customerIdField = new JTextField(15);
        formPanel.add(customerIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(createLabel("Technician:"), gbc);
        gbc.gridx = 1;
        technicianCombo = new JComboBox<>(new String[]{"TECH001", "TECH501", "TECH502", "TECH503"});
        formPanel.add(technicianCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(createLabel("Appointment Type:"), gbc);
        gbc.gridx = 1;
        appointmentTypeCombo = new JComboBox<>(new String[]{"Normal", "Major"});
        formPanel.add(appointmentTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(createLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        dateField = new JTextField(15);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        formPanel.add(dateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(createLabel("Time (HH:MM):"), gbc);
        gbc.gridx = 1;
        timeField = new JTextField(15);
        formPanel.add(timeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(createLabel("Price:"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField(15);
        formPanel.add(priceField, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));

        JButton saveButton = new JButton("Save Appointment");
        saveButton.setBackground(new Color(100, 141, 174));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.PLAIN, 12));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> saveAppointment());

        JButton clearButton = new JButton("Clear");
        clearButton.setBackground(new Color(200, 200, 200));
        clearButton.setForeground(Color.BLACK);
        clearButton.setFont(new Font("Arial", Font.PLAIN, 12));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(new Color(50, 50, 50));
        return label;
    }

    private void saveAppointment() {
        String customerId = customerIdField.getText().trim();
        String technician = technicianCombo.getSelectedItem().toString();
        String type = appointmentTypeCombo.getSelectedItem().toString();
        String date = dateField.getText().trim();
        String time = timeField.getText().trim();
        String price = priceField.getText().trim();

        if (ValidationHelper.isNullOrEmpty(customerId)) {
            JOptionPane.showMessageDialog(this, "Customer ID cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (ValidationHelper.isNullOrEmpty(date)) {
            JOptionPane.showMessageDialog(this, "Date cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationHelper.isValidDateFormat(date)) {
            JOptionPane.showMessageDialog(this, "Follow Date format: YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationHelper.isTodayOrLater(date)) {
            JOptionPane.showMessageDialog(this, "Date must be today or in the future.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (ValidationHelper.isNullOrEmpty(time)) {
            JOptionPane.showMessageDialog(this, "Time cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationHelper.isValidTimeFormat(time)) {
            JOptionPane.showMessageDialog(this, "Follow Time format: HH:mm.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationHelper.isValidTimeRange(time)) {
            JOptionPane.showMessageDialog(this, "Time must be between 09:30 and 18:30.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (ValidationHelper.isNullOrEmpty(price)) {
            JOptionPane.showMessageDialog(this, "Price cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = appointmentManager.createAppointment(
                customerId, type, date, time, priceValue, technician);
            
            if (success) {
                clearForm();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        customerIdField.setText("");
        technicianCombo.setSelectedIndex(0);
        appointmentTypeCombo.setSelectedIndex(0);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        timeField.setText("");
        priceField.setText("");
    }
}
