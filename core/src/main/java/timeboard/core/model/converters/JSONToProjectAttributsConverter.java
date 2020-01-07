package timeboard.core.model.converters;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import timeboard.core.model.ProjectAttributValue;
import timeboard.core.model.ProjectAttributeValueMap;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Converter
public class JSONToProjectAttributsConverter implements AttributeConverter<Map<String, ProjectAttributValue>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @Override
    public String convertToDatabaseColumn(Map<String, ProjectAttributValue> stringProjectAttributValueMap) {
        String res = "{}";
        try {
            res = OBJECT_MAPPER.writeValueAsString(stringProjectAttributValueMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Map<String, ProjectAttributValue> convertToEntityAttribute(String s) {
        final Map<String, ProjectAttributValue> attrs = new HashMap<>();
        try {
            attrs.putAll(OBJECT_MAPPER.readValue(s, ProjectAttributeValueMap.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return attrs;
    }
}
