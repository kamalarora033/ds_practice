
package com.mtn.esf.xmlns.xsd.common;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.mtn.esf.xmlns.xsd.common package. 
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

    private final static QName _CommonComponents_QNAME = new QName("http://xmlns.esf.mtn.com/xsd/Common", "CommonComponents");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.mtn.esf.xmlns.xsd.common
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CommonComponentsType }
     * 
     */
    public CommonComponentsType createCommonComponentsType() {
        return new CommonComponentsType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CommonComponentsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xmlns.esf.mtn.com/xsd/Common", name = "CommonComponents")
    public JAXBElement<CommonComponentsType> createCommonComponents(CommonComponentsType value) {
        return new JAXBElement<CommonComponentsType>(_CommonComponents_QNAME, CommonComponentsType.class, null, value);
    }

}
