package hr.algebra.iisfinal.grpc;

import hr.algebra.iisfinal.grpc.generated.CityRequest;
import hr.algebra.iisfinal.grpc.generated.TemperatureResponse;
import hr.algebra.iisfinal.grpc.generated.WeatherServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;

@Component
@Slf4j
public class WeatherGrpcService extends WeatherServiceGrpc.WeatherServiceImplBase {

    private final org.springframework.web.reactive.function.client.WebClient dhmzWebClient;

    public WeatherGrpcService(@Qualifier("dhmzWebClient") org.springframework.web.reactive.function.client.WebClient dhmzWebClient) {
        this.dhmzWebClient = dhmzWebClient;
    }

    @Override
    public void getTemperature(CityRequest request, StreamObserver<TemperatureResponse> responseObserver) {
        String citySearch = request.getCityName().toLowerCase().trim();
        try {
            String xml = dhmzWebClient.get()
                    .uri("/hrvatska_n.xml")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (xml == null) {
                responseObserver.onError(Status.UNAVAILABLE
                        .withDescription("DHMZ returned empty response").asRuntimeException());
                return;
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xml)));

            NodeList gradovi = doc.getElementsByTagName("Grad");
            boolean found = false;

            for (int i = 0; i < gradovi.getLength(); i++) {
                Element grad = (Element) gradovi.item(i);
                String gradIme = getTextContent(grad, "GradIme");
                if (gradIme != null && gradIme.toLowerCase().contains(citySearch)) {
                    String temp = getTextContent(grad, "Temp");
                    responseObserver.onNext(TemperatureResponse.newBuilder()
                            .setCity(gradIme)
                            .setTemperature(temp != null ? temp : "N/A")
                            .setTimestamp(LocalDateTime.now().toString())
                            .build());
                    found = true;
                }
            }

            if (!found) {
                responseObserver.onNext(TemperatureResponse.newBuilder()
                        .setCity(request.getCityName())
                        .setTemperature("N/A")
                        .setTimestamp(LocalDateTime.now().toString())
                        .build());
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC weather lookup failed", e);
            responseObserver.onError(Status.UNAVAILABLE
                    .withDescription("DHMZ service unavailable: " + e.getMessage()).asRuntimeException());
        }
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl.getLength() > 0) {
            String text = nl.item(0).getTextContent().trim();
            return text.isEmpty() ? null : text;
        }
        return null;
    }
}