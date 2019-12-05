package timeboard.account;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import timeboard.core.model.TASData;

public class ExcelTASReport extends AbstractExcelReport {

    private static final String TEMPLATE_FILE = "/resources/templates/template-TAS_fr.xls";

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
        try (InputStream templatePath = this.getClass().getResourceAsStream(TEMPLATE_FILE)) {

            final POIFSFileSystem fs = new POIFSFileSystem(templatePath);
            this.wb = new HSSFWorkbook(fs, true);
            this.sheet = this.wb.getSheetAt(0);

            // Updating the year
            this.sheet.getRow(YEAR_ROW).getCell(YEAR_MONTH_COLUMN).setCellValue(tasData.getYear());
            // Updating the month
            this.sheet.getRow(MONTH_ROW).getCell(YEAR_MONTH_COLUMN).setCellValue(tasData.getMonth());
            // Updating matriculeID
            this.sheet.getRow(MATRICULE_ROW).getCell(MATRICULE_NAME_FIRSTNAME_COLUMN)
                    .setCellValue(tasData.getMatriculeID());
            this.sheet.getRow(NAME_ROW).getCell(MATRICULE_NAME_FIRSTNAME_COLUMN).setCellValue(tasData.getName());
            this.sheet.getRow(FIRSTNAME_ROW).getCell(MATRICULE_NAME_FIRSTNAME_COLUMN)
                    .setCellValue(tasData.getFirstName());
            this.sheet.getRow(BUSINESS_CODE).getCell(MATRICULE_NAME_FIRSTNAME_COLUMN)
                    .setCellValue(tasData.getBusinessCode());

            List<String> dayMonthName = tasData.getDayMonthNames();
            Map<Integer, BigDecimal> mapWorkedDAys = tasData.getWorkedDays();
            Map<Integer, BigDecimal> mapOffDays = tasData.getOffDays();
            Map<Integer, BigDecimal> mapOtherDays = tasData.getOtherDays();
            Map<Integer, String> mapComments = tasData.getComments();

            for (int i = 0; i < tasData.getDayMonthNames().size(); i++) {

                this.sheet.getRow(START_ROW_DAYS + i).getCell(DAY_NAME_COLLUMN).setCellValue(dayMonthName.get(i));
                // Les clés des maps commencent à 1 (jour dans le mois) et non pas 0
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
                if (currentDayName.equalsIgnoreCase(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendarSaturday).toLowerCase())
                        || currentDayName.equalsIgnoreCase(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendarSunday).toLowerCase())) {
                    for (int j = DAY_NAME_COLLUMN; j <= LAST_COLUMN; j++) {
                        // On recopie le style de la première ligne, qui est prédéfinie de la bonne façon
                        this.sheet.getRow(START_ROW_DAYS + i).getCell(j)
                                .setCellStyle(this.sheet.getRow(START_ROW_DAYS).getCell(j).getCellStyle());
                    }
                }
            }

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
}
