package timeboard.webapp;

/*-
 * #%L
 * integ-tests
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

import cucumber.api.java.en.Given;
import org.aspectj.weaver.World;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import timeboard.core.api.AccountService;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.ProjectService;
import timeboard.core.model.Account;
import timeboard.core.model.Organization;
import timeboard.core.security.TimeboardAuthentication;
import timeboard.home.HomeController;

import java.util.Collections;
import java.util.UUID;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = App.class
)
@ActiveProfiles("test")
@ContextConfiguration
@AutoConfigureMockMvc
public class TimeboardTest {

    protected TimeboardWorld world;

    public TimeboardTest(TimeboardWorld world) {
        this.world = world;
    }
    public TimeboardTest() {
        this.world = new TimeboardWorld();
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected HomeController controller;

    @Autowired
    protected ProjectService projectService;

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected OrganizationService organizationService;






}
