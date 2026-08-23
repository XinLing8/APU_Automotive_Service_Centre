package ui.common;

import models.Appointment;
import models.Feedback;
import logic.AppointmentManager;
import logic.IFeedback;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public abstract class AppointmentAbstract extends JPanel {

    protected AppointmentManager appointmentManager;
    protected IFeedback feedbackManager;
    protected DefaultTableModel tableModel;
    protected JTable appointmentTable;

    protected static final Color BG_COLOR     = StyleConfig.GREY;
    protected static final Color HEADER_COLOR = StyleConfig.GREY;
    protected static final Color BUTTON_COLOR = StyleConfig.BLUE;
    protected static final Color BUTTON_TEXT  = StyleConfig.WHITE;
    protected static final Color TEXT_COLOR   = Color.BLACK;

    public void openDialog(String appointmentID) {
        appointmentManager.refreshAppointments();   
        Appointment apt = appointmentManager.getAppointmentById(appointmentID);
        if (apt == null) {
            JOptionPane.showMessageDialog(this,
                "Appointment not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) null,
            "Appointment Details - " + appointmentID, true);
        dialog.setSize(450, 550);
        dialog.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1
        JPanel detailsPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        detailsPanel.setBackground(BG_COLOR);

        detailsPanel.add(new JLabel("Appointment ID:"));
        detailsPanel.add(new JLabel(apt.getAppointmentID()));
        detailsPanel.add(new JLabel("Customer ID:"));
        detailsPanel.add(new JLabel(apt.getCustomerID()));
        detailsPanel.add(new JLabel("Technician ID:"));
        detailsPanel.add(new JLabel(apt.getTechnicianID()));
        detailsPanel.add(new JLabel("Service Type:"));
        detailsPanel.add(new JLabel(apt.getServiceType()));
        detailsPanel.add(new JLabel("Date:"));
        detailsPanel.add(new JLabel(apt.getDate()));
        detailsPanel.add(new JLabel("Time:"));
        detailsPanel.add(new JLabel(apt.getTime()));
        detailsPanel.add(new JLabel("Status:"));
        detailsPanel.add(new JLabel(apt.getStatus()));
        detailsPanel.add(new JLabel("Total Price:"));
        detailsPanel.add(new JLabel(String.format("RM %.2f", apt.getTotalPrice())));
        detailsPanel.add(new JLabel("Duration:"));
        detailsPanel.add(new JLabel(apt.getServiceDuration() + " hours"));

        tabbedPane.addTab("Details", detailsPanel);

        // Tab 2
        tabbedPane.addTab("Feedback", buildFeedbackTab(apt));

        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(BG_COLOR);
        JButton closeButton = createStyledButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        dialog.add(tabbedPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    protected JPanel buildConversationPanel(String appointmentID, JScrollPane conversationScroll) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        List<Feedback> feedbacks = feedbackManager.getFeedbacksByAppointment(appointmentID);

        if (feedbacks.isEmpty()) {
            JLabel noComments = new JLabel("No comments yet...");
            noComments.setForeground(Color.GRAY);
            noComments.setFont(new Font("Arial", Font.ITALIC, 11));
            noComments.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(noComments);
            return panel;
        }

        for (Feedback fb : feedbacks) {
            panel.add(buildCommentBox(fb));

            if (fb.getUserId().equals(getUserId())) {
                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                actionPanel.setBackground(BG_COLOR);
                actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JButton editBtn = createStyledButton("Edit");
                editBtn.addActionListener(e -> {
                    String newFeedback = JOptionPane.showInputDialog(null, 
                        "Edit your feedback: ", fb.getFeedback());
                    
                    if (newFeedback != null && !newFeedback.trim().isEmpty()) {
                        feedbackManager.updateFeedback(
                                fb.getDate(), fb.getTime(), fb.getAppointmentId(), fb.getUserId(), newFeedback.trim());
                        
                        conversationScroll.setViewportView(buildConversationPanel(appointmentID, conversationScroll));
                    }
                });

                JButton deleteBtn = createStyledButton("Delete");
                deleteBtn.setBackground(new Color(220, 53, 69));
                deleteBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(null,
                        "Delete this feedback?", "Confirm", JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        feedbackManager.deleteFeedback(
                            fb.getDate(), fb.getTime(), fb.getAppointmentId(), fb.getUserId());
                        
                        conversationScroll.setViewportView(buildConversationPanel(appointmentID, conversationScroll));
                    }
                });

                actionPanel.add(editBtn);
                actionPanel.add(deleteBtn);
                panel.add(actionPanel);
            }

            panel.add(Box.createVerticalStrut(3));
        }

        return panel;
    }
    
    protected JPanel buildCommentBox(Feedback fb) {
        return FeedbackUIUtil.buildCommentBox(fb);
    }

    protected JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BUTTON_COLOR);
        btn.setForeground(BUTTON_TEXT);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    public JPanel buildFeedbackTab(Appointment apt) {
        JPanel feedbackPanel = new JPanel(new BorderLayout(10, 10));
        feedbackPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        feedbackPanel.setBackground(BG_COLOR);

        if (!apt.getStatus().equalsIgnoreCase(Appointment.STATUS_COMPLETED)) {
            JLabel notComplete = new JLabel("Your Appointment is still On-Going...");
            notComplete.setForeground(Color.GRAY);
            notComplete.setFont(new Font("Arial", Font.ITALIC, 12));
            notComplete.setHorizontalAlignment(SwingConstants.CENTER);
            feedbackPanel.add(notComplete, BorderLayout.CENTER);
            return feedbackPanel;
        }

        JScrollPane conversationScroll = new JScrollPane();
        conversationScroll.setViewportView(buildConversationPanel(apt.getAppointmentID(), conversationScroll));
        conversationScroll.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(BG_COLOR), "Comments"));
        conversationScroll.setPreferredSize(new Dimension(400, 170));
        feedbackPanel.add(conversationScroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBackground(BG_COLOR);

        JLabel instrLabel = new JLabel("Add a comment:");
        instrLabel.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(instrLabel, BorderLayout.NORTH);

        JTextArea feedbackArea = new JTextArea(3, 30);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setFont(new Font("Arial", Font.PLAIN, 12));
        feedbackArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220)),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        inputPanel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);

        JButton submitBtn = createStyledButton("Submit");
        submitBtn.addActionListener(e -> {
            String text = feedbackArea.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(feedbackPanel,
                    "Please enter your feedback.", "Empty Feedback", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean success = feedbackManager.submitFeedback(
                apt.getAppointmentID(), getUserId(), text);
            if (success) {
                JOptionPane.showMessageDialog(feedbackPanel,
                    "Feedback submitted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                feedbackArea.setText("");
                conversationScroll.setViewportView(buildConversationPanel(apt.getAppointmentID(), conversationScroll));
            } else {
                JOptionPane.showMessageDialog(feedbackPanel,
                    "Failed to submit feedback.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitPanel.setBackground(BG_COLOR);
        submitPanel.add(submitBtn);
        inputPanel.add(submitPanel, BorderLayout.SOUTH);
        feedbackPanel.add(inputPanel, BorderLayout.SOUTH);

        return feedbackPanel;
    }

    public abstract String getUserId();
    public abstract String getUserRole();
    public abstract void refreshView();
}