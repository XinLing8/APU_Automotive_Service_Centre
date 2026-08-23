package ui.counterstaff;

import models.Appointment;
import logic.AppointmentManager;
import logic.ValidationHelper;
import logic.PriceConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageAppointmentUI extends JPanel {

    private AppointmentManager appointmentManager;
    private ValidationHelper validationHelper;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;

    private static final Color BG_COLOR = new Color(245, 247, 250);
    private static final Color HEADER_COLOR = new Color(60, 72, 107);
    private static final Color BUTTON_COLOR = new Color(100, 141, 174);
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(50, 50, 50);

    public ManageAppointmentUI() {
        this.appointmentManager = new AppointmentManager();
        this.validationHelper = new ValidationHelper();

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContentPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        refreshAppointmentTable();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());

        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("Manage Appointments");

        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        mainPanel.setBackground(BG_COLOR);

        String[] columnNames = {
                "Appointment ID",
                "Customer ID",
                "Technician ID",
                "Service Type",
                "Date",
                "Time",
                "Status",
                "Total Price"
        };
        tableModel =
                new DefaultTableModel(columnNames, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
        appointmentTable = new JTable(tableModel);
        appointmentTable.setBackground(Color.WHITE);
        appointmentTable.setForeground(TEXT_COLOR);
        appointmentTable.setGridColor(new Color(200, 200, 200));
        appointmentTable.setRowHeight(25);
        appointmentTable.getTableHeader().setBackground(new Color(200, 220, 255));
        appointmentTable.getTableHeader().setForeground(TEXT_COLOR);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBackground(BG_COLOR);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BG_COLOR);

        JButton createButton = createActionButton("Create New Appointment");
        createButton.addActionListener(e -> openCreateAppointmentDialog());
        buttonPanel.add(createButton);

        JButton assignButton = createActionButton("Assign Technician");
        assignButton.addActionListener(e -> openAssignTechnicianDialog());
        buttonPanel.add(assignButton);

        JButton refreshButton = createActionButton("Refresh");
        refreshButton.addActionListener(e -> refreshAppointmentTable());
        buttonPanel.add(refreshButton);

        return buttonPanel;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);

        button.setBackground(BUTTON_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        return button;
    }

    private void openCreateAppointmentDialog() {
        JDialog dialog = new JDialog((Frame) null, "Create New Appointment", true);

        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
                panel = new JPanel(new GridLayout(8, 2, 10, 10));

        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.setBackground(BG_COLOR);

        panel.add(new JLabel("Customer ID:"));
        JTextField customerIDField = new JTextField();
        panel.add(customerIDField);

        panel.add(new JLabel("Service Type:"));
        JComboBox<String> serviceTypeCombo = new JComboBox<>(new String[]{Appointment.TYPE_NORMAL, Appointment.TYPE_MAJOR});

        panel.add(serviceTypeCombo);

        panel.add(new JLabel("Date (YYYY-MM-DD):"));
        JTextField dateField = new JTextField();
        panel.add(dateField);

        panel.add(new JLabel("Time (HH:mm):"));
        JTextField timeField = new JTextField();
        panel.add(timeField);

        // Technician
        panel.add(new JLabel("Technician:"));
        JComboBox<String> technicianCombo = new JComboBox<>(new String[]{"TECH001", "TECH501", "TECH502", "TECH503"});
                panel.add(technicianCombo);

        // Price
        panel.add(new JLabel("Total Price:"));
        JLabel priceLabel = new JLabel(PriceConfig.getPriceFormatted(Appointment.TYPE_NORMAL));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        priceLabel.setForeground(BUTTON_COLOR);
        panel.add(priceLabel);

        serviceTypeCombo.addActionListener(e -> {
            String selectedService = (String) serviceTypeCombo.getSelectedItem();
            priceLabel.setText(PriceConfig.getPriceFormatted(selectedService));
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        buttonPanel.setBackground(BG_COLOR);

        JButton submitButton = createActionButton("Create");
        submitButton.addActionListener(e -> {
            if (validateCreateAppointmentInput(customerIDField, dateField, timeField)) {
                String customerID = customerIDField.getText().trim();
                String serviceType = (String) serviceTypeCombo.getSelectedItem();
                String date = dateField.getText().trim();
                String time = timeField.getText().trim();
                String technicianID = (String) technicianCombo.getSelectedItem();
                double price = PriceConfig.getPrice(serviceType);
                boolean created = appointmentManager.createAppointment(customerID, serviceType, date, time, price, technicianID);

                if (created) {
                    JOptionPane.showMessageDialog(dialog, "Appointment created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAppointmentTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create appointment.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(submitButton);

        JButton cancelButton = createActionButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openAssignTechnicianDialog() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String appointmentID = (String) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 6);
        if (!currentStatus.equals(Appointment.STATUS_PENDING)) {
            JOptionPane.showMessageDialog(this, "Only pending appointments can be reassigned.", "Invalid Action", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) null, "Assign Technician", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(BG_COLOR);
        panel.add(new JLabel("Technician ID:"));
        JTextField technicianIDField = new JTextField();
        panel.add(technicianIDField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BG_COLOR);
        JButton submitButton = createActionButton("Assign");
        submitButton.addActionListener(e -> {
            String technicianID = technicianIDField.getText().trim();
            if (validationHelper.isValidID(technicianID)) {
                boolean assigned = appointmentManager.assignTechnician(appointmentID, technicianID);
                if (assigned) {
                    JOptionPane.showMessageDialog(dialog, "Technician assigned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAppointmentTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to assign technician.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid technician ID.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(submitButton);

        JButton cancelButton = createActionButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private boolean validateCreateAppointmentInput(JTextField customerIDField, JTextField dateField, JTextField timeField) {
        String customerID = customerIDField.getText().trim();
        String date = dateField.getText().trim();
        String time = timeField.getText().trim();
        if (!validationHelper.isValidID(customerID)) {
            JOptionPane.showMessageDialog(this, "Please select a customer.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!validationHelper.isValidDate(date)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!validationHelper.isValidTime(time)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid time.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void refreshAppointmentTable() {
        appointmentManager.refreshAppointments();
        tableModel.setRowCount(0);
        List<Appointment> appointments = appointmentManager.getAllAppointments();
        for (Appointment apt : appointments) {
            Object[] row = {apt.getAppointmentID(), apt.getCustomerID(), apt.getTechnicianID(), apt.getServiceType(), apt.getDate(), apt.getTime(), apt.getStatus(), String.format("%.2f", apt.getTotalPrice())};
            tableModel.addRow(row);
        }
    }

    public void refreshAfterPayment() {
        appointmentManager.refreshAppointments();
        refreshAppointmentTable();
    }
}
