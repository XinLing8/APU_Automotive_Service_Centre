package ui.manager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;
import ui.common.StyleConfig;
import logic.ExcelExportUtil;

public class ReportPanel extends JPanel {
    private static final Color PANEL_BG = StyleConfig.GREY;
    private static final Color HEADER_COLOR = new Color(60, 72, 107);
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final Color BUTTON_COLOR = StyleConfig.BLUE;
    private static final Color BUTTON_TEXT_COLOR = StyleConfig.WHITE;
    private static final Color TABLE_HEADER = new Color(200, 220, 255);

    private Object dataManager;
    private boolean dataManagerAvailable;
    private JComboBox<YearMonth> monthCombo;
    private JComboBox<Integer> yearCombo;
    private LocalDate selectedDate;
    private String selectedPeriod;
    private JTabbedPane reportTabs;
    private JPanel summaryMetricsPanel;
    private boolean initializingFilters;

    public ReportPanel() {
        selectedDate = LocalDate.now();
        selectedPeriod = "MONTHLY";

        initializeDataManager();

        setLayout(new BorderLayout(10, 10));
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        reportTabs = createReportTabs();
        add(reportTabs, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(PANEL_BG);
        topPanel.add(createHeaderPanel());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(createSummaryPanel());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(createFilterPanel());

        add(topPanel, BorderLayout.NORTH);

        loadReports();
        updateSummaryMetrics();
    }

    private void initializeDataManager() {
        try {
            Class.forName("logic.Period");
            Class<?> managerClass = Class.forName("logic.ReportDataManager");
            dataManager = managerClass.getDeclaredConstructor().newInstance();
            dataManagerAvailable = true;
        } catch (Throwable t) {
            dataManager = null;
            dataManagerAvailable = false;
            logReportError(t);
        }
    }

    private void logReportError(Throwable t) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            String trace = sw.toString();
            File log = new File("error_log.txt");
            try (FileWriter fw = new FileWriter(log, true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter filePw = new PrintWriter(bw)) {
                filePw.println("---- " + java.time.LocalDateTime.now() + " ----");
                filePw.println(trace);
            }
        } catch (Exception ignored) {
            t.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T invokeReportDataManager(String methodName, Class<?>[] parameterTypes, Object... args) {
        if (!dataManagerAvailable || dataManager == null) {
            return null;
        }

        try {
            java.lang.reflect.Method method = dataManager.getClass().getMethod(methodName, parameterTypes);
            return (T) method.invoke(dataManager, args);
        } catch (Throwable t) {
            dataManagerAvailable = false;
            logReportError(t);
            return null;
        }
    }

    private Object resolveReportPeriod() {
        try {
            Class<?> periodClass = Class.forName("logic.Period");
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object period = Enum.valueOf((Class<? extends Enum>) periodClass.asSubclass(Enum.class), selectedPeriod);
            return period;
        } catch (Throwable t) {
            dataManagerAvailable = false;
            logReportError(t);
            return null;
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("View Reports");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createSummaryPanel() {
        summaryMetricsPanel = new JPanel(new GridLayout(1, 5, 15, 10));
        summaryMetricsPanel.setBackground(PANEL_BG);
        summaryMetricsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        summaryMetricsPanel.setPreferredSize(new Dimension(0, 80));

        for (int i = 0; i < 5; i++) {
            JPanel metricBox = createMetricBox("Loading...", "");
            summaryMetricsPanel.add(metricBox);
        }

        return summaryMetricsPanel;
    }

    private JPanel createMetricBox(String label, String value) {
        JPanel box = new JPanel(new BorderLayout(5, 5));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BUTTON_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 10));
        labelComponent.setForeground(TEXT_COLOR);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.BOLD, 14));
        valueComponent.setForeground(new Color(0, 102, 204));

        box.add(labelComponent, BorderLayout.NORTH);
        box.add(valueComponent, BorderLayout.CENTER);

        return box;
    }

