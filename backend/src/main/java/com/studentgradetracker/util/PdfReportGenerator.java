package com.studentgradetracker.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.studentgradetracker.dto.ClassStatisticsResponse;
import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.dto.SubjectMarkResponse;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PdfReportGenerator {

    private static final Color NAVY_BLUE = new Color(30, 58, 138);
    private static final Color LIGHT_GRAY = new Color(243, 244, 246);
    private static final Color PASS_GREEN = new Color(16, 149, 106);
    private static final Color FAIL_RED = new Color(220, 38, 38);

    private PdfReportGenerator() {
    }

    public static byte[] generateClassReportPdf(List<StudentResponse> students, ClassStatisticsResponse stats, String className) {
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 25, 25);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Document Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, NAVY_BLUE);
            Paragraph title = new Paragraph("STUDENT GRADE MANAGEMENT SYSTEM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
            String classLabel = (className != null && !className.isBlank() && !className.equalsIgnoreCase("ALL"))
                    ? "Class: " + className : "All Classes";
            Paragraph subtitle = new Paragraph("Comprehensive Grade Report - " + classLabel, subTitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(8);
            document.add(subtitle);

            // Statistics Summary Banner
            if (stats != null) {
                PdfPTable summaryTable = new PdfPTable(5);
                summaryTable.setWidthPercentage(100);
                summaryTable.setSpacingAfter(15);

                addSummaryBox(summaryTable, "Total Students", String.valueOf(stats.getTotalStudents()));
                addSummaryBox(summaryTable, "Class Average", String.format("%.2f%%", stats.getAveragePercentage()));
                addSummaryBox(summaryTable, "Highest Marks", String.format("%.2f", stats.getHighestMarks()));
                addSummaryBox(summaryTable, "Lowest Marks", String.format("%.2f", stats.getLowestMarks()));
                addSummaryBox(summaryTable, "Pass Rate", String.format("%.2f%%", stats.getPassPercentage()));

                document.add(summaryTable);
            }

            // Student Table
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 1.8f, 3.5f, 1.5f, 1.5f, 1.5f, 1.8f, 1.2f, 1.5f});

            // Table Headers
            String[] headers = {"Rank", "Student ID", "Full Name", "Class", "Marks", "Max", "Percentage", "Grade", "Status"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                cell.setBackgroundColor(NAVY_BLUE);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);
            }

            // Table Data
            boolean alt = false;
            for (StudentResponse s : students) {
                Color rowBg = alt ? LIGHT_GRAY : Color.WHITE;
                alt = !alt;

                addTableCell(table, s.getRank() != null ? String.valueOf(s.getRank()) : "-", Element.ALIGN_CENTER, rowBg);
                addTableCell(table, s.getStudentId(), Element.ALIGN_CENTER, rowBg);
                addTableCell(table, s.getName(), Element.ALIGN_LEFT, rowBg);
                addTableCell(table, s.getClassName(), Element.ALIGN_CENTER, rowBg);
                addTableCell(table, s.getMarks() != null ? String.format("%.2f", s.getMarks()) : "0.00", Element.ALIGN_RIGHT, rowBg);
                addTableCell(table, s.getTotalMaxMarks() != null ? String.format("%.2f", s.getTotalMaxMarks()) : "100.00", Element.ALIGN_RIGHT, rowBg);
                addTableCell(table, String.format("%.2f%%", s.getPercentage()), Element.ALIGN_RIGHT, rowBg);
                addTableCell(table, s.getGrade(), Element.ALIGN_CENTER, rowBg);

                PdfPCell statusCell = new PdfPCell(new Phrase(s.getStatus(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, "PASS".equalsIgnoreCase(s.getStatus()) ? PASS_GREEN : FAIL_RED)));
                statusCell.setBackgroundColor(rowBg);
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                statusCell.setPadding(5);
                table.addCell(statusCell);
            }

            document.add(table);

            // Footer note
            Paragraph footer = new Paragraph("\nGenerated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + " | Official Grade Management Record", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY));
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error rendering class report PDF: " + e.getMessage(), e);
        }
    }

    public static byte[] generateIndividualStudentPdf(StudentResponse student) {
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Header Banner
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, NAVY_BLUE);
            Paragraph header = new Paragraph("STUDENT GRADE REPORT", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph sub = new Paragraph("Academic Performance & Official Transcript", FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY));
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(20);
            document.add(sub);

            // Student Information Box
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);

            addInfoRow(infoTable, "Student ID:", student.getStudentId());
            addInfoRow(infoTable, "Student Name:", student.getName());
            addInfoRow(infoTable, "Class / Section:", student.getClassName());
            addInfoRow(infoTable, "Email Address:", student.getEmail() != null ? student.getEmail() : "N/A");
            addInfoRow(infoTable, "Class Rank:", student.getRank() != null ? "Rank " + student.getRank() : "N/A");
            addInfoRow(infoTable, "Date Issued:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

            document.add(infoTable);

            // Subject marks or overall marks
            if (student.getSubjectMarks() != null && !student.getSubjectMarks().isEmpty()) {
                Paragraph sectionTitle = new Paragraph("Subject-Wise Breakdown", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, NAVY_BLUE));
                sectionTitle.setSpacingAfter(8);
                document.add(sectionTitle);

                PdfPTable marksTable = new PdfPTable(6);
                marksTable.setWidthPercentage(100);
                marksTable.setWidths(new float[]{1.5f, 3.5f, 1.5f, 1.5f, 1.5f, 1.5f});
                marksTable.setSpacingAfter(20);

                String[] subHeaders = {"Code", "Subject Name", "Max", "Pass Marks", "Obtained", "Result"};
                for (String sh : subHeaders) {
                    PdfPCell cell = new PdfPCell(new Phrase(sh, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                    cell.setBackgroundColor(NAVY_BLUE);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(6);
                    marksTable.addCell(cell);
                }

                for (SubjectMarkResponse sm : student.getSubjectMarks()) {
                    addTableCell(marksTable, sm.getSubjectCode(), Element.ALIGN_CENTER, Color.WHITE);
                    addTableCell(marksTable, sm.getSubjectName(), Element.ALIGN_LEFT, Color.WHITE);
                    addTableCell(marksTable, String.format("%.1f", sm.getMaxMarks()), Element.ALIGN_RIGHT, Color.WHITE);
                    addTableCell(marksTable, String.format("%.1f", sm.getPassMarks()), Element.ALIGN_RIGHT, Color.WHITE);
                    addTableCell(marksTable, String.format("%.1f", sm.getMarksObtained()), Element.ALIGN_RIGHT, Color.WHITE);

                    PdfPCell resCell = new PdfPCell(new Phrase(sm.getIsPassed() ? "PASS" : "FAIL",
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, sm.getIsPassed() ? PASS_GREEN : FAIL_RED)));
                    resCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    resCell.setPadding(5);
                    marksTable.addCell(resCell);
                }

                document.add(marksTable);
            }

            // Results Summary Card
            PdfPTable summary = new PdfPTable(4);
            summary.setWidthPercentage(100);
            summary.setSpacingAfter(25);

            addResultBox(summary, "Total Marks", String.format("%.2f / %.2f", student.getMarks(), student.getTotalMaxMarks()));
            addResultBox(summary, "Percentage", String.format("%.2f%%", student.getPercentage()));
            addResultBox(summary, "Final Grade", student.getGrade());
            addResultBox(summary, "Academic Status", student.getStatus());

            document.add(summary);

            // Signatures
            PdfPTable sigTable = new PdfPTable(2);
            sigTable.setWidthPercentage(100);
            sigTable.setSpacingBefore(40);

            PdfPCell sig1 = new PdfPCell(new Phrase("___________________________\nClass Teacher Signature", FontFactory.getFont(FontFactory.HELVETICA, 10)));
            sig1.setBorder(Rectangle.NO_BORDER);
            sig1.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell sig2 = new PdfPCell(new Phrase("___________________________\nPrincipal / Academic Head", FontFactory.getFont(FontFactory.HELVETICA, 10)));
            sig2.setBorder(Rectangle.NO_BORDER);
            sig2.setHorizontalAlignment(Element.ALIGN_CENTER);

            sigTable.addCell(sig1);
            sigTable.addCell(sig2);

            document.add(sigTable);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error rendering student report PDF: " + e.getMessage(), e);
        }
    }

    private static void addSummaryBox(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pLabel = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY));
        pLabel.setAlignment(Element.ALIGN_CENTER);
        Paragraph pValue = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, NAVY_BLUE));
        pValue.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(pLabel);
        cell.addElement(pValue);
        table.addCell(cell);
    }

    private static void addResultBox(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setPadding(10);
        cell.setBorderColor(NAVY_BLUE);

        Paragraph pLabel = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY));
        pLabel.setAlignment(Element.ALIGN_CENTER);

        Color valColor = NAVY_BLUE;
        if ("PASS".equalsIgnoreCase(value)) valColor = PASS_GREEN;
        else if ("FAIL".equalsIgnoreCase(value)) valColor = FAIL_RED;

        Paragraph pValue = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, valColor));
        pValue.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(pLabel);
        cell.addElement(pValue);
        table.addCell(cell);
    }

    private static void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, NAVY_BLUE)));
        cLabel.setBorder(Rectangle.BOTTOM);
        cLabel.setBorderColor(Color.LIGHT_GRAY);
        cLabel.setPadding(6);

        PdfPCell cVal = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK)));
        cVal.setBorder(Rectangle.BOTTOM);
        cVal.setBorderColor(Color.LIGHT_GRAY);
        cVal.setPadding(6);

        table.addCell(cLabel);
        table.addCell(cVal);
    }

    private static void addTableCell(PdfPTable table, String text, int align, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", FontFactory.getFont(FontFactory.HELVETICA, 9)));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(align);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
