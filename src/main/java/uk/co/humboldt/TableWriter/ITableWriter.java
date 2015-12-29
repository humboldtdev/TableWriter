package uk.co.humboldt.TableWriter;


import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.List;

/**
 * Interface for writing either CSV or Excel tables.
 */
public interface ITableWriter extends Closeable {

    /**
     * Accept a row of headers. The last header row written before data will be taken as
     * the table headers in Excel.
     * @param headers List of header titles.
     */
    void writeHeaders(@Nonnull List<String> headers);

    /**
     * Accept a summary row. This can be used to provide summaries in CSV files. Excel files do
     * not currently display summaries..
     * @param summary List of summary fields titles.
     * @param subtotal If true, this is not the final summary, and may be omitted.
     */
    void writeSummary(@Nonnull List<Object> summary, boolean subtotal);

    /**
     * Accept a row of data objects. These will be stored in the most appropriate format.
     * @param data  List of data items.
     */
    void writeDataRow(@Nonnull List<Object> data);

    /**
     * If the output format supports freezing (Excel), freeze this many columns on the left.
     */
    void setFreezeColumns(int columns);

}
