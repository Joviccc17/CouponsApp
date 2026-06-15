package hr.algebra.iisfinal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class JsonValidationService {

    public List<String> validate(String jsonContent) {

        List<String> errors = new ArrayList<>();

        try {

            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            InputStream schemaStream = getClass().getResourceAsStream("/schemas/coupon-schema.json");
            JsonSchema schema = factory.getSchema(schemaStream);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonContent);
            Set<ValidationMessage> message = schema.validate(node);
            message.forEach(m -> errors.add(m.getMessage()));

        } catch (Exception e) {
            errors.add("Validation failed: " + e.getMessage());
        }
        return errors;
    }
}