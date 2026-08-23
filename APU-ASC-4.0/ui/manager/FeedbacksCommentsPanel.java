package ui.manager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import models.Feedback;
import logic.IFeedback;
import logic.AppointmentManager;
import logic.FeedbackManager;
import ui.common.StyleConfig;
import ui.common.FeedbackUIUtil;

public class FeedbacksCommentsPanel extends JPanel {
    private static final Color BG = new Color(245, 247, 250);
    private static final Color BUTTON_COLOR = StyleConfig.BLUE;
    private static final Color BUTTON_TEXT_COLOR = StyleConfig.WHITE;
    private static final Color TEXT_COLOR = Color.BLACK;

    private final AppointmentManager appointmentManager;
    private final IFeedback feedbackManager;
    private List<Feedback> allFeedbacks;

    private DefaultListModel<String> listModel;
    private JList<String> appointmentList;
    private JPanel conversationPanel;
    private JScrollPane conversationScroll;

    public FeedbacksCommentsPanel() {
        this.feedbackManager = new FeedbackManager();
        this.appointmentManager = new AppointmentManager();
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BG);

        JLabel titleLabel = new JLabel("Feedbacks & Comments");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        add(titleLabel, BorderLayout.NORTH);

        // Left panel
        listModel = new DefaultListModel<>();
        appointmentList = new JList<>(listModel);
        appointmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentList.setFont(new Font("Arial", Font.PLAIN, 13));
        appointmentList.setFixedCellHeight(50);
        appointmentList.setCellRenderer(new AppointmentListRenderer());
        appointmentList.setBackground(Color.WHITE);

        appointmentList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = appointmentList.getSelectedValue();
                if (selected != null) {
                    String appointmentId = selected.split(" ")[0];
                    loadConversation(appointmentId);
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(appointmentList);
        listScroll.setPreferredSize(new Dimension(250, 0));
        listScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220)), "Appointments"));

        // Right panel
        conversationPanel = new JPanel();
        conversationPanel.setLayout(new BoxLayout(conversationPanel, BoxLayout.Y_AXIS));
        conversationPanel.setBackground(BG);
        conversationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel placeholder = createPlaceholderLabel("Select an appointment to view comments");
        conversationPanel.add(placeholder);

        conversationScroll = new JScrollPane(conversationPanel);
        conversationScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        conversationScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220)), "Comments"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, conversationScroll);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.3);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(BG);

        JButton viewDetailsButton = createButton("View Appointment Details");
        viewDetailsButton.addActionListener(e -> openDetailsDialog());
        bottomPanel.add(viewDetailsButton);

        JButton refreshButton = createButton("Refresh");
        refreshButton.addActionListener(e -> loadData());
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        allFeedbacks = feedbackManager.getAllFeedbacks();
        listModel.clear();

        Map<String, Long> countMap = allFeedbacks.stream()
            .collect(Collectors.groupingBy(Feedback::getAppointmentId, Collectors.counting()));

        List<String> seen = new ArrayList<>();
        for (Feedback fb : allFeedbacks) {
            String id = fb.getAppointmentId();
            if (!seen.contains(id)) {
                seen.add(id);
                long count = countMap.get(id);
                listModel.addElement(id + " — " + count + " comment" + (count > 1 ? "s" : ""));
            }
        }

        conversationPanel.removeAll();
        JLabel placeholder = createPlaceholderLabel("Select an appointment to view comments");
        conversationPanel.add(placeholder);
        conversationPanel.revalidate();
        conversationPanel.repaint();
    }

    private void loadConversation(String appointmentId) {
        conversationPanel.removeAll();

        List<Feedback> apptFeedbacks = allFeedbacks.stream()
            .filter(f -> f.getAppointmentId().equals(appointmentId))
            .collect(Collectors.toList());

        if (apptFeedbacks.isEmpty()) {
            conversationPanel.add(createPlaceholderLabel("No comments for this appointment."));
        } else {
            for (Feedback f : apptFeedbacks) {
                conversationPanel.add(FeedbackUIUtil.buildCommentBox(f));
                conversationPanel.add(Box.createVerticalStrut(8));
            }
        }

        conversationPanel.revalidate();
        conversationPanel.repaint();
        SwingUtilities.invokeLater(() ->
            conversationScroll.getVerticalScrollBar().setValue(0));
    }
    
    private void openDetailsDialog() {
        String selected = appointmentList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Please select an appointment first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appointmentId = selected.split(" ")[0];

        appointmentManager.refreshAppointments();
        models.Appointment apt = appointmentManager.getAppointmentById(appointmentId);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Appointment Details — " + appointmentId, true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (apt != null) {
            addRow(panel, "Appointment ID", apt.getAppointmentID());
            addRow(panel, "Customer ID", apt.getCustomerID());
            addRow(panel, "Technician ID", apt.getTechnicianID());
            addRow(panel, "Service", apt.getServiceType());
            addRow(panel, "Date", apt.getDate());
            addRow(panel, "Time", apt.getTime());
            addRow(panel, "Status", apt.getStatus());
            addRow(panel, "Total Price","RM " + apt.getTotalPrice());
        } else {
            addRow(panel, "Appointment ID", appointmentId);
            addRow(panel, "*", "Appointment details not found...");
        }

        JButton closeBtn = createButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(BG);
        btnPanel.add(closeBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lbl);
        panel.add(new JLabel(value != null ? value : "—"));
    }
    
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JLabel createPlaceholderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.GRAY);
        label.setFont(new Font("Arial", Font.ITALIC, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static class AppointmentListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            label.setFont(new Font("Arial", Font.PLAIN, 13));
            return label;
        }
    }
}