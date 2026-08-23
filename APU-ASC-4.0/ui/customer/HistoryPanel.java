package ui.customer;

import logic.PaymentManager;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;

public class HistoryPanel extends JPanel {

    private String customerId;
    private JTable table;
    private DefaultTableModel model;

    private PaymentManager paymentManager; 
    private static final String FILE_PATH = "src/data/payments.txt";

    public HistoryPanel(String customerId) {
        this.customerId = customerId;
        paymentManager = new PaymentManager(); 

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);

        loadPaymentHistory();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(245, 247, 250));
        JLabel title = new JLabel("Payment History");
        title.setFont(new Font("Arial", Font.BOLD, 16));

        panel.add(title);
        return panel;
    }

    private JScrollPane createTable() {

        String[] cols = {
                "Payment ID",
                "Appointment ID",
                "Amount",
                "Method",
                "Status",
                "Date",
        };

        model = new DefaultTableModel(cols, 0); 
        table = new JTable(model){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        return scrollPane;
    }

    private void loadPaymentHistory() {

        model.setRowCount(0);

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");

                if (p.length == 7) {

                    String custID = p[2];

                    if (custID.equals(customerId)) {

                        model.addRow(new Object[]{ 
                                p[0], 
                                p[1], 
                                "RM" + p[3], 
                                p[4], 
                                p[5], 
                                p[6]  
                        });
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading payment history: " + e.getMessage());
        }
    }
}