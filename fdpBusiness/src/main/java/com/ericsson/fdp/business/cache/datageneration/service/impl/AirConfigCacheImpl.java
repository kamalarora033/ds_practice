package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.AirConfig.parser.AirConfigParserManager;
import com.ericsson.fdp.AirConfig.pojo.Offer;
import com.ericsson.fdp.AirConfig.pojo.Refill;
import com.ericsson.fdp.business.cache.datageneration.service.AirConfigCacheService;
import com.ericsson.fdp.business.vo.FDPOfferAttributeVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;

@Stateless(mappedName = "AirConfigCacheImpl")
public class AirConfigCacheImpl implements AirConfigCacheService {

	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AirConfigCacheImpl.class);

	private AirConfigParserManager airConfigParserManager;

	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO)
			throws FDPServiceException {

		return false;
	}

	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle)
			throws FDPServiceException {
		String airConfigFilePath = PropertyUtils
				.getProperty("COM.ERICSSON.AIRCONFIGFILEPATH");
		Map<String, String> mapAirConfigPath = getCircleNamesConfig(airConfigFilePath);
		List<FDPCircle> fdpcircles = entityService.getActiveCircleVOs();
		if (mapAirConfigPath != null) {
			for (int i = 0; i < mapAirConfigPath.size(); i++) {
				FDPCircle fdpCircletmp = getFDPCircleFromName(
						fdpcircles.get(i), mapAirConfigPath);
				if(fdpCircletmp!=null && mapAirConfigPath!=null)
				{
				updateCacheCircleBased(fdpCircletmp,
						mapAirConfigPath.get(fdpCircletmp.getCircleName()));
				}
			}
		}
		return true;
	}

	/** Create the cache for Air Configuration CircleWise */
	private void updateCacheCircleBased(FDPCircle fdpCircletmp,
			String configurationFolderPath) {
		List<Offer> offerlst;
		List<Refill> refilllst;
		if(configurationFolderPath!=null)
		{
		airConfigParserManager = new AirConfigParserManager(
				configurationFolderPath);

		try {
			airConfigParserManager.parseRefill();
			airConfigParserManager.parseOffer();

			offerlst = airConfigParserManager.getOfferlst();
			refilllst = airConfigParserManager.getRefilllst();

			createCacheOfferwise(fdpCircletmp, offerlst);
			createCacheRefillwise(fdpCircletmp, refilllst, offerlst);
		} catch (XPathExpressionException e) {

			LOGGER.error("XPATH Expression Error");
		} catch (ParserConfigurationException e) {

			LOGGER.error("Parsing File Error");
		} catch (SAXException e) {

			LOGGER.error("Parsing File Error");
		} catch (IOException e) {

			LOGGER.error("Error in reading config file");
		}
		}
	}

	/**
	 * Refill is stored prefixed by REFILL:
	 * 
	 * @param offerlst
	 */
	private void createCacheRefillwise(FDPCircle fdpCircletmp,
			List<Refill> refilllst, List<Offer> offerlst) {
		String offerid;
		for (Refill refill : refilllst) {
			offerid = getOfferID(refill, offerlst);
			refill.setOfferid(offerid);
			FDPMetaBag metaBag = new FDPMetaBag(fdpCircletmp,
					ModuleType.AIR_CONFIG, "REFILL" + FDPConstant.COLON
							+ refill.getRefillname());
			fdpCache.putValue(metaBag, refill);
		}

	}

	private String getOfferID(Refill refill, List<Offer> offerlst) {
		for (Offer offer : offerlst) {
			if (offer.get_refill().getRefillname()
					.equals(refill.getRefillname()))
				return offer.get_offerID();
		}
		return null;
	}

	/**
	 * Refill is stored prefixed by OFFER:
	 * 
	 * @param refilllst
	 */
	private void createCacheOfferwise(FDPCircle fdpCircletmp,
			List<Offer> offerlst) {
		for (Iterator<Offer> iterator = offerlst.iterator(); iterator.hasNext();) {
			Offer offer = (Offer) iterator.next();
			FDPMetaBag metaBag = new FDPMetaBag(fdpCircletmp,
					ModuleType.AIR_CONFIG, "OFFER:" + offer.get_offerID());
			fdpCache.putValue(metaBag, offer);
		}

	}

	/** Get the FDPCircle on the basis of circle name */
	private FDPCircle getFDPCircleFromName(FDPCircle fdpCircle,
			Map<String, String> mapAirConfigPath) {
		if (mapAirConfigPath.get(fdpCircle.getCircleName()) != null) {
			return fdpCircle;
		}
		return null;
	}

	/**
	 * This method will return the Configuration based on circle names
	 * 
	 * @param airConfigFilePath
	 * @return
	 */
	private Map<String, String> getCircleNamesConfig(String airConfigFilePath) {
		Map<String, String> tempMap = null;
		if (airConfigFilePath != null) {
			String[] airConfigs = airConfigFilePath.split(FDPConstant.COMMA);
			tempMap = new HashMap<String, String>();
			for (int i = 0; i < airConfigs.length; i++) {
				airConfigs = airConfigFilePath.split(FDPConstant.AT_THE_RATE);
				if ((airConfigs.length >= 2 && airConfigs.length <= 3) ? true
						: false) {
					if (airConfigs.length == 2)
						tempMap.put(airConfigs[0], airConfigs[1]);
					else
						tempMap.put(airConfigs[0], airConfigs[1] + "/"
								+ airConfigs[2]);
				} else
					LOGGER.error("Configuration in not correct : "
							+ airConfigFilePath);
			}
		}
		return tempMap;
	}

	/**
	 * @return ModuleType.AIR_CONFIG
	 */
	@Override
	public ModuleType getModuleType() {

		return ModuleType.AIR_CONFIG;
	}

}
