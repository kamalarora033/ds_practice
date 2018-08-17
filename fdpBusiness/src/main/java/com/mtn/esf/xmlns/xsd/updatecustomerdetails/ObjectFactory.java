
package com.mtn.esf.xmlns.xsd.updatecustomerdetails;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.mtn.esf.xmlns.xsd.updatecustomerdetails package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _UpdateCustomerDetailsRequest_QNAME = new QName("http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails", "UpdateCustomerDetailsRequest");
    private final static QName _UpdateCustomerDetailsResponse_QNAME = new QName("http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails", "UpdateCustomerDetailsResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.mtn.esf.xmlns.xsd.updatecustomerdetails
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link UpdateCustomerDetailsRequestType }
     * 
     */
    public UpdateCustomerDetailsRequestType createUpdateCustomerDetailsRequestType() {
        return new UpdateCustomerDetailsRequestType();
    }

    /**
     * Create an instance of {@link UpdateCustomerDetailsResponseType }
     * 
     */
    public UpdateCustomerDetailsResponseType createUpdateCustomerDetailsResponseType() {
        return new UpdateCustomerDetailsResponseType();
    }

    /**
     * Create an instance of {@link ActivationMethodLangType }
     * 
     */
    public ActivationMethodLangType createActivationMethodLangType() {
        return new ActivationMethodLangType();
    }

    /**
     * Create an instance of {@link ChangeSubscriberCreditLimtStatusType }
     * 
     */
    public ChangeSubscriberCreditLimtStatusType createChangeSubscriberCreditLimtStatusType() {
        return new ChangeSubscriberCreditLimtStatusType();
    }

    /**
     * Create an instance of {@link SessionInfoType }
     * 
     */
    public SessionInfoType createSessionInfoType() {
        return new SessionInfoType();
    }

    /**
     * Create an instance of {@link IdentificationType }
     * 
     */
    public IdentificationType createIdentificationType() {
        return new IdentificationType();
    }

    /**
     * Create an instance of {@link CustomReferenceType }
     * 
     */
    public CustomReferenceType createCustomReferenceType() {
        return new CustomReferenceType();
    }

    /**
     * Create an instance of {@link AdditionalStatusType }
     * 
     */
    public AdditionalStatusType createAdditionalStatusType() {
        return new AdditionalStatusType();
    }

    /**
     * Create an instance of {@link StatusInfoType }
     * 
     */
    public StatusInfoType createStatusInfoType() {
        return new StatusInfoType();
    }

    /**
     * Create an instance of {@link ChangeSubscriberLifeCycleStatusType }
     * 
     */
    public ChangeSubscriberLifeCycleStatusType createChangeSubscriberLifeCycleStatusType() {
        return new ChangeSubscriberLifeCycleStatusType();
    }

    /**
     * Create an instance of {@link ChangeSubscriberBlacklistRechargeStatusType }
     * 
     */
    public ChangeSubscriberBlacklistRechargeStatusType createChangeSubscriberBlacklistRechargeStatusType() {
        return new ChangeSubscriberBlacklistRechargeStatusType();
    }

    /**
     * Create an instance of {@link AdditionalInfoType }
     * 
     */
    public AdditionalInfoType createAdditionalInfoType() {
        return new AdditionalInfoType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateCustomerDetailsRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails", name = "UpdateCustomerDetailsRequest")
    public JAXBElement<UpdateCustomerDetailsRequestType> createUpdateCustomerDetailsRequest(UpdateCustomerDetailsRequestType value) {
        return new JAXBElement<UpdateCustomerDetailsRequestType>(_UpdateCustomerDetailsRequest_QNAME, UpdateCustomerDetailsRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateCustomerDetailsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xmlns.esf.mtn.com/xsd/UpdateCustomerDetails", name = "UpdateCustomerDetailsResponse")
    public JAXBElement<UpdateCustomerDetailsResponseType> createUpdateCustomerDetailsResponse(UpdateCustomerDetailsResponseType value) {
        return new JAXBElement<UpdateCustomerDetailsResponseType>(_UpdateCustomerDetailsResponse_QNAME, UpdateCustomerDetailsResponseType.class, null, value);
    }

}
