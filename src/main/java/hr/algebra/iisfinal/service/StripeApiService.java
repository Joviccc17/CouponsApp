package hr.algebra.iisfinal.service;

import hr.algebra.iisfinal.dto.CouponDTO;
import hr.algebra.iisfinal.dto.StripeListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StripeApiService {

    private final WebClient stripeWebClient;

    public StripeApiService(@Qualifier("stripeWebClient") WebClient stripeWebClient) {
        this.stripeWebClient = stripeWebClient;
    }

    public List<CouponDTO> fetchAll() {
        try {
            StripeListResponse response = stripeWebClient.get()
                    .uri("/v1/coupons")
                    .retrieve()
                    .bodyToMono(StripeListResponse.class)
                    .block();
            return response != null ? response.getData() : List.of();
        } catch (WebClientResponseException e) {
            log.error("Stripe API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("Failed to fetch coupons from Stripe", e);
            return List.of();
        }
    }

    public Optional<CouponDTO> fetchById(String id) {
        try {
            CouponDTO coupon = stripeWebClient.get()
                    .uri("/v1/coupons/{id}", id)
                    .retrieve()
                    .bodyToMono(CouponDTO.class)
                    .block();
            return Optional.ofNullable(coupon);
        } catch (Exception e) {
            log.warn("Coupon {} not found on Stripe: {}", id, e.getMessage());
            return Optional.empty();
        }
    }
}
