package uk.co.humboldt.TableWriter;

import com.opencsv.CSVWriter;
import org.joda.time.DateTimeZone;
import org.joda.time.base.AbstractInstant;
import org.joda.time.format.DateTimeFormat;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * CSV Implementation of tables
 */
public class CsvTableWriter implements ITableWriter {

    private final CSVWriter writer;

    public CsvTableWriter(OutputStream out) {

        writer = new CSVWriter(new OutputStreamWriter(out));

    }

    @Override
    public void writeDataRow(@Nonnull List<Object> data) {

        String[] row = new String[data.size()];

        for(int i = 0; i < data.size(); i++) {

            Object o = data.get(i);
            if (o instanceof AbstractInstant) {
                row[i] = DateTimeFormat.mediumDateTime().withZone(DateTimeZone.getDefault()).print((AbstractInstant)o);
            } else if (o != null) {
                row[i] = o.toString();
            }
        }

        writer.writeNext(row);

    }

    @Override
    public void writeHeaders(@Nonnull List<String> headers) {

        writer.writeNext(headers.toArray(new String[headers.size()]));

    }

    @Override
    public void writeSummary(@Nonnull List<Object> summary, boolean subtotal) {
        writeDataRow(summary);
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    @Override
    public void setFreezeColumns(int columns) {
        // CSV cannot freeze columns. Ignore
    }
}
