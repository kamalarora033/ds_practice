package com.ericsson.fdp.AirConfig.parser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.ericsson.fdp.AirConfig.pojo.Offer;
import com.ericsson.fdp.AirConfig.pojo.Refill;
import com.ericsson.fdp.business.util.XPathUtil;
import com.ericsson.fdp.business.util.XPathUtilImpl;

public class AirConfigParserManager {

	// should remove FILEPATH
	static String folderPath;

	// xpaths for the xml files
	static String xPathRefill = ParsingConstant.REFILLFILE.get_value();
	static String xPathOffer = ParsingConstant.ACCOUNTREFILLFILE.get_value();
	static String xPathDA = ParsingConstant.ACCOUNTDIVISIONFILE.get_value();

	private List<Refill> refilllst = new ArrayList<Refill>();
	private List<Offer> offerlst = new ArrayList<Offer>();
	
	//private List<Offer> 
	public AirConfigParserManager(String circleAirConfigFolderPath) {
		folderPath=circleAirConfigFolderPath;
	}

	// returning list of offers extracted from xml
	public List<Offer> parseOffer() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		ParseStrategy offerparserStrategy;
		DAParserStategy daparserstrategy;
		
		ArrayList<Offer> tempOfferlst;
		Offer tempoffer;
		ParseContext parsercontext;
		File fileOffer;
		File fileDA;

		offerparserStrategy = new OfferParserStategy();
		daparserstrategy = new DAParserStategy();

		fileOffer = findFiles(folderPath, xPathOffer);
		fileDA = findFiles(folderPath, xPathDA);


		if (!refilllst.isEmpty()) {
			for (Refill refill: refilllst) {
				if (refill.getRefillcatgory() == RefillCategoryEnum.SETSEGMENTATIONMODIFIER) {
					parsercontext = new ParseContext(offerparserStrategy);
					tempOfferlst = (ArrayList<Offer>) parsercontext.executeStrategy(fileOffer, refill);
					parsercontext = new ParseContext(daparserstrategy);
					tempoffer = (Offer) parsercontext.executeStrategy(fileDA,tempOfferlst.get(0));
					tempOfferlst.add(tempoffer);
					offerlst.addAll(tempOfferlst);
				}

			}
			
		}
		updateRefill(offerlst);
		return offerlst;
	}

	// returning list of refill type extracted from Refill.xml
	public List<Refill> parseRefill() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		RefillIDPraserStrategy refillparserStrategy = new RefillIDPraserStrategy();
		ParseContext parsercontext = new ParseContext(refillparserStrategy);

		File file = findFiles(folderPath, xPathRefill);

		if (file != null) {
			List<Refill> tmplst = (List<Refill>) parsercontext
					.executeStrategy(file);
			//System.out.println("tmplst is :: " + tmplst);
			for (Iterator iterator = tmplst.iterator(); iterator.hasNext();) {
				Refill refill = (Refill) iterator.next();
				refilllst.add(refill);
			}
			// parseOffer();
		} else {
			//System.out.println("REFILL FILE NOT FOUND"); // add to log

		}
		return refilllst;
	}

	// returning list of refill type extracted from Refill.xml
	public synchronized List<Refill> updateRefill(List<Offer> offList)  {
	
		List<Refill> temprefillList = new ArrayList<>();
		int i =0;
		Enumeration<Refill> refillEnumLst=Collections.enumeration(refilllst);
		while(refillEnumLst.hasMoreElements())
		{
		
			Refill tmprefill=refillEnumLst.nextElement();
			
			for(Offer offer: offList) {
				Refill tempofferrefill = offer.get_refill();
				if(tmprefill.getRefillID()==tempofferrefill.getRefillID())
				{
					
					refilllst.get(i).setOfferid(tmprefill.getOfferid());
					
				}
			}
			i++;
		}
		return temprefillList;
	}
	private File findFiles(String folderPath, String xPath)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		//System.out.println(folderPath);
		File f = new File(folderPath);
		File file;
		File[] fileList = f.listFiles();

		XPathUtil xpathUtil = XPathUtilImpl.INSTANCE;

		String string = new String();
		
		if(fileList!=null)
		for (int i = 0; i < fileList.length; i++) {
			file = fileList[i];
			String value = xpathUtil.parseXPath(xpathUtil.getDocument(file),
					xPath, string);
			if (value != null) {
				return file;
			}
		}
		return null;

	}

	public List<Refill> getRefilllst() {
		return refilllst;
	}

	public List<Offer> getOfferlst() {
		return offerlst;
	}
	
	
	
}
