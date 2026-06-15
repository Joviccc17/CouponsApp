package hr.algebra.iisfinal.service;

import org.springframework.stereotype.Service;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class XsdValidationService {

    public List<String> validate(String xmlContent) {
        List<String> errors = new ArrayList<>();

        try {

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(getClass().getResource("/schemas/coupon.xsd"));
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) {
                    errors.add("WARNING [line " + e.getLineNumber() + "]: " + e.getMessage());
                }
                @Override
                public void error(SAXParseException e) {
                    errors.add("ERROR [line " + e.getLineNumber() + "]: " + e.getMessage());
                }
                @Override
                public void fatalError(SAXParseException e) {
                    errors.add("FATAL [line " + e.getLineNumber() + "]: " + e.getMessage());
                }
            });
            validator.validate(new StreamSource(new StringReader(xmlContent)));
        } catch (Exception e) {
            errors.add("Validation failed: " + e.getMessage());
        }
        return errors;
    }
}