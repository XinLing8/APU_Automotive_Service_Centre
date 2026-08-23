package logic;

import models.Appointment;
import models.User;
import models.Payment;
import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportDataManager {
    private static final String APPOINTMENTS_FILE = "src/data/appointments.txt";
    private static final String PAYMENTS_FILE = "src/data/payments.txt";
    private static final String USERS_FILE = "src/data/users.txt";
    private static final String FEEDBACKS_FILE = "src/data/feedbacks.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Map<String, Double> getTotalRevenueByPeriod(LocalDate referenceDate, Period period) {
        Map<String, Double> result = new LinkedHashMap<>();
        LocalDate startDate = period.getStartDate(referenceDate);
        LocalDate endDate = period.getEndDate(referenceDate);

        System.out.println("Calculating revenue from " + startDate + " to " + endDate);

        double totalRevenue = 0;
        double completedRevenue = 0;
        double pendingRevenue = 0;
        int lineCount = 0;
        int matchedCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 8) {
                        try {
                            String appointmentID = parts[0].trim();
                            String dateStr = parts[4].trim();
                            String status = parts[6].trim();
                            double totalPrice = Double.parseDouble(parts[7].trim());

                            LocalDate appointmentDate = LocalDate.parse(dateStr, DATE_FORMAT);

                            if (isDateInRange(appointmentDate, startDate, endDate)) {
                                matchedCount++;
                                totalRevenue += totalPrice;
                                if (status.equalsIgnoreCase("Completed")) {
                                    completedRevenue += totalPrice;
                                } else if (status.equalsIgnoreCase("Pending")) {
                                    pendingRevenue += totalPrice;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Could not parse appointment in getTotalRevenue: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read the file: " + e.getMessage());
            e.printStackTrace();
        }

        result.put("Total Revenue", totalRevenue);
        result.put("Completed Revenue", completedRevenue);
        result.put("Pending Revenue", pendingRevenue);

        return result;
    }

    public List<String[]> getRevenueByServiceType(LocalDate referenceDate, Period period) {
        Map<String, Double> serviceRevenue = new LinkedHashMap<>();
        Map<String, Integer> serviceCount = new LinkedHashMap<>();
        LocalDate startDate = period.getStartDate(referenceDate);
        LocalDate endDate = period.getEndDate(referenceDate);
        double totalRevenue = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 8) {
                        String serviceType = parts[3].trim();
                        String dateStr = parts[4].trim();
                        String status = parts[6].trim();
                        double totalPrice = Double.parseDouble(parts[7].trim());

                        LocalDate appointmentDate = LocalDate.parse(dateStr, DATE_FORMAT);

                        if (isDateInRange(appointmentDate, startDate, endDate) && status.equalsIgnoreCase("Completed")) {
                            serviceRevenue.put(serviceType, serviceRevenue.getOrDefault(serviceType, 0.0) + totalPrice);
                            serviceCount.put(serviceType, serviceCount.getOrDefault(serviceType, 0) + 1);
                            totalRevenue += totalPrice;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String[]> result = new ArrayList<>();
        for (String service : serviceRevenue.keySet()) {
            double revenue = serviceRevenue.get(service);
            int count = serviceCount.get(service);
            double percentage = totalRevenue > 0 ? (revenue / totalRevenue) * 100 : 0;
            double avgPrice = count > 0 ? revenue / count : 0;
            result.add(new String[]{
                service,
                String.format("RM %.2f", revenue),
                String.format("%.1f%%", percentage),
                String.format("RM %.2f", avgPrice),
                String.valueOf(count)
            });
        }
        return result;
    }

    public List<String[]> getTechnicianPerformance(LocalDate referenceDate, Period period) {
        Map<String, TechStats> techStats = new LinkedHashMap<>();
        LocalDate startDate = period.getStartDate(referenceDate);
        LocalDate endDate = period.getEndDate(referenceDate);

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        String userID = parts[0].trim();
                        String name = parts[1].trim();
                        String role = parts[4].trim();
                        if (role.equalsIgnoreCase("Technician")) {
                            techStats.put(userID, new TechStats(name));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 8) {
                        String techID = parts[2].trim();
                        String dateStr = parts[4].trim();
                        String timeStr = parts[5].trim();
                        String status = parts[6].trim();
                        double totalPrice = Double.parseDouble(parts[7].trim());

                        LocalDate appointmentDate = LocalDate.parse(dateStr, DATE_FORMAT);

                        if (isDateInRange(appointmentDate, startDate, endDate) && !techID.equals("Unassigned")) {
                            if (techStats.containsKey(techID)) {
                                TechStats stats = techStats.get(techID);
                                if (status.equalsIgnoreCase("Completed")) {
                                    stats.completedCount++;
                                    stats.totalRevenue += totalPrice;
                                } else if (status.equalsIgnoreCase("Pending")) {
                                    stats.pendingCount++;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String[]> result = new ArrayList<>();
        for (TechStats stats : techStats.values()) {
            result.add(new String[]{
                stats.name,
                String.valueOf(stats.completedCount),
                String.valueOf(stats.pendingCount),
                String.format("%.1f hrs", stats.completedCount * 0.5), 
                String.format("RM %.2f", stats.totalRevenue)
            });
        }
        return result;
    }

    public List<String[]> getServiceAnalytics(LocalDate referenceDate, Period period) {
        Map<String, ServiceStats> serviceStats = new LinkedHashMap<>();
        LocalDate startDate = period.getStartDate(referenceDate);
        LocalDate endDate = period.getEndDate(referenceDate);

        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 8) {
                        String serviceType = parts[3].trim();
                        String dateStr = parts[4].trim();
                        String status = parts[6].trim();
                        double totalPrice = Double.parseDouble(parts[7].trim());

                        LocalDate appointmentDate = LocalDate.parse(dateStr, DATE_FORMAT);

                        if (isDateInRange(appointmentDate, startDate, endDate)) {
                            if (!serviceStats.containsKey(serviceType)) {
                                serviceStats.put(serviceType, new ServiceStats(serviceType));
                            }
                            ServiceStats stats = serviceStats.get(serviceType);
                            stats.requestCount++;
                            if (status.equalsIgnoreCase("Completed")) {
                                stats.completedRevenue += totalPrice;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String mostFrequent = "";
        int maxRequests = 0;
        for (ServiceStats stats : serviceStats.values()) {
            if (stats.requestCount > maxRequests) {
                maxRequests = stats.requestCount;
                mostFrequent = stats.serviceName;
            }
        }

        List<String[]> result = new ArrayList<>();
        for (ServiceStats stats : serviceStats.values()) {
            double avgPrice = stats.requestCount > 0 ? stats.completedRevenue / stats.requestCount : 0;
            String isMostFrequent = stats.serviceName.equals(mostFrequent) ? "Yes" : "No";
            result.add(new String[]{
                stats.serviceName,
                String.valueOf(stats.requestCount),
                String.format("RM %.2f", stats.completedRevenue),
                String.format("RM %.2f", avgPrice),
                isMostFrequent
            });
        }
        return result;
    }

    public List<String[]> getPaymentStatus(LocalDate referenceDate, Period period) {
        Map<String, PaymentStats> paymentStats = new LinkedHashMap<>();
        LocalDate startDate = period.getStartDate(referenceDate);
        LocalDate endDate = period.getEndDate(referenceDate);

        try (BufferedReader reader = new BufferedReader(new FileReader(PAYMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 7) {
                        try {
                            String method = parts[6].trim();
                            String status = parts[4].trim();
                            double amount = Double.parseDouble(parts[3].trim());
                            String dateStr = parts[5].trim();

                            LocalDate paymentDate = LocalDate.parse(dateStr, DATE_FORMAT);

                            if (isDateInRange(paymentDate, startDate, endDate)) {
                                if (!paymentStats.containsKey(method)) {
                                    paymentStats.put(method, new PaymentStats(method));
                                }
                                PaymentStats stats = paymentStats.get(method);
                                if (isPaidStatus(status)) {
                                    stats.completedCount++;
                                    stats.completedAmount += amount;
                                } else if (status.equalsIgnoreCase("Pending")) {
                                    stats.pendingCount++;
                                    stats.pendingAmount += amount;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Could not parse payment record: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read payments file: " + e.getMessage());
            e.printStackTrace();
        }

        List<String[]> result = new ArrayList<>();
        for (PaymentStats stats : paymentStats.values()) {
            double completionRate = (stats.completedCount + stats.pendingCount) > 0 ? 
                (stats.completedCount / (double)(stats.completedCount + stats.pendingCount)) * 100 : 0;
            result.add(new String[]{
                stats.method,
                String.valueOf(stats.completedCount),
                String.format("RM %.2f", stats.completedAmount),
                String.valueOf(stats.pendingCount),
                String.format("RM %.2f", stats.pendingAmount),
                String.format("%.1f%%", completionRate)
            });
        }
        return result;
    }

    private boolean isPaidStatus(String status) {
        return status.equalsIgnoreCase("Paid");
    }

    public List<String[]> getCustomerInsights(LocalDate referenceDate, Period period) {
        Map<String, CustomerInsight> customerInsights = new LinkedHashMap<>();
        Map<String, String> appointmentToCustomer = new LinkedHashMap<>();
        LocalDate startDate = period.getStartDate(referenceDate);
        LocalDate endDate = period.getEndDate(referenceDate);

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        String userID = parts[0].trim();
                        String name = parts[1].trim();
                        String role = parts[4].trim();
                        if (role.equalsIgnoreCase("Customer")) {
                            customerInsights.put(userID, new CustomerInsight(name));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 8) {
                        try {
                            String appointmentID = parts[0].trim();
                            String customerID = parts[1].trim();
                            String dateStr = parts[4].trim();
                            double totalPrice = Double.parseDouble(parts[7].trim());

                            LocalDate appointmentDate = LocalDate.parse(dateStr, DATE_FORMAT);

                            appointmentToCustomer.put(appointmentID, customerID);

                            if (isDateInRange(appointmentDate, startDate, endDate)) {
                                if (customerInsights.containsKey(customerID)) {
                                    CustomerInsight insight = customerInsights.get(customerID);
                                    insight.appointmentCount++;
                                    insight.totalSpent += totalPrice;
                                    insight.lastServiceDate = appointmentDate;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error reading customer insight line: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read appointments file: " + e.getMessage());
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FEEDBACKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        try {
                            String appointmentID = parts[2].trim();
                            if (appointmentToCustomer.containsKey(appointmentID)) {
                                String customerID = appointmentToCustomer.get(appointmentID);
                                if (customerInsights.containsKey(customerID)) {
                                    customerInsights.get(customerID).feedbackCount++;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error reading feedback line: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read feedbacks file: " + e.getMessage());
            e.printStackTrace();
        }

        List<String[]> result = new ArrayList<>();
        for (CustomerInsight insight : customerInsights.values()) {
            if (insight.appointmentCount > 0) {  
                result.add(new String[]{
                    insight.name,
                    String.valueOf(insight.appointmentCount),
                    String.format("RM %.2f", insight.totalSpent),
                    String.valueOf(insight.appointmentCount),
                    String.valueOf(insight.feedbackCount)
                });
            }
        }

        return result;
    }

    public Map<String, String> getSummaryMetrics(LocalDate referenceDate, Period period) {
        Map<String, String> metrics = new LinkedHashMap<>();
        LocalDate startDate = period.getStartDate(referenceDate);
        LocalDate endDate = period.getEndDate(referenceDate);

        int totalAppointments = 0;
        int completedAppointments = 0;
        double totalRevenue = 0;
        double pendingRevenue = 0;
        int lineCount = 0;
        int matchedCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 8) {
                        try {
                            String dateStr = parts[4].trim();
                            String status = parts[6].trim();
                            double totalPrice = Double.parseDouble(parts[7].trim());

                            LocalDate appointmentDate = LocalDate.parse(dateStr, DATE_FORMAT);

                            if (isDateInRange(appointmentDate, startDate, endDate)) {
                                matchedCount++;
                                totalAppointments++;
                                totalRevenue += totalPrice;
                                if (status.equalsIgnoreCase("Completed")) {
                                    completedAppointments++;
                                } else if (status.equalsIgnoreCase("Pending")) {
                                    pendingRevenue += totalPrice;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing line: " + line + " - " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read appointments file: " + e.getMessage());
            e.printStackTrace();
        }

        double completionRate = totalAppointments > 0 ? (completedAppointments / (double) totalAppointments) * 100 : 0;
        double avgRevenue = totalAppointments > 0 ? totalRevenue / totalAppointments : 0;

        metrics.put("Total Revenue", String.format("RM %.2f", totalRevenue));
        metrics.put("Total Appointments", String.valueOf(totalAppointments));
        metrics.put("Avg Revenue/Appointment", String.format("RM %.2f", avgRevenue));
        metrics.put("Pending Amount", String.format("RM %.2f", pendingRevenue));
        metrics.put("Completion Rate", String.format("%.1f%%", completionRate));

        return metrics;
    }

    public List<String[]> getMonthlyBreakdown(LocalDate referenceDate, Period period) {
        Map<YearMonth, MonthlyStats> monthlyStats = new LinkedHashMap<>();
        LocalDate startDate = period.getStartDate(referenceDate);
        LocalDate endDate = period.getEndDate(referenceDate);

        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 8) {
                        String dateStr = parts[4].trim();
                        String status = parts[6].trim();
                        double totalPrice = Double.parseDouble(parts[7].trim());

                        LocalDate appointmentDate = LocalDate.parse(dateStr, DATE_FORMAT);

                        if (isDateInRange(appointmentDate, startDate, endDate)) {
                            YearMonth ym = YearMonth.from(appointmentDate);
                            if (!monthlyStats.containsKey(ym)) {
                                monthlyStats.put(ym, new MonthlyStats(ym));
                            }
                            MonthlyStats stats = monthlyStats.get(ym);
                            stats.totalAppointments++;
                            stats.totalRevenue += totalPrice;
                            if (status.equalsIgnoreCase("Completed")) {
                                stats.completedAppointments++;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String[]> result = new ArrayList<>();
        for (MonthlyStats stats : monthlyStats.values()) {
            result.add(new String[]{
                String.valueOf(stats.totalAppointments),
                String.valueOf(stats.completedAppointments),
                String.valueOf(stats.totalAppointments - stats.completedAppointments)
            });
        }
        return result;
    }

    private boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return (date.isEqual(startDate) || date.isAfter(startDate)) && (date.isEqual(endDate) || date.isBefore(endDate));
    }

    public LocalDate[] getEarliestAndLatestDates() {
        LocalDate earliestDate = LocalDate.now();
        LocalDate latestDate = LocalDate.of(2000, 1, 1);

        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        LocalDate dateFromFile = LocalDate.parse(parts[4].trim(), DATE_FORMAT);
                        if (dateFromFile.isBefore(earliestDate)) {
                            earliestDate = dateFromFile;
                        }
                        if (dateFromFile.isAfter(latestDate)) {
                            latestDate = dateFromFile;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading appointments file: " + e.getMessage());
            e.printStackTrace();
        }

        if (latestDate.isBefore(earliestDate)) {
            latestDate = LocalDate.now();
        }

        return new LocalDate[]{earliestDate, latestDate};
    }

    public List<YearMonth> generateMonthRange() {
        List<YearMonth> monthList = new ArrayList<>();
        LocalDate[] dateRange = getEarliestAndLatestDates();
        LocalDate earliest = dateRange[0];
        LocalDate latest = dateRange[1];

        YearMonth currentMonth = YearMonth.from(earliest);
        YearMonth endMonth = YearMonth.from(latest);

        while (!currentMonth.isAfter(endMonth)) {
            monthList.add(currentMonth);
            currentMonth = currentMonth.plusMonths(1);
        }

        return monthList;
    }

    public List<Integer> generateYearRange() {
        List<Integer> yearList = new ArrayList<>();
        LocalDate[] dateRange = getEarliestAndLatestDates();
        int earliestYear = dateRange[0].getYear();
        int latestYear = dateRange[1].getYear();

        for (int year = earliestYear; year <= latestYear; year++) {
            yearList.add(year);
        }

        return yearList;
    }

    private static class TechStats {
        String name;
        int completedCount = 0;
        int pendingCount = 0;
        double totalRevenue = 0;

        TechStats(String name) {
            this.name = name;
        }
    }

    private static class ServiceStats {
        String serviceName;
        int requestCount = 0;
        double completedRevenue = 0;

        ServiceStats(String serviceName) {
            this.serviceName = serviceName;
        }
    }

    private static class PaymentStats {
        String method;
        int completedCount = 0;
        int pendingCount = 0;
        double completedAmount = 0;
        double pendingAmount = 0;

        PaymentStats(String method) {
            this.method = method;
        }
    }

    private static class CustomerInsight {
        String name;
        int appointmentCount = 0;
        double totalSpent = 0;
        int feedbackCount = 0;
        LocalDate lastServiceDate = null;

        CustomerInsight(String name) {
            this.name = name;
        }
    }

    private static class MonthlyStats {
        YearMonth month;
        String monthName;
        int totalAppointments = 0;
        int completedAppointments = 0;
        double totalRevenue = 0;

        MonthlyStats(YearMonth month) {
            this.month = month;
            this.monthName = month.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        }
    }
}
