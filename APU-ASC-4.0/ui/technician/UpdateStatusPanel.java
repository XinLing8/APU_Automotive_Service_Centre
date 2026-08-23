package ui.technician;

import ui.common.AppointmentAbstract;
import ui.common.StyleConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import models.Appointment;
import logic.AppointmentManager;
import logic.FeedbackManager;

public class UpdateStatusPanel extends AppointmentAbstract {
    private DefaultTableModel tableModel;
    private String technicianId;

    public UpdateStatusPanel(String technicianId) {
        this.technicianId = technicianId;
        this.appointmentManager = new AppointmentManager();
        this.feedbackManager = new FeedbackManager();
        initializeUI();
        loadAppointments();
    }

    @Override
    public String getUserId() { return technicianId; }

    @Override
    public String getUserRole() { return "Technician"; }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BG_COLOR);

        JLabel titleLabel = new JLabel("Update Appointment Status");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Appointment ID", "Customer", "Type", "Date", "Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        appointmentTable = new JTable(tableModel);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setRowHeight(25);
        appointmentTable.getTableHeader().setBackground(StyleConfig.BLUE);
        appointmentTable.getTableHeader().setForeground(StyleConfig.WHITE);
        appointmentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(BG_COLOR);

        JButton completeButton = createStyledButton("Mark as Completed");
        completeButton.addActionListener(e -> openCompleteAppointmentDialog());
        bottomPanel.add(completeButton);
        
        JButton detailsButton = createStyledButton("View Details");
        detailsButton.addActionListener(e -> {
            int selectedRow = appointmentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an appointment to view details.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = (String) tableModel.getValueAt(selectedRow, 0);
            openDialog(id);
        });
        bottomPanel.add(detailsButton);

        JButton refreshButton = createStyledButton("Refresh");
        refreshButton.addActionListener(e -> {
            tableModel.setRowCount(0);
            loadAppointments();
        });
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void openCompleteAppointmentDialog() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to mark as completed.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appointmentID = (String) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 5);

        if (currentStatus.equals("Completed")) {
            JOptionPane.showMessageDialog(this, "This appointment is already completed.", "No Action Needed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to mark this appointment as Completed?", 
            "Confirm Status Update", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean updated = appointmentManager.updateAppointmentStatus(appointmentID, "Completed");
            if (updated) {
                JOptionPane.showMessageDialog(this, "Appointment status updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                appointmentManager.refreshAppointments();
                tableModel.setRowCount(0);
                loadAppointments();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update appointment status. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadAppointments() {
        List<Appointment> appointments = appointmentManager.getAppointmentsByTechnician(technicianId);
        for (Appointment apt : appointments) {
            tableModel.addRow(new Object[]{
                apt.getAppointmentID(),
                apt.getCustomerID(),
                apt.getServiceType(),
                apt.getDate(),
                apt.getTime(),
                apt.getStatus()
            });
        }
    }

    private void updateAppointmentStatus(String appointmentId, String newStatus) {
        boolean updated = appointmentManager.updateAppointmentStatus(appointmentId, newStatus);
        if (updated) {
            JOptionPane.showMessageDialog(this, "Appointment status updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Error updating appointment", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void refreshView() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        loadAppointments();
    }
}
