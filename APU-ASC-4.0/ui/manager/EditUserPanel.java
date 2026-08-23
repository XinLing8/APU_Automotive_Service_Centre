package ui.manager;

import logic.*;
import models.*;
import ui.common.StyleConfig;
import javax.swing.*;
import java.awt.*;

public class EditUserPanel extends JDialog {
    private JTextField txtFirstName, txtLastName, txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> roleBox;

    private ManageUser manageUser = new ManageUser();
    private UserAuthentication auth = new UserAuthentication();
    private User selectedUser;

    private static final Color PANEL_BG = StyleConfig.GREY;
    private static final Color FORM_BG = Color.WHITE;
    private static final Color BUTTON_COLOR = StyleConfig.BLUE;
    private static final Color BUTTON_TEXT_COLOR = StyleConfig.WHITE;

    public EditUserPanel(User currentUser, User selectedUser) {
        this.selectedUser = selectedUser;

        setTitle("Edit User");
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

        txtFirstName = new JTextField(selectedUser.getFirstName());
        txtLastName = new JTextField(selectedUser.getLastName());
        txtEmail = new JTextField(selectedUser.getEmail());
        txtPassword = new JPasswordField();
        txtPassword.setText("********");

        roleBox = new JComboBox<>(new String[]{
                "Manager", "Technician", "Counter Staff", "Customer"
        });
        roleBox.setSelectedItem(selectedUser.getRole());

        if (!currentUser.getRole().equals("Manager")) {
            roleBox.setEnabled(false);
        }
        
        if (currentUser.getRole().equals("Counter Staff")) {
            roleBox = new JComboBox<>(new String[]{
                "Customer"
            });
        }

        form.add(new JLabel("User ID"));
        form.add(new JLabel(selectedUser.getUserID()));

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

        JButton updateBtn = new JButton("Update");
        JButton cancelBtn = new JButton("Cancel");

        styleButton(updateBtn);
        styleButton(cancelBtn);

        buttons.add(updateBtn);
        buttons.add(cancelBtn);
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        updateBtn.addActionListener(e -> updateUser());
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

    private void updateUser() {
        if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Fields cannot be empty");
            return;
        }

        if (!txtEmail.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format");
            return;
        }
        
        selectedUser.setFirstName(txtFirstName.getText());
        selectedUser.setLastName(txtLastName.getText());
        selectedUser.setEmail(txtEmail.getText());
        
        String passwordInput = new String(txtPassword.getPassword()).trim();

        if (!passwordInput.equals("********") && !passwordInput.isEmpty()) {
            if (!ValidationHelper.isStrongPassword(passwordInput)) {
                JOptionPane.showMessageDialog(this,
                    "Password must be at least 8 characters with 1 number and 1 special character");
                return;
            }
            String hashed = HashUtil.hash(passwordInput);
            selectedUser.setPassword(hashed);
        }
        
        selectedUser.setRole(roleBox.getSelectedItem().toString());
        
        try {
            manageUser.updateUser(selectedUser);
            JOptionPane.showMessageDialog(this, "User updated!");

            if (selectedUser.getRole().equals("Customer")) {
                new Thread(() -> EmailUtil.sendAccUpdateConfirmation(
                    selectedUser.getEmail(),
                    selectedUser.getUserID(),
                    selectedUser.getFullName()
                )).start();
            }
            dispose();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void styleButton(JButton btn) {
        btn.setBackground(BUTTON_COLOR);
        btn.setForeground(BUTTON_TEXT_COLOR);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
