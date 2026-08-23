package ui.counterstaff;

import models.Appointment;
import models.User;
import logic.AppointmentManager;
import logic.ValidationHelper;
import logic.ServiceConfig;
import ui.common.StyleConfig;
import javax.swing.*;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import logic.EmailUtil;
import logic.ManageUser;

public class BookAppointmentCalendarUI extends JPanel {
    private AppointmentManager appointmentManager;
    private ManageUser manageUser;
    private ValidationHelper validationHelper;

    private JPanel calendarPanel;
    private JButton prevButton;  
    private LocalDate selectedStartDate;
    private LocalDate currentWeekStart;  
    private String selectedServiceType;

    private static final Color BG_COLOR = StyleConfig.GREY;
    private static final Color HEADER_COLOR = new Color(60, 72, 107);
    private static final Color BUTTON_COLOR = StyleConfig.BLUE;
    private static final Color BUTTON_TEXT_COLOR = StyleConfig.WHITE;
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final Color EMPTY_SLOT = new Color(144, 238, 144);     // green for empty slot
    private static final Color BOOKED_SLOT = new Color(240, 128, 128);    // red for booked slot
    private static final Color SELECTED_SLOT = new Color(100, 149, 237); 
    private static final Color HEADER_SLOT = new Color(200, 200, 200);    
    
    private static final LocalTime FIRST_SLOT = LocalTime.of(9, 30);
    private static final LocalTime LAST_SLOT = LocalTime.of(18, 30);
    private static final int SLOT_DURATION_MINUTES = 30;
    private static final int DISPLAY_DAYS = 7;  
    
    public BookAppointmentCalendarUI() {
        this.appointmentManager = new AppointmentManager();
        this.manageUser = new ManageUser();
        this.validationHelper = new ValidationHelper();
        this.currentWeekStart = LocalDate.now();  
        this.selectedStartDate = currentWeekStart;  
        this.selectedServiceType = ServiceConfig.TYPE_NORMAL;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createControlPanel(), BorderLayout.WEST);

        calendarPanel = createCalendarPanel();
        add(new JScrollPane(calendarPanel), BorderLayout.CENTER);
        
