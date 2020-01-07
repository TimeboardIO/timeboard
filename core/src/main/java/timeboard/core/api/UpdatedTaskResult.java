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

public class UpdatedTaskResult {

    private final long projectID;
    private final long taskID;
    private final double effortSpent;
    private final double effortLeft;
    private final double originalEstimate;
    private final double realEffort;

    public UpdatedTaskResult(long projectID,
                             long taskID,
                             double effortSpent,
                             double effortLeft,
                             double originalEstimate,
                             double realEffort) {

        this.projectID = projectID;
        this.taskID = taskID;
        this.effortSpent = effortSpent;
        this.effortLeft = effortLeft;
        this.originalEstimate = originalEstimate;
        this.realEffort = realEffort;
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

    public double getEffortLeft() {
        return this.effortLeft;
    }

    public double getOriginalEstimate() {
        return this.originalEstimate;
    }

    public double getRealEffort() {

        return this.realEffort;
    }
}
