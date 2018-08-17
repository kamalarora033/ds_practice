package com.ericsson.fdp.business.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public interface XPathUtil {
	Document getDocument(File file) throws ParserConfigurationException,
			SAXException, IOException;

	<T> T parseXPath(Document document, String querypath, T t)
			throws XPathExpressionException;
}
