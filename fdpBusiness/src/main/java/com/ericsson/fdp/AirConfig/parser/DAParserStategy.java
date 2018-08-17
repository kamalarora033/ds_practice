package com.ericsson.fdp.AirConfig.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.ericsson.fdp.AirConfig.pojo.Offer;
import com.ericsson.fdp.business.util.XPathUtil;
import com.ericsson.fdp.business.util.XPathUtilImpl;

/**
 * This class will be responsible for parsing DAID's
 * */
public class DAParserStategy implements ParseStrategy {

	private List<String> daidslst;

	private static final XPathUtil XPATHUTIL = XPathUtilImpl.INSTANCE;

	@Override
	public Object parse(File file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object parse(File file, String Expression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object parse(File file, String Expression, Object... additionalparam) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object parse(File file, Object... additionalparam) {

		Offer offer = (Offer) additionalparam[0];
		String daids = replaceAdditionalParam(
				offer.get_accountDivisionModifierID(), ParsingConstant.DA_IDS);

		daidslst = getListValue(file, daids);
		populateDAIDs(daidslst, offer);

		// TODO Auto-generated method stub
		return offer;
	}

	private void populateDAIDs(List<String> daidslst2, Offer offer) {
		offer.set_daID(daidslst.get(0));

	}

	private String replaceAdditionalParam(String additionalparam,
			ParsingConstant refillidforoffer) {
		// refillidforoffer.get_value().replace(target, replacement)

		return refillidforoffer.get_value().replace("{AP_1}", additionalparam);
	}

	private List<String> getListValue(File file, String expression) {

		List<String> list = new ArrayList<String>();
		try {
			list = XPATHUTIL.parseXPath(XPATHUTIL.getDocument(file),
					expression, list);
		} catch (XPathExpressionException xpathExpress) {

		} catch (IOException ioException) {

		} catch (SAXException saxException) {
			// TODO: handle exception
		} catch (ParserConfigurationException pce) {

		}

		return list;

	}
}
