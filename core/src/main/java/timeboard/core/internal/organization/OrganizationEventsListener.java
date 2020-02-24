package timeboard.core.internal.organization;

/*-
 * #%L
 * core
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.MembershipRole;

@Component
public class OrganizationEventsListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationEventsListener.class);

    @Value("${timeboard.tasks.default.vacation}")
    private String defaultVacationTaskName;

    @Autowired
    private OrganizationService organizationService;

    @EventListener
    public void createDefaultTaskAfterOrgCreation(final CreateOrganizationEvent event) {
        if (event.getActor() != null) {
            try {
                this.organizationService.createDefaultTask(
                        event.getActor(),
                        event.getOrganization(),
                        this.defaultVacationTaskName);

            } catch (final BusinessException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    @EventListener
    public void addFirstMembership(final CreateOrganizationEvent event) {
        if (event.getActor() != null) {
            try {
                this.organizationService.addMembership(
                        event.getActor(),
                        event.getOrganization(),
                        event.getActor(),
                        MembershipRole.OWNER
                );
            } catch (final BusinessException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
