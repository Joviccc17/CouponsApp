package hr.algebra.iisfinal.soap;

import hr.algebra.iisfinal.dto.CouponDTO;
import jakarta.jws.WebService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@WebService(
    endpointInterface = "hr.algebra.iisfinal.soap.CouponSoapService",
    serviceName = "CouponService",
    portName = "CouponPort",
    targetNamespace = "http://soap.iisfinal.algebra.hr/"
)
@Service
@Slf4j
public class CouponSoapServiceImpl implements CouponSoapService {

    @Value("${app.xml-output-path:./data/coupons.xml}")
    private String xmlOutputPath;

    @Override
    public List<CouponDTO> searchCoupons(String term) {
        List<CouponDTO> results = new ArrayList<>();
        if (term == null || term.isBlank()) return results;

        File xmlFile = new File(xmlOutputPath);
        if (!xmlFile.exists()) {

            log.warn("coupons.xml not found at {}. Add coupons first.", xmlOutputPath);
            return results;
        }

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlFile);

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            String lower = term.toLowerCase().replace("'", "");
            String xpathExpr = String.format(
                "//coupon[contains(translate(name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')" +
                " or contains(translate(id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]",
                lower, lower
            );

            NodeList nodes = (NodeList) xpath.evaluate(xpathExpr, doc, XPathConstants.NODESET);

            JAXBContext context = JAXBContext.newInstance(CouponDTO.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                JAXBElement<CouponDTO> element = unmarshaller.unmarshal(node, CouponDTO.class);
                results.add(element.getValue());
            }
        } catch (Exception e) {
            log.error("SOAP search failed", e);
        }
        return results;
    }
}