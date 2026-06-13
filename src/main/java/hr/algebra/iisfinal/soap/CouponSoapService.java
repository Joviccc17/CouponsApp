package hr.algebra.iisfinal.soap;

import hr.algebra.iisfinal.dto.CouponDTO;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

import java.util.List;

@WebService(name = "CouponSoapService", targetNamespace = "http://soap.iisfinal.algebra.hr/")
public interface CouponSoapService {

    @WebMethod(operationName = "searchCoupons")
    List<CouponDTO> searchCoupons(@WebParam(name = "term") String term);
}