    private void updateSummaryMetrics() {
        if (monthCombo == null || yearCombo == null) {
            return;
        }
        if (!dataManagerAvailable) {
            summaryMetricsPanel.removeAll();
            String[] metricNames = {"Total Revenue", "Total Appointments", "Avg Revenue/Appointment", "Pending Amount", "Completion Rate"};
            for (String metricName : metricNames) {
                summaryMetricsPanel.add(createMetricBox(metricName, "N/A"));
            }
            summaryMetricsPanel.revalidate();
            summaryMetricsPanel.repaint();
            return;
        }
        Object period = resolveReportPeriod();
        if (period == null) {
            return;
        }
        Map<String, String> metrics = invokeReportDataManager(
            "getSummaryMetrics",
            new Class<?>[]{LocalDate.class, period.getClass()},
            selectedDate,
            period
        );

        if (metrics == null) {
            metrics = new LinkedHashMap<>();
        }

        summaryMetricsPanel.removeAll();

        String[] metricNames = {"Total Revenue", "Total Appointments", "Avg Revenue/Appointment", "Pending Amount", "Completion Rate"};
        for (String metricName : metricNames) {
            JPanel metricBox = createMetricBox(metricName, metrics.getOrDefault(metricName, "N/A"));
            summaryMetricsPanel.add(metricBox);
        }
        summaryMetricsPanel.revalidate();
        summaryMetricsPanel.repaint();
    }

    private JPanel createFilterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(PANEL_BG);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        filterPanel.setBackground(PANEL_BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel monthLabel = new JLabel("Select Month:");
        monthLabel.setFont(new Font("Arial", Font.BOLD, 12));
        monthLabel.setForeground(TEXT_COLOR);

        monthCombo = new JComboBox<>();
        monthCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        monthCombo.setPreferredSize(new Dimension(180, 30));
        monthCombo.addActionListener(e -> {
            if (initializingFilters) {
                return;
            }
            selectedPeriod = "MONTHLY";
            if (monthCombo.getSelectedItem() != null) {
                YearMonth selectedMonth = (YearMonth) monthCombo.getSelectedItem();
                selectedDate = selectedMonth.atDay(1);
                loadReports();
                updateSummaryMetrics();
            }
        });

        JLabel yearLabel = new JLabel("Select Year:");
        yearLabel.setFont(new Font("Arial", Font.BOLD, 12));
        yearLabel.setForeground(TEXT_COLOR);

        yearCombo = new JComboBox<>();
        yearCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        yearCombo.setPreferredSize(new Dimension(100, 30));
        yearCombo.addActionListener(e -> {
            if (initializingFilters) {
                return;
            }
            selectedPeriod = "YEARLY";
            if (yearCombo.getSelectedItem() != null) {
                Integer selectedYear = (Integer) yearCombo.getSelectedItem();
                selectedDate = LocalDate.of(selectedYear, 1, 1);
                loadReports();
                updateSummaryMetrics();
            }
        });

        JButton refreshButton = createActionButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(120, 30));
        refreshButton.addActionListener(e -> {
            loadReports();
            updateSummaryMetrics();
        });

        JButton exportButton = createActionButton("Export Excel");
        exportButton.setPreferredSize(new Dimension(120, 30));
        exportButton.addActionListener(e -> exportReport());

        populateMonthYearDropdowns();

        filterPanel.add(monthLabel);
        filterPanel.add(monthCombo);
        filterPanel.add(yearLabel);
        filterPanel.add(yearCombo);
        filterPanel.add(refreshButton);
        filterPanel.add(exportButton);

        mainPanel.add(filterPanel, BorderLayout.NORTH);

