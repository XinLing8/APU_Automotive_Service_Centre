package ui.counterstaff;

import ui.common.PaymentTestingUI;
import javax.swing.*;
import java.awt.*;

public class PaymentPanel extends JPanel{
    
    public PaymentPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        JPanel header = new JPanel();
        header.setBackground(new Color(60, 72, 107));

        PaymentTestingUI paymentUI = new PaymentTestingUI();

        add(header, BorderLayout.NORTH);
        add(paymentUI, BorderLayout.CENTER);
    }
    
}
