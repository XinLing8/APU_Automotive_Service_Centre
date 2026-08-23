package logic;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JOptionPane;

// Export Manager Report, CSV format
public class ExcelExportUtil {
    private static final String DEFAULT_EXPORT_DIR = System.getProperty("user.home") + "/Documents/Reports";

    public static String exportReportToExcel(String yearMonth, String[] summaryLabels, 
                                            String[] summaryValues, String reportTitle, 
                                            String[] columnHeaders, String[][] reportData) {
        return exportAsCSV(yearMonth, summaryLabels, summaryValues, reportTitle, columnHeaders, reportData);
    }
    
    private static String exportAsCSV(String yearMonth, String[] summaryLabels, 
                                      String[] summaryValues, String reportTitle, 
                                      String[] columnHeaders, String[][] reportData) {
        try {
            Files.createDirectories(Paths.get(DEFAULT_EXPORT_DIR));
            
            String filename = "Report_" + yearMonth.replace("-", "_") + ".csv";
            String filePath = DEFAULT_EXPORT_DIR + File.separator + filename;
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write("\"" + reportTitle + " - " + yearMonth + "\"");
                writer.newLine();
                writer.newLine();
                
                writer.write("\"Summary Metrics\"");
                writer.newLine();
                
                for (int i = 0; i < summaryLabels.length; i++) {
                    writer.write("\"" + summaryLabels[i] + "\",\"" + escapeCSV(summaryValues[i]) + "\"");
                    writer.newLine();
                }
                
                writer.newLine();
                
                writer.write("\"" + String.join("\",\"", columnHeaders) + "\"");
                writer.newLine();
                
                for (String[] row : reportData) {
                    StringBuilder line = new StringBuilder();
                    for (int i = 0; i < row.length; i++) {
                        if (i > 0) {
                            line.append(",");
                        }
                        line.append("\"").append(escapeCSV(row[i])).append("\"");
                    }
                    writer.write(line.toString());
                    writer.newLine();
                }
            }
            return filePath;
        } catch (Exception e) {
            System.err.println("Error exporting report: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Export failed: " + e.getMessage(), e);
        }
    }
    
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }
}
