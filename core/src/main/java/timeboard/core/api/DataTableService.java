package timeboard.core.api;

/*-
 * #%L
 * core
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

import java.util.Arrays;
import java.util.List;
import timeboard.core.model.DataTableConfig;
import timeboard.core.model.User;

/**
 * Service for datatable configs.
 */
public interface DataTableService {

    public static String TABLE_TASK_ID = "tableTask";
    public static List<String> ALL_COLUMNS_TABLE_TASK = Arrays.asList(
            /*"taskName", "taskComments",*/"startDate","endDate","originalEstimate","assignee","status","milestoneID","typeID");

    public String[] defaultCols = {"taskName"};

    boolean checkColumnDisplayed(String tableId, User actor, String colName);

    boolean checkColumnDisplayedFromDB(String tableId, User actor, String colName);
    
    DataTableConfig findTableConfigByUserAndTable(String tableId, User actor);

    DataTableConfig addOrUpdateTableConfig(String tableId, User actor, List<String> columnsNamesList);

}
