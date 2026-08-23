package ui.common;

import javax.swing.*;
import java.awt.*;
import models.User;
import logic.ManageUser;
import logic.ValidationHelper;
import ui.common.StyleConfig;

public class ProfilePanel extends JPanel{
    private JTextField txtUserID, txtRole, txtFirstName, txtLastName, txtEmail;
    private User currentUser;

    private static final Color HEADER_BG = new Color(60, 72, 107);
    private static final Color PANEL_BG = new Color(245, 247, 250);
    private static final Color FORM_BG = Color.WHITE;
    private static final Color BUTTON_COLOR = StyleConfig.BLUE;
    private static final Color BUTTON_TEXT_COLOR = StyleConfig.WHITE;
    
    public ProfilePanel(User currentUser, Runnable onBack) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout(10, 10));
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createButtonPanel(onBack), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setBackground(HEADER_BG);

        JLabel title = new JLabel("Edit Profile");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        panel.add(title);
        return panel;
    }

    private JPanel createForm() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBackground(FORM_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String userID = (currentUser.getUserID() != null) ? currentUser.getUserID() : "";
        String role = (currentUser.getRole() != null) ? currentUser.getRole() : "";
        String firstName = (currentUser.getFirstName() != null) ? currentUser.getFirstName() : "";
        String lastName = (currentUser.getLastName() != null) ? currentUser.getLastName() : "";
        String email = (currentUser.getEmail() != null) ? currentUser.getEmail() : "";

        txtUserID = createStyledTextField(userID, false);
        txtRole = createStyledTextField(role, false);
        txtFirstName = createStyledTextField(firstName, true);
        txtLastName = createStyledTextField(lastName, true);
        txtEmail = createStyledTextField(email, true);

        panel.add(new JLabel("User ID:"));
        panel.add(txtUserID);

        panel.add(new JLabel("Role:"));
        panel.add(txtRole);

        panel.add(new JLabel("First Name:"));
        panel.add(txtFirstName);

        panel.add(new JLabel("Last Name:"));
        panel.add(txtLastName);

        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);

        return panel;
    }

    private JPanel createButtonPanel(Runnable onBack) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BG);

        JButton saveBtn = new JButton("Save");
        JButton backBtn = new JButton("Back");

        styleButton(saveBtn);
        styleButton(backBtn);

        panel.add(backBtn);
        panel.add(saveBtn);

        saveBtn.addActionListener(e -> saveProfile());
        backBtn.addActionListener(e -> onBack.run());

        return panel;
    }
    
    private void styleButton(JButton btn) {
        btn.setBackground(BUTTON_COLOR);
        btn.setForeground(BUTTON_TEXT_COLOR);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JTextField createStyledTextField(String value, boolean editable) {
        JTextField field = new JTextField(value);
        field.setEditable(editable);
        return field;
    }

    private void saveProfile() {
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required");
            return;
        }
        
        if (!firstName.matches("[a-zA-Z ]+") || !lastName.matches("[a-zA-Z ]+")) {
            JOptionPane.showMessageDialog(this, "First name and last name must contain only letters");
            return;
        }

        if (!ValidationHelper.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format");
            return;
        }

        try {
            User updatedUser = currentUser;

            updatedUser.setFirstName(firstName);
            updatedUser.setLastName(lastName);
            updatedUser.setEmail(email);

            new ManageUser().updateUser(updatedUser);

            JOptionPane.showMessageDialog(this, "Profile updated successfully!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
