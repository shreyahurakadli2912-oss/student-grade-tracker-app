package com.studentgradetracker.util;

import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.exception.BadRequestException;
import com.studentgradetracker.service.GradeCalculationService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class ExcelHelper {

    private ExcelHelper() {
    }

    public static byte[] generateStudentSampleExcel() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students Template");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"studentId", "name", "className", "marks"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Object[][] sampleData = {
                    {"S101", "Alice Johnson", "10A", 94.5},
                    {"S102", "Bob Smith", "10A", 82.0},
                    {"S103", "Charlie Brown", "10A", 74.0},
                    {"S104", "David Miller", "10A", 65.5},
                    {"S105", "Eva Green", "10A", 52.0}
            };

            for (int r = 0; r < sampleData.length; r++) {
                Row row = sheet.createRow(r + 1);
                row.createCell(0).setCellValue((String) sampleData[r][0]);
                row.createCell(1).setCellValue((String) sampleData[r][1]);
                row.createCell(2).setCellValue((String) sampleData[r][2]);
                row.createCell(3).setCellValue((Double) sampleData[r][3]);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating sample Excel template", e);
        }
    }

    public static List<String[]> parseExcel(InputStream inputStream) {
        List<String[]> records = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BadRequestException("Excel workbook contains no sheets.");
            }

            for (Row row : sheet) {
                // Determine row emptiness
                boolean hasContent = false;
                List<String> rowCells = new ArrayList<>();
                int lastCellNum = Math.max(row.getLastCellNum(), 4);

                for (int c = 0; c < lastCellNum; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String val = cell != null ? formatter.formatCellValue(cell).trim() : "";
                    if (!val.isEmpty()) {
                        hasContent = true;
                    }
                    rowCells.add(val);
                }

                if (hasContent) {
                    records.add(rowCells.toArray(new String[0]));
                }
            }
        } catch (Exception e) {
            throw new BadRequestException("Failed to read Excel (.xlsx) file: " + e.getMessage());
        }

        return records;
    }

    public static byte[] exportStudentsToExcel(List<StudentResponse> students, String title) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Grade Report");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Pass / Fail styles
            CellStyle passStyle = workbook.createCellStyle();
            Font passFont = workbook.createFont();
            passFont.setColor(IndexedColors.GREEN.getIndex());
            passFont.setBold(true);
            passStyle.setFont(passFont);

            CellStyle failStyle = workbook.createCellStyle();
            Font failFont = workbook.createFont();
            failFont.setColor(IndexedColors.RED.getIndex());
            failFont.setBold(true);
            failStyle.setFont(failFont);

            String[] columns = {"Rank", "Student ID", "Full Name", "Class", "Marks", "Total Max", "Percentage (%)", "Grade", "Status"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            double sumPercentage = 0.0;
            for (StudentResponse s : students) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(s.getRank() != null ? s.getRank() : 0);
                row.createCell(1).setCellValue(FormulaSanitizer.sanitize(s.getStudentId()));
                row.createCell(2).setCellValue(FormulaSanitizer.sanitize(s.getName()));
                row.createCell(3).setCellValue(FormulaSanitizer.sanitize(s.getClassName()));
                row.createCell(4).setCellValue(s.getMarks() != null ? s.getMarks() : 0.0);
                row.createCell(5).setCellValue(s.getTotalMaxMarks() != null ? s.getTotalMaxMarks() : 100.0);
                row.createCell(6).setCellValue(s.getPercentage());
                row.createCell(7).setCellValue(s.getGrade());

                Cell statusCell = row.createCell(8);
                statusCell.setCellValue(s.getStatus());
                if ("PASS".equalsIgnoreCase(s.getStatus())) {
                    statusCell.setCellStyle(passStyle);
                } else {
                    statusCell.setCellStyle(failStyle);
                }

                sumPercentage += s.getPercentage();
            }

            // Summary Row
            if (!students.isEmpty()) {
                Row summaryRow = sheet.createRow(rowIdx);
                CellStyle summaryStyle = workbook.createCellStyle();
                Font summaryFont = workbook.createFont();
                summaryFont.setBold(true);
                summaryStyle.setFont(summaryFont);
                summaryStyle.setBorderTop(BorderStyle.DOUBLE);

                Cell labelCell = summaryRow.createCell(2);
                labelCell.setCellValue("CLASS AVERAGE");
                labelCell.setCellStyle(summaryStyle);

                Cell avgCell = summaryRow.createCell(6);
                double avg = GradeCalculationService.roundTwoDecimals(sumPercentage / students.size());
                avgCell.setCellValue(avg);
                avgCell.setCellStyle(summaryStyle);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }
}
