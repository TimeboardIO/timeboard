package timeboard.core.async;

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
import org.springframework.stereotype.Controller;
import timeboard.core.api.ThreadLocalStorage;
import timeboard.core.model.Account;

import javax.transaction.Transactional;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Controller
public class AsyncJobService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AsyncJobService.class);

    private final ExecutorService asyncJobsExecutorService = Executors.newFixedThreadPool(10);

    @Autowired
    private AsyncJobRepository jobRepository;

    public AsyncJobState runAsyncJob(final Account organization, final Account jobOwner,
                                     final String jobTitle, final Callable jobToRun){

        // Save job state in db
        final AsyncJobState jobState = this.createJobState(organization, jobOwner, jobTitle);

        //Run async job
        this.startJob(jobState, jobToRun);

        return jobState;
    }

    public List<AsyncJobState> getAccountJobs(Account owner) {
        List<AsyncJobState> jobs = new ArrayList<>();
        try {
            jobs.addAll(this.jobRepository.findByOwnerID(owner.getId())
                    .stream()
                    .filter(asyncJobState ->
                            asyncJobState.getState().equals(AsyncJobState.State.IN_PROGRESS)
                    )
                    .collect(Collectors.toList()));

        }catch (Exception e){
            jobs.clear();
        }
        return jobs;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    private void startJob(final AsyncJobState jobState, final Callable<String> jobToRun) {
        this.asyncJobsExecutorService.execute(() -> {

            ThreadLocalStorage.setCurrentOrganizationID(jobState.getOrganizationID());

            final Future<String> jobFuture = this.asyncJobsExecutorService.submit(jobToRun);
            LOGGER.info(String.format("Start async job:%s for org:'%s' and user:'%s'",
                    jobState.getId(), jobState.getOrganizationID(), jobState.getOwnerID()));

            try {
                final String jobResult = jobFuture.get(5, TimeUnit.MINUTES);
                jobState.setResult(jobResult);
                jobState.setState(AsyncJobState.State.DONE);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                jobState.setState(AsyncJobState.State.FAILED);

                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String err = sw.toString();
                if(err.length()> 1000){
                    err = err.substring(0, 999);
                }
                jobState.setError(err);
                e.printStackTrace();
            }finally {
                jobState.setEndDate(new Date());
            }
            this.jobRepository.save(jobState);
            LOGGER.info(String.format("Stop async job:%s for org:'%s' and user:'%s' with state:%s",
                    jobState.getId(), jobState.getOrganizationID(), jobState.getOwnerID(), jobState.getState()));

        });
    }





    @Transactional(Transactional.TxType.REQUIRES_NEW)
    private AsyncJobState createJobState(final Account organization,
                                         final Account jobOwner,
                                         String jobTitle) {

        final AsyncJobState asyncJobState = new AsyncJobState();
        asyncJobState.setOrganizationID(organization.getId());
        asyncJobState.setOwnerID(jobOwner.getId());
        asyncJobState.setTitle(jobTitle);
        asyncJobState.setStartDate(new Date());
        asyncJobState.setState(AsyncJobState.State.IN_PROGRESS);

        return this.jobRepository.save(asyncJobState);
    }



}
