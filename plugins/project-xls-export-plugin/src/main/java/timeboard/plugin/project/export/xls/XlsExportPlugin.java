package timeboard.plugin.project.export.xls;

/*-
 * #%L
 * project-export-plugin
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

import timeboard.core.api.ProjectExportService;
import timeboard.core.api.ProjectService;
import timeboard.core.model.Project;
import timeboard.core.model.Task;
import timeboard.core.model.User;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.io.OutputStream;

@Component(
        service = ProjectExportService.class,
        immediate = true
)
public class XlsExportPlugin implements ProjectExportService {


    @Reference
    private ProjectService projectService;

    @Override
    public String getMimeType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String getName() {
        return "Microsoft Excel";
    }

    @Override
    public void export(User actor, long projectID, OutputStream output) throws IOException {

        final Project project = this.projectService.getProjectByID(actor, projectID);

        String sheetName = project.getName();

       try(HSSFWorkbook wb = new HSSFWorkbook()) {
           HSSFSheet sheet = wb.createSheet(sheetName);

           HSSFRow headerRow = sheet.createRow(0);
           headerRow.createCell(0).setCellValue("Task name");
           headerRow.createCell(1).setCellValue("Task estimated work");

           int rowNum = 1;
           for (Task task : this.projectService.listProjectTasks(project)) {

               HSSFRow taskRow = sheet.createRow(rowNum);

               taskRow.createCell(0).setCellValue(task.getName());
               taskRow.createCell(1).setCellValue(task.getOriginalEstimate());
               rowNum++;
           }

           wb.write(output);
       }

    }

    @Override
    public String getExtension() {
        return "xlsx";
    }
}
