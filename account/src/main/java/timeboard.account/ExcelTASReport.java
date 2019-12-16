package timeboard.account;

/*-
 * #%L
 * account
 * %%
 * Copyright (C) 2019 Timeboard
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import timeboard.core.model.TASData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelTASReport extends AbstractExcelReport {

    private static final String TEMPLATE_FILE = "template-TAS_fr.xls";

    private static final int START_ROW_DAYS = 14;

    private static final int SUMS_ROW = 45;
    private static final int DAY_NAME_COLLUMN = 1;
    private static final int WORKED_DAY_COLUMN = 3;
    private static final int OFF_DAY_COLUMN = 4;
    private static final int OTHER_DAY_COLUMN = 5;
    private static final int COMMENTS_COLUMN = 7;
    private static final int YEAR_ROW = 8;
    private static final int YEAR_MONTH_COLUMN = 10;
    private static final int MONTH_ROW = 7;
    private static final int MATRICULE_ROW = 5;
    private static final int NAME_ROW = 6;
    private static final int FIRSTNAME_ROW = 7;
    private static final int BUSINESS_CODE = 8;
    private static final int MATRICULE_NAME_FIRSTNAME_COLUMN = 5;
    private static final int LAST_COLUMN = 11;

    private static final int MONTH_DAY_COUNT = 31;

    public ExcelTASReport(final OutputStream reportFile) {
        this.reportFile = reportFile;
    }

    public void generateFAT(final TASData tasData) throws IOException {
        this.format = true;
        try (InputStream templatePath = this.getClass().getClassLoader().getResourceAsStream(TEMPLATE_FILE)) {

            final POIFSFileSystem fs = new POIFSFileSystem(templatePath);
            this.wb = new HSSFWorkbook(fs, true);
            this.sheet = this.wb.getSheetAt(0);

            // Updating the year
            this.sheet.getRow(YEAR_ROW).getCell(YEAR_MONTH_COLUMN).setCellValue(tasData.getYear());

            // Updating the month
            this.sheet.getRow(MONTH_ROW).getCell(YEAR_MONTH_COLUMN)
                    .setCellValue("1/" + tasData.getMonth() + "/" + tasData.getYear());

            // Updating matriculeID
            updateMatriculeID(tasData);

            // Hide the excess lines for months that do not have 31 days
            int i = MONTH_DAY_COUNT;
            while (tasData.getDayMonthNames().size() < i) {
                this.sheet.getRow((START_ROW_DAYS + i) - 1).setHeight((short) 0);
                i--;
            }

            // Refresh totals formulas
            this.sheet.getRow(SUMS_ROW).getCell(WORKED_DAY_COLUMN).setCellFormula("SUM(D15:D45)");
            this.sheet.getRow(SUMS_ROW).getCell(OFF_DAY_COLUMN).setCellFormula("SUM(E15:E45)");
            this.sheet.getRow(SUMS_ROW).getCell(OTHER_DAY_COLUMN).setCellFormula("SUM(F15:F45)");

            this.save();


        }
    }

    private void updateMatriculeID(TASData tasData) {
        this.sheet.getRow(MATRICULE_ROW).getCell(MATRICULE_NAME_FIRSTNAME_COLUMN)
                .setCellValue(tasData.getMatriculeID());
        this.sheet.getRow(NAME_ROW).getCell(MATRICULE_NAME_FIRSTNAME_COLUMN).setCellValue(tasData.getName());
        this.sheet.getRow(FIRSTNAME_ROW).getCell(MATRICULE_NAME_FIRSTNAME_COLUMN)
                .setCellValue(tasData.getFirstName());
        this.sheet.getRow(BUSINESS_CODE).getCell(MATRICULE_NAME_FIRSTNAME_COLUMN)
                .setCellValue(tasData.getBusinessCode());

        List<String> dayMonthName = tasData.getDayMonthNames();
        Map<Integer, Double> mapWorkedDAys = tasData.getWorkedDays();
        Map<Integer, Double> mapOffDays = tasData.getOffDays();
        Map<Integer, Double> mapOtherDays = tasData.getOtherDays();
        Map<Integer, String> mapComments = tasData.getComments();

        for (int i = 0; i < tasData.getDayMonthNames().size(); i++) {

            this.sheet.getRow(START_ROW_DAYS + i).getCell(DAY_NAME_COLLUMN).setCellValue(dayMonthName.get(i));
            // Map keys start at 1 (day in the month) and not 0
            if (mapWorkedDAys.containsKey(i + 1)) {
                this.sheet.getRow(START_ROW_DAYS + i).getCell(WORKED_DAY_COLUMN)
                        .setCellValue(mapWorkedDAys.get(i + 1).doubleValue());
            }
            if (mapOffDays.containsKey(i + 1)) {
                this.sheet.getRow(START_ROW_DAYS + i).getCell(OFF_DAY_COLUMN)
                        .setCellValue(mapOffDays.get(i + 1).doubleValue());
            }
            if (mapOtherDays.containsKey(i + 1)) {
                this.sheet.getRow(START_ROW_DAYS + i).getCell(OTHER_DAY_COLUMN)
                        .setCellValue(mapOtherDays.get(i + 1).doubleValue());
            }
            if (mapComments.containsKey(i + 1)) {
                this.sheet.getRow(START_ROW_DAYS + i).getCell(COMMENTS_COLUMN)
                        .setCellValue(mapComments.get(i + 1));
            }

            // Apply the dark yellow style for the first day of the month and weekends
            // The first day of the template month is already dark yellow
            Calendar calendarSaturday = new GregorianCalendar();
            calendarSaturday.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            Calendar calendarSunday = new GregorianCalendar();
            calendarSunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

            String currentDayName = tasData.getDayMonthNames().get(i);
            if (currentDayName.equalsIgnoreCase(
                    new SimpleDateFormat("EEEE", Locale.ENGLISH)
                            .format(calendarSaturday.getTime()).toLowerCase())
                    || currentDayName.equalsIgnoreCase(new SimpleDateFormat("EEEE", Locale.ENGLISH)
                    .format(calendarSunday.getTime()).toLowerCase())) {
                for (int j = DAY_NAME_COLLUMN; j <= LAST_COLUMN; j++) {
                    // We copy the style of the first line, which is predefined in the right way
                    this.sheet.getRow(START_ROW_DAYS + i).getCell(j)
                            .setCellStyle(this.sheet.getRow(START_ROW_DAYS).getCell(j).getCellStyle());
                }
            }
        }
    }
}
