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

import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.*;
import timeboard.core.model.*;


@Component
@Transactional
public class ReportServiceImpl implements ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Autowired
    private EntityManager em;


    @Override
    public Report createReport(Account owner, String reportName, Account organization, List<Project> projects, ReportType type) {
        Account ownerAccount = this.em.find(Account.class, owner.getId());
        Report newReport = new Report();
        newReport.setOrganization(organization);
        newReport.setName(reportName);
        newReport.setProjects(projects);
        newReport.setType(type);
        em.persist(newReport);

        em.flush();

        LOGGER.info("Report " + reportName + " created by user " + owner.getId());
        return newReport;
    }

    @Override
    public List<Report> listReports(Account owner) {
        TypedQuery<Report> query = em.createQuery("select r from Report r where r.organization = :owner", Report.class);
        query.setParameter("owner", owner);
        return query.getResultList();
    }

    @Override
    public Report updateReport(Account owner, Report report) {
        em.merge(report);
        em.flush();
        return report;
    }
}
