
package com.mtn.esf.xmlns.wsdl.updatecustomerdetails;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.mtn.esf.xmlns.xsd.updatecustomerdetails.UpdateCustomerDetailsRequestType;
import com.mtn.esf.xmlns.xsd.updatecustomerdetails.UpdateCustomerDetailsResponseType;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.4-b01
 * Generated source version: 2.2
 * 
 */
@WebService(name = "UpdateCustomerDetailsPortType", targetNamespace = "http://xmlns.esf.mtn.com/wsdl/UpdateCustomerDetails")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    com.mtn.esf.xmlns.xsd.common.ObjectFactory.class,
    com.mtn.esf.xmlns.xsd.updatecustomerdetails.ObjectFactory.class
})
public interface UpdateCustomerDetailsPortType {


    /**
     * 
     * @param updateCustomerDetailsRequest
     * @return
     *     returns com.mtn.esf.xmlns.xsd.updatecustomerdetails.UpdateCustomerDetailsResponseType
     */
    @WebMethod(operationName = "UpdateCustomerDetailsOperation")
    @WebResult(name = "UpdateCustomerDetailsResponse", targetNamespace = "http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails", partName = "UpdateCustomerDetailsResponse")
    public UpdateCustomerDetailsResponseType updateCustomerDetailsOperation(
        @WebParam(name = "UpdateCustomerDetailsRequest", targetNamespace = "http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails", partName = "UpdateCustomerDetailsRequest")
        UpdateCustomerDetailsRequestType updateCustomerDetailsRequest);

}
