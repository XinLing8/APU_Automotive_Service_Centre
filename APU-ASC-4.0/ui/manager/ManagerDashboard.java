package ui.manager;

import javax.swing.*;
import java.awt.*;
import models.User;
import models.Manager;
import ui.common.ProfilePanel;
import ui.common.StyleConfig;
import ui.manager.UserMgmtPanel;

public class ManagerDashboard extends JPanel {
    private static final Color PANEL_BG = StyleConfig.GREY;
    private static final Color BUTTON_COLOR = StyleConfig.BLUE;
    private static final Color BUTTON_TEXT_COLOR = StyleConfig.WHITE;
    private static final Color TEXT_COLOR = Color.BLACK;
    private String managerId;
    private User currentUser;
    private JPanel contentPanel;

    public ManagerDashboard(User currentUser) {
        this.currentUser = currentUser;
        this.managerId = currentUser.getUserID();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton homeButton = new JButton("Logout");
        homeButton.setFont(new Font("Dialog", Font.PLAIN, 14));
        homeButton.setToolTipText("Logout");
        homeButton.setBackground(PANEL_BG);
        homeButton.setBorderPainted(false);
        homeButton.setContentAreaFilled(false);
        homeButton.setFocusPainted(false);
        homeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        homeButton.setForeground(TEXT_COLOR);
        homeButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        JLabel welcomeLabel = new JLabel("Manager Dashboard - " + managerId);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(TEXT_COLOR);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(homeButton, BorderLayout.WEST);
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(PANEL_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel messageLabel = new JLabel("No modules available yet");
        messageLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        messageLabel.setForeground(TEXT_COLOR);
        
        JButton userMgmtBtn = createModuleButton("User Management", "", 200, 200);
        userMgmtBtn.addActionListener(e -> showUserManagement());
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(userMgmtBtn, gbc);
        
        JButton pricingBtn = createModuleButton("Set Prices", "", 200, 200);
        pricingBtn.addActionListener(e -> showPricingModule());
        gbc.gridx = 1;
        gbc.gridy = 0;
        contentPanel.add(pricingBtn, gbc);

        JButton editProfileBtn = createModuleButton("Edit Profile", "", 200, 200);
        editProfileBtn.addActionListener(e -> showEditProfile());
        gbc.gridx = 2;
        gbc.gridy = 0;
        contentPanel.add(editProfileBtn, gbc);
        
        JButton reportsBtn = createModuleButton("View Reports", "", 200, 200);
        reportsBtn.addActionListener(e -> showReports());
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(reportsBtn, gbc);

        JButton viewFeedbacksComments = createModuleButton("View Feedbacks/Comments", "", 200, 200);
        viewFeedbacksComments.addActionListener(e -> showFeedbacksCommentsModule());
        gbc.gridx = 1;
        gbc.gridy = 1;
        contentPanel.add(viewFeedbacksComments, gbc);

        return contentPanel;
    }
    
    private JButton createModuleButton(String title, String icon, int width, int height) {
        JButton button = new JButton("<html><center>" + icon + "<br>" + title + "</center></html>");
        button.setPreferredSize(new Dimension(width, height));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(BUTTON_COLOR, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
    
    private void showUserManagement() {
        contentPanel.removeAll();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(PANEL_BG);

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.setBackground(PANEL_BG);

        JButton backButton = new JButton("← Back to Dashboard");
        styleButton(backButton);

        backButton.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.add(createContentPanel(), BorderLayout.CENTER);

            contentPanel.revalidate();
            contentPanel.repaint();
        });

        backPanel.add(backButton);

        wrapper.add(backPanel, BorderLayout.NORTH);
        wrapper.add(new UserMgmtPanel(currentUser), BorderLayout.CENTER);

        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(wrapper, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showEditProfile() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());
        
        contentPanel.add(new ProfilePanel(currentUser, () -> {
            contentPanel.removeAll();
            contentPanel.add(createContentPanel(), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();

        }), BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showPricingModule() {
        if (contentPanel == null) {
            initializeUI();
        }

        contentPanel.removeAll();

        PricingPanel panel = new PricingPanel();

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.setBackground(PANEL_BG);
        JButton backButton = new JButton("← Back");
        styleButton(backButton);

        backButton.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.add(createContentPanel());
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        backPanel.add(backButton);

        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(backPanel, BorderLayout.NORTH);
        contentPanel.add(panel, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showReports() {
        if (contentPanel == null) {
            initializeUI();
        }
        contentPanel.removeAll();

        ReportPanel panel = new ReportPanel();

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.setBackground(PANEL_BG);
        JButton backButton = new JButton("← Back");
        styleButton(backButton);

        backButton.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.add(createContentPanel());
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        backPanel.add(backButton);

        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(backPanel, BorderLayout.NORTH);
        contentPanel.add(panel, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showFeedbacksCommentsModule() {
        if (contentPanel == null) {
            initializeUI();
        }

        contentPanel.removeAll();

        FeedbacksCommentsPanel panel = new FeedbacksCommentsPanel();

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.setBackground(PANEL_BG);
        JButton backButton = new JButton("← Back");
        styleButton(backButton);
        backButton.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.add(createContentPanel());
            contentPanel.revalidate();
            contentPanel.repaint();
        });
        backPanel.add(backButton);

        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(backPanel, BorderLayout.NORTH);
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void styleButton(JButton btn) {
        btn.setBackground(BUTTON_COLOR);
        btn.setForeground(BUTTON_TEXT_COLOR);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
