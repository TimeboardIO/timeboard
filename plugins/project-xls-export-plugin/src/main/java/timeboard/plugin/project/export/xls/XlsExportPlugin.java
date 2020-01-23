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

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.ProjectExportService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.model.Task;

import java.io.IOException;
import java.io.OutputStream;


@Component
public class XlsExportPlugin implements ProjectExportService {


    @Autowired
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
    public void export(final Account actor, final long orgID, final long projectID, final OutputStream output) throws IOException, BusinessException {

        final Project project = this.projectService.getProjectByID(actor, orgID, projectID);

        final String sheetName = project.getName();

        try (final HSSFWorkbook wb = new HSSFWorkbook()) {
            final HSSFSheet sheet = wb.createSheet(sheetName);

            final HSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Task name");
            headerRow.createCell(1).setCellValue("Task Original Estimate");

            int rowNum = 1;
            for (final Task task : this.projectService.listProjectTasks(actor, project)) {

                final HSSFRow taskRow = sheet.createRow(rowNum);

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
