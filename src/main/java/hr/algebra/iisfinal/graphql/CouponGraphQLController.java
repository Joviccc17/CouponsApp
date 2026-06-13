package hr.algebra.iisfinal.graphql;

import hr.algebra.iisfinal.dto.CouponDTO;
import hr.algebra.iisfinal.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CouponGraphQLController {

    private final CouponService couponService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<CouponDTO> coupons() {
        return couponService.getAllCoupons();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public CouponDTO coupon(@Argument String id) {
        return couponService.getCouponById(id).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public CouponDTO createCoupon(@Argument String id,
                                   @Argument String name,
                                   @Argument String duration,
                                   @Argument Boolean valid,
                                   @Argument Double percentOff,
                                   @Argument Long amountOff) {
        CouponDTO dto = CouponDTO.builder()
                .id(id)
                .name(name)
                .duration(duration)
                .valid(valid)
                .percentOff(percentOff)
                .amountOff(amountOff)
                .build();
        return couponService.saveCoupon(dto);
    }

    @MutationMapping
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public CouponDTO updateCoupon(@Argument String id,
                                   @Argument String name,
                                   @Argument Double percentOff,
                                   @Argument Long amountOff,
                                   @Argument String currency,
                                   @Argument String duration,
                                   @Argument Integer durationInMonths,
                                   @Argument Integer maxRedemptions,
                                   @Argument Boolean valid) {
        CouponDTO update = CouponDTO.builder()
                .name(name)
                .percentOff(percentOff)
                .amountOff(amountOff)
                .currency(currency)
                .duration(duration)
                .durationInMonths(durationInMonths)
                .maxRedemptions(maxRedemptions)
                .valid(valid)
                .build();
        return couponService.updateCoupon(id, update).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('FULL_ACCESS')")
    public Boolean deleteCoupon(@Argument String id) {
        return couponService.deleteCoupon(id);
    }
}
