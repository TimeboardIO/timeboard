package timeboard.core.observers.emails;

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


import java.util.List;

public class EmailStructure {

    private List<String> targetUserList;
    private List<String> targetCCUserList;
    private String subject;
    private String message;

    public EmailStructure(List<String> targetUserList, List<String> targetCCUserList, String subject, String message) {
        this.targetUserList = targetUserList;
        this.targetCCUserList = targetCCUserList;
        this.subject = subject;
        this.message = message;
    }

    public List<String> getTargetUserList() {
        return targetUserList;
    }

    public void setTargetUserList(List<String> targetUserList) {
        this.targetUserList = targetUserList;
    }

    public List<String> getTargetCCUserList() {
        return targetCCUserList;
    }

    public void setTargetCCUserList(List<String> targetCCUserList) {
        this.targetCCUserList = targetCCUserList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
