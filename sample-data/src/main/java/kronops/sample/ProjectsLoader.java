package kronops.sample;

/*-
 * #%L
 * sample-data
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

import kronops.core.api.ProjectService;
import kronops.core.api.UserService;
import kronops.core.api.exceptions.BusinessException;
import kronops.core.model.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;
import java.util.Random;


public class ProjectsLoader {

    @Reference
    ProjectService projectService;

    @Reference
    UserLoader userLoader;


    @Reference
    UserService userService;


    @Activate
    public void load() throws BusinessException {

        for(int i =0; i<10; i++) {
            Random r = new Random();
            int low = 0;
            int high = 10;
            int result = r.nextInt(high-low) + low;

            User u1 = this.userService.findUserByLogin("kronops"+result);
            Project newProject = this.projectService.createProject(u1, "Sample Project "+i);

            for(int k=0; k<100; k++){
                Task t = new Task();
                t.setName("Hello Task "+k);
                t.setStartDate(new Date());
                t.setEndDate(new Date());
                t.setComments("This is a sample task "+k);
                t.setEstimateWork(5);
                this.projectService.createTask(newProject, t);
            }


        }



    }


}
