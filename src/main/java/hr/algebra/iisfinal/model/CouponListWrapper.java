package hr.algebra.iisfinal.model;

import hr.algebra.iisfinal.dto.CouponDTO;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "coupons")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponListWrapper {

    @XmlElement(name = "coupon")
    private List<CouponDTO> coupons = new ArrayList<>();
}