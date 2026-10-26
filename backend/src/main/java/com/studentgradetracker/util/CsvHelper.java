package com.studentgradetracker.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.studentgradetracker.dto.BulkImportRowPreview;
import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.exception.BadRequestException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class CsvHelper {

    private CsvHelper() {
    }

    public static byte[] generateStudentSampleCsv() {
        String csvContent = "studentId,name,className,marks\n" +
                "S101,Alice Johnson,10A,94.5\n" +
                "S102,Bob Smith,10A,82\n" +
                "S103,Charlie Brown,10A,74\n" +
                "S104,David Miller,10A,65.5\n" +
                "S105,Eva Green,10A,52\n";
        return csvContent.getBytes(StandardCharsets.UTF_8);
    }

    public static List<String[]> parseCsv(InputStream inputStream) {
        List<String[]> records = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                // Skip empty lines
                if (line.length == 0 || (line.length == 1 && line[0].trim().isEmpty())) {
                    continue;
                }
                records.add(line);
            }
        } catch (IOException | CsvValidationException e) {
            throw new BadRequestException("Failed to read CSV file: " + e.getMessage());
        }
        return records;
    }

    public static byte[] exportStudentsToCsv(List<StudentResponse> students) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            // Header
            writer.writeNext(new String[]{"Rank", "Student ID", "Full Name", "Class", "Marks", "Total Max", "Percentage (%)", "Grade", "Status"});

            for (StudentResponse s : students) {
                writer.writeNext(new String[]{
                        s.getRank() != null ? String.valueOf(s.getRank()) : "-",
                        FormulaSanitizer.sanitize(s.getStudentId()),
                        FormulaSanitizer.sanitize(s.getName()),
                        FormulaSanitizer.sanitize(s.getClassName()),
                        s.getMarks() != null ? String.format("%.2f", s.getMarks()) : "0.00",
                        s.getTotalMaxMarks() != null ? String.format("%.2f", s.getTotalMaxMarks()) : "100.00",
                        String.format("%.2f", s.getPercentage()),
                        s.getGrade(),
                        s.getStatus()
                });
            }

            writer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating CSV export", e);
        }
    }

    public static byte[] generateRejectedRowsCsv(List<BulkImportRowPreview> rejectedRows) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            writer.writeNext(new String[]{"Row Number", "Student ID", "Name", "Class", "Marks", "Status", "Error Reasons"});

            for (BulkImportRowPreview row : rejectedRows) {
                String errorReason = String.join(" | ", row.getErrors());
                writer.writeNext(new String[]{
                        String.valueOf(row.getRowNumber()),
                        FormulaSanitizer.sanitize(row.getStudentId()),
                        FormulaSanitizer.sanitize(row.getName()),
                        FormulaSanitizer.sanitize(row.getClassName()),
                        row.getMarks() != null ? String.valueOf(row.getMarks()) : "",
                        row.getStatus(),
                        FormulaSanitizer.sanitize(errorReason)
                });
            }

            writer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating rejected rows CSV", e);
        }
    }
}
