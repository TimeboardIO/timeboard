package timeboard.core.security;

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

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component("jsonFilePolicyDefinition")
public class JsonFilePolicyDefinition implements PolicyDefinition {

    private static Logger LOGGER = LoggerFactory.getLogger(JsonFilePolicyDefinition.class);

    private static String DEFAULT_POLICY_FILE_NAME = "policy-set.json";

    @Value("${policy.json.filePath}")
    private String policyFilePath;

    private List<PolicyRuleSet> rules;

    @PostConstruct
    private void init() {
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(Expression.class, new SpelDeserializer());
        mapper.registerModule(module);
        try {
            PolicyRuleSet[] rulesArray = null;
            LOGGER.debug("[init] Checking policy file at: {}", policyFilePath);
            if (policyFilePath != null && !policyFilePath.isEmpty()
                    && Files.exists(Paths.get(policyFilePath))) {
                LOGGER.info("[init] Loading policy from custom file: {}", policyFilePath);
                rulesArray = mapper.readValue(new File(policyFilePath), PolicyRuleSet[].class);
            } else {
                LOGGER.info("[init] Custom policy file not found. Loading default policy");
                rulesArray = mapper.readValue(getClass().getClassLoader().getResourceAsStream(DEFAULT_POLICY_FILE_NAME), PolicyRuleSet[].class);
            }
            this.rules = rulesArray != null ? Arrays.asList(rulesArray) : null;
            LOGGER.info("[init] Policy loaded successfully.");
        } catch (final JsonMappingException e) {
            LOGGER.error("An error occurred while parsing the policy file.", e);
        } catch (final IOException e) {
            LOGGER.error("An error occurred while reading the policy file.", e);
        }
    }

    @Override
    public List<PolicyRuleSet> getAllPolicyRules() {
        return rules;
    }

}
