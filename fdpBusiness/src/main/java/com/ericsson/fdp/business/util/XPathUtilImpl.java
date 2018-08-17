package com.ericsson.fdp.business.util;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;




import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList;



/**
 * Xpath Util for Parsing XPATH'S
 * Singleton implementation 
 * */
public enum XPathUtilImpl implements XPathUtil{

	INSTANCE;

	

	private XPath _xPath ;
//	private static final Logger LOGGER = LoggerFactory.getLogger(XPathUtil.class);
	
	
	private XPathUtilImpl() {
		_xPath=XPathFactory.newInstance().newXPath();
	}

	/**
	 * Get the document from the file only be used in case of xml file
	 * @param java.io.File
	 * @return org.w3c.dom.Document
	 * */
	public Document getDocument(File file) throws ParserConfigurationException,
			SAXException, IOException {

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(file);
		return document;
	};
	
	
	/**
	 * Get file based on XPath
	 * @param org.w3c.dom.Document
	 * @param String 
	 * @throws XPathExpressionException 
	 * 
	 * */
	public <T> T parseXPath(Document document,String querypath,T t) throws XPathExpressionException
	{
		if(t instanceof String)
		{
			return  (T) parseXpathString(document,querypath);
		}
		else if(t instanceof List)
		{
			//System.out.println("Entered for List case");
			return (T) parseXpathList(document,querypath);
		}
		else if(t instanceof Node)
		{
			return (T) getNode(document, querypath);
		}
		else if(t instanceof NodeList)
		{
			//System.out.println("Entered for NodeList case");
			return (T) getNodeList(document, querypath);
		}
		else
		{
			//set Logger Here
		}
		return null;
	}

	/**
	 * For return Xpath List Type
	 * @param org.w3c.dom.Document 
	 * @param query for evaluation 
	 * */
	private List<String> parseXpathList(Document document, String querypath) throws XPathExpressionException {
		NodeList nodeList=getNodeList(document,querypath);
		
		
		List<String> valueList= null;
		for(int i=0;i<nodeList.getLength();i++)
		{
			Node node=nodeList.item(i);
			if(node.getNodeType()==Node.ATTRIBUTE_NODE || node.getNodeType()==Node.ELEMENT_NODE||node.getNodeType()==Node.TEXT_NODE)
			{
				if(valueList==null)
				{
					valueList=new ArrayList<String>(); 
				}
				valueList.add(node.getNodeValue());
			}
		}
		
		//System.out.println("valueList :: "+valueList);
		return valueList;
		}

	//not returning anything
	private NodeList getNodeList(Document document, String querypath) throws XPathExpressionException {
	
		
		return (NodeList) _xPath.compile(querypath).evaluate(document,XPathConstants.NODESET);
		
	}

	/**
	 * For return Xpath String Type
	 * @param org.w3c.dom.Document 
	 * @param query for evaluation 
	 **/
	private String parseXpathString(Document document, String querypath) throws XPathExpressionException {
		Node node =getNode(document,querypath);
		if(node!=null)
		{
		if((node.getNodeType()==Node.ATTRIBUTE_NODE || node.getNodeType()==Node.ELEMENT_NODE)&& node.getNodeValue()!=null)
			return node.getNodeValue();
		else
			return node.getNodeName();
		}
		else
		{
		return  null;
		}
	}

	/**
	 * For return Xpath Node Type
	 * @param org.w3c.dom.Document 
	 * @param query for evaluation 
	 **/
	private Node getNode(Document document, String querypath) throws XPathExpressionException {
		
		Node node=(Node) _xPath.compile(querypath).evaluate(document,XPathConstants.NODE);
		
		if(node!=null)
		{
			return node;
		}
		else
		{
			return null;
		}
		
	};
	

};
