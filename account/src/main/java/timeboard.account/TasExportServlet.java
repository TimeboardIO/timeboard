package timeboard.account;

/*-
 * #%L
 * webui
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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.osgi.service.component.annotations.*;
import timeboard.core.api.ProjectImportService;
import timeboard.core.api.UserService;
import timeboard.core.model.User;
import timeboard.core.ui.TimeboardServlet;
import timeboard.core.ui.ViewModel;

@Component(
        service = Servlet.class,
        scope = ServiceScope.PROTOTYPE,
        property = {
                "osgi.http.whiteboard.servlet.pattern=/account/exportTAS",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=timeboard)"
        }
)
//TIME ATTACHMENT SHEET
public class TasExportServlet extends TimeboardServlet {

    @Reference
    private UserService userService;

    @Reference(
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE,
            collectionType = CollectionType.SERVICE
    )
    private List<ProjectImportService> projectImportServlets;

    @Override
    protected ClassLoader getTemplateResolutionClassLoader() {
        return TasExportServlet.class.getClassLoader();
    }

    @Override
    protected void handlePost(User actor, HttpServletRequest request, HttpServletResponse response, ViewModel viewModel) throws ServletException, IOException {

        try{
            int month = Integer.parseInt(request.getParameter("month"));
            int year = Integer.parseInt(request.getParameter("year"));

            Calendar cal = Calendar.getInstance();
            cal.set(year, month-1, 1, 2, 0);

            try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
                final String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                final HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream("../timeboard/account/src/main/resources/templates/template-TAS_fr.xls")); //TODO FIXME add a relative path in karaf !! This won't work in production !!
                HSSFSheet sheet = wb.getSheet("Feuil1");

                Calendar start = Calendar.getInstance();
                start.set(year, month-1, 1, 2, 0);
                Calendar end = Calendar.getInstance();
                end.set(year, month, 1, 2, 0);


                HSSFPalette palette = wb.getCustomPalette();
                //replacing the standard red with freebsd.org red
                HSSFColor yellow = palette.findSimilarColor(255, 255, 153);
                HSSFColor lightYellow = palette.findSimilarColor(255, 255, 204);

                this.setCell(sheet, 6, 5, actor.getName());
                this.setCell(sheet, 7, 5, actor.getFirstName());

                this.setCell(sheet, 7, 10, month+"");
                this.setCell(sheet, 8, 10, year+"");

                int i = 0;
                for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {


                    Calendar currentDateCalendar = Calendar.getInstance();
                    currentDateCalendar.setTime(date);
                    HSSFCell cell = sheet.getRow(14 + i).getCell(1);
                    HSSFCellStyle cellStyle = cell.getCellStyle();
                    cellStyle.setFillForegroundColor(lightYellow.getIndex());
                    if (currentDateCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || currentDateCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ) {
                    }
                    this.setCell(sheet, 14 + i,1, new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date).toLowerCase());


                    i++;
                }

                wb.write(buf);

                String filename = "TAS_"+year+"_"+month+"_"+actor.getScreenName().replaceAll("'| |", "")+"_"+new Date().getTime();
                String sheetName = "TAS_"+year+"_"+month+"_"+actor.getScreenName().replaceAll("'| |", "");
                response.setContentLengthLong(buf.toByteArray().length);
                response.setHeader("Expires:", "0");
                response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xls");
                response.setContentType(mimeType);

                response.getOutputStream().write(buf.toByteArray());
                response.getOutputStream().flush();

               // response.setStatus(201);
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        response.sendRedirect("/account");


    }

    public void setCell(HSSFSheet sheet, int row, int cell, String value){
        HSSFRow headerRow = sheet.getRow(row);
        headerRow.getCell(cell).setCellValue(value);
    }

}


                /*
                int rowNum = 1;
                for (Task task : this.projectService.listProjectTasks(actor, project)) {

                    HSSFRow taskRow = sheet.createRow(rowNum);

                    taskRow.createCell(0).setCellValue(task.getName());
                    taskRow.createCell(1).setCellValue(task.getOriginalEstimate());
                    rowNum++;
                }*/
