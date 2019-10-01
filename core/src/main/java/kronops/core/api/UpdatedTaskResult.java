package kronops.core.api;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Kronops
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

public class UpdatedTaskResult {

    private final long projectID;
    private final long taskID;
    private final double effortSpent;
    private final double remainToBeDone;
    private final double estimateWork;
    private final double reEstimateWork;

    public UpdatedTaskResult(long projectID, long taskID, double effortSpent, double remainToBeDone, double estimateWork, double reEstimateWork) {
        this.projectID = projectID;
        this.taskID = taskID;
        this.effortSpent = effortSpent;
        this.remainToBeDone = remainToBeDone;
        this.estimateWork = estimateWork;
        this.reEstimateWork = reEstimateWork;
    }

    public long getProjectID() {
        return projectID;
    }

    public long getTaskID() {
        return taskID;
    }

    public double getEffortSpent() {
        return this.effortSpent;
    }

    public double getRemainToBeDone() {
        return this.remainToBeDone;
    }

    public double getEstimateWork() {
        return this.estimateWork;
    }

    public double getReEstimateWork() {

        return this.reEstimateWork;
    }
}
