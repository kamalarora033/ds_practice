package com.ericsson.fdp.business.util;

import org.apache.camel.builder.RouteBuilder;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.dao.dto.FDPADCConfigDTO;
import com.ericsson.fdp.dao.dto.FDPAIRConfigDTO;
import com.ericsson.fdp.dao.dto.FDPAbilityConfigDTO;
import com.ericsson.fdp.dao.dto.FDPCGWConfigDTO;
import com.ericsson.fdp.dao.dto.FDPCMSConfigDTO;
import com.ericsson.fdp.dao.dto.FDPDMCConfigDTO;
import com.ericsson.fdp.dao.dto.FDPMCLoanConfigDTO;
import com.ericsson.fdp.dao.dto.FDPOfflineConfigDTO;
import com.ericsson.fdp.dao.dto.FDPVASConfigDTO;
import com.ericsson.fdp.dao.dto.FDPRSConfigDTO;
import com.ericsson.fdp.dao.entity.ExternalSystemType;

/**
 * The Class HttpUtil contains utility methods for Http Endpoint Routes in
 * Camel.
 */
public class HttpUtil {

	/**
	 * Gets the route builder.
	 * 
	 * @return the route builder
	 */
	public static RouteBuilder getRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {

			}
		};
	}

	/**
	 * Gets the http enpoint url.
	 * 
	 * @return the http enpoint url
	 */
	public static String getHttpEnpointUrl() {
		return new StringBuilder(BusinessConstants.HTTP_COMPONENT_TYPE).append("127.0.0.1:8080/").toString();
	}

	/**
	 * Gets the route id for http endpoint.
	 * 
	 * @param circldeCode
	 *            the circlde code
	 * @param ip
	 *            the ip
	 * @param port
	 *            the port
	 * @param channelType
	 *            the channel type
	 * @return the route id for http endpoint
	 */
	public static String getRouteIdForHttpEndpoint(final String circldeCode, final String ip, final String port,
			final String channelType) {
		return new StringBuilder(circldeCode).append(BusinessConstants.COLON).append(BusinessConstants.COLON)
				.append(port).append(BusinessConstants.COLON).append(channelType).toString();
	}

	/**
	 * Gets the endpoint for m carbon.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param mcarbonType
	 *            the external system type
	 * @param logicalName
	 *            the logical name
	 * @return the endpoint for m carbon
	 */
	public static String getEndpointForMCarbon(final String circleCode, final ExternalSystemType mcarbonType,
			final String logicalName) {
		return new StringBuilder(BusinessConstants.CAMEL_DIRECT).append(
				getRouteIdForMCarbonHttpRoutes(circleCode, mcarbonType, logicalName)).toString();
	}
	
	/**
	 * Gets the endpoint for m carbon.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param mcarbonType
	 *            the external system type
	 * @param logicalName
	 *            the logical name
	 * @return the endpoint for m carbon
	 */
	public static String getEndpointForMCarbonLoan(final String circleCode, final ExternalSystemType mcarbonType,
			final String logicalName, final Long systemId) {
		return new StringBuilder(BusinessConstants.CAMEL_DIRECT).append(
				getRouteIdForMCarbonLoanHttpRoutes(circleCode, mcarbonType, logicalName, systemId)).toString();
	}
	
	

	/**
	 * Gets the http endpoint for m carbon.
	 * 
	 * @param fdpMCarbonConfigDTO
	 *            the fdp m carbon config dto
	 * @return the http endpoint for m carbon
	 */
	public static String getHttpEndpointForMCarbon(final FDPVASConfigDTO fdpMCarbonConfigDTO) {
		return new StringBuilder(BusinessConstants.HTTP_COMPONENT_TYPE)
				.append(fdpMCarbonConfigDTO.getIpAddress().getValue()).append(BusinessConstants.COLON)
				.append(fdpMCarbonConfigDTO.getPort()).append(BusinessConstants.FORWARD_SLASH)
				.append(fdpMCarbonConfigDTO.getContextPath()).toString();
	}
	
	/**
	 * Gets the http endpoint for m carbon.
	 * 
	 * @param fdpMCarbonConfigDTO
	 *            the fdp mcloan config dto
	 * @return the http endpoint for mcloan
	 */
	public static String getHttpEndpointForMCLoan(final FDPMCLoanConfigDTO fdpMCLoanConfigDTO) {
		return new StringBuilder(BusinessConstants.HTTP_COMPONENT_TYPE)
				.append(fdpMCLoanConfigDTO.getIpAddress().getValue()).append(BusinessConstants.COLON)
				.append(fdpMCLoanConfigDTO.getPort()).append(BusinessConstants.QUERY_STRING_SEPARATOR)
				.append(BusinessConstants.HTTP_CLIENT_SO_TIMEOUT).append(BusinessConstants.EQUALS)
				.append(fdpMCLoanConfigDTO.getResponseTimeout())
				.toString();
	}

	/**
	 * Gets the http endpoint for manhattan.
	 * 
	 * @param fdpMCarbonConfigDTO
	 *            the fdp m carbon config dto
	 * @return the http endpoint for manhattan
	 */
	public static String getHttpEndpointForManhattan(final FDPVASConfigDTO fdpMCarbonConfigDTO) {
		return new StringBuilder(BusinessConstants.HTTP_COMPONENT_TYPE)
				.append(fdpMCarbonConfigDTO.getIpAddress().getValue()).append(BusinessConstants.COLON)
				.append(fdpMCarbonConfigDTO.getPort()).append(BusinessConstants.FORWARD_SLASH)
				.append(fdpMCarbonConfigDTO.getContextPath()).toString();
	}

	/**
	 * Gets the route id for m carbon http routes.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param manhattanType
	 *            the manhattan type
	 * @param logicalName
	 *            the logical name
	 * @return the route id for m carbon http routes
	 */
	public static String getRouteIdForMCarbonHttpRoutes(final String circleCode,
			final ExternalSystemType mCarbonType, final String logicalName) {
		return new StringBuilder(circleCode).append(BusinessConstants.UNDERSCORE).append(mCarbonType.getValue())
				.append(BusinessConstants.UNDERSCORE).append(logicalName).toString();
	}
	
	/**
	 * Gets the route id for m carbon http routes.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param manhattanType
	 *            the manhattan type
	 * @param logicalName
	 *            the logical name
	 * @return the route id for m carbon http routes
	 */
	public static String getRouteIdForMCarbonLoanHttpRoutes(final String circleCode,
			final ExternalSystemType mCarbonType, final String logicalName, final Long systemId) {
		return new StringBuilder(circleCode).append(BusinessConstants.UNDERSCORE).append(mCarbonType.getValue())
				.append(BusinessConstants.UNDERSCORE).append(systemId)
				.append(BusinessConstants.UNDERSCORE).append(logicalName).toString();
	}

	/**
	 * Gets the endpoint for manhattan.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param manhattanType
	 *            the manhattan type
	 * @param logicalName
	 *            the logical name
	 * @return the endpoint for manhattan
	 */
	public static String getEndpointForManhattan(final String circleCode, final ExternalSystemType manhattanType,
			final String logicalName) {
		return new StringBuilder(BusinessConstants.CAMEL_DIRECT).append(
				getRouteIdForManhattanHttpRoutes(circleCode, manhattanType, logicalName)).toString();
	}

	/**
	 * Gets the route id for manhattan http routes.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param manhattanType
	 *            the manhattan type
	 * @param logicalName
	 *            the logical name
	 * @return the route id for manhattan http routes
	 */
	public static String getRouteIdForManhattanHttpRoutes(final String circleCode,
			final ExternalSystemType manhattanType, final String logicalName) {
		return new StringBuilder(circleCode).append(BusinessConstants.UNDERSCORE).append(manhattanType.getValue())
				.append(BusinessConstants.UNDERSCORE).append(logicalName).toString();
	}

	/**
	 * Gets the air sub route id.
	 * 
	 * @param fdpairConfigDTO
	 *            the fdpair config dto
	 * @param circleCode
	 *            the circle code
	 * @return the air sub route id
	 */
	public static String getAirSubRouteId(final FDPAIRConfigDTO fdpairConfigDTO, final String circleCode) {
		return new StringBuilder(BusinessConstants.SUB_ROUTE_UNDERSCORE).append(circleCode)
				.append(fdpairConfigDTO.getExternalSystemId()).append(BusinessConstants.UNDERSCORE)
				.append(fdpairConfigDTO.getLogicalName()).append(BusinessConstants.UNDERSCORE)
				.append(ExternalSystem.AIR.name()).toString();
	}
	
	
	public static String getDmcSubRouteId(final FDPDMCConfigDTO fdpdmcConfigDTO, final String circleCode) {
		return new StringBuilder(BusinessConstants.SUB_ROUTE_UNDERSCORE).append(circleCode)
				.append(fdpdmcConfigDTO.getExternalSystemId()).append(BusinessConstants.UNDERSCORE)
				.append(fdpdmcConfigDTO.getLogicalName()).append(BusinessConstants.UNDERSCORE)
				.append(ExternalSystem.DMC.name()).toString();
	}
	
	/**
	 * Gets the cms sub route id.
	 * 
	 * @param fdpcmsConfigDTO
	 *            the fdpcms config dto
	 * @param circleCode
	 *            the circle code
	 * @return the air sub route id
	 */
	public static String getCmsSubRouteId(final FDPCMSConfigDTO fdpcmsConfigDTO, final String circleCode) {
		return new StringBuilder(BusinessConstants.SUB_ROUTE_UNDERSCORE).append(circleCode)
				.append(fdpcmsConfigDTO.getExternalSystemId()).append(BusinessConstants.UNDERSCORE)
				.append(fdpcmsConfigDTO.getLogicalName()).append(BusinessConstants.UNDERSCORE)
				.append(ExternalSystem.AIR.name()).toString();
	}

	/**
	 * Gets the RS sub route id.
	 * 
	 * @param fdpRsConfigDTO
	 *            the fdp rs config dto
	 * @param circleCode
	 *            the circle code
	 * @return the RS sub route id
	 */
	public static String getRSSubRouteId(FDPRSConfigDTO fdpRsConfigDTO, String circleCode) {
		return new StringBuilder(BusinessConstants.SUB_ROUTE_UNDERSCORE).append(circleCode)
				.append(fdpRsConfigDTO.getExternalSystemId()).append(BusinessConstants.UNDERSCORE)
				.append(fdpRsConfigDTO.getLogicalName()).append(BusinessConstants.UNDERSCORE)
				.append(ExternalSystem.RS.name()).toString();
	}
	
	/**
	 * Gets the ADC sub route id.
	 * 
	 * @param fdpADCConfigDTO
	 *            the fdp adc config dto
	 * @param circleCode
	 *            the circle code
	 * @return the ADC sub route id
	 */
	public static String getADCSubRouteId(FDPADCConfigDTO fdpADCConfigDTO, String circleCode) {
		return new StringBuilder(BusinessConstants.SUB_ROUTE_UNDERSCORE).append(circleCode)
				.append(fdpADCConfigDTO.getExternalSystemId()).append(BusinessConstants.UNDERSCORE)
				.append(fdpADCConfigDTO.getLogicalName()).append(BusinessConstants.UNDERSCORE)
				.append(ExternalSystem.ADC.name()).toString();
	}

	/**
	 * Gets the cgw sub route id.
	 * 
	 * @param fdpCgwConfigDTO
	 *            the fdp cgw config dto
	 * @param circleCode
	 *            the circle code
	 * @return the cgw sub route id
	 */
	public static String getCgwSubRouteId(FDPCGWConfigDTO fdpCgwConfigDTO, String circleCode) {
		return new StringBuilder(BusinessConstants.SUB_ROUTE_UNDERSCORE).append(circleCode)
				.append(fdpCgwConfigDTO.getExternalSystemId()).append(BusinessConstants.UNDERSCORE)
				.append(fdpCgwConfigDTO.getLogicalName()).append(BusinessConstants.UNDERSCORE)
				.append(ExternalSystem.CGW.name()).toString();
	}
	
	/**
	 * Gets the endpoint for fdp offline.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param offlineType
	 *            the external system type
	 * @param logicalName
	 *            the logical name
	 * @return the endpoint for fdp offline
	 */
	public static String getEndpointForOffline(final String circleCode, final ExternalSystemType offlineType,
			final String logicalName, final Long systemId) {
		return new StringBuilder(BusinessConstants.CAMEL_DIRECT).append(
				getRouteIdForOfflineHttpRoutes(circleCode, offlineType, logicalName, systemId)).toString();
	}
	
	/**
	 * Gets the route id for fdp offline http routes.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param offlineType
	 *            the fdp offline type
	 * @param logicalName
	 *            the logical name
	 * @return the route id for fdp offline http routes
	 */
	public static String getRouteIdForOfflineHttpRoutes(final String circleCode,
			final ExternalSystemType offlineType, final String logicalName, final Long systemId) {
		return new StringBuilder(circleCode).append(BusinessConstants.UNDERSCORE).append(offlineType.getValue())
				.append(BusinessConstants.UNDERSCORE).append(systemId)
				.append(BusinessConstants.UNDERSCORE).append(logicalName).toString();
	}
	
	/**
	 * Gets the http endpoint for fdp offline
	 * 
	 * @param FDPOfflineConfigDTO
	 *            the fdp offline config dto
	 * @return the http endpoint for mcloan
	 */
	public static String getEndpointForOffline(final FDPOfflineConfigDTO fdpOfflineConfigDTO) {
		return new StringBuilder(BusinessConstants.HTTP_COMPONENT_TYPE)
				.append(fdpOfflineConfigDTO.getIpAddress().getValue()).append(BusinessConstants.COLON)
				.append(fdpOfflineConfigDTO.getPort()).append(BusinessConstants.QUERY_STRING_SEPARATOR)
				.append(BusinessConstants.HTTP_CLIENT_SO_TIMEOUT).append(BusinessConstants.EQUALS).append("0")
				.toString();
	}
	
	/**
	 * Gets the http endpoint for sbbb.
	 * 
	 * @param fdpVASConfigDTO
	 *            the fdp sbbb config dto
	 * @return the http endpoint for sbbb
	 */
	public static String getHttpEndpointForSBBB(final FDPVASConfigDTO fdpSBBBConfigDTO, final String commandName) {
		return new StringBuilder(BusinessConstants.HTTP_COMPONENT_TYPE)
				.append(fdpSBBBConfigDTO.getIpAddress().getValue()).append(BusinessConstants.COLON)
				.append(fdpSBBBConfigDTO.getPort()).append(BusinessConstants.FORWARD_SLASH)
				.append(fdpSBBBConfigDTO.getContextPath()).append(BusinessConstants.FORWARD_SLASH).append(commandName)
				.append(BusinessConstants.QUERY_STRING_SEPARATOR)
					.append(BusinessConstants.HTTP_CLIENT_SO_TIMEOUT).append(BusinessConstants.EQUALS)
					.append(30000)
				.toString();
	}
	 
	/**
	 * Gets the endpoint for sbbb.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param sbbbType
	 *            the sbbb type
	 * @param logicalName
	 *            the logical name
	 * @return the endpoint for sbbb
	 */
	public static String getEndpointForSBBB(final String circleCode, final ExternalSystemType sbbbType,
			final String logicalName, final String commandName) {
		return new StringBuilder(BusinessConstants.CAMEL_DIRECT).append(
				getRouteIdForSBBBHttpRoutes(circleCode, sbbbType, logicalName)).append(BusinessConstants.UNDERSCORE).append(commandName)
				.toString();
	}
	
	/**
	 * Gets the route id for sbbb http routes.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param sbbbType
	 *            the sbbb type
	 * @param logicalName
	 *            the logical name
	 * @return the route id for sbbb http routes
	 */
	public static String getRouteIdForSBBBHttpRoutes(final String circleCode,
			final ExternalSystemType sbbbType, final String logicalName) {
		return new StringBuilder(circleCode).append(BusinessConstants.UNDERSCORE).append(sbbbType.getValue())
				.append(BusinessConstants.UNDERSCORE).append(logicalName).toString();
	}


    public static String getAbilitySubRouteId(final FDPAbilityConfigDTO abilityConfigDTO, final String circleCode) {
        return new StringBuilder(BusinessConstants.SUB_ROUTE_UNDERSCORE).append(circleCode).append(abilityConfigDTO.getExternalSystemId())
                .append(BusinessConstants.UNDERSCORE).append(abilityConfigDTO.getLogicalName()).append(BusinessConstants.UNDERSCORE)
                .append(ExternalSystem.Ability.name()).toString();
    }
	   
}
