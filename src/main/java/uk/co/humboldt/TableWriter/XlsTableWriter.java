package uk.co.humboldt.TableWriter;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.joda.time.base.AbstractInstant;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  Export CSV results as Excel
 */
public class XlsTableWriter implements ITableWriter {

    private final OutputStream stream;
    private final XSSFWorkbook workbook;
    private final XSSFSheet sheet;
    private int row;
    private CellStyle dateStyle;

    /**
     * When collecting a table section, these is the header row.
     */
    private int table_header_row;

    private List<String> table_headers;

    private XSSFTable table;

    public XlsTableWriter(OutputStream stream) {
        this.stream = stream;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat((short)0x16);
        row = 0;
    }

    /**
     * Sets the number of leftmost columns to freeze.
     */
    private int freezeColumns;

    public int getFreezeColumns() {
        return freezeColumns;
    }

    @Override
    public void setFreezeColumns(int freezeColumns) {
        this.freezeColumns = freezeColumns;
    }

    @Override
    public void writeDataRow(@Nonnull List<Object> data) {
        if (table == null && table_headers != null)
            start_table();


        XSSFRow dataRow = sheet.createRow(row++);
        int column = 0;
        for(Object o: data) {
            XSSFCell cell = dataRow.createCell(column++);
            storeObject(cell, o);
        }

    }

    @Override
    public void writeHeaders(@Nonnull List<String> headers) {

        if (table == null) {
            table_headers = new ArrayList<>(headers);
            table_header_row = row;

            XSSFRow headerRow = sheet.createRow(row);
            row++;


            int column = 0;
            Set<String> usedColumns = new HashSet<>();
            for(String h: headers) {
                XSSFCell cell = headerRow.createCell(column++);
                if (h == null || h.isEmpty())
                    h = "Column " + column;

                if (usedColumns.contains(h.toUpperCase()))
                    h = h + " " + column;

                usedColumns.add(h.toUpperCase());
                cell.setCellValue(h);
            }
        }
    }

    @Override
    public void writeSummary(@Nonnull List<Object> summary, boolean subtotal) {
        if (! subtotal && table != null)
            end_table();

        // Don't write summary rows to XLS output - leave for Excel to generate
    }

    private  void storeObject(XSSFCell cell, Object object) {
        if (object != null) {

            if (object instanceof Number) {
                cell.setCellValue(((Number)object).doubleValue());
            } else if (object instanceof AbstractInstant) {
                cell.setCellValue(((AbstractInstant) object).toDate());
                cell.setCellStyle(dateStyle);
            } else {
                cell.setCellValue(object.toString());
            }
        }
    }

    /**
     * Call to start a table section
     */
    private void start_table() {
        table = sheet.createTable();
        table.setDisplayName("Data");
        CTTable ctTable = table.getCTTable();
        ctTable.setDisplayName("Data");
        ctTable.setId(1L);
        ctTable.setName("DATA");
            /* Let us define the required Style for the table */
        CTTableStyleInfo table_style = ctTable.addNewTableStyleInfo();
        table_style.setName("TableStyleMedium9");

        /* Set Table Style Options */
        table_style.setShowColumnStripes(false); //showColumnStripes=0
        table_style.setShowRowStripes(true); //showRowStripes=1

        CTTableColumns ctColumns = ctTable.addNewTableColumns();
        CTAutoFilter autofilter = ctTable.addNewAutoFilter();
        ctColumns.setCount(table_headers.size()); //define number of columns

        XSSFRow headerRow = sheet.getRow(table_header_row);

        for(int i = 0; i < table_headers.size(); i++) {
            CTTableColumn column = ctColumns.addNewTableColumn();
            String name = headerRow.getCell(i).getStringCellValue();
            column.setId(i + 1);
            column.setName(name);
            CTFilterColumn filter = autofilter.addNewFilterColumn();
            filter.setColId(i + 1);
            filter.setShowButton(true);
        }
    }

    /**
     * Call to end a table section.
     */
    private void end_table() {
        CTTable ctTable = table.getCTTable();

        AreaReference my_data_range = new AreaReference(new CellReference(table_header_row, 0),
                new CellReference(row - 1, table_headers.size() - 1));

        /* Set Range to the Table */
        ctTable.setRef(my_data_range.formatAsString());

        sheet.createFreezePane(freezeColumns, table_header_row + 1);
        for(int i = 0; i < table_headers.size(); i++) {
            sheet.autoSizeColumn(i);
            // Include width of drop down button
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }

        table = null;
    }

    @Override
    public void close() throws IOException {

        if (table != null)
            end_table();


        workbook.write(stream);
    }
}