        updatePreviousButtonState();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("Book Appointment");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(BG_COLOR);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(180, 0));
        
        controlPanel.add(Box.createVerticalStrut(20));
        
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBackground(BG_COLOR);
        legendPanel.setMaximumSize(new Dimension(160, 50));
        
        JPanel availableLegend = createLegendItemHorizontal(EMPTY_SLOT, "Available");
        legendPanel.add(availableLegend);
        
        legendPanel.add(Box.createVerticalStrut(3));
        
        JPanel bookedLegend = createLegendItemHorizontal(BOOKED_SLOT, "Booked");
        legendPanel.add(bookedLegend);
        
        controlPanel.add(legendPanel);
        controlPanel.add(Box.createVerticalStrut(20));
        
        prevButton = new JButton("← Previous Week");
        styleNavigationButton(prevButton);
        prevButton.addActionListener(e -> {
            LocalDate potentialDate = selectedStartDate.minusWeeks(1);
            if (potentialDate.isAfter(currentWeekStart) || potentialDate.isEqual(currentWeekStart)) {
                selectedStartDate = potentialDate;
                updateCalendarGrid();
                updatePreviousButtonState();
            }
        });
        controlPanel.add(prevButton);
        controlPanel.add(Box.createVerticalStrut(5));
        
        JButton nextButton = new JButton("Next Week →");
        styleNavigationButton(nextButton);
        nextButton.addActionListener(e -> {
            selectedStartDate = selectedStartDate.plusWeeks(1);
            updateCalendarGrid();
            updatePreviousButtonState(); 
        });
        controlPanel.add(nextButton);
        controlPanel.add(Box.createVerticalStrut(5));
        
        JButton todayButton = new JButton("Today");
        styleNavigationButton(todayButton);
        todayButton.addActionListener(e -> {
            selectedStartDate = currentWeekStart;
            updateCalendarGrid();
            updatePreviousButtonState();  
        });
        controlPanel.add(todayButton);
        controlPanel.add(Box.createVerticalGlue());
        
        return controlPanel;
    }
    
    private JPanel createLegendItemHorizontal(Color color, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setBackground(BG_COLOR);
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(12, 12));
        colorBox.setBorder(new LineBorder(Color.BLACK));
        item.add(colorBox);
        JLabel labelText = new JLabel(label);
        labelText.setForeground(TEXT_COLOR);
        labelText.setFont(new Font("Arial", Font.PLAIN, 13));
        item.add(labelText);
        return item;
    }

    private void styleNavigationButton(JButton button) {
        button.setBackground(BUTTON_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(160, 30));
        button.setPreferredSize(new Dimension(160, 30));
    }
    
    private JPanel createLegendItem(Color color, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT));
        item.setBackground(BG_COLOR);
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(15, 15));
        colorBox.setBorder(new LineBorder(Color.BLACK));
        item.add(colorBox);
        item.add(new JLabel(label));
        return item;
    }
    
    private List<String> getServicesForType(String serviceType) {
        return ServiceConfig.getAllServices();
    }
    
    private void updatePreviousButtonState() {
        if (prevButton != null) {
            boolean isAtCurrentWeek = selectedStartDate.isEqual(currentWeekStart);
            prevButton.setEnabled(!isAtCurrentWeek);
        }
    }
    
    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, DISPLAY_DAYS + 1, 1, 1));
        gridPanel.setBackground(Color.WHITE);
        
        List<LocalTime> timeSlots = generateTimeSlots();
        
        gridPanel.add(new JLabel("Time")); 
        LocalDate currentDate = selectedStartDate;
        for (int d = 0; d < DISPLAY_DAYS; d++) {
            JLabel dateHeader = new JLabel(currentDate.format(DateTimeFormatter.ofPattern("MM/dd")));
            dateHeader.setBackground(HEADER_SLOT);
            dateHeader.setOpaque(true);
            dateHeader.setHorizontalAlignment(SwingConstants.CENTER);
            dateHeader.setFont(new Font("Arial", Font.BOLD, 11));
            dateHeader.setBorder(new LineBorder(Color.BLACK, 1));
            gridPanel.add(dateHeader);
            currentDate = currentDate.plusDays(1);
        }
        
        for (LocalTime timeSlot : timeSlots) {
            JLabel timeLabel = new JLabel(timeSlot.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.setBackground(HEADER_SLOT);
            timeLabel.setOpaque(true);
            timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            timeLabel.setFont(new Font("Arial", Font.BOLD, 11));
            timeLabel.setBorder(new LineBorder(Color.BLACK, 1));
            gridPanel.add(timeLabel);
            
            currentDate = selectedStartDate;
            for (int d = 0; d < DISPLAY_DAYS; d++) {
                JButton slotButton = createSlotButton(currentDate, timeSlot);
                gridPanel.add(slotButton);
                currentDate = currentDate.plusDays(1);
            }
        }
        panel.add(gridPanel, BorderLayout.NORTH);
        return panel;
    }
    
    private JButton createSlotButton(LocalDate date, LocalTime time) {
        JButton button = new JButton(" ");
        button.setPreferredSize(new Dimension(80, 30));
        button.setFocusPainted(false);
        
        boolean isAvailable = isSlotAvailable(date, time);
        
        if (isAvailable) {
            button.setBackground(EMPTY_SLOT);
            button.setForeground(TEXT_COLOR);
            button.addActionListener(e -> openQuickBookingDialog(date, time));
        } else {
            button.setBackground(BOOKED_SLOT);
            button.setForeground(Color.DARK_GRAY);
            
            button.addActionListener(e -> openAllAppointmentsDialog(date, time));
        }
        button.setBorder(new LineBorder(Color.BLACK, 1));
        button.setOpaque(true);
        
        return button;
    }
    
    private boolean isSlotAvailable(LocalDate date, LocalTime time) {
        LocalDateTime slotDateTime = LocalDateTime.of(date, time);
        LocalDateTime currentDateTime = LocalDateTime.now();

        if (slotDateTime.isBefore(currentDateTime)) {
            return false;
        }

        int serviceDuration;
        if (selectedServiceType.equalsIgnoreCase(ServiceConfig.TYPE_NORMAL)) {
            serviceDuration = 1;  // Normal services
        } else {
            serviceDuration = 3;  // Major services
        }

        List<String> allTechs = appointmentManager.getAllTechniciansFromFile();
        for (String techID : allTechs) {
            if (isTechnicianAvailableForSlot(date, time, techID, serviceDuration)) {
                return true; 
            }
        }
        return false;  
    }
    
    private boolean isTechnicianAvailableForSlot(LocalDate date, LocalTime time, String technicianID, int serviceDuration) {
        List<Appointment> appointments = appointmentManager.getAllAppointments();
        
        for (Appointment apt : appointments) {
            try {
                LocalDate aptDate = LocalDate.parse(apt.getDate());
                LocalTime aptTime = LocalTime.parse(apt.getTime());
                
                if (!date.equals(aptDate)) continue;

                if (!apt.getTechnicianID().equalsIgnoreCase(technicianID)) {
                    continue; 
                }
                
                LocalTime aptEndTime = aptTime.plusHours(apt.getServiceDuration());
                LocalTime slotEndTime = time.plusMinutes(SLOT_DURATION_MINUTES);

                if (!time.isAfter(aptEndTime) && !aptTime.isAfter(slotEndTime)) {
                    return false;  
                }
            } catch (Exception e) {
            }
        }
        return true;  
    }
    
    private Appointment getAppointmentForSlot(LocalDate date, LocalTime time) {
        List<Appointment> appointments = appointmentManager.getAllAppointments();
        
        for (Appointment apt : appointments) {
            try {
                LocalDate aptDate = LocalDate.parse(apt.getDate());
                LocalTime aptTime = LocalTime.parse(apt.getTime());
                
                if (!date.equals(aptDate)) continue;
                
                if (!selectedServiceType.equalsIgnoreCase("All")) {
                    if (!apt.getServiceType().equalsIgnoreCase(selectedServiceType)) {
                        continue; 
                    }
                }
                
                LocalTime aptEndTime = aptTime.plusHours(apt.getServiceDuration());
                
                if (!time.isBefore(aptTime) && time.isBefore(aptEndTime)) {
                    return apt;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }
    
    private Appointment getAnyAppointmentForSlot(LocalDate date, LocalTime time) {
        List<Appointment> appointments = appointmentManager.getAllAppointments();
        
        for (Appointment apt : appointments) {
            try {
                LocalDate aptDate = LocalDate.parse(apt.getDate());
                LocalTime aptTime = LocalTime.parse(apt.getTime());
                
                if (!date.equals(aptDate)) continue;
                
                LocalTime aptEndTime = aptTime.plusHours(apt.getServiceDuration());
                
                if (!time.isBefore(aptTime) && time.isBefore(aptEndTime)) {
                    return apt;  
                }
            } catch (Exception e) {
            }
        }
        return null;
    }
    
    private List<Appointment> getAllAppointmentsForSlot(LocalDate date, LocalTime time) {
        List<Appointment> slotAppointments = new ArrayList<>();
        List<Appointment> appointments = appointmentManager.getAllAppointments();
        
        for (Appointment apt : appointments) {
            try {
                LocalDate aptDate = LocalDate.parse(apt.getDate());
                LocalTime aptTime = LocalTime.parse(apt.getTime());
                
                if (!date.equals(aptDate)) continue;
                
                LocalTime aptEndTime = aptTime.plusHours(apt.getServiceDuration());
                LocalTime slotEndTime = time.plusMinutes(SLOT_DURATION_MINUTES);
                
                if (!time.isAfter(aptEndTime) && !aptTime.isAfter(slotEndTime)) {
                    slotAppointments.add(apt);
                }
            } catch (Exception e) {
            }
        }       
        return slotAppointments;
    }
    
    private void openAllAppointmentsDialog(LocalDate date, LocalTime time) {
        appointmentManager.refreshAppointments();
        List<Appointment> slotAppointments = getAllAppointmentsForSlot(date, time);
        
        LocalDateTime slotDateTime = LocalDateTime.of(date, time);
        LocalDateTime currentDateTime = LocalDateTime.now();
        boolean isSlotPast = slotDateTime.isBefore(currentDateTime);
        
        if (slotAppointments.isEmpty()) {
            if (isSlotPast) {
                JOptionPane.showMessageDialog(this, "Past Time Slot\n\nNot allowed to make new appointments for past time slots.\n\nView previous details in the 'View Appointments' tab.");
            } else {
                JOptionPane.showMessageDialog(this, "No appointments found at this time.");
            }
            return;
        }
        
        JDialog dialog = new JDialog((Frame) null, "Appointments at " + time.format(DateTimeFormatter.ofPattern("HH:mm")), true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(BG_COLOR);
        
        String[] columnNames = {"Appointment ID", "Customer ID", "Technician ID", "Service Type", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        for (Appointment apt : slotAppointments) {
            Object[] row = {
                apt.getAppointmentID(),
                apt.getCustomerID(),
                apt.getTechnicianID(),
                apt.getServiceType(),
                apt.getStatus()
            };
            tableModel.addRow(row);
        }
        
        JTable appointmentsTable = new JTable(tableModel);
        appointmentsTable.setBackground(Color.WHITE);
        appointmentsTable.setForeground(TEXT_COLOR);
        appointmentsTable.setGridColor(new Color(200, 200, 200));
        appointmentsTable.setRowHeight(25);
        appointmentsTable.getTableHeader().setBackground(new Color(200, 220, 255));
        appointmentsTable.getTableHeader().setForeground(TEXT_COLOR);
        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BG_COLOR);
        
        JButton cancelButton = new JButton("Cancel Appointment");
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> {
            int selectedRow = appointmentsTable.getSelectedRow();
                if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Please select an appointment to cancel.");
                return;
            }

            String appointmentID = (String) tableModel.getValueAt(selectedRow, 0);
            String currentStatus = (String) tableModel.getValueAt(selectedRow, 4);

            if (!currentStatus.equalsIgnoreCase("Pending")) {
                JOptionPane.showMessageDialog(dialog, "Only Pending appointments can be cancelled.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                dialog,
                "Are you sure you want to cancel appointment " + appointmentID + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = appointmentManager.deleteAppointment(appointmentID);
                if (deleted) {
                    JOptionPane.showMessageDialog(dialog, "Appointment cancelled successfully.");
                    appointmentManager.refreshAppointments();
                    refreshCalendar();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to cancel appointment.");
                }
            }
        });
        buttonPanel.add(cancelButton);
        
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(new Color(220, 53, 69));
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void openQuickBookingDialog(LocalDate date, LocalTime time) {
        JDialog dialog = new JDialog((Frame) null, "Book Appointment Slot", true);
        dialog.setSize(450, 480);
        dialog.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(BG_COLOR);
        
        panel.add(new JLabel("Date:"));
        JLabel dateLabel = new JLabel(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(dateLabel);
        
        panel.add(new JLabel("Time:"));
        JLabel timeLabel = new JLabel(time.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(timeLabel);
        
        panel.add(new JLabel("Service:"));
        List<String> availableServices = getServicesForType(selectedServiceType);
        JComboBox<String> serviceSelectCombo = new JComboBox<>(availableServices.toArray(new String[0]));
        serviceSelectCombo.setFont(new Font("Arial", Font.PLAIN, 11));
        
        JLabel dialogPriceLabel = new JLabel("");
        dialogPriceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        dialogPriceLabel.setForeground(BUTTON_COLOR);

        DefaultComboBoxModel<String> technicianModel = new DefaultComboBoxModel<>();
        JComboBox<String> technicianCombo = new JComboBox<>(technicianModel);
        technicianCombo.setFont(new Font("Arial", Font.PLAIN, 11));

        Runnable refreshTechnicians = () -> {
            technicianModel.removeAllElements();

            String selectedService = (String) serviceSelectCombo.getSelectedItem();
            int serviceDuration = ServiceConfig.getServiceDuration(selectedService);
            Map<String, String> availableTechsMap = appointmentManager.getAvailableTechniciansWithNames(
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                time.format(DateTimeFormatter.ofPattern("HH:mm")),
                serviceDuration
            );

            if (availableTechsMap.isEmpty()) {
                technicianModel.addElement("! No technicians available");
                technicianCombo.setEnabled(false);
                return;
            }

            technicianCombo.setEnabled(true);
            for (String techDisplay : availableTechsMap.values()) {
                technicianModel.addElement(techDisplay);
            }
            technicianCombo.setSelectedIndex(0);
        };
        
        serviceSelectCombo.addActionListener(e -> {
            String selected = (String) serviceSelectCombo.getSelectedItem();
            if (selected != null) {
                dialogPriceLabel.setText(ServiceConfig.getServicePriceFormatted(selected));
                refreshTechnicians.run();
            }
        });
        
        // Set initial price
        if (!availableServices.isEmpty()) {
            serviceSelectCombo.setSelectedIndex(0);
            dialogPriceLabel.setText(ServiceConfig.getServicePriceFormatted(availableServices.get(0)));
        }

        refreshTechnicians.run();
        
        panel.add(serviceSelectCombo);
        
        panel.add(new JLabel("Price:"));
        panel.add(dialogPriceLabel);
        
        panel.add(new JLabel("Technician:"));
        panel.add(technicianCombo);
        
        panel.add(new JLabel("Customer:"));
        String[] selectedCustomer = {""};  
        JLabel customerLabel = new JLabel("(No customer selected)");
        customerLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        customerLabel.setForeground(new Color(100, 100, 100));
        
        JButton selectCustomerBtn = new JButton("Select");
        selectCustomerBtn.setBackground(BUTTON_COLOR);
        selectCustomerBtn.setForeground(Color.WHITE);
        selectCustomerBtn.addActionListener(e -> {
            String selected = openCustomerSelectionDialog(dialog);
            if (selected != null && !selected.isEmpty()) {
                selectedCustomer[0] = selected;
                customerLabel.setText(selected);
            }
        });
        
        JPanel customerPanel = new JPanel(new BorderLayout(5, 0));
        customerPanel.setBackground(BG_COLOR);
        customerPanel.add(customerLabel, BorderLayout.CENTER);
        customerPanel.add(selectCustomerBtn, BorderLayout.EAST);
        panel.add(customerPanel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BG_COLOR);
        
        JButton confirmButton = new JButton("Confirm Booking");
        confirmButton.setBackground(new Color(34, 180, 34));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.addActionListener(e -> {
            String customerID = "";
            
            if (selectedCustomer[0] != null && !selectedCustomer[0].isEmpty()) {
                if (selectedCustomer[0].contains(" - ")) {
                    customerID = selectedCustomer[0].split(" - ")[0].trim();
                } else {
                    customerID = selectedCustomer[0].trim();
                }
            }
            
            String selectedTechDisplay = (String) technicianCombo.getSelectedItem();
            String selectedServiceName = (String) serviceSelectCombo.getSelectedItem();
            
                if (selectedTechDisplay == null || selectedTechDisplay.isEmpty() || selectedTechDisplay.startsWith("!")) {
                JOptionPane.showMessageDialog(dialog, "Please select a technician.");
                return;
            }
            
            if (selectedServiceName == null || selectedServiceName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please select a service.");
                return;
            }
            
            String selectedTech = selectedTechDisplay.split(" - ")[0].trim();
            
            if (validationHelper.isValidID(customerID)) {
                boolean created = appointmentManager.createAppointment(
                    customerID,
                    selectedServiceName,
                    date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    ServiceConfig.getServicePrice(selectedServiceName),
                    selectedTech
                );
                
                if (created) {
                    String customerName = "";
                    String customerEmail = "";
                    
                    for (User user: manageUser.getAllUsers()) {
                        if (user.getUserID().equals(customerID)) {
                            customerName = user.getFullName();
                            customerEmail = user.getEmail();
                            break;
                        }
                    }
                    
                    if (!customerEmail.isEmpty()) {
                        final String tempCustomerName = customerName;
                        final String tempCustomerEmail = customerEmail;
                        final String tempCustomerID = customerID;
                        double price = ServiceConfig.getServicePrice(selectedServiceName);
                        final String tempServicePrice = String.valueOf(price);
                        new Thread(() -> {
                            EmailUtil.sendAppointmentConfirmation(
                                    tempCustomerEmail, 
                                    tempCustomerID,
                                    tempCustomerName,
                                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                selectedServiceName,
                                tempServicePrice);
                        }).start();
                    }
                    appointmentManager.refreshAppointments();
                    updateCalendarGrid();
                    JOptionPane.showMessageDialog(dialog, "Appointment booked successfully.");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to book appointment.");
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a customer.");
            }
        });
        buttonPanel.add(confirmButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private String openCustomerSelectionDialog(JDialog parentDialog) {
        Map<String, String> customersMap = appointmentManager.getCustomersWithNames();
        List<String> customersList = new ArrayList<>(customersMap.values());
        
        JDialog searchDialog = new JDialog(parentDialog, "Select Customer", true);
        searchDialog.setSize(400, 350);
        searchDialog.setLocationRelativeTo(parentDialog);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(BG_COLOR);
        
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(BG_COLOR);
        searchPanel.add(new JLabel("Search (ID or Name):"), BorderLayout.WEST);
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 11));
        searchPanel.add(searchField, BorderLayout.CENTER);
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String customer : customersList) {
            listModel.addElement(customer);
        }
        
        JList<String> customerJList = new JList<>(listModel);
        customerJList.setFont(new Font("Arial", Font.PLAIN, 11));
        customerJList.setBackground(Color.WHITE);
        customerJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(customerJList);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        String[] selectedCustomer = {null};
        
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
            
            private void filterList() {
                String searchText = searchField.getText().toUpperCase();
                listModel.clear();
                
                for (String customer : customersList) {
                    if (customer.toUpperCase().contains(searchText)) {
                        listModel.addElement(customer);
                    }
                }

                if (listModel.size() > 0) {
                    customerJList.setSelectedIndex(0);
                }
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BG_COLOR);
        
        JButton selectBtn = new JButton("Select");
        selectBtn.setBackground(new Color(34, 180, 34));
        selectBtn.setForeground(Color.WHITE);
        selectBtn.addActionListener(e -> {
                if (customerJList.getSelectedValue() != null) {
                selectedCustomer[0] = customerJList.getSelectedValue();
                searchDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(searchDialog, "Please select a customer.");
            }
        });
        buttonPanel.add(selectBtn);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(220, 53, 69));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> searchDialog.dispose());
        buttonPanel.add(cancelBtn);
        
        searchDialog.add(contentPanel, BorderLayout.CENTER);
        searchDialog.add(buttonPanel, BorderLayout.SOUTH);
        searchDialog.setVisible(true);
        
        return selectedCustomer[0];
    }
    
    private List<LocalTime> generateTimeSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = FIRST_SLOT;
        while (!current.isAfter(LAST_SLOT)) {
            slots.add(current);
            current = current.plusMinutes(SLOT_DURATION_MINUTES);
        }
        return slots;
    }
    
    private void updateCalendarGrid() {
        if (calendarPanel != null) {
            calendarPanel.removeAll();
            JPanel gridPanel = new JPanel();
            gridPanel.setLayout(new GridLayout(0, DISPLAY_DAYS + 1, 1, 1));
            gridPanel.setBackground(Color.WHITE);

            List<LocalTime> timeSlots = generateTimeSlots();

            gridPanel.add(new JLabel("Time")); 
            LocalDate currentDate = selectedStartDate;
            for (int d = 0; d < DISPLAY_DAYS; d++) {
                JLabel dateHeader = new JLabel(currentDate.format(DateTimeFormatter.ofPattern("MM/dd")));
                dateHeader.setBackground(HEADER_SLOT);
                dateHeader.setOpaque(true);
                dateHeader.setHorizontalAlignment(SwingConstants.CENTER);
                dateHeader.setFont(new Font("Arial", Font.BOLD, 11));
                dateHeader.setBorder(new LineBorder(Color.BLACK, 1));
                gridPanel.add(dateHeader);
                currentDate = currentDate.plusDays(1);
            }

            for (LocalTime timeSlot : timeSlots) {
                JLabel timeLabel = new JLabel(timeSlot.format(DateTimeFormatter.ofPattern("HH:mm")));
                timeLabel.setBackground(HEADER_SLOT);
                timeLabel.setOpaque(true);
                timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
                timeLabel.setFont(new Font("Arial", Font.BOLD, 11));
                timeLabel.setBorder(new LineBorder(Color.BLACK, 1));
                gridPanel.add(timeLabel);

                currentDate = selectedStartDate;
                for (int d = 0; d < DISPLAY_DAYS; d++) {
                    JButton slotButton = createSlotButton(currentDate, timeSlot);
                    gridPanel.add(slotButton);
                    currentDate = currentDate.plusDays(1);
                }
            }
            calendarPanel.add(gridPanel, BorderLayout.NORTH);
            calendarPanel.revalidate();
            calendarPanel.repaint();
        }
    }
    
    private void refreshCalendar() {
        removeAll();
        initializeUI();
        revalidate();
        repaint();
    }
}
