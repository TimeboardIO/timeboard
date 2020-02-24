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

import timeboard.core.model.Account;
import timeboard.core.model.DataTableConfig;

import java.util.List;

/**
 * Service for datatable configs.
 */
public interface DataTableService {


    /**
     * Get a {@link DataTableConfig} object that represent a specific confication for a table instance and relevant to
     * actor
     * @param tableId
     * @param actor
     * @return an instance of {@link DataTableConfig}, else return null
     */
    DataTableConfig findTableConfigByUserAndTable(String tableId, Account actor);

    /**
     * Create a {@link DataTableConfig} object
     * @param tableId a unique identifier that represent user
     * @param actor account associated to new datatable config
     * @param columnsNamesList list of displayed columns
     * @return a instance of {@link DataTableConfig}
     */
    DataTableConfig addOrUpdateTableConfig(String tableId, Account actor, List<String> columnsNamesList);

}