        return mainPanel;
    }

    private void populateMonthYearDropdowns() {
        initializingFilters = true;

        List<YearMonth> months = invokeReportDataManager(
            "generateMonthRange",
            new Class<?>[]{}
        );
        if (months == null) {
            months = Collections.singletonList(YearMonth.from(LocalDate.now()));
        }
        for (YearMonth month : months) {
            monthCombo.addItem(month);
        }

        YearMonth defaultMonth = months.isEmpty() ? YearMonth.from(LocalDate.now()) : months.get(months.size() - 1);
        monthCombo.setSelectedItem(defaultMonth);

        List<Integer> years = invokeReportDataManager(
            "generateYearRange",
            new Class<?>[]{}
        );
        if (years == null) {
            years = Collections.singletonList(LocalDate.now().getYear());
        }
        for (Integer year : years) {
            yearCombo.addItem(year);
        }

        int defaultYear = years.isEmpty() ? LocalDate.now().getYear() : years.get(years.size() - 1);
        yearCombo.setSelectedItem(defaultYear);

        if (defaultMonth != null) {
            selectedDate = defaultMonth.atDay(1);
            selectedPeriod = "MONTHLY";
        }

        initializingFilters = false;

        loadReports();
        updateSummaryMetrics();
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        return button;
    }

    private JTabbedPane createReportTabs() {
        reportTabs = new JTabbedPane();
        reportTabs.setFont(new Font("Arial", Font.PLAIN, 12));

        reportTabs.addTab("Revenue Analytics", createRevenueTab());
        reportTabs.addTab("Monthly Breakdown", createMonthlyTab());
        reportTabs.addTab("Technician Performance", createTechnicianTab());
        reportTabs.addTab("Service Analytics", createServiceTab());
        reportTabs.addTab("Payment Status", createPaymentTab());
        reportTabs.addTab("Customer Insights", createCustomerTab());

        return reportTabs;
    }

    private JPanel createMonthlyTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Total Appointments", "Completed Count", "Pending Count"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        panel.putClientProperty("tableModel", model);
        panel.putClientProperty("loadFunction", (Runnable) () -> loadMonthlyData(model));

        return panel;
    }

    private void loadMonthlyData(DefaultTableModel model) {
        model.setRowCount(0);
        Object period = resolveReportPeriod();
        if (period == null) {
            return;
        }
        List<String[]> monthlyData = invokeReportDataManager(
            "getMonthlyBreakdown",
            new Class<?>[]{LocalDate.class, period.getClass()},
            selectedDate,
            period
        );

        if (monthlyData == null) {
            monthlyData = Collections.emptyList();
        }

        for (String[] row : monthlyData) {
            model.addRow(row);
        }
    }

    private JPanel createRevenueTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Metric", "Amount (RM)", "% of Total"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.putClientProperty("tableModel", model);
        panel.putClientProperty("loadFunction", (Runnable) () -> loadRevenueData(model));

        return panel;
    }

    private JPanel createTechnicianTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Technician Name", "Completed", "Pending", "Avg Hours", "Total Revenue (RM)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        panel.putClientProperty("tableModel", model);
        panel.putClientProperty("loadFunction", (Runnable) () -> loadTechnicianData(model));

        return panel;
    }

    private JPanel createServiceTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Service Type", "Request Count", "Total Revenue (RM)", "Avg Revenue (RM)", "Most Popular"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        panel.putClientProperty("tableModel", model);
        panel.putClientProperty("loadFunction", (Runnable) () -> loadServiceData(model));

        return panel;
    }

    private JPanel createPaymentTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Payment Method", "Paid Count", "Paid (RM)", "Pending Count", "Pending (RM)", "Collection Rate %"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        panel.putClientProperty("tableModel", model);
        panel.putClientProperty("loadFunction", (Runnable) () -> loadPaymentData(model));

        return panel;
    }

    private JPanel createCustomerTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Customer Name", "Appointment Count", "Total Spent (RM)", "Repeat Count", "Feedback Count"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        panel.putClientProperty("tableModel", model);
        panel.putClientProperty("loadFunction", (Runnable) () -> loadCustomerData(model));

        return panel;
    }

    private void loadReports() {
        if (monthCombo == null || yearCombo == null) {
            return;
        }

        String[] tabNames = {"Revenue Analytics", "Monthly Breakdown", "Technician Performance", "Service Analytics", "Payment Status", "Customer Insights"};
        for (int i = 0; i < reportTabs.getTabCount(); i++) {
            String tabName = tabNames.length > i ? tabNames[i] : "Tab " + i;
            JPanel tabPanel = (JPanel) reportTabs.getComponentAt(i);
            Runnable loadFunc = (Runnable) tabPanel.getClientProperty("loadFunction");
            if (loadFunc != null) {
                loadFunc.run();
            }
        }
    }

    private void loadRevenueData(DefaultTableModel model) {
        model.setRowCount(0);
        Object period = resolveReportPeriod();
        if (period == null) {
            return;
        }
        Map<String, Double> revenueData = invokeReportDataManager(
            "getTotalRevenueByPeriod",
            new Class<?>[]{LocalDate.class, period.getClass()},
            selectedDate,
            period
        );

        if (revenueData == null) {
            revenueData = new LinkedHashMap<>();
        }

        double total = revenueData.getOrDefault("Total Revenue", 0.0);
        double completed = revenueData.getOrDefault("Completed Revenue", 0.0);
        double pending = revenueData.getOrDefault("Pending Revenue", 0.0);

        double completedPct = total > 0 ? (completed / total) * 100 : 0;
        double pendingPct = total > 0 ? (pending / total) * 100 : 0;

        model.addRow(new Object[]{"Completed Revenue", String.format("RM %.2f", completed), String.format("%.1f%%", completedPct)});
        model.addRow(new Object[]{"Pending Revenue", String.format("RM %.2f", pending), String.format("%.1f%%", pendingPct)});
    }

    private void loadTechnicianData(DefaultTableModel model) {
        model.setRowCount(0);
        Object period = resolveReportPeriod();
        if (period == null) {
            return;
        }
        List<String[]> techData = invokeReportDataManager(
            "getTechnicianPerformance",
            new Class<?>[]{LocalDate.class, period.getClass()},
            selectedDate,
            period
        );

        if (techData == null) {
            techData = Collections.emptyList();
        }

        for (String[] row : techData) {
            model.addRow(row);
        }
    }

    private void loadServiceData(DefaultTableModel model) {
        model.setRowCount(0);
        Object period = resolveReportPeriod();
        if (period == null) {
            return;
        }
        List<String[]> serviceData = invokeReportDataManager(
            "getServiceAnalytics",
            new Class<?>[]{LocalDate.class, period.getClass()},
            selectedDate,
            period
        );

        if (serviceData == null) {
            serviceData = Collections.emptyList();
        }

        for (String[] row : serviceData) {
            model.addRow(row);
        }
    }

    private void loadPaymentData(DefaultTableModel model) {
        model.setRowCount(0);
        Object period = resolveReportPeriod();
        if (period == null) {
            return;
        }
        List<String[]> paymentData = invokeReportDataManager(
            "getPaymentStatus",
            new Class<?>[]{LocalDate.class, period.getClass()},
            selectedDate,
            period
        );

        if (paymentData == null) {
            paymentData = Collections.emptyList();
        }

        for (String[] row : paymentData) {
            model.addRow(row);
        }
    }

    private void loadCustomerData(DefaultTableModel model) {
        model.setRowCount(0);
        Object period = resolveReportPeriod();
        if (period == null) {
            return;
        }
        List<String[]> customerData = invokeReportDataManager(
            "getCustomerInsights",
            new Class<?>[]{LocalDate.class, period.getClass()},
            selectedDate,
            period
        );

        if (customerData == null) {
            customerData = Collections.emptyList();
        }

        for (String[] row : customerData) {
            model.addRow(row);
        }
    }

    private void exportReport() {
        try {
            String yearMonthString = getYearMonthString();
            
            String[] summaryLabels = {"Total Revenue", "Total Appointments", "Avg Revenue/Appointment", "Pending Amount", "Completion Rate"};
            String[] summaryValues = new String[5];
            
            for (int i = 0; i < summaryMetricsPanel.getComponentCount(); i++) {
                if (summaryMetricsPanel.getComponent(i) instanceof JPanel) {
                    JPanel metricBox = (JPanel) summaryMetricsPanel.getComponent(i);
                    Component[] components = metricBox.getComponents();
                    if (components.length >= 2 && components[1] instanceof JLabel) {
                        summaryValues[i] = ((JLabel) components[1]).getText();
                    }
                }
            }
            
            int selectedTabIndex = reportTabs.getSelectedIndex();
            JPanel selectedTab = (JPanel) reportTabs.getComponentAt(selectedTabIndex);
            String reportTitle = reportTabs.getTitleAt(selectedTabIndex);
            
            DefaultTableModel tableModel = (DefaultTableModel) selectedTab.getClientProperty("tableModel");
            if (tableModel == null) {
                JOptionPane.showMessageDialog(this, "No data available to export.", "Export Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String[] columnHeaders = new String[tableModel.getColumnCount()];
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                columnHeaders[i] = tableModel.getColumnName(i);
            }
            
            String[][] reportData = new String[tableModel.getRowCount()][];
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                reportData[i] = new String[tableModel.getColumnCount()];
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    Object value = tableModel.getValueAt(i, j);
                    reportData[i][j] = value != null ? value.toString() : "";
                }
            }
            
            String filePath = ExcelExportUtil.exportReportToExcel(yearMonthString, summaryLabels, summaryValues, reportTitle, columnHeaders, reportData);
            
            JOptionPane.showMessageDialog(this, 
                "Report exported successfully!\n\nFile: " + filePath, 
                "Export Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            System.err.println("Error exporting report: " + e.getMessage());
            e.printStackTrace();
            logReportError(e);
            JOptionPane.showMessageDialog(this, 
                "Error exporting report: " + e.getMessage(), 
                "Export Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getYearMonthString() {
        if (monthCombo != null && monthCombo.getSelectedItem() != null) {
            YearMonth selectedMonth = (YearMonth) monthCombo.getSelectedItem();
            return selectedMonth.getYear() + "-" + String.format("%02d", selectedMonth.getMonthValue());
        } else if (yearCombo != null && yearCombo.getSelectedItem() != null) {
            Integer selectedYear = (Integer) yearCombo.getSelectedItem();
            return selectedYear.toString();
        }
        return LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue());
    }
}
