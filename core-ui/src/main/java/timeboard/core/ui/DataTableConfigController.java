package timeboard.core.ui;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import timeboard.core.api.DataTableService;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.DataTableConfig;
import timeboard.core.model.Project;
import timeboard.core.model.ProjectTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/config/datatable")
public class DataTableConfigController {

    @Autowired
    private DataTableService dataTableService;

    @Autowired
    private UserInfo userInfo;


    @GetMapping(value = "/{tableID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataTableConfigWrapper> listTags( @PathVariable String tableID ) {
        final Account actor = this.userInfo.getCurrentAccount();

        DataTableConfig dataTable = null;
        try {
            dataTable = this.dataTableService.findTableConfigByUserAndTable(tableID, actor);
        } catch (Exception e) {
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

    @PatchMapping(value="/{tableID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataTableConfig> patchTag(@PathVariable String tableID, @ModelAttribute DataTableConfigWrapper dataConfig) {
        final Account actor = this.userInfo.getCurrentAccount();
        DataTableConfig dataTableConfig = dataTableService.addOrUpdateTableConfig(tableID, actor, dataConfig.getColNames());
        return ResponseEntity.ok(dataTableConfig);
    }

    public static class DataTableConfigWrapper {

        private List<String> colNames;
        private String tableID;
        private Long userID;

        public DataTableConfigWrapper(DataTableConfig data) {
            this.colNames = data.getColumns();
            this.tableID = data.getTableInstanceId();
            this.userID = data.getUser().getId();
        }


        public List<String> getColNames() {
            return colNames;
        }

        public void setColNames(List<String> colNames) {
            this.colNames = colNames;
        }

        public String getTableID() {
            return tableID;
        }

        public void setTableID(String tableID) {
            this.tableID = tableID;
        }

        public Long getUserID() {
            return userID;
        }

        public void setUserID(Long userID) {
            this.userID = userID;
        }
    }

}
