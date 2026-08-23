package ui.technician;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import models.Feedback;
import logic.FeedbackManager;
import logic.AppointmentManager;
import models.Appointment;
import ui.common.StyleConfig;
import ui.common.FeedbackUIUtil;

public class MyCommentsPanel extends JPanel {
    private static final Color PANEL_BG = StyleConfig.GREY;
    private static final Color BUTTON_COLOR = StyleConfig.BLUE;
    private static final Color BUTTON_TEXT_COLOR = StyleConfig.WHITE;
    
    private JTable feedbackTable;
    private DefaultTableModel tableModel;
    private List<Feedback> cachedFeedbacks;

    private final String technicianID;
    private final FeedbackManager feedbackManager;
    
    private final Runnable onNavigateToAppointments;
    private final UpdateStatusPanel updateStatusPanel;
    private final AppointmentManager appointmentManager;
    
    public MyCommentsPanel(String technicianID, Runnable onNavigateToAppointments, UpdateStatusPanel updateStatusPanel) {
        this.technicianID = technicianID;
        this.onNavigateToAppointments = onNavigateToAppointments;
        this.feedbackManager = new FeedbackManager();
        this.updateStatusPanel = updateStatusPanel;
        this.appointmentManager = new AppointmentManager();
        initializeUI();
        loadFeedbacks();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(PANEL_BG);

        JLabel titleLabel = new JLabel("All Comments");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Date", "Time", "Appointment ID", "Customer ID", "Technician ID", "Feedback"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        feedbackTable = new JTable(tableModel);
        feedbackTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        feedbackTable.setRowHeight(26);
        feedbackTable.getTableHeader().setBackground(BUTTON_COLOR);
        feedbackTable.getTableHeader().setForeground(BUTTON_TEXT_COLOR);
        feedbackTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        feedbackTable.getColumnModel().getColumn(5).setPreferredWidth(220);

        feedbackTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openDetailsDialog();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(feedbackTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(PANEL_BG);
        bottomPanel.add(makeButton("View Details", e -> openDetailsDialog()));
        bottomPanel.add(makeButton("Refresh", e -> loadFeedbacks()));
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadFeedbacks() {
        tableModel.setRowCount(0);
        cachedFeedbacks = feedbackManager.getFeedbacksByUser(technicianID);
        
        for (Feedback fb : cachedFeedbacks) {
            Appointment appt = appointmentManager.getAppointmentById(fb.getAppointmentId());
            String customerID = (appt != null) ? appt.getCustomerID(): "Unknown Customer";

            tableModel.addRow(new Object[]{
                fb.getDate(),
                fb.getTime(),
                fb.getAppointmentId(),
                customerID,
                technicianID,
                fb.getFeedback()
            });
        }
    }
    
    private void openDetailsDialog() {
        int selectedRow = feedbackTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a row to view details.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Feedback fb = cachedFeedbacks.get(selectedRow);
        String apptID = fb.getAppointmentId();

        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Feedback Details — " + apptID, true);
        dialog.setSize(460, 500);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(PANEL_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        infoPanel.setBackground(PANEL_BG);
        
        Appointment appt = appointmentManager.getAppointmentById(apptID);
        String customerID = (appt != null) ? appt.getCustomerID() : "Unknown Customer";

        addRow(infoPanel, "Appointment ID", apptID);
        addRow(infoPanel, "Customer ID", customerID);
        addRow(infoPanel, "Technician ID", technicianID);
        addRow(infoPanel, "Date", fb.getDate());
        addRow(infoPanel, "Time", fb.getTime());
        contentPanel.add(infoPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(StyleConfig.GREY), ""));
        scrollPane.setViewportView(reloadConversationFeedback(apptID, technicianID, PANEL_BG, scrollPane));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(PANEL_BG);
        btnRow.add(makeButton("View Appointment Details", e -> {
            dialog.dispose();
            onNavigateToAppointments.run();
            updateStatusPanel.openDialog(apptID);
        }));
        btnRow.add(makeButton("Close", e -> dialog.dispose()));
        contentPanel.add(btnRow, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    private JPanel reloadConversationFeedback(String apptID, String currentUserID, Color bg, JScrollPane scrollPane) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bg);

        List<Feedback> feedbacks = feedbackManager.getFeedbacksByAppointment(apptID);

        for (Feedback f : feedbacks) {
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(bg);
            wrapper.add(FeedbackUIUtil.buildCommentBox(f), BorderLayout.CENTER);
            panel.add(wrapper);

            if (f.getUserId().equals(currentUserID)) {
                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                actionPanel.setBackground(bg);

                JButton editBtn = makeButton("Edit", e -> {
                    String newText = JOptionPane.showInputDialog(null, "Edit your feedback: ", f.getFeedback());
                    
                    if (newText != null && !newText.trim().isEmpty()) {
                        feedbackManager.updateFeedback(f.getDate(), f.getTime(), f.getAppointmentId(), f.getUserId(), newText.trim());
                        
                        scrollPane.setViewportView(reloadConversationFeedback(apptID, currentUserID, bg, scrollPane));
                    }
                });

                JButton deleteBtn = makeButton("Delete", e -> {
                    int confirm = JOptionPane.showConfirmDialog(null, "Delete this feedback? ", "Confirm", JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        feedbackManager.deleteFeedback(f.getDate(), f.getTime(), f.getAppointmentId(), f.getUserId());
                        
                        scrollPane.setViewportView(reloadConversationFeedback(apptID, currentUserID, bg, scrollPane));
                    }
                });
                
                deleteBtn.setBackground(new Color(220, 53, 69));

                actionPanel.add(editBtn);
                actionPanel.add(deleteBtn);
                panel.add(actionPanel);
            }
            panel.add(Box.createVerticalStrut(8));
        }
        return panel;
    }
    
    private void addRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lbl);
        panel.add(new JLabel(value != null ? value : "—"));
    }

    private JButton makeButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBackground(BUTTON_COLOR);
        btn.setForeground(BUTTON_TEXT_COLOR);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }
}
