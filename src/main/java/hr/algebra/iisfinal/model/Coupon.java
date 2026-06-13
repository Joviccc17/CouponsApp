package hr.algebra.iisfinal.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupon")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    private String id;
    private String name;
    private Double percentOff;
    private Long amountOff;
    private String currency;
    private String duration;
    private Integer durationInMonths;
    private Integer maxRedemptions;
    private Integer timesRedeemed;
    private Boolean valid;
    private Long created;
}