package hr.algebra.iisfinal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import hr.algebra.iisfinal.dto.CouponDTO;
import hr.algebra.iisfinal.service.CouponService;
import hr.algebra.iisfinal.service.JsonValidationService;
import hr.algebra.iisfinal.service.XmlValidationService;
import hr.algebra.iisfinal.service.XsdValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponRestController {

    private final CouponService couponService;
    private final XsdValidationService xsdValidationService;
    private final JsonValidationService jsonValidationService;
    private final XmlValidationService xmlValidationService;

    @GetMapping
    public ResponseEntity<List<CouponDTO>> getAll() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponDTO> getById(@PathVariable String id) {
        return couponService.getCouponById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/xml", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> createFromXml(@RequestBody String xmlBody) {
        List<String> errors = xsdValidationService.validate(xmlBody);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        try {
            XmlMapper xmlMapper = new XmlMapper();
            CouponDTO couponDTO = xmlMapper.readValue(xmlBody, CouponDTO.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(couponService.saveCoupon(couponDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("errors", List.of("XML parse error: " + e.getMessage())));
        }
    }

    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createFromJson(@RequestBody String jsonBody) {
        List<String> errors = jsonValidationService.validate(jsonBody);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            CouponDTO couponDTO = mapper.readValue(jsonBody, CouponDTO.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(couponService.saveCoupon(couponDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("errors", List.of("JSON parse error: " + e.getMessage())));
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CouponDTO> create(@RequestBody CouponDTO couponDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.saveCoupon(couponDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponDTO> update(@PathVariable String id, @RequestBody CouponDTO couponDTO) {
        return couponService.updateCoupon(id, couponDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (couponService.deleteCoupon(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/validate-xml")
    public ResponseEntity<Map<String, Object>> validateXml() {
        List<String> errors = xmlValidationService.validateCouponsXml();
        return ResponseEntity.ok(Map.of(
                "valid", errors.isEmpty(),
                "errors", errors
        ));
    }
}