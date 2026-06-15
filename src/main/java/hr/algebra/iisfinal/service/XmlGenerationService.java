package hr.algebra.iisfinal.service;

import hr.algebra.iisfinal.dto.CouponDTO;
import hr.algebra.iisfinal.model.Coupon;
import hr.algebra.iisfinal.model.CouponListWrapper;
import hr.algebra.iisfinal.repository.CouponRepository;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class XmlGenerationService {

    private final CouponRepository couponRepository;
    private final StripeApiService stripeApiService;

    @Value("${app.xml-output-path:./data/coupons.xml}")
    private String xmlOutputPath;

    @Value("${app.use-stripe-api:false}")
    private boolean useStripeApi;

    @EventListener(ApplicationReadyEvent.class)
    public void generateOnStartup() {
        generateCouponsXml();
    }

    public void generateCouponsXml() {

        try {
            List<CouponDTO> coupons;

            if (useStripeApi) {
                coupons = stripeApiService.fetchAll();
            }
            else {
                coupons = couponRepository.findAll().stream()
                        .map(this::toDto)
                        .toList();
            }

            CouponListWrapper wrapper = new CouponListWrapper();
            wrapper.setCoupons(coupons);

            JAXBContext context = JAXBContext.newInstance(CouponListWrapper.class, CouponDTO.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File outputFile = new File(xmlOutputPath);
            outputFile.getParentFile().mkdirs();
            marshaller.marshal(wrapper, outputFile);

            log.debug("Generated coupons.xml with {} entries (source: {}) at {}",
                    coupons.size(), useStripeApi ? "Stripe" : "local DB", xmlOutputPath);

        } catch (Exception e) {
            log.error("Failed to generate coupons XML", e);
        }
    }

    private CouponDTO toDto(Coupon c) {
        return CouponDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .percentOff(c.getPercentOff())
                .amountOff(c.getAmountOff())
                .currency(c.getCurrency())
                .duration(c.getDuration())
                .durationInMonths(c.getDurationInMonths())
                .maxRedemptions(c.getMaxRedemptions())
                .timesRedeemed(c.getTimesRedeemed())
                .valid(c.getValid())
                .created(c.getCreated())
                .build();
    }
}
