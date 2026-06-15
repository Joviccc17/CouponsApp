package hr.algebra.iisfinal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import hr.algebra.iisfinal.dto.CouponDTO;
import hr.algebra.iisfinal.service.CouponService;
import hr.algebra.iisfinal.service.JsonValidationService;
import hr.algebra.iisfinal.service.XmlValidationService;
import hr.algebra.iisfinal.service.XsdValidationService;
import hr.algebra.iisfinal.soap.CouponSoapService;
import jakarta.xml.ws.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final CouponService couponService;
    private final XmlValidationService xmlValidationService;
    private final XsdValidationService xsdValidationService;
    private final JsonValidationService jsonValidationService;

    @Value("${server.port:8080}")
    private int serverPort;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/web/dashboard";
    }

    @GetMapping("/web/dashboard")
    public String dashboard(Model model) {
        long count = couponService.getAllCoupons().size();
        model.addAttribute("couponCount", count);
        return "dashboard";
    }

    @GetMapping("/web/coupons")
    public String coupons(Model model) {
        model.addAttribute("coupons", couponService.getAllCoupons());
        return "coupons/list";
    }

    @GetMapping("/web/coupons/upload")
    public String uploadForm() {
        return "coupons/upload";
    }

    @PostMapping("/web/coupons/upload")
    public String uploadCoupon(@RequestParam("content") String content,
                                @RequestParam("type") String type,
                                Model model) {
        List<String> errors = new ArrayList<>();
        String success = null;
        try {
            CouponDTO couponDTO = null;
            if ("xml".equalsIgnoreCase(type)) {
                errors = xsdValidationService.validate(content);
                if (errors.isEmpty()) {
                    couponDTO = new XmlMapper().readValue(content, CouponDTO.class);
                }
            } else {
                errors = jsonValidationService.validate(content);
                if (errors.isEmpty()) {
                    couponDTO = new ObjectMapper().readValue(content, CouponDTO.class);
                }
            }
            if (errors.isEmpty() && couponDTO != null) {
                if (couponService.getCouponById(couponDTO.getId()).isPresent()) {
                    errors.add("Coupon '" + couponDTO.getId() + "' already exists.");
                } else {
                    couponService.saveCoupon(couponDTO);
                    success = "Coupon '" + couponDTO.getId() + "' saved successfully!";
                }
            }
        } catch (Exception e) {
            errors.add("Processing error: " + e.getMessage());
        }
        model.addAttribute("errors", errors);
        model.addAttribute("success", success);
        return "coupons/upload";
    }

    @GetMapping("/web/coupons/delete/{id}")
    public String deleteCoupon(@PathVariable String id) {
        couponService.deleteCoupon(id);
        return "redirect:/web/coupons";
    }

    @GetMapping("/web/soap")
    public String soapSearchForm() {
        return "soap/search";
    }

    @PostMapping("/web/soap")
    public String soapSearch(@RequestParam("term") String term, Model model) {
        try {
            URL wsdlUrl = new URL("http://localhost:" + serverPort + "/soap/coupons?wsdl");
            QName serviceName = new QName("http://soap.iisfinal.algebra.hr/", "CouponService");
            Service wsSvc = Service.create(wsdlUrl, serviceName);
            CouponSoapService port = wsSvc.getPort(CouponSoapService.class);
            List<CouponDTO> results = port.searchCoupons(term);
            model.addAttribute("results", results);
        } catch (Exception e) {
            log.error("SOAP client call failed", e);
            model.addAttribute("results", List.of());
            model.addAttribute("soapError", "SOAP service unavailable: " + e.getMessage());
        }
        model.addAttribute("term", term);
        return "soap/search";
    }

    @GetMapping("/web/validate")
    public String validateXml(Model model) {
        List<String> errors = xmlValidationService.validateCouponsXml();
        model.addAttribute("valid", errors.isEmpty());
        model.addAttribute("errors", errors);
        return "validation/result";
    }

    @GetMapping("/web/weather")
    public String weatherPage() {
        return "grpc/weather";
    }

    @GetMapping("/web/weather/search")
    public String weatherSearch(@RequestParam(required = false) String city, Model model) {
        if (city != null && !city.isBlank()) {
            model.addAttribute("city", city);
        }
        return "grpc/weather";
    }

    @GetMapping("/web/coupons/edit/{id}")
    public String editCouponForm(@PathVariable String id, Model model) {
        return couponService.getCouponById(id)
                .map(coupon -> {
                    model.addAttribute("coupon", coupon);
                    return "coupons/edit";
                })
                .orElse("redirect:/web/coupons");
    }

    @PostMapping("/web/coupons/edit/{id}")
    public String editCouponSubmit(@PathVariable String id, @ModelAttribute CouponDTO dto, Model model) {
        return couponService.updateCoupon(id, dto)
                .map(updated -> "redirect:/web/coupons")
                .orElseGet(() -> {
                    model.addAttribute("coupon", dto);
                    model.addAttribute("error", "Coupon not found");
                    return "coupons/edit";
                });
    }

    @GetMapping("/web/graphql")
    public String graphqlPage() {
        return "graphql/index";
    }
}
