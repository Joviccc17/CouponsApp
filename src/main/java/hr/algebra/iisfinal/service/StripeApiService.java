package hr.algebra.iisfinal.service;

import hr.algebra.iisfinal.dto.CouponDTO;
import hr.algebra.iisfinal.dto.StripeListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
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
            CouponDTO couponDto = stripeWebClient.get()
                    .uri("/v1/coupons/{id}", id)
                    .retrieve()
                    .bodyToMono(CouponDTO.class)
                    .block();
            return Optional.ofNullable(couponDto);

        } catch (Exception e) {
            log.warn("Coupon {} not found on Stripe: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public CouponDTO create(CouponDTO dto) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        if (dto.getId() != null && !dto.getId().isBlank())
            params.add("id", dto.getId());
        if (dto.getName() != null && !dto.getName().isBlank())
            params.add("name", dto.getName());
        if (dto.getPercentOff() != null)
            params.add("percent_off", String.valueOf(dto.getPercentOff()));
        if (dto.getAmountOff() != null) {
            params.add("amount_off", String.valueOf(dto.getAmountOff()));
            params.add("currency", dto.getCurrency() != null ? dto.getCurrency() : "usd");
        }

        if (dto.getDuration() != null)
            params.add("duration", dto.getDuration());
        if (dto.getDurationInMonths() != null)
            params.add("duration_in_months", String.valueOf(dto.getDurationInMonths()));
        if (dto.getMaxRedemptions() != null)
            params.add("max_redemptions", String.valueOf(dto.getMaxRedemptions()));

        return stripeWebClient.post()
                .uri("/v1/coupons")
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(CouponDTO.class)
                .block();
    }

    public Optional<CouponDTO> update(String id, CouponDTO dto) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        if (dto.getName() != null)
            params.add("name", dto.getName());

        try {
            CouponDTO updated = stripeWebClient.post()
                    .uri("/v1/coupons/{id}", id)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .bodyToMono(CouponDTO.class)
                    .block();
            return Optional.ofNullable(updated);

        } catch (Exception e) {
            log.warn("Failed to update coupon {} on Stripe: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public boolean delete(String id) {
        try {
            stripeWebClient.delete()
                    .uri("/v1/coupons/{id}", id)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return true;

        } catch (Exception e) {
            log.warn("Failed to delete coupon {} on Stripe: {}", id, e.getMessage());
            return false;
        }
    }
}
