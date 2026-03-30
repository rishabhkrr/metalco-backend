package com.indona.invento.services.impl;

import com.indona.invento.entities.CertificateOfConfidenceEntity;
import com.indona.invento.entities.CocLineItemEntity;
import com.indona.invento.services.CoCPdfGenerationService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class CoCPdfGenerationServiceImpl implements CoCPdfGenerationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    
    // Standardized font sizes
    private static final float REGULAR_FONT_SIZE = 10f;
    private static final float HEADER_FONT_SIZE = 11f;
    private static final float TITLE_FONT_SIZE = 18f;
    private static final float COMPANY_NAME_FONT_SIZE = 18f;

    @Override
    public ByteArrayOutputStream generateCoCPdf(CertificateOfConfidenceEntity coc) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);

            // Set page size to A4 Landscape for more width
            pdfDoc.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4.rotate());

            Document document = new Document(pdfDoc);
            document.setMargins(15, 15, 15, 15);

            // Main table with 10 columns to match Excel grid with fixed widths
            float[] columnWidths = {10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f}; // Equal width columns
            Table mainTable = new Table(columnWidths);
            mainTable.setWidth(UnitValue.createPercentValue(100));
            mainTable.setFixedLayout(); // Use fixed layout for consistent cell sizes

            // Row 1: Company Name Header - METALCO EXTRUSIONS GLOBAL LLP (merged across all columns)
            Cell companyNameCell = new Cell(1, 10).add(new Paragraph("Metalco Extrusions Global LLP")
                    .setFontSize(COMPANY_NAME_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            companyNameCell.setBackgroundColor(ColorConstants.WHITE);
            companyNameCell.setPadding(5);
            mainTable.addCell(companyNameCell);

            // Row 2-4: Title - CERTIFICATE OF CONFORMANCE (merged across all columns)
            Cell titleCell = new Cell(3, 10).add(new Paragraph("CERTIFICATE OF CONFORMANCE")
                    .setFontSize(TITLE_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            titleCell.setBackgroundColor(ColorConstants.WHITE);
            titleCell.setPadding(8);
            mainTable.addCell(titleCell);

            // Row 5-7: Company Header (merged across all columns)
            Cell companyCell = new Cell(3, 10)
                    .add(new Paragraph("PLOT NO A-17 [PLOT NO.493]1ST CROSS 10 MAIN ROAD PEENYA 3RD STAGE")
                            .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER))
                    .add(new Paragraph("INDUSTRIAL AREA NALLAKADARAN HALLIYESHWANTHPURA HOBLI BANGALORE-560058")
                            .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER))
                    .add(new Paragraph("GSTIN: 29ABZ FM4786D1ZE")
                            .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER).setUnderline());
            companyCell.setBackgroundColor(ColorConstants.WHITE);
            companyCell.setPadding(3);
            mainTable.addCell(companyCell);

            // Row 8-9: COC NO and DATE
            Cell cocNoLabel = new Cell(2, 2).add(new Paragraph("COC NO:")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            cocNoLabel.setBackgroundColor(ColorConstants.WHITE);
            cocNoLabel.setPadding(3);
            mainTable.addCell(cocNoLabel);

            Cell cocNoValue = new Cell(2, 5).add(new Paragraph(coc.getCocNumber())
                    .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            cocNoValue.setBackgroundColor(ColorConstants.WHITE);
            cocNoValue.setPadding(3);
            mainTable.addCell(cocNoValue);

            Cell dateLabel = new Cell(2, 1).add(new Paragraph("DATE")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            dateLabel.setBackgroundColor(ColorConstants.WHITE);
            dateLabel.setPadding(3);
            mainTable.addCell(dateLabel);

            Cell dateValue = new Cell(2, 2).add(new Paragraph(coc.getTimestamp().format(DATE_FORMATTER))
                    .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            dateValue.setBackgroundColor(ColorConstants.WHITE);
            dateValue.setPadding(3);
            mainTable.addCell(dateValue);

            // Row 10: Headers
            Cell invoiceHeader = new Cell(1, 2).add(new Paragraph("INVOICE NO &DATE")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            invoiceHeader.setBackgroundColor(ColorConstants.WHITE);
            invoiceHeader.setPadding(3);
            mainTable.addCell(invoiceHeader);

            Cell dispatchHeader = new Cell(1, 3).add(new Paragraph("DISPATCH QTY IN KGS")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            dispatchHeader.setBackgroundColor(ColorConstants.WHITE);
            dispatchHeader.setPadding(3);
            mainTable.addCell(dispatchHeader);

            Cell poHeader = new Cell(1, 3).add(new Paragraph("PO NO & DATE")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            poHeader.setBackgroundColor(ColorConstants.WHITE);
            poHeader.setPadding(3);
            mainTable.addCell(poHeader);

            Cell poQtyHeader = new Cell(1, 2).add(new Paragraph("PO QTY IN KGS")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            poQtyHeader.setBackgroundColor(ColorConstants.WHITE);
            poQtyHeader.setPadding(3);
            mainTable.addCell(poQtyHeader);

            // Row 11: Data
            Cell invoiceData = new Cell(1, 2).add(new Paragraph(coc.getInvoiceNumber() + "\n" + coc.getTimestamp().format(DATE_FORMATTER))
                    .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            invoiceData.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(invoiceData);

            // Extract only the numeric part from dispatchedQuantity to avoid company name contamination
            String cleanDispatchQuantity = extractNumericValue(coc.getDispatchedQuantity());
            Cell dispatchData = new Cell(1, 3).add(new Paragraph(cleanDispatchQuantity + " " )
                    .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            dispatchData.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(dispatchData);

            // Format: PO NO-B/2425/10381/PO DT-1.6.2024 OR just PO DT-1.6.2024 if no PO number
            String poInfo = "";
            
            // Add PO Number if exists
            if (coc.getCustomerPONumber() != null && !coc.getCustomerPONumber().isEmpty()) {

                // First line: PO Number
                poInfo = "PO NO - " + coc.getCustomerPONumber();

                // Second line: PO Date (only if PO NO exists)
                if (coc.getCustomerPODate() != null) {
                    poInfo += "\nPO DATE - " +
                            coc.getCustomerPODate().format(DateTimeFormatter.ofPattern("d.M.yyyy"));
                }
            }

            // If PO NO is missing → leave poInfo empty (show blank cell)
            Cell poData = new Cell(1, 3).add(new Paragraph(poInfo)
                    .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            poData.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(poData);

            Cell poQtyData = new Cell(1, 2).add(new Paragraph(coc.getCustomerPOQuantity() != null ? coc.getCustomerPOQuantity() + " KGS" : "")
                    .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            poQtyData.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(poQtyData);

            // Row 12: SOLD TO / SHIP TO
            Cell soldToHeader = new Cell(1, 5).add(new Paragraph("SOLD TO")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            soldToHeader.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(soldToHeader);

            Cell shipToHeader = new Cell(1, 5).add(new Paragraph("SHIP TO")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            shipToHeader.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(shipToHeader);

            // Row 13-18: Address lines from Customer Master
            // SOLD TO - Billing Address
            String billingAddress = coc.getCustomerBillingAddress() != null ? coc.getCustomerBillingAddress() : "";
            String[] soldToLines = billingAddress.split("\n");

            // SHIP TO - Shipping Address
            String shippingAddress = coc.getCustomerShippingAddress() != null ? coc.getCustomerShippingAddress() : "";
            String[] shipToLines = shippingAddress.split("\n");

            // Get max lines to display (up to 6 lines)
            int maxLines = Math.max(soldToLines.length, shipToLines.length);
            maxLines = Math.min(maxLines, 6); // Limit to 6 lines

            for (int i = 0; i < maxLines; i++) {
                // SOLD TO line
                String soldToText = i < soldToLines.length ? soldToLines[i] : "";
                Cell soldToLine = new Cell(1, 5).add(new Paragraph(soldToText)
                        .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
                soldToLine.setBackgroundColor(ColorConstants.WHITE);
                mainTable.addCell(soldToLine);

                // SHIP TO line
                String shipToText = i < shipToLines.length ? shipToLines[i] : "";
                Cell shipToLine = new Cell(1, 5).add(new Paragraph(shipToText)
                        .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
                shipToLine.setBackgroundColor(ColorConstants.WHITE);
                mainTable.addCell(shipToLine);
            }

            // Row 19-20: Line Items Header
            Cell itemDescHeader = new Cell(2, 3).add(new Paragraph("ITEM DESCRIPTION /DIMENSION")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            itemDescHeader.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(itemDescHeader);

            Cell gradeHeader = new Cell(2, 2).add(new Paragraph("GRADE & TEMPER")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            gradeHeader.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(gradeHeader);

            Cell kgsHeader = new Cell(2, 1).add(new Paragraph("KGS")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER).setUnderline());
            kgsHeader.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(kgsHeader);

            Cell millHeader = new Cell(2, 2).add(new Paragraph("MILL / SUPPLIERS")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            millHeader.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(millHeader);

            Cell heatNoHeader = new Cell(2, 2).add(new Paragraph("L/C NO /\nHEAT NO")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            heatNoHeader.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(heatNoHeader);

            // Row 22-23: Line Items Data
            if (coc.getLineItems() != null && !coc.getLineItems().isEmpty()) {
                for (CocLineItemEntity item : coc.getLineItems()) {
                    Cell itemDesc = new Cell(1, 3).add(new Paragraph(item.getDimension())
                            .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
                    itemDesc.setBackgroundColor(ColorConstants.WHITE);
                    mainTable.addCell(itemDesc);

                    Cell grade = new Cell(1, 2).add(new Paragraph(item.getGrade() + " " + item.getTemper())
                            .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
                    grade.setBackgroundColor(ColorConstants.WHITE);
                    mainTable.addCell(grade);

                    Cell kgs = new Cell(1, 1).add(new Paragraph(item.getQuantityKg() + " KGS")
                            .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER).setUnderline());
                    kgs.setBackgroundColor(ColorConstants.WHITE);
                    mainTable.addCell(kgs);

                    Cell mill = new Cell(1, 2).add(new Paragraph(item.getBrand())
                            .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
                    mill.setBackgroundColor(ColorConstants.WHITE);
                    mainTable.addCell(mill);

                    Cell heatNo = new Cell(1, 2).add(new Paragraph(item.getCocNumber() != null ? item.getCocNumber() : "")
                            .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
                    heatNo.setBackgroundColor(ColorConstants.WHITE);
                    mainTable.addCell(heatNo);
                }
            }

            // Empty rows 24
            for (int i = 0; i < 10; i++) {
                Cell emptyCell = new Cell(1, 1).add(new Paragraph(" ").setFontSize(REGULAR_FONT_SIZE));
                emptyCell.setBackgroundColor(ColorConstants.WHITE);
                mainTable.addCell(emptyCell);
            }

            // Row 25-30: Declaration Section
            // ME PROCESSING: SUPPLIER + REPRESENTATIVE NAME / TITLE - Combined into ONE cell (3 columns, 6 rows)
            Cell processingRepCell = new Cell(6, 3)
                    .add(new Paragraph("ME PROCESSING : SUPPLIER")
                            .setFontSize(HEADER_FONT_SIZE).setBold())
                    .add(new Paragraph("REPRESENTATIVE NAME / TITLE:")
                            .setFontSize(HEADER_FONT_SIZE).setBold());
            processingRepCell.setBackgroundColor(ColorConstants.WHITE);
            processingRepCell.setMinHeight(120); // Increased height for signature space
            processingRepCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.TOP);
            mainTable.addCell(processingRepCell);

            // Date - 2 columns, 1 row
            Cell dateCell = new Cell(1, 2).add(new Paragraph("Date:-" + coc.getTimestamp().format(DATE_FORMATTER))
                    .setFontSize(REGULAR_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            dateCell.setBackgroundColor(ColorConstants.WHITE);
            mainTable.addCell(dateCell);

            // STAMP - 2 columns, 6 rows (with increased height for stamping)
            Cell stampCell = new Cell(6, 2).add(new Paragraph("STAMP")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            stampCell.setBackgroundColor(ColorConstants.WHITE);
            stampCell.setMinHeight(120); // Increased height for stamp space
            stampCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.TOP);
            mainTable.addCell(stampCell);

            // DECLARATION - 3 columns, 6 rows
            String declarationText = coc.getDeclaration() != null && !coc.getDeclaration().isEmpty() 
                ? coc.getDeclaration() 
                : "DECLARATION:- I here by certify that all materials , Processes and products deliveredunder this certification are in compliance with all requirements of the specifications stated in the refernce as per EN 573-3 & EN485-2 Standard";
            Cell declarationCell = new Cell(6, 3).add(new Paragraph(declarationText)
                    .setFontSize(REGULAR_FONT_SIZE).setBold());
            declarationCell.setBackgroundColor(ColorConstants.WHITE);
            declarationCell.setMinHeight(120); // Match stamp height
            mainTable.addCell(declarationCell);

            // REMARKS - 2 columns, 5 rows (with increased height for remarks/signatures)
            Cell remarksCell = new Cell(5, 2).add(new Paragraph("REMARKS")
                    .setFontSize(HEADER_FONT_SIZE).setBold().setTextAlignment(TextAlignment.CENTER));
            remarksCell.setBackgroundColor(ColorConstants.WHITE);
            remarksCell.setMinHeight(100); // Increased height for remarks space
            remarksCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.TOP);
            mainTable.addCell(remarksCell);

            document.add(mainTable);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }

        return baos;
    }

    @Override
    public ByteArrayOutputStream generateLineItemCoCPdf(CertificateOfConfidenceEntity coc, CocLineItemEntity lineItem) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Title
            document.add(new Paragraph("CERTIFICATE OF CONFIDENCE - LINE ITEM")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // CoC Header Information
            document.add(new Paragraph("CoC Number: " + lineItem.getCocNumber()).setBold());
            document.add(new Paragraph("Generated Date: " + lineItem.getCocTimestamp().format(DATE_FORMATTER)));

            document.add(new Paragraph("\n"));

            // Bill Summary Section
            document.add(new Paragraph("BILL SUMMARY").setBold().setFontSize(12));
            Table billTable = new Table(2);
            billTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(billTable, "SO Number:", coc.getSoNumber());
            addTableRow(billTable, "Invoice Number:", coc.getInvoiceNumber());
            addTableRow(billTable, "Unit:", coc.getUnit());

            document.add(billTable);
            document.add(new Paragraph("\n"));

            // Customer Information
            document.add(new Paragraph("CUSTOMER INFORMATION").setBold().setFontSize(12));
            Table customerTable = new Table(2);
            customerTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(customerTable, "Customer Code:", coc.getCustomerCode());
            addTableRow(customerTable, "Customer Name:", coc.getCustomerName());
            addTableRow(customerTable, "Email:", coc.getCustomerEmail());
            addTableRow(customerTable, "Phone:", coc.getCustomerPhone());

            document.add(customerTable);
            document.add(new Paragraph("\n"));

            // Line Item Details
            document.add(new Paragraph("LINE ITEM DETAILS").setBold().setFontSize(12));
            Table itemTable = new Table(2);
            itemTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(itemTable, "Line Number:", lineItem.getLineNumber());
            addTableRow(itemTable, "Product Category:", lineItem.getProductCategory());
            addTableRow(itemTable, "Item Description:", lineItem.getItemDescription());
            addTableRow(itemTable, "Brand:", lineItem.getBrand());
            addTableRow(itemTable, "Grade:", lineItem.getGrade());
            addTableRow(itemTable, "Temper:", lineItem.getTemper());
            addTableRow(itemTable, "Dimension:", lineItem.getDimension());
            addTableRow(itemTable, "Quantity (Kg):", lineItem.getQuantityKg());

            document.add(itemTable);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }

        return baos;
    }

    private void addGreenHeaderCell(Table table, String header) {
        Cell cell = new Cell().add(new Paragraph(header).setBold().setFontSize(HEADER_FONT_SIZE));
        cell.setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(144, 238, 144)); // Light green
        table.addCell(cell);
    }

    private void addGreenDataCell(Table table, String value) {
        Cell cell = new Cell().add(new Paragraph(value != null ? value : "").setFontSize(REGULAR_FONT_SIZE).setBold());
        cell.setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(144, 238, 144)); // Light green
        table.addCell(cell);
    }

    private void addHeaderCell(Table table, String header) {
        Cell cell = new Cell().add(new Paragraph(header).setBold().setFontSize(HEADER_FONT_SIZE));
        cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        table.addCell(cell);
    }

    private void addDataCell(Table table, String value) {
        Cell cell = new Cell().add(new Paragraph(value != null ? value : "").setFontSize(REGULAR_FONT_SIZE).setBold());
        table.addCell(cell);
    }

    private void addTableRow(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label).setBold());
        Cell valueCell = new Cell().add(new Paragraph(value != null ? value : "N/A"));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Extract only the numeric value from a string that might contain additional text
     * This handles cases where quantity field contains company name or other text
     */
    private String extractNumericValue(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "0";
        }
        
        // Use regex to extract the first decimal number from the string
        String numericPattern = "\\d+(?:\\.\\d+)?";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(numericPattern);
        java.util.regex.Matcher matcher = pattern.matcher(input.trim());
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        // If no numeric value found, return "0"
        return "0";
    }
}

