package logic;

import models.Payment;
import ui.common.IPayment;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class PaymentManager implements IPayment{

    private static final String FILE_PATH = "src/data/payments.txt";
    private List<Payment> payments;

    public String generatePaymentID() {
    int max = 0;

    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
        String line;

        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");

            if (data.length > 0 && data[0].startsWith("PAY")) {
                try {
                    int num = Integer.parseInt(data[0].substring(3));
                    if (num > max) {
                        max = num;
                    }
                } catch (Exception ignored) {}
            }
        }
    } catch (IOException e) {
    }
    return String.format("PAY%03d", max + 1);
}
   
    @Override
   public String createPayment(String appointmentID, String customerID,
                             double amount, String method) {

    String paymentID = generatePaymentID();
    String date = LocalDate.now().toString();

    Payment payment = new Payment(paymentID, appointmentID, customerID,
                                  amount, "Paid", date, method);

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
        bw.write(payment.toString());
        bw.newLine();
        payments.add(payment);
        return paymentID; 
    } catch (IOException e) {
        return null;
    }
}
   @Override
    public boolean isAlreadyPaid(String appointmentID) {

    File file = new File(FILE_PATH);
    if (!file.exists()) return false;

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;

        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");

            if (data.length >= 7) {

                String appID = data[1].trim();
                String status = data[4].trim();

                if (appID.equalsIgnoreCase(appointmentID)
                        && status.equalsIgnoreCase("Paid")) {
                    return true;
                }
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
        return false;
    }
    
    public PaymentManager() {
        payments = new ArrayList<>();
        loadPayments();
    }
    private void loadPayments() {
        payments.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Payment p = parse(line);
                    if (p != null) payments.add(p);
                }
            }

        } catch (FileNotFoundException e) {
            createFile();
        } catch (IOException e) {
            System.err.println("Error loading payments");
        }
    }
    private void createFile() {
        try {
            new File(FILE_PATH).createNewFile();
        } catch (IOException e) {
            System.err.println("Error creating file");
        }
    }
    private Payment parse(String line) {
        try {
            String[] parts = line.split(",");
            return new Payment(
                    parts[0],
                    parts[1],
                    parts[2],
                    Double.parseDouble(parts[3]),
                    parts[4],
                    parts[5],
                    parts[6]
            );
        } catch (Exception e) {
            return null;
        }
    }
    public List<Payment> getPaymentsByCustomer(String customerID) {
        List<Payment> list = new ArrayList<>();

        for (Payment p : payments) {
            if (p.getCustomerID().equals(customerID)) {
                list.add(p);
            }
        }
        return list;
    }

}
