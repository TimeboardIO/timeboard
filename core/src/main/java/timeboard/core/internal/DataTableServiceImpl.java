package timeboard.core.internal;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.DataTableService;
import timeboard.core.model.Account;
import timeboard.core.model.DataTableConfig;
import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;


@Component
@Transactional
public class DataTableServiceImpl implements DataTableService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private EntityManager em;

    public DataTableServiceImpl() {
    }

    @Override
    public boolean checkColumnDisplayed(String tableId, Account actor, String colName){

        boolean isDefault = Arrays.stream(defaultCols)
                .map(s -> s.equals(colName))
                .reduce(false, (aBoolean, aBoolean2) -> aBoolean || aBoolean2);

        return isDefault || checkColumnDisplayedFromDB(tableId, actor, colName);
    }

    @Override
    public boolean checkColumnDisplayedFromDB(String tableId, Account actor, String colName) {
        DataTableConfig tableConfig = findTableConfigByUserAndTable(tableId, actor);
        if(tableConfig == null){
            return false;
        }
        return tableConfig.getColumns().contains(colName);
    }

    @Override
    public DataTableConfig findTableConfigByUserAndTable(String tableId, Account actor) {
        TypedQuery<DataTableConfig> q = this.em
                .createQuery("select d from DataTableConfig d where d.user=:user and d.tableInstanceId=:tableId", DataTableConfig.class);
        q.setParameter("user", actor);
        q.setParameter("tableId", tableId);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public DataTableConfig addOrUpdateTableConfig(String tableId, Account actor, List<String> columnsNamesList) {
            DataTableConfig datatableConfig = this.findTableConfigByUserAndTable(tableId, actor);
            if (datatableConfig != null) {
                datatableConfig.setUser(actor);
                datatableConfig.setTableInstanceId(tableId);
                datatableConfig.setColumns(columnsNamesList);
            }else{
                datatableConfig = new DataTableConfig();
                datatableConfig.setUser(actor);
                datatableConfig.setTableInstanceId(tableId);
                datatableConfig.setColumns(columnsNamesList);
                this.em.persist(datatableConfig);
            }
            this.em.flush();
            LOGGER.info("DataTableConfig " + datatableConfig.getUser() + "/" + datatableConfig.getTableInstanceId() +" updated.");
            return datatableConfig;
    }

}
