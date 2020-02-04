package timeboard.core.internal.observers.emails;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailStructure {

    private List<String> targetUserList;
    private List<String> targetCCUserList;
    private String subject;
    private String template;
    private Map<String, Object> model;

    private EmailStructure(final String subject) {
        this.model = new HashMap<>();
        this.targetUserList = new ArrayList<>();
        this.targetCCUserList = new ArrayList<>();
        this.template = "mail/simple.html";
        this.subject = subject;
        this.model.put("message", "");
    }


    public EmailStructure(final List<String> targetUserList, final List<String> targetCCUserList, final String subject, final String message) {
        this(subject);
        this.targetUserList = targetUserList;
        this.targetCCUserList = targetCCUserList;
        this.model.put("message", message);

    }

    public EmailStructure(final List<String> targetUserList, final List<String> targetCCUserList,
                          final String subject, Map<String, Object> model, final String template) {
        this(targetUserList, targetCCUserList, subject, "");
        this.template = template;
        this.model = model;
    }

    public EmailStructure(final String targetUser, final String subject, final String message) {
        this(targetUser, null, subject, message);
    }


    public EmailStructure(final String targetUser, final String targetCCUser, final String subject, final String message) {
        this(subject);
        this.targetUserList.add(targetUser);
        if (targetCCUser !=null) {
            this.targetCCUserList.add(targetCCUser);
        }
        this.model.put("message", message);
        this.template = "mail/simple.html";
    }

    public EmailStructure(final String targetUser, final String targetCCUser,
                          final String subject, Map<String, Object> model, final String template) {
        this(subject);
        this.targetUserList.add(targetUser);
        if (targetCCUser !=null) {
            this.targetCCUserList.add(targetCCUser);
        }
        this.template = template;
        this.model = model;
    }



    public List<String> getTargetUserList() {
        return targetUserList;
    }

    public void setTargetUserList(final List<String> targetUserList) {
        this.targetUserList = targetUserList;
    }

    public List<String> getTargetCCUserList() {
        return targetCCUserList;
    }

    public void setTargetCCUserList(final List<String> targetCCUserList) {
        this.targetCCUserList = targetCCUserList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }
}
