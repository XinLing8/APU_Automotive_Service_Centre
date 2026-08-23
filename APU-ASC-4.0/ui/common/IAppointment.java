package ui.common;

import models.Appointment;
import javax.swing.JPanel;

public interface IAppointment {
    String getUserId();
    String getUserRole();
    JPanel buildFeedbackTab(Appointment apt);
    void openDialog(String appointmentID);
    void refreshView();
}