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

import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Account;
import timeboard.core.model.Project;
import timeboard.core.model.ProjectSnapshot;
import timeboard.core.model.TaskSnapshot;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface ProjectSnapshotService {


    /*
    === ProjectSnapshots ===
    */

    /**
     * Create a ProjectSnapshot for the current project
     *
     * @return ProjectSnapshot
     */
    ProjectSnapshot createProjectSnapshot(Account actor, Project project) throws BusinessException;

    List<TaskSnapshot> findAllTaskSnapshotByTaskID(Account actor, Long taskID);

    void regression(ProjectSnapshotService.ProjectSnapshotGraphWrapper wrapper, List<String> listOfProjectSnapshotDates,
                    List<ProjectSnapshot> projectSnapshotList);

    class ProjectSnapshotGraphWrapper implements Serializable {
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

        public void setQuotationRegressionData(Collection<Double> quotationRegressionData) {
            this.quotationRegressionData = quotationRegressionData;
        }

        public void setOriginalEstimateRegressionData(Collection<Double> originalEstimateRegressionData) {
            this.originalEstimateRegressionData = originalEstimateRegressionData;
        }

        public void setRealEffortRegressionData(Collection<Double> realEffortRegressionData) {
            this.realEffortRegressionData = realEffortRegressionData;
        }

        public void setEffortLeftRegressionData(Collection<Double> effortLeftRegressionData) {
            this.effortLeftRegressionData = effortLeftRegressionData;
        }

        public void setEffortSpentRegressionData(Collection<Double> effortSpentRegressionData) {
            this.effortSpentRegressionData = effortSpentRegressionData;
        }
    }
}
