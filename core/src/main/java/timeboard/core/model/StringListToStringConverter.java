package timeboard.core.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class StringListToStringConverter
       implements AttributeConverter<List<String>, String> {
    private static final String DELIMITER = "|";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attributes) {
        if ( attributes == null || attributes.isEmpty() ) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String data) {
      if ( data == null ) {
        return new ArrayList<String>();
      }
        List attrs = null;
        TypeReference<ArrayList<String>> typeRef = new TypeReference<ArrayList<String>>() {
        };
        try {
            attrs = OBJECT_MAPPER.readValue(data, typeRef);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return attrs;
    }
}