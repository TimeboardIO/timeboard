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
import timeboard.core.api.ProjectSnapshotService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.*;
import java.util.Calendar;
import org.apache.commons.math3.stat.regression.SimpleRegression;

@Component
@Transactional
public class ProjectSnapshotServiceImpl implements ProjectSnapshotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

    /* nombre de jours de projection pour la regression*/
    private static final int projection_days = 180;

    @Autowired
    private ProjectServiceImpl projectService;

    @Autowired
    private EntityManager em;

    @Override
    public ProjectSnapshot createProjectSnapshot(final Account actor, final Project project) throws BusinessException {
        try {
            final List<Task> listProjectTasks = this.projectService.listProjectTasks(actor, project);
            final ProjectSnapshot projectSnapshot = new ProjectSnapshot();
            listProjectTasks.forEach(task -> {
                final TaskSnapshot taskSnapshot = new TaskSnapshot(new Date(), task, actor);
                projectSnapshot.getTaskSnapshots().add(taskSnapshot);
                projectSnapshot.setOriginalEstimate(projectSnapshot.getOriginalEstimate() + taskSnapshot.getOriginalEstimate());
                projectSnapshot.setEffortLeft(projectSnapshot.getEffortLeft() + taskSnapshot.getEffortLeft());
                projectSnapshot.setEffortSpent(projectSnapshot.getEffortSpent() + taskSnapshot.getEffortSpent());
            });
            projectSnapshot.setQuotation(project.getQuotation());
            projectSnapshot.setProjectSnapshotDate(new Date());
            return projectSnapshot;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
            throw new BusinessException(e);
        }
    }

    @Override
    public List<TaskSnapshot> findAllTaskSnapshotByTaskID(final Account actor, final Long taskID) {
        final TypedQuery<TaskSnapshot> q = em
                .createQuery("select t from TaskSnapshot t left join fetch t.task where "
                        + "t.task.id = :taskID"
                        + "group by t.task "
                        + "having max(t.snapshotDate)", TaskSnapshot.class);
        q.setParameter("taskID", taskID);
        return q.getResultList();
    }

    @Override
    public void regression(ProjectSnapshotGraphWrapper wrapper, List<String> listOfProjectSnapshotDates, List<ProjectSnapshot> projectSnapshotList){

        final SimpleRegression quotationRegression = new SimpleRegression();
        final SimpleRegression originalEstimateRegression = new SimpleRegression();
        final SimpleRegression realEffortRegression = new SimpleRegression();
        final SimpleRegression effortLeftRegression = new SimpleRegression();
        final SimpleRegression effortSpentRegression = new SimpleRegression();
        final List<Double> quotationRegressionPoints = new ArrayList<>();
        final List<Double> originalEstimateRegressionPoints = new ArrayList<>();
        final List<Double> realEffortRegressionPoints = new ArrayList<>();
        final List<Double> effortLeftRegressionPoints = new ArrayList<>();
        final List<Double> effortSpentRegressionPoints = new ArrayList<>();
        projectSnapshotList.forEach(snapshot -> {
            quotationRegression.addData(snapshot.getProjectSnapshotDate().getTime(), snapshot.getQuotation());
            originalEstimateRegression.addData(snapshot.getProjectSnapshotDate().getTime(), snapshot.getOriginalEstimate());
            realEffortRegression.addData(snapshot.getProjectSnapshotDate().getTime(), snapshot.getRealEffort());
            effortLeftRegression.addData(snapshot.getProjectSnapshotDate().getTime(), snapshot.getEffortLeft());
            effortSpentRegression.addData(snapshot.getProjectSnapshotDate().getTime(), snapshot.getEffortSpent());
        });

        projectSnapshotList.forEach(snapshot -> {
            quotationRegressionPoints.add(quotationRegression.predict(snapshot.getProjectSnapshotDate().getTime()));
            originalEstimateRegressionPoints.add(originalEstimateRegression.predict(snapshot.getProjectSnapshotDate().getTime()));
            realEffortRegressionPoints.add(realEffortRegression.predict(snapshot.getProjectSnapshotDate().getTime()));
            effortLeftRegressionPoints.add(effortLeftRegression.predict(snapshot.getProjectSnapshotDate().getTime()));
            effortSpentRegressionPoints.add(effortSpentRegression.predict(snapshot.getProjectSnapshotDate().getTime()));
        });

        final java.util.Calendar c = java.util.Calendar.getInstance();

        quotationRegressionPoints.add(quotationRegression.predict(c.getTime().getTime()));
        originalEstimateRegressionPoints.add(originalEstimateRegression.predict(c.getTime().getTime()));
        realEffortRegressionPoints.add(realEffortRegression.predict(c.getTime().getTime()));
        effortLeftRegressionPoints.add(effortLeftRegression.predict(c.getTime().getTime()));
        effortSpentRegressionPoints.add(effortSpentRegression.predict(c.getTime().getTime()));

        listOfProjectSnapshotDates.add(c.getTime().toString());

        c.add(Calendar.DATE, this.projection_days);

        quotationRegressionPoints.add(quotationRegression.predict(c.getTime().getTime()));
        originalEstimateRegressionPoints.add(originalEstimateRegression.predict(c.getTime().getTime()));
        realEffortRegressionPoints.add(realEffortRegression.predict(c.getTime().getTime()));
        effortLeftRegressionPoints.add(effortLeftRegression.predict(c.getTime().getTime()));
        effortSpentRegressionPoints.add(effortSpentRegression.predict(c.getTime().getTime()));

        listOfProjectSnapshotDates.add(c.getTime().toString());

        wrapper.setQuotationRegressionData(quotationRegressionPoints);
        wrapper.setOriginalEstimateRegressionData(originalEstimateRegressionPoints);
        wrapper.setRealEffortRegressionData(realEffortRegressionPoints);
        wrapper.setEffortLeftRegressionData(effortLeftRegressionPoints);
        wrapper.setEffortSpentRegressionData(effortSpentRegressionPoints);

        wrapper.setListOfProjectSnapshotDates(listOfProjectSnapshotDates);
    }

    public static class ProjectSnapshotGraphWrapper implements Serializable {
        public List<String> listOfProjectSnapshotDates;
        public Collection<Double> quotationData;
        public Collection<Double> originalEstimateData;
        public Collection<Double> realEffortData;
        public Collection<Double> effortSpentData;
        public Collection<Double> effortLeftData;
        public Collection<Double> quotationRegressionData;
        public Collection<Double> originalEstimateRegressionData;
        public Collection<Double> realEffortRegressionData;
        public Collection<Double> effortLeftRegressionData;
        public Collection<Double> effortSpentRegressionData;

        public ProjectSnapshotGraphWrapper() {
        }

        public void setListOfProjectSnapshotDates(final List<String> listOfProjectSnapshotDates) {
            this.listOfProjectSnapshotDates = listOfProjectSnapshotDates;
        }

        public void setQuotationData(final Collection<Double> quotationData) {
            this.quotationData = quotationData;

        }

        public void setRealEffortData(final Collection<Double> realEffortData) {
            this.realEffortData = realEffortData;
        }

        public void setOriginalEstimateData(final Collection<Double> originalEstimateData) {
            this.originalEstimateData = originalEstimateData;
        }

        public void setEffortSpentData(final Collection<Double> effortSpentData) {
            this.effortSpentData = effortSpentData;
        }

        public void setEffortLeftData(final Collection<Double> effortLeftData) {
            this.effortLeftData = effortLeftData;
        }

        public void setQuotationRegressionData(Collection<Double> quotationRegressionData) { this.quotationRegressionData = quotationRegressionData; }

        public void setOriginalEstimateRegressionData(Collection<Double> originalEstimateRegressionData) {
            this.originalEstimateRegressionData = originalEstimateRegressionData; }

        public void setRealEffortRegressionData(Collection<Double> realEffortRegressionData) {
            this.realEffortRegressionData = realEffortRegressionData; }

        public void setEffortLeftRegressionData(Collection<Double> effortLeftRegressionData) {
            this.effortLeftRegressionData = effortLeftRegressionData; }

        public void setEffortSpentRegressionData(Collection<Double> effortSpentRegressionData) {
            this.effortSpentRegressionData = effortSpentRegressionData; }
    }

}
