package ui;

import javax.swing.*;
import java.awt.*;
import logic.UserAuthentication;
import logic.ValidationHelper;
import models.User;
import ui.manager.ManagerDashboard;
import ui.counterstaff.CounterStaffDashboard;
import ui.technician.TechnicianDashboard;
import ui.customer.CustomerDashboard;

public class LoginFrame extends JFrame {
    private JTextField txtUserID;
    private JPasswordField txtPassword;
    private int loginAttempts = 0;
    private UserAuthentication auth = new UserAuthentication();
    private boolean isLocked = false;
    
    private static final Color BG_COLOR = new Color(245, 247, 250);
    private static final Color BUTTON_COLOR = new Color(100, 141, 174);
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    
    public LoginFrame() {
        setTitle("APU ASC Login");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            createLeftPanel(),
            createRightPanel()
        );

        splitPane.setResizeWeight(0.4);
        splitPane.setDividerSize(0);
        splitPane.setEnabled(false);

        add(splitPane);
        
        setVisible(true);
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(60, 72, 107));
        
        JLabel title = new JLabel("<html><center>APU Automotive Service Centre<br>Management System</html></center>");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        
        panel.add(title);
        
        return panel;
    }
        
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel header = new JLabel("Login", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 28));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        panel.add(header, BorderLayout.NORTH);
        
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 60, 8, 60);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        
        txtUserID = new JTextField();
        txtPassword = new JPasswordField();

        txtUserID.setPreferredSize(new Dimension(200, 30));
        txtPassword.setPreferredSize(new Dimension(200, 30));
        
        JLabel lblUserID = new JLabel("User ID");
        JLabel lblPassword = new JLabel("Password");
        
        lblUserID.setFont(new Font("Arial", Font.BOLD, 14));
        lblPassword.setFont(new Font("Arial", Font.BOLD, 14));
        
        JButton loginBtn = new JButton("Login");
        createButton(loginBtn);
        JButton clearBtn = new JButton("Clear");
        createButton(clearBtn);

        JCheckBox showPassword = new JCheckBox("Show Password");
        showPassword.setBackground(Color.WHITE);
        showPassword.addActionListener(e -> {
            txtPassword.setEchoChar(showPassword.isSelected() ? (char) 0 : '•');
        });
        
        int y = 0;
        
        gbc.gridy = y++; 
        form.add(lblUserID, gbc);
        gbc.gridy = y++; 
        form.add(txtUserID, gbc);
        gbc.gridy = y++; 
        form.add(lblPassword, gbc);
        gbc.gridy = y++; 
        form.add(txtPassword, gbc);
        gbc.gridy = y++; 
        form.add(showPassword, gbc);
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(clearBtn);
        buttonPanel.add(loginBtn);
        
        gbc.gridy = y++;
        form.add(buttonPanel, gbc);

        gbc.gridy = y;
        gbc.weighty = 1;
        form.add(new JLabel(), gbc);

        panel.add(form, BorderLayout.CENTER);

        loginBtn.addActionListener(e -> login());
        clearBtn.addActionListener(e -> clear());

        return panel;
    }

    private void login() {
        
        if (isLocked) {
            JOptionPane.showMessageDialog(
                this,
                "Too many failed attempts. Contact staff.",
                "Account Locked",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        String userID = ValidationHelper.sanitizeInput(txtUserID.getText());
        String password = new String(txtPassword.getPassword());
        
        if (userID == null || userID.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required");
            return;
        }

        User user = auth.login(userID, password);
        
        if (user == null) {
            handleFailedLogin();
            return;
        }
        
        if (!handleOTP(user)) {
            return;
        }
        
        JOptionPane.showMessageDialog(this, "Login successful!");
        loginAttempts = 0;
        
        openDashboard(user);
    }

    private void handleFailedLogin() {
        loginAttempts++;

        JOptionPane.showMessageDialog(
            this,
            "Incorrect User ID or Password",
            "Login Failed",
            JOptionPane.ERROR_MESSAGE
        );

        if (loginAttempts >= 3) {
            isLocked = true;
        }
    }
    
    private boolean handleOTP(User user) {
        String userEmail = user.getEmail();
        auth.sendOTP(userEmail);
        JOptionPane.showMessageDialog(this, "OTP is sent to email: " + userEmail);

        String input = JOptionPane.showInputDialog("Enter OTP:");

        if (input == null || !auth.verifyOTP(input)) {
            JOptionPane.showMessageDialog(this, "Invalid OTP");
            return false;
        }
        return true;
    }
    
    private void createButton(JButton btn) {
        btn.setBackground(BUTTON_COLOR);
        btn.setForeground(BUTTON_TEXT_COLOR);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
    }

    private void clear() {
        txtUserID.setText("");
        txtPassword.setText("");
        JOptionPane.showMessageDialog(this, "Fields cleared");
    }
    
    private void openDashboard(User user) {
        dispose();

        switch (user.getRole()) {
            case "Manager":
                JFrame mgrFrame = new JFrame("Manager Dashboard");
                mgrFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mgrFrame.setSize(1000, 700);
                mgrFrame.setLocationRelativeTo(null);

                mgrFrame.setContentPane(new ManagerDashboard(user));
                mgrFrame.setVisible(true);
                break;
            case "Counter Staff":
                JFrame staffFrame = new JFrame("Counter Staff Dashboard");
                staffFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                staffFrame.setSize(1000, 700);
                staffFrame.setLocationRelativeTo(null);

                staffFrame.setContentPane(new CounterStaffDashboard(user));
                staffFrame.setVisible(true);
                break;
            case "Technician":
                JFrame techFrame = new JFrame("Technician Dashboard");
                techFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                techFrame.setSize(1000, 700);
                techFrame.setLocationRelativeTo(null);

                techFrame.setContentPane(new TechnicianDashboard(user));
                techFrame.setVisible(true);
                break;
            case "Customer":
                JFrame custFrame = new JFrame("Customer Dashboard");
                custFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                custFrame.setSize(1000, 700);
                custFrame.setLocationRelativeTo(null);

                custFrame.setContentPane(new CustomerDashboard(user));
                custFrame.setVisible(true);
                break;
            default: 
                JOptionPane.showMessageDialog(this, "Unknown role");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
