package timeboard.core;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import timeboard.core.api.OrganizationService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Organization;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
@ComponentScan(basePackages = "timeboard.core")
@EntityScan(basePackages = {"timeboard.core.model", "timeboard.core.internal.async"})
@EnableJpaRepositories
@EnableTransactionManagement
public class CoreConfiguration {

    @Autowired
    private OrganizationService organizationService;

    @Value("${timeboard.organizations.default}")
    private String defaultOrganizationName;

    @Value("${timeboard.tasks.default.vacation}")
    private String defaultVacationTaskName;

    @PostConstruct
    private void verifyPublicOrganization() throws BusinessException {

        final Optional<Organization> defaultOrganization = this.organizationService
                .getOrganizationByName(this.defaultOrganizationName);

        if (!defaultOrganization.isPresent()) {
            final Map<String, String> props = new HashMap<>();
            props.put(Organization.SETUP_PUBLIC, "true");

            this.organizationService.createOrganization(null, this.defaultOrganizationName, props);
        }

        this.organizationService.checkOrganizationVacationTask(defaultVacationTaskName);


    }


}
