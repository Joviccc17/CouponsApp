package hr.algebra.iisfinal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JacksonXmlRootElement(localName = "coupon")
@XmlRootElement(name = "coupon")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponDTO {

    private String id;
    private String name;

    @JsonAlias("percent_off")
    private Double percentOff;

    @JsonAlias("amount_off")
    private Long amountOff;

    private String currency;
    private String duration;

    @JsonAlias("duration_in_months")
    private Integer durationInMonths;

    @JsonAlias("max_redemptions")
    private Integer maxRedemptions;

    @JsonAlias("times_redeemed")
    private Integer timesRedeemed;

    private Boolean valid;
    private Long created;
}