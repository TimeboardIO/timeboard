package timeboard.core.internal.reports;

/*-
 * #%L
 * reports
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
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

import org.quartz.JobKey;
import timeboard.core.model.Report;
import timeboard.core.security.TimeboardAuthentication;

import java.io.Serializable;

public interface ReportHandler {

    <T extends Serializable> T getReportModel(
            final TimeboardAuthentication authentication,
            final Report report);

    /**
     * Unique report id
     *
     * @return unique id as string
     */
    String handlerID();

    /**
     * i18n report name as code
     *
     * @return report.*
     */
    String handlerLabel();

    /**
     * Report template name
     *
     * @return template name relative to reports/src/main/resources/templates/fragments
     */
    String handlerView();

    /**
     * if true, report exec is async
     *
     * @return true if async, false else
     */
    Boolean isAsyncHandler();


    /**
     * job key used to run async tasks
     *
     * @return job key as string
     */
    default JobKey handlerJobJey() {
        return new JobKey("timeboard::report-handler::" + String.valueOf(this.handlerID()));
    }

}
