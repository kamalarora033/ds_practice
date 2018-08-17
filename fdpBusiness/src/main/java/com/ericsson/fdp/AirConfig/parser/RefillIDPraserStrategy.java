package com.ericsson.fdp.AirConfig.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.ericsson.fdp.AirConfig.pojo.Refill;
import com.ericsson.fdp.business.util.XPathUtil;
import com.ericsson.fdp.business.util.XPathUtilImpl;

/**
 * Strategy for parsing the Refill File
 * */
public class RefillIDPraserStrategy implements ParseStrategy {

	private static final XPathUtil XPATHUTIL = XPathUtilImpl.INSTANCE;
	private List<String> _refillnameLst;
	private List<String> _refillnamewithSegmentationIDLst;
	private List<String> _refillIDLst;
	private List<String> _refillIDwithSegmentationIDLst;
	private List<String> _serviceIDlst;
	private List<String> _serviceValuelst;

	@Override
	public Object parse(File file) {

		List<Refill> refillListwithsetfieldModifier = new ArrayList<Refill>();
		List<Refill> refillListwithsetsegmentationModifier = new ArrayList<Refill>();

		_refillnameLst = getListValue(file,
				ParsingConstant.TARIFFWITHSETFIELDMODIFIER.get_value());
		_refillnamewithSegmentationIDLst = getListValue(file,
				ParsingConstant.TARIFFWITHSETSEGMENTAIONIDMODIFIER.get_value());
		_refillIDLst = getListValue(file,
				ParsingConstant.TARIFFIDWITHSETFIELDMODIFIER.get_value());
		_refillIDwithSegmentationIDLst = getListValue(file,
				ParsingConstant.TARIFFIDWITHSETSEGMENTAIONIDMODIFIER
						.get_value());
		_serviceIDlst = getListValue(file,
				ParsingConstant.SERVICE_IDS.get_value());
		_serviceValuelst = getListValue(file,
				ParsingConstant.SERVICE_VALUES.get_value());

		// set the Refill Name and Category
		populateRefillNameWithSetModifier(_refillnameLst,
				refillListwithsetfieldModifier);
		populateRefillNameWithSegmentationId(_refillnamewithSegmentationIDLst,
				refillListwithsetsegmentationModifier);
		// set the Refill ID
		populateTariffIDwithSetFieldModifier(_refillIDLst,
				refillListwithsetfieldModifier);
		populateTariffIDwithSegmentationId(_refillIDwithSegmentationIDLst,
				refillListwithsetsegmentationModifier);

		refillListwithsetfieldModifier
				.addAll(refillListwithsetsegmentationModifier);
		pupulateServiceID(refillListwithsetfieldModifier, _serviceIDlst,
				_serviceValuelst);
		return refillListwithsetfieldModifier;
	}

	private void pupulateServiceID(List<Refill> refillListwithsetfieldModifier,
			List<String> _serviceIDlst2, List<String> _serviceValuelst2) {
		for (int i = 0; i < _serviceIDlst2.size(); i++) {
			String[] splitedValues = _serviceValuelst2.get(i).split(";");
			for (int j = 0; j < splitedValues.length; j++) {
				String refillid = splitedValues[j].split(",")[0];
				for (int k = 0; k < refillListwithsetfieldModifier.size(); k++) {
					if (refillListwithsetfieldModifier.get(k).getRefillID()
							.equals(refillid)) {
						if (refillListwithsetfieldModifier.get(k)
								.getServiceids() == null) {
							refillListwithsetfieldModifier.get(k)
									.setServiceids(new ArrayList<String>(4));
						}
						refillListwithsetfieldModifier.get(k).getServiceids()
								.add(_serviceIDlst2.get(i));
					}
				}
			}

		}
	}

	/**
	 * Sets the tariff ID with segmentationId
	 **/

	private void populateTariffIDwithSegmentationId(
			List<String> refillIDwithSegmentationIDLst,
			List<Refill> refillListwithsetsegmentationModifier) {
		populateTariffIDwithSetFieldModifier(refillIDwithSegmentationIDLst,
				refillListwithsetsegmentationModifier);

	}

	/**
	 * Sets the refill ID with setFieldModifier
	 * 
	 * */
	private void populateTariffIDwithSetFieldModifier(List<String> refillIDLst,
			List<Refill> refillList) {
		int i = 0;
		for (String refillID : refillIDLst) {
			refillList.get(i).setRefillID(refillID);
			i++;
		}
	}

	/**
	 * Set the Refill Ids with set field Modifier token
	 * */
	private void populateRefillNameWithSetModifier(List<String> refillnameLst,
			List<Refill> refillLst) {
		Refill refill;

		for (Iterator iterator = refillnameLst.iterator(); iterator.hasNext();) {
			String refillName = (String) iterator.next();
			refill = new Refill();
			refill.setRefillcatgory(RefillCategoryEnum.SETFIELDMODIFIER);
			refill.setRefillname(refillName);
			refillLst.add(refill);
		}
	}

	/**
	 * Set the Refill Ids with set segmentation Modifier
	 * */
	private void populateRefillNameWithSegmentationId(
			List<String> refillnameLst, List<Refill> refillLst) {
		Refill refill;

		for (Iterator iterator = refillnameLst.iterator(); iterator.hasNext();) {
			String refillName = (String) iterator.next();
			refill = new Refill();
			refill.setRefillcatgory(RefillCategoryEnum.SETSEGMENTATIONMODIFIER);
			refill.setRefillname(refillName);
			refillLst.add(refill);
		}
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
		// TODO Auto-generated method stub
		return null;
	};

}
