package hr.algebra.iisfinal.service;

import hr.algebra.iisfinal.dto.CouponDTO;
import hr.algebra.iisfinal.model.Coupon;
import hr.algebra.iisfinal.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final StripeApiService stripeApiService;
    private final XmlGenerationService xmlGenerationService;

    @Value("${app.use-stripe-api:false}")
    private boolean useStripeApi;

    public List<CouponDTO> getAllCoupons() {
        if (useStripeApi) {
            return stripeApiService.fetchAll();
        }
        return couponRepository.findAll().stream().map(this::toDto).toList();
    }

    public Optional<CouponDTO> getCouponById(String id) {
        if (useStripeApi) {
            return stripeApiService.fetchById(id);
        }
        return couponRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public CouponDTO saveCoupon(CouponDTO dto) {
        Coupon coupon = toEntity(dto);
        if (coupon.getCreated() == null) {
            coupon.setCreated(System.currentTimeMillis() / 1000);
        }
        if (coupon.getTimesRedeemed() == null) {
            coupon.setTimesRedeemed(0);
        }
        Coupon saved = couponRepository.save(coupon);
        xmlGenerationService.generateCouponsXml();
        return toDto(saved);
    }

    @Transactional
    public Optional<CouponDTO> updateCoupon(String id, CouponDTO dto) {
        return couponRepository.findById(id).map(existing -> {
            if (dto.getName() != null) existing.setName(dto.getName());
            if (dto.getPercentOff() != null) existing.setPercentOff(dto.getPercentOff());
            if (dto.getAmountOff() != null) existing.setAmountOff(dto.getAmountOff());
            if (dto.getCurrency() != null) existing.setCurrency(dto.getCurrency());
            if (dto.getDuration() != null) existing.setDuration(dto.getDuration());
            if (dto.getDurationInMonths() != null) existing.setDurationInMonths(dto.getDurationInMonths());
            if (dto.getMaxRedemptions() != null) existing.setMaxRedemptions(dto.getMaxRedemptions());
            if (dto.getValid() != null) existing.setValid(dto.getValid());
            Coupon saved = couponRepository.save(existing);
            xmlGenerationService.generateCouponsXml();
            return toDto(saved);
        });
    }

    @Transactional
    public boolean deleteCoupon(String id) {
        if (couponRepository.existsById(id)) {
            couponRepository.deleteById(id);
            xmlGenerationService.generateCouponsXml();
            return true;
        }
        return false;
    }

    public CouponDTO toDto(Coupon c) {
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

    public Coupon toEntity(CouponDTO d) {
        return Coupon.builder()
                .id(d.getId())
                .name(d.getName())
                .percentOff(d.getPercentOff())
                .amountOff(d.getAmountOff())
                .currency(d.getCurrency())
                .duration(d.getDuration())
                .durationInMonths(d.getDurationInMonths())
                .maxRedemptions(d.getMaxRedemptions())
                .timesRedeemed(d.getTimesRedeemed())
                .valid(d.getValid())
                .created(d.getCreated())
                .build();
    }
}