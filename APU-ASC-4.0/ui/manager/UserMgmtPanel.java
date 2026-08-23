package ui.manager;

import ui.common.StyleConfig;
import logic.*;
import models.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class UserMgmtPanel extends JPanel{
    private JTable userTable;
    private JTextField searchField;
    private User currentUser;
    private static final Color PANEL_BG = StyleConfig.GREY;
    private static final Color BG_COLOR = new Color(245, 247, 250);
    private static final Color BUTTON_COLOR = StyleConfig.BLUE;
    private static final Color BUTTON_TEXT_COLOR = StyleConfig.WHITE;
    private static final Color TEXT_COLOR = Color.BLACK;
    
    public UserMgmtPanel(User currentUser) {
        this.currentUser = currentUser;
        if (currentUser == null) {
            throw new IllegalArgumentException("currentUser cannot be null");
        }
        
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setBackground(PANEL_BG);
        setLayout(new BorderLayout());
        
        JLabel title = new JLabel("User Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(TEXT_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(150, 30));
        searchField.setText("Enter User ID/Name/Email");
        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Enter User ID/Name/Email")) {
                    searchField.setText("");
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().equals("")) {
                    searchField.setText("Enter User ID/Name/Email");
                }
            }
        });
        JButton searchBtn = new JButton("Search");
        createButton(searchBtn);
        JButton addBtn = new JButton("Add User");
        createButton(addBtn);
        
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            
            if (keyword.equals("Enter User ID/Name/Email")) {
                keyword = "";
            }
            
            if (keyword.isEmpty()) {
                loadUserTable(null);
                return;
            }

            if (!keyword.matches("[A-Za-z0-9@. ]+")) {
                JOptionPane.showMessageDialog(this, "Invalid search input");
                return;
            }

            List<User> result = new ManageUser().searchUsers(keyword);

            if (result.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No user found");
                loadUserTable(null);
            } else {
                loadUserTable(keyword);
            }
        });
        
        addBtn.addActionListener(e -> {
            AddUserPanel dialog = new AddUserPanel(currentUser);
            dialog.setVisible(true);
            SwingUtilities.invokeLater(() -> loadUserTable(null));
        });

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(PANEL_BG);

        JPanel firstRow = new JPanel(new BorderLayout());
        firstRow.setBackground(PANEL_BG);
        firstRow.add(title, BorderLayout.CENTER);

        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        secondRow.setBackground(PANEL_BG);
        secondRow.add(searchField);
        secondRow.add(searchBtn);
        secondRow.add(addBtn);

        topWrapper.add(firstRow, BorderLayout.NORTH);
        topWrapper.add(secondRow, BorderLayout.SOUTH);

        userTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(userTable);
        
        add(topWrapper, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        userTable.setRowHeight(50);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        userTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        loadUserTable(null);  
    }
    
    private void loadUserTable(String keyword) {
        ManageUser manageUser = new ManageUser();
        
        List<User> users = (keyword == null || keyword.isEmpty())
                ? manageUser.getAllUsers()
                : manageUser.searchUsers(keyword);

        String[] columns = {"User ID", "Name", "Email", "Role", "Actions"};
        Object[][] data = new Object[users.size()][5];

        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            data[i][0] = u.getUserID();
            data[i][1] = u.getFullName();
            data[i][2] = u.getEmail();
            data[i][3] = u.getRole();
            data[i][4] = "Edit | Delete";
        }

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        userTable.setModel(model);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < userTable.getColumnCount(); i++) {
            userTable.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        userTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        userTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox(), userTable));
    }
    
    public class ButtonRenderer extends JPanel implements TableCellRenderer {
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
            setOpaque(true);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            add(edit);
            createButton(edit);
            add(delete);
            createButton(delete);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
    
    public class ButtonEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel();
        private JButton edit = new JButton("Edit");
        private JButton delete = new JButton("Delete");
        private String userID;

        public ButtonEditor(JCheckBox checkBox, JTable table) {
            super(checkBox);
            
            setClickCountToStart(1);

            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
            panel.setOpaque(true);
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            panel.add(edit);
            panel.add(delete);

            edit.addActionListener(e -> {
                fireEditingStopped();
                
                ManageUser mu = new ManageUser();
                
                User selectedUser = mu.getAllUsers().stream()
                        .filter(u -> u.getUserID().equals(userID))
                        .findFirst().orElse(null);
                        
                if (selectedUser != null) {
                    if (!UserAuthorization.canUpdate(currentUser, selectedUser.getRole())) {
                        JOptionPane.showMessageDialog(null, "Not authorized");
                        return;
                    }
                new EditUserPanel(currentUser, selectedUser).setVisible(true);
                loadUserTable(null);
                }
            });

            delete.addActionListener(e -> {
                fireEditingStopped();
                
                ManageUser mu = new ManageUser();

                User selectedUser = mu.getAllUsers().stream()
                        .filter(u -> u.getUserID().equals(userID))
                        .findFirst().orElse(null);

                if (selectedUser == null) return;

                if (!UserAuthorization.canDelete(currentUser, selectedUser.getRole())) {
                    JOptionPane.showMessageDialog(null, "Not authorized");
                    return;
                }
                
                String[] reasons = {"Duplicate", "User Request", "Inactive", "Other"};
                String reason = (String) JOptionPane.showInputDialog(
                        null,
                        "Select reason:",
                        "Delete User",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        reasons,
                        reasons[0]
                );

                if (reason == null) return;

                int confirm = JOptionPane.showConfirmDialog(null,
                        "Delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    mu.deleteUser(userID);
                    mu.logDeletedUser(selectedUser, reason);
                    loadUserTable(null);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                    boolean isSelected, int row, int column) {

            userID = table.getValueAt(row, 0).toString();
            return panel;
        }
    }
    
    private void createButton(JButton btn) {
        btn.setBackground(BUTTON_COLOR);
        btn.setForeground(BUTTON_TEXT_COLOR);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
    }
}
