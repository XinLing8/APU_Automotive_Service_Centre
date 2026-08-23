package ui.counterstaff;

import ui.common.StyleConfig;
import models.Appointment;
import logic.AppointmentManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ViewAppointmentsUI extends JPanel {
    private AppointmentManager appointmentManager;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;

    private JTextField filterAppointmentID;
    private JTextField filterCustomerID;
    private JTextField filterTechnicianID;
    private JTextField filterServiceType;
    private JTextField filterDate;
    private JTextField filterStatus;

    private static final Color PANEL_BG = StyleConfig.GREY;
    private static final Color HEADER_BG = new Color(60, 72, 107);
    private static final Color BUTTON_COLOR = StyleConfig.BLUE;
    private static final Color BUTTON_TEXT_COLOR = StyleConfig.WHITE;
    private static final Color TEXT_COLOR = Color.BLACK;

    public ViewAppointmentsUI() {
        this.appointmentManager = new AppointmentManager();
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel contentArea = new JPanel(new BorderLayout(10, 10));
        contentArea.setBackground(PANEL_BG);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(PANEL_BG);
        topPanel.add(createHeaderPanel());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(createFilterPanel());

        contentArea.add(topPanel, BorderLayout.NORTH);
        contentArea.add(createMainContentPanel(), BorderLayout.CENTER);
        contentArea.add(createButtonPanel(), BorderLayout.SOUTH);

        add(contentArea, BorderLayout.CENTER);

        refreshAppointmentTable();
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridLayout(2, 6, 8, 2));
        filterPanel.setBackground(PANEL_BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        filterAppointmentID = new JTextField();
        filterCustomerID = new JTextField();
        filterTechnicianID = new JTextField();
        filterServiceType = new JTextField();
        filterDate = new JTextField();
        filterStatus = new JTextField();

        filterPanel.add(new JLabel("Appointment ID:"));
        filterPanel.add(new JLabel("Customer ID:"));
        filterPanel.add(new JLabel("Technician ID:"));
        filterPanel.add(new JLabel("Service Type:"));
        filterPanel.add(new JLabel("Date:"));
        filterPanel.add(new JLabel("Status:"));

        filterPanel.add(filterAppointmentID);
        filterPanel.add(filterCustomerID);
        filterPanel.add(filterTechnicianID);
        filterPanel.add(filterServiceType);
        filterPanel.add(filterDate);
        filterPanel.add(filterStatus);

        for (JTextField field : new JTextField[]{
            filterAppointmentID, filterCustomerID, filterTechnicianID,
            filterServiceType, filterDate, filterStatus
        }) {
            field.setFont(new Font("Arial", Font.PLAIN, 12));
        }

        javax.swing.event.DocumentListener filterListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        };
        filterAppointmentID.getDocument().addDocumentListener(filterListener);
        filterCustomerID.getDocument().addDocumentListener(filterListener);
        filterTechnicianID.getDocument().addDocumentListener(filterListener);
        filterServiceType.getDocument().addDocumentListener(filterListener);
        filterDate.getDocument().addDocumentListener(filterListener);
        filterStatus.getDocument().addDocumentListener(filterListener);

        return filterPanel;
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        JLabel titleLabel = new JLabel("View All Appointments");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(PANEL_BG);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        String[] columnNames = {"Appointment ID", "Customer ID", "Technician ID", "Service Type", "Date", "Time", "Status", "Total Price"};
        tableModel = new DefaultTableModel(columnNames, 0) {
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
        appointmentTable.getTableHeader().setBackground(BUTTON_COLOR);
        appointmentTable.getTableHeader().setForeground(BUTTON_TEXT_COLOR);
        appointmentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBackground(PANEL_BG);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(PANEL_BG);
        
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
        button.setBorder(BorderFactory.createLineBorder(BUTTON_COLOR, 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        return button;
    }
    
    private void refreshAppointmentTable() {
        appointmentManager.refreshAppointments();

        tableModel.setRowCount(0);

        List<Appointment> appointments = appointmentManager.getAllAppointments();
        for (Appointment apt : appointments) {
            Object[] row = {
                apt.getAppointmentID(),
                apt.getCustomerID(),
                apt.getTechnicianID(),
                apt.getServiceType(),
                apt.getDate(),
                apt.getTime(),
                apt.getStatus(),
                String.format("%.2f", apt.getTotalPrice())
            };
            tableModel.addRow(row);
        }
    }

    private void filterTable() {
        appointmentManager.refreshAppointments();

        String aptId = filterAppointmentID.getText().trim().toLowerCase();
        String custId = filterCustomerID.getText().trim().toLowerCase();
        String techId = filterTechnicianID.getText().trim().toLowerCase();
        String serviceType = filterServiceType.getText().trim().toLowerCase();
        String date = filterDate.getText().trim().toLowerCase();
        String status = filterStatus.getText().trim().toLowerCase();

        tableModel.setRowCount(0);
        List<Appointment> appointments = appointmentManager.getAllAppointments();
        for (Appointment apt : appointments) {
            if (!apt.getAppointmentID().toLowerCase().contains(aptId)) continue;
            if (!apt.getCustomerID().toLowerCase().contains(custId)) continue;
            if (!apt.getTechnicianID().toLowerCase().contains(techId)) continue;
            if (!apt.getServiceType().toLowerCase().contains(serviceType)) continue;
            if (!apt.getDate().toLowerCase().contains(date)) continue;
            if (!apt.getStatus().toLowerCase().contains(status)) continue;
            Object[] row = {
                apt.getAppointmentID(),
                apt.getCustomerID(),
                apt.getTechnicianID(),
                apt.getServiceType(),
                apt.getDate(),
                apt.getTime(),
                apt.getStatus(),
                String.format("%.2f", apt.getTotalPrice())
            };
            tableModel.addRow(row);
        }
    }
}
