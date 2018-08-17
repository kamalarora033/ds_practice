/**
 * 
 */
package com.ericsson.fdp.business.constants;

/**
 * @author Ericsson
 * 
 */
public class XMLResponseConstants {

	public static final String XML_RESPONSE ="<methodResponse><params><param><value><struct>"
			+ "<member>\n<name>TransactionId</name>\n<value><string>${TransactionId}</string></value>\n</member>"
			+ "<member>\n<name>TransactionTime</name>\n<value><dateTime.iso8601>${TransactionTime}+0000</dateTime.iso8601></value>\n</member>"
			+ "<member>\n<name>USSDResponseString</name>\n<value><string>${USSDResponseString}</string> </value>\n</member>"
			+ "<member><name>action</name>\n<value><string>${action}</string></value>\n</member>"
			+ "</struct></value></param></params></methodResponse>";
	
	public static final String XML_FAULT_RESPONSE = "<methodResponse><params><param><value><struct>"
			+ "<member><name>TransactionId</name><value><string>${TransactionId}</string></value></member>"
			+ "<member><name>TransactionTime</name><value><dateTime.iso8601>${TransactionTime}+0000</dateTime.iso8601></value></member>"
			+ "<member><name>faultCode</name><value><string>${faultCode}</string> </value></member>"
			+ "<member><name>faultString</name><value><string>${faultString}</string></value></member>"
			+ "</struct></value></param></params></methodResponse>";

}
