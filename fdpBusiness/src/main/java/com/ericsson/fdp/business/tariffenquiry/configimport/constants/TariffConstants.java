package com.ericsson.fdp.business.tariffenquiry.configimport.constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class  TariffConstants {

	private TariffConstants() {
		
	}
	
	/** The JSON_TAG_MODULECONFIGURATION */
	public static String JSON_TAG_MODULECONFIGURATION="ModuleConfiguration";
	
	/** The JSON_TAG_DIRECTORY_PATH */
	public static String JSON_TAG_DIRECTORY_PATH="DirectoryPath";
	
	/** The JSON_TAG_OUTPUT_FILE_NAME */
	public static String JSON_TAG_OUTPUT_FILE_NAME="OutputFileName";
	
	/** The JSON_TAG_OUTPUT_FILE_NAME_FOR_SERVICE */
	public static String JSON_TAG_OUTPUT_FILE_NAME_FOR_SERVICE="OutputFileNameForServices";
	
	/** The JSON_TAG_STATUS_REPORT_CIRCLE_FILE_BASE_PATH */
	public static String JSON_TAG_STATUS_REPORT_CIRCLE_FILE_BASE_PATH="StatusReportCirclesFileBasePath";
	
	/** The JSON_TAG_STATUS_REPORT_CIRCLE_FILE_NAME */
	public static String JSON_TAG_STATUS_REPORT_CIRCLE_FILE_NAME="StatusReportCirclesFileName";
	
	/** The JSON_TAG_OUTPUT_DIRECTORY */
	public static String JSON_TAG_OUTPUT_DIRECTORY="OutputDirectory";
	
	/** The JSON_TAG_CIRCLE_PARSING_REPORT */
	public static String JSON_TAG_CIRCLE_PARSING_REPORT="CirclesParsingReport";
	
	/** The JSON_TAG_CIRCLE_NAME */
	public static String JSON_TAG_CIRCLE_NAME="CircleName";
	
	/** The JSON_TAG_STATUS */
	public static String JSON_TAG_STATUS="Status";
	
	/** The JSON_TAG_SUCESSFULL */
	public static String JSON_TAG_SUCESSFULL="Successful";
	
	/** The JSON_TAG_KEY */
	public static String JSON_TAG_KEY="Key";
	
	/** The JSON_TAG_VALUES */
	public static String JSON_TAG_VALUES="Values";
	
	/** The TGZ_FILE_EXTENSION */ 
	public static String TGZ_FILE_EXTENSION=".tgz";
	
	/** The JAVA_JAR */
	public static String JAVA_JAR="java -jar ";
	
	/** The SPACE */
	public static String SPACE=" ";
	
	/** The LINE_SEPERATOR */
	public static String FILE_SEPERATOR="file.separator";
	
	/** The CSV_FILE_TYPE */
	public static String CSV_FILE_TYPE=".csv";
	
	/** The JSON_FILE_TYPE*/
	public static String JSON_FILE_TYPE=".json";
	
	/** The compressedExtensions */
	public static Set<String> compressedExtensions = new HashSet<String>(Arrays.asList(".zip" , ".tar" , ".tgz"));
	
	/** The TARIFF_ATTRIBTE_CACHE_VALUE*/
	public static String TARIFF_ATTRIBTE_CACHE_VALUE="tariff_cachel_value";
	
	/** The TARIFF_ATTRIBUTE_CACHE_VALUE_STATUS */
	public static String TARIFF_ATTRIBUTE_CACHE_VALUE_STATUS="tariff_cache_value_status";
	
	/** The TARIFF_SERVICE_CLASS_MAP_VALUE */
	public static String TARIFF_SERVICE_CLASS_MAP_VALUE="tariff.sc.map.key.value";
	
	/** The TARIFF_SERVICE_CLASS_MAP_STATUS */
	public static String TARIFF_SERVICE_CLASS_MAP_STATUS="tariff.sc.map.key.status";
	
	/** The DEFAULT_TARIFF_NOT_FOUND */
	public static String DEFAULT_TARIFF_NOT_FOUND="DEFAULT_TNF";
	
	/** The TARIFF_ATTRIBUTES_EXPRESSION_KEY */
	public static String TARIFF_ATTRIBUTES_EXPRESSION_KEY="_FILTER";
}
