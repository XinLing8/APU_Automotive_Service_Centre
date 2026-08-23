package ui.common;

import models.Feedback;
import javax.swing.*;
import java.awt.*;

public class FeedbackUIUtil {

    public static JPanel buildCommentBox(Feedback fb) {
        String role;
        Color bgColor;

        if (fb.getUserId().startsWith("TECH")) {
            role = "Technician";
            bgColor = new Color(220, 235, 255); // blue
        } else {
            role = "Customer";
            bgColor = new Color(220, 245, 220); // green
        }

        String header = role + " (" + fb.getUserId() + ")  •  " + fb.getDate() + " " + fb.getTime();
        JLabel headerLabel = new JLabel(header);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String feedbackText = fb.getFeedback() != null ? fb.getFeedback() : "-";
        JTextArea textArea = new JTextArea(feedbackText);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));
        textArea.setBackground(bgColor);
        textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        textArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(bgColor);
        container.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        container.add(headerLabel);
        container.add(Box.createVerticalStrut(4));
        container.add(textArea);

        return container;
    }
}