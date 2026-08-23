package ui.manager;

import logic.*;
import models.*;
import ui.common.StyleConfig;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AddUserPanel extends JDialog{
    private JTextField txtFirstName, txtLastName, txtEmail;
    private JComboBox<String> roleBox;
    private JPasswordField txtPassword;
    private User currentUser;

    private ManageUser manageUser = new ManageUser();
    private UserAuthentication auth = new UserAuthentication();
    
    private static final Color PANEL_BG = StyleConfig.GREY;
    private static final Color FORM_BG = Color.WHITE;
    private static final Color BUTTON_COLOR = StyleConfig.BLUE;
    private static final Color BUTTON_TEXT_COLOR = StyleConfig.WHITE;
    
    public AddUserPanel(User currentUser) {
        this.currentUser = currentUser;
        
        setTitle("Add User");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        
        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBackground(FORM_BG);
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel buttons = new JPanel();
        buttons.setBackground(PANEL_BG);
        
        JCheckBox showPassword = new JCheckBox("Show Password");
        showPassword.setBackground(FORM_BG);

        txtFirstName = new JTextField();
        txtLastName = new JTextField();
        txtEmail = new JTextField();
        txtPassword = new JPasswordField();

        if (currentUser.getRole().equals("Manager")) {
            roleBox = new JComboBox<>(new String[]{
                "Manager", "Technician", "Counter Staff"
            });
        } else if (currentUser.getRole().equals("Counter Staff")) {
            roleBox = new JComboBox<>(new String[]{
                "Customer"
            });
        }
        
        form.add(new JLabel("First Name"));
        form.add(txtFirstName);

        form.add(new JLabel("Last Name"));
        form.add(txtLastName);

        form.add(new JLabel("Email"));
        form.add(txtEmail);

        form.add(new JLabel("Password"));
        form.add(txtPassword);
        
        form.add(new JLabel(""));
        form.add(showPassword);

        form.add(new JLabel("Role"));
        form.add(roleBox);

        JButton confirmBtn = new JButton("Confirm");
        JButton clearBtn = new JButton("Clear");
        JButton cancelBtn = new JButton("Cancel");

        styleButton(confirmBtn);
        styleButton(clearBtn);
        styleButton(cancelBtn);

        buttons.add(confirmBtn);
        buttons.add(clearBtn);
        buttons.add(cancelBtn);
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        confirmBtn.addActionListener(e -> registerUser());
        clearBtn.addActionListener(e -> clearFields());
        cancelBtn.addActionListener(e -> dispose());
        showPassword.addActionListener(e -> {
            if (showPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('\u2022');
            }
        });
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(FORM_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        wrapper.add(form, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void registerUser() {
        String firstName = txtFirstName.getText();
        String lastName = txtLastName.getText();
        String email = txtEmail.getText();
        String password = new String(txtPassword.getPassword());
        String role = roleBox.getSelectedItem().toString();
        
        if (!UserAuthorization.canCreate(currentUser, role)) {
            JOptionPane.showMessageDialog(this, "Not authorized");
            return;
        }

        if (firstName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format");
            return;
        }

        if (!ValidationHelper.isStrongPassword(password)) {
            JOptionPane.showMessageDialog(this,
                "Password must be at least 8 characters with 1 number and 1 special character");
            return;
        }
        
        String hashedPassword = HashUtil.hash(password);

        List<User> users = manageUser.getAllUsers();
        String userID = manageUser.generateUserID(role, users);

        User newUser;

        switch (role) {
            case "Manager":
                newUser = new Manager(userID, firstName, lastName, email, hashedPassword);
                break;
            case "Technician":
                newUser = new Technician(userID, firstName, lastName, email, hashedPassword);
                break;
            case "Counter Staff":
                newUser = new CounterStaff(userID, firstName, lastName, email, hashedPassword);
                break;
            default:
                newUser = new Customer(userID, firstName, lastName, email, hashedPassword);
        }

//        auth.sendOTP(email);
//        JOptionPane.showMessageDialog(this, "OTP is sent to email: " + email);
//        
//        String input = JOptionPane.showInputDialog(this, "Enter OTP: ");
//
//        if (input == null) return;
//
//        if (!auth.verifyOTP(input)) {
//            if (auth.isOTPExpired()) {
//                JOptionPane.showMessageDialog(this, "OTP has expired. Please try again.");
//            } else {
//                JOptionPane.showMessageDialog(this, "Invalid OTP. User not created.");
//            }
//            return;
//        }

        if (manageUser.registerUser(newUser)) {
            JOptionPane.showMessageDialog(this, "User created!\nID: " + userID);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Email already exists! Please try again.");
        }
    }

    private void clearFields() {
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
    }

    private void styleButton(JButton btn) {
        btn.setBackground(BUTTON_COLOR);
        btn.setForeground(BUTTON_TEXT_COLOR);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
