package ui.customer;

import ui.common.AppointmentAbstract;
import ui.common.StyleConfig;
import models.Appointment;
import logic.FeedbackManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import logic.AppointmentManager;
import java.util.List;

public class MyAppointmentsPanel extends AppointmentAbstract {
    private String customerId;

    public MyAppointmentsPanel(String customerId) {
        this.customerId = customerId;
        this.appointmentManager = new AppointmentManager();
        this.feedbackManager = new FeedbackManager();
        initializeUI();
        loadAppointments();
    }

    @Override
    public String getUserId() { 
        return customerId;
    }

    @Override
    public String getUserRole() {
        return "Customer";
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(StyleConfig.GREY);

        JLabel titleLabel = new JLabel("My Appointments");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Appointment ID", "Technician", "Type", "Date", "Time", "Status", "Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        appointmentTable = new JTable(tableModel);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setRowHeight(25);
        appointmentTable.getTableHeader().setBackground(StyleConfig.GREY);
        appointmentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(StyleConfig.GREY));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(StyleConfig.GREY);

        JButton detailsButton = createStyledButton("View Details");
        detailsButton.addActionListener(e -> {
            int selectedRow = appointmentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                    "Please select an appointment to view details.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String appointmentID = (String) tableModel.getValueAt(selectedRow, 0);
            openDialog(appointmentID);
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

    @Override
    public void refreshView() {
        tableModel.setRowCount(0);
        loadAppointments();
    }

    private void loadAppointments() {
        List<Appointment> appointments = appointmentManager.getAppointmentsByCustomer(customerId);
        for (Appointment apt : appointments) {
            tableModel.addRow(new Object[]{
                apt.getAppointmentID(),
                apt.getTechnicianID(),
                apt.getServiceType(),
                apt.getDate(),
                apt.getTime(),
                apt.getStatus(),
                String.format("%.2f", apt.getTotalPrice())
            });
        }
    }
}
