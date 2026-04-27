package com.stockpro.report.export;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PdfReportExporter implements ReportExporter {

    private static final DateTimeFormatter FILE_NAME_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter HEADER_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean supports(ReportFormat format) {
        return ReportFormat.PDF == format;
    }

    @Override
    public Path export(ReportType reportType,
            ReportFormat format,
            List<String> headers,
            List<List<String>> rows,
            Path exportDirectory) throws IOException {
        Files.createDirectories(exportDirectory);
        String fileName = reportType.name().toLowerCase() + "-" + FILE_NAME_TIMESTAMP.format(LocalDateTime.now()) + ".pdf";
        Path filePath = exportDirectory.resolve(fileName);

        try (FileOutputStream out = new FileOutputStream(filePath.toFile())) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph(reportType.name().replace("_", " ") + " REPORT", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Metadata
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Paragraph meta = new Paragraph("Generated on: " + HEADER_TIMESTAMP.format(LocalDateTime.now()), metaFont);
            meta.setAlignment(Paragraph.ALIGN_CENTER);
            meta.setSpacingAfter(20);
            document.add(meta);

            // Table
            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            // Headers
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(63, 81, 181)); // StockPro Blue
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Rows
            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            for (List<String> row : rows) {
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", rowFont));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
        }

        return filePath;
    }
}
