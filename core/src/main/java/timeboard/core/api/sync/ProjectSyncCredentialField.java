package timeboard.core.api.sync;

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

import java.io.Serializable;

public class ProjectSyncCredentialField implements Serializable {

    private String fieldKey;
    private String fieldName;
    private Type fieldType;
    private String value;
    private int weight;

    public ProjectSyncCredentialField(final String fieldKey, final String fieldName, final Type fieldType, final int weight) {
        this.fieldKey = fieldKey;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.weight = weight;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(final int weight) {
        this.weight = weight;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(final String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    public Type getFieldType() {
        return fieldType;
    }

    public void setFieldType(final Type fieldType) {
        this.fieldType = fieldType;
    }

    public enum Type {
        TEXT("text"),
        PASSWORD("password");

        private String name;

        private Type(final String v) {
            this.name = v;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
