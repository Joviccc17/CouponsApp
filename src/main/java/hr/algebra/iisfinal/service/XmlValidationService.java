package hr.algebra.iisfinal.service;

import hr.algebra.iisfinal.model.CouponListWrapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class XmlValidationService {

    @Value("${app.xml-output-path:./data/coupons.xml}")
    private String xmlOutputPath;

    public List<String> validateCouponsXml() {
        List<String> errors = new ArrayList<>();
        File xmlFile = new File(xmlOutputPath);

        if (!xmlFile.exists()) {
            errors.add("XML file not found at: " + xmlOutputPath + " — save at least one coupon first.");
            return errors;
        }

        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(getClass().getResource("/schemas/coupon.xsd"));

            JAXBContext ctx = JAXBContext.newInstance(CouponListWrapper.class);
            Unmarshaller u = ctx.createUnmarshaller();
            u.setSchema(schema);
            u.setEventHandler(event -> {
                errors.add("[" + severityLabel(event.getSeverity()) + "] " + event.getMessage());
                return true;
            });
            u.unmarshal(xmlFile);
        } catch (Exception e) {
            errors.add("Validation error: " + e.getMessage());
        }
        return errors;
    }

    private String severityLabel(int severity) {
        return switch (severity) {
            case 0 -> "WARNING";
            case 1 -> "ERROR";
            default -> "FATAL";
        };
    }
}