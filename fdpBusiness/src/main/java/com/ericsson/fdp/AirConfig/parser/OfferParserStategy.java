package com.ericsson.fdp.AirConfig.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.ericsson.fdp.AirConfig.pojo.Offer;
import com.ericsson.fdp.AirConfig.pojo.Refill;
import com.ericsson.fdp.business.util.XPathUtil;
import com.ericsson.fdp.business.util.XPathUtilImpl;

public class OfferParserStategy implements ParseStrategy {

	private static final XPathUtil XPATHUTIL = XPathUtilImpl.INSTANCE;

	private List<Offer> offerlst;
	private List<String> offeridlst;
	private List<String> offerValuelst;
	private List<String> offerTypelst;

	private List<String> accountDivisionmodifieridlst;

	@Override
	public Object parse(File file) {

		return null;
	}

	@Override
	public Object parse(File file, String Expression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object parse(File file, String Expression, Object... additionalparam) {

		return null;
	}

	@Override
	public Object parse(File file, Object... additionalparam) {

		offeridlst = new ArrayList<String>();
		offerValuelst = new ArrayList<String>();
		offerTypelst = new ArrayList<String>();
		offerlst = new ArrayList<Offer>();
		Refill refill = (Refill) additionalparam[0];

		String additionalParamOfferIDXpath = replaceAdditionalParam(
				refill.getRefillID(), ParsingConstant.REFILLIDFOROFFER);
		String additionalParamOfferValueXpath = replaceAdditionalParam(
				refill.getRefillID(), ParsingConstant.REFILLVALUEFOROFFER);
		String accountDivisionmodifierid = replaceAdditionalParam(
				refill.getRefillID(), ParsingConstant.ACCOUNTDIVISIONMODIFIERID);

		offeridlst = getListValue(file, additionalParamOfferIDXpath);
		offerValuelst = getListValue(file, additionalParamOfferValueXpath);
		for (String tmp : offerValuelst) {
			offerTypelst.add(RefillTypeForOfferEnum.getNameByValue(tmp));
		}

		accountDivisionmodifieridlst = getListValue(file,
				accountDivisionmodifierid);

		populateOfferID(offeridlst, offerlst, refill);
		populateOfferValue(offerTypelst, offerlst);
		populateAccountDivisionModifier(accountDivisionmodifieridlst, offerlst);

		/*System.out.println("OFFER ID:-" + offeridlst.get(0));
		System.out.println("OFFER VALUE :-" + offerTypelst.get(0));
		System.out.println("ADDITIONAL CONFIG :-"
				+ accountDivisionmodifieridlst.get(0));*/

		// populateOfferID();
		return offerlst;
	}

	private void populateDAIDs(List<String> daidslst, List<Offer> offerlst) {
		for (int i = 0; i < daidslst.size(); i++) {
			offerlst.get(i).set_daID(daidslst.get(i));
		}
	}

	private void populateAccountDivisionModifier(
			List<String> accountDivisionmodifieridlst, List<Offer> offerlst) {

		for (int i = 0; i < accountDivisionmodifieridlst.size(); i++) {
			offerlst.get(i).set_accountDivisionModifierID(
					accountDivisionmodifieridlst.get(i));
		}

	}

	private void populateOfferValue(List<String> offerValuelst,
			List<Offer> offerlst) {
		for (int i = 0; i < offerValuelst.size(); i++) {
			offerlst.get(i).set_offerName(offerValuelst.get(i));
		}

	}

	private void populateOfferID(List<String> offeridlststr,
			List<Offer> offerlst, Refill refill) {

		for (String offerid : offeridlststr) {
			Offer offer = new Offer();
			offer.set_refill(refill);
			offer.set_offerID(offerid);
			offerlst.add(offer);

		}
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
