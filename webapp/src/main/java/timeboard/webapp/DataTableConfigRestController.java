package timeboard.webapp;

/*-
 * #%L
 * projects
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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.DataTableService;
import timeboard.core.model.Account;
import timeboard.core.model.DataTableConfig;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Component
@RestController
@RequestMapping(value = "/api/datatable", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataTableConfigRestController {

    @Autowired
    private DataTableService dataTableService;

    @GetMapping
    public ResponseEntity<DataTableConfigWrapper> getConfig(final TimeboardAuthentication authentication, final HttpServletRequest request) {
        final Account actor = authentication.getDetails();

        final String tableID = request.getParameter("tableID");

        DataTableConfig dataTable = null;
        try {
            dataTable = this.dataTableService.findTableConfigByUserAndTable(tableID, actor);
        } catch (final Exception e) {
            // nothing to do except handle exception
        }
        if (dataTable == null) {
            dataTable = new DataTableConfig();
            dataTable.setUser(actor);
            dataTable.setTableInstanceId(tableID);
            dataTable.setColumns(new ArrayList<>());
        }
        return ResponseEntity.ok(new DataTableConfigWrapper(dataTable));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateConfig(final TimeboardAuthentication authentication, @RequestBody final DataTableConfigWrapper dataConfig) {
        final Account actor = authentication.getDetails();

        final DataTableConfig dataTableConfig = dataTableService.addOrUpdateTableConfig(dataConfig.tableID, actor, dataConfig.getColNames());
        return ResponseEntity.ok(new DataTableConfigWrapper(dataTableConfig));
    }

    public static class DataTableConfigWrapper implements Serializable {

        public List<String> colNames = new ArrayList<String>();
        public String tableID;
        public Long userID;

        public DataTableConfigWrapper() {
            colNames = new ArrayList<String>();
        }

        public DataTableConfigWrapper(final DataTableConfig data) {
            this.colNames = data.getColumns();
            this.tableID = data.getTableInstanceId();
            this.userID = data.getUser().getId();
        }


        public List<String> getColNames() {
            return colNames;
        }

        public void setColNames(final List<String> colNames) {
            this.colNames = colNames;
        }

        public String getTableID() {
            return tableID;
        }

        public void setTableID(final String tableID) {
            this.tableID = tableID;
        }

        public Long getUserID() {
            return userID;
        }

        public void setUserID(final Long userID) {
            this.userID = userID;
        }
    }

}
