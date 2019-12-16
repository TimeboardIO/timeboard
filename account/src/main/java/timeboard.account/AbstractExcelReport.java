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

import org.apache.commons.lang.StringUtils;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractExcelReport {
    /* logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExcelReport.class);
    protected final DateFormat dateformatter = new SimpleDateFormat("dd/MM/yyyy");
    private final Map<String, HSSFCellStyle> styles = new HashMap<>();
    protected HSSFSheet sheet;
    protected HSSFWorkbook wb;
    protected OutputStream reportFile;
    protected boolean format = false;

    /**
     * Liste des noms d'onglet utilisé dans excel, il ne peut pas y avoir de doublon.
     */
    private List<String> sheetNames = new ArrayList<>();

    /**
     * Update row reference in formula (not implemented in POI).
     */
    private static void updateRownumInFormula(final HSSFCell cell, final int oldRownum, final int newRownum) {
        try {
            cell.setCellFormula(updateRownumInFormula(cell.getCellFormula(), oldRownum, newRownum));
        } catch (Exception e) {
            LOGGER.error("problème lors de la mise à jour des formules du rapport excel", e);
            setCellComment(cell, "Erreur : " + e.getMessage());
        }
    }

    /**
     * Update row reference in formula (not implemented in POI).
     */
    private static String updateRownumInFormula(final String formula, final int oldRownum, final int newRownum) {
        return formula.replaceAll("([A-Z]{1,2})" + oldRownum, "$1" + newRownum);
    }

    /**
     * Delete row (not implemented in POI).
     */
    private static void deleteRow(final HSSFRow row) {
        int rowIndex = row.getRowNum();
        HSSFSheet parentSheet = row.getSheet();
        parentSheet.removeRow(row); // this only deletes all the cell values

        int lastRowNum = parentSheet.getLastRowNum();

        if ((rowIndex >= 0) && (rowIndex < lastRowNum)) {
            parentSheet.shiftRows(rowIndex + 1, lastRowNum, -1);
        }
    }

    /**
     * Add comment to cell (not implemented in POI).
     */
    private static void setCellComment(final Cell cell, final String message) {
        if (cell != null) {
            Drawing drawing = cell.getSheet().createDrawingPatriarch();
            CreationHelper factory = cell.getSheet().getWorkbook().getCreationHelper();
            // When the comment box is visible, have it show in a 1x3 space
            ClientAnchor anchor = factory.createClientAnchor();
            anchor.setCol1(cell.getColumnIndex());
            anchor.setCol2(cell.getColumnIndex() + 1);
            anchor.setRow1(cell.getRowIndex());
            anchor.setRow2(cell.getRowIndex() + 1);
            anchor.setDx1(100);
            anchor.setDx2(100);
            anchor.setDy1(100);
            anchor.setDy2(100);

            // Create the comment and set the text+author
            Comment comment = drawing.createCellComment(anchor);
            RichTextString str = factory.createRichTextString(message);
            comment.setString(str);
            comment.setAuthor("Apache POI");
            // Assign the comment to the cell
            cell.setCellComment(comment);
        }
    }

    private static void setCellType(final HSSFCell oldCell, final HSSFCell newCell) {
        // Set the cell data value
        switch (oldCell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                newCell.setCellValue(oldCell.getRichStringCellValue());
                break;
            default:
                break;
        }
    }

    /**
     * Given a decimal number returns a percentage equivalent of the number as string.
     */
    protected static String doubleToPercentage(final double value) {
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf = new DecimalFormat("#00.00%");
        return nf.format(value);
    }

    /**
     * Group Rows.
     */
    public static void groupRows(final HSSFSheet sheet, final int startRowNum, final int endRowNum,
                                 final boolean collapsed) {

        if ((startRowNum != 0) && (startRowNum <= endRowNum)) {
            sheet.groupRow(startRowNum, endRowNum);
            if (collapsed) {
                sheet.setRowGroupCollapsed(startRowNum, true);
            }
        }
    }

    public static void updateCellStyle(final HSSFCell cell, final HSSFCellStyle style) {
        // HSSFCellStyle cellStyle = cell.getCellStyle();
        // cellStyle.setAlignment(style.getAlignment());
        // cellStyle.setFillForegroundColor(style.getFillForegroundColor());
        // cellStyle.setFillPattern(style.getFillPattern());
        // cellStyle.setFont(style.getFont(this.wb));
        cell.setCellStyle(style);
    }

    public static void addURLHyperlink(final HSSFCell cell, final String address) {
        CreationHelper createHelper = cell.getSheet().getWorkbook().getCreationHelper();
        Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
        link.setAddress(address);
    }

    /**
     * Permet de mettre une formule dans un champ en vérifiant que ce n'est pas un des caractères spéciaux utilisés dans
     * les dashboards.
     */
    public static void setNumberInCell(final Cell cell, final String data) {
        // si la valeur contient des caractères spéciaux
        if (data.contains("NaN") || data.equalsIgnoreCase("-")) {
            cell.setCellValue(data);
        } else {
            double valueDouble;
            // si c'est un pourcentage
            if (StringUtils.contains(data, "%")) {
                String[] splitData = data.split("%");
                // on enlève le % et on divise par 100 car les cellules du template sont pré-formatées en pourcentage
                valueDouble = Double.parseDouble(splitData[0]) / 100;
            } else {
                valueDouble = Double.parseDouble(data);
            }
            cell.setCellValue(valueDouble);
        }
    }

    /**
     * Save current workbook in <code>this.reportFile</code>.
     */
    protected void save() throws IOException {
        try (OutputStream out = this.reportFile) {

            if (this.wb == null) {
                this.sheet.getWorkbook().write(out);
            } else {
                this.wb.write(out);
            }
        }
    }

    /**
     * Copy row to destination (not implemented in POI).
     * use <code>shiftRows()</code> if a row exists in destinationRowNum, else use <code>createRow()</code>
     */
    private void copyRow(final int sourceRowNum, final int destinationRowNum) {
        this.copyRow(this.sheet.getRow(sourceRowNum), destinationRowNum);
    }

    /**
     * Copy row to destination (not implemented in POI).
     * use <code>shiftRows()</code> if a row exists in destinationRowNum, else use <code>createRow()</code>
     */
    private void copyRow(final HSSFRow sourceRow, final int destinationRowNum) {

        HSSFSheet parentSheet = sourceRow.getSheet();
        HSSFRow newRow = parentSheet.getRow(destinationRowNum);
        // If the row exist in destination, push down all rows by 1 else create
        // a new row
        if (newRow != null) {
            parentSheet.shiftRows(destinationRowNum, parentSheet.getLastRowNum(), 1);
        } else {
            newRow = parentSheet.createRow(destinationRowNum);
        }

        // Loop through source columns to add to new row
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            // Grab a copy of the old/new cell
            final HSSFCell oldCell = sourceRow.getCell(i);
            HSSFCell newCell = newRow.createCell(i);

            // If the old cell is null jump to next cell
            if (oldCell == null) {
                newCell = null;
                continue;
            }
            // Copy style from old cell and apply to new cell
            // final HSSFCellStyle newCellStyle = this.wb.createCellStyle();
            // newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
            newCell.setCellStyle(oldCell.getCellStyle());
            // If there is a cell comment, copy
            if (newCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }
            // If there is a cell hyperlink, copy
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }
            // Set the cell data type
            newCell.setCellType(oldCell.getCellType());
            if (this.format) {
                setCellType(oldCell, newCell);
            }

        }
        // If there are are any merged regions in the source row, copy to new
        // row
        for (int i = 0; i < parentSheet.getNumMergedRegions(); i++) {
            final CellRangeAddress cellRangeAddress = parentSheet.getMergedRegion(i);
            if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
                final CellRangeAddress newCellRangeAddress =
                        new CellRangeAddress(
                                newRow.getRowNum(),
                                (newRow.getRowNum() + (cellRangeAddress.getFirstRow() - cellRangeAddress.getLastRow())),
                                cellRangeAddress.getFirstColumn(), cellRangeAddress.getLastColumn());
                parentSheet.addMergedRegion(newCellRangeAddress);
            }
        }
    }

    /**
     * Replace existing color with custom color.
     */
    public void setCustomColor(final short colorToReplace, final int red, final int green, final int blue) {
        // creating a custom palette for the workbook
        HSSFPalette palette = this.wb.getCustomPalette();
        palette.setColorAtIndex(colorToReplace, (byte) red, (byte) green, (byte) blue);

    }

    public Font createFont(final short hssfColorIndex, final int height, final boolean bold) {
        HSSFFont newFont = this.wb.createFont();
        newFont.setFontHeightInPoints((short) height);
        newFont.setColor(hssfColorIndex);
        if (bold) {
            newFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        }
        return newFont;
    }

    protected void updateCellBackgroundColor(final HSSFCell cell, final HSSFColor color) {
        HSSFCellStyle initStyle = cell.getCellStyle();
        String styleName = initStyle.getUserStyleName().split("_")[0] + color.getClass().getSimpleName();
        HSSFCellStyle newStyle = this.styles.get(styleName);
        if (newStyle == null) {
            newStyle = this.wb.createCellStyle();
            newStyle.cloneStyleFrom(initStyle);
            newStyle.setFillBackgroundColor(color.getIndex());
        } else {
            cell.setCellStyle(newStyle);
        }
    }

    public HSSFCellStyle createStyle(final short hssfColorIndex, final Font font) {
        HSSFCellStyle style = this.wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFillForegroundColor(hssfColorIndex);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(font);
        return style;
    }
}
