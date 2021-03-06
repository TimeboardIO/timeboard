package timeboard.core.internal.observers.logs;

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

import io.reactivex.disposables.Disposable;
import org.springframework.stereotype.Component;
import timeboard.core.api.TimeboardSubjects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


@Component
public class LogTasksComponent {


    private Disposable disposable;


    @PostConstruct
    private void init() {
        this.disposable = TimeboardSubjects.TASK_EVENTS.subscribe(taskEvent -> {
          /*  LOGGER.info(String.format("User % has %s task %s",
                    taskEvent.getActor().getScreenName(), taskEvent.getEventType(), taskEvent.getTask().getId())
            );*/
        });
    }

    @PreDestroy
    private void destroy() {

        if (this.disposable != null && !this.disposable.isDisposed()) {
            this.disposable.dispose();
        }

    }
}
