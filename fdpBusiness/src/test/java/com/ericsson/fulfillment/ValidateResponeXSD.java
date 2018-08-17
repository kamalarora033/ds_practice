package com.ericsson.fulfillment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Before;
import org.junit.Test;

public class ValidateResponeXSD {

	/** This directory stores all the response xml files. **/
	final static String dirPath = "C:\\Users\\ashish2555\\Desktop\\FDP_IVR_Integration_SampleResponses\\";

	/** This is the path of fulfillmentService.xsd **/
	final static String xsdPath = "C:\\Users\\ashish2555\\Desktop\\FDP_IVR_Integration_SampleResponses\\fulfillmentService.xsd";

	/** This is the list containing all file names in the dirPath **/
	private static List<String> xmlFileList = null;

	@Before
	public void setUp() {
		xmlFileList = getAllXmlFiles();
	}

	/**
	 * This method gets all the file listing to validate.
	 * 
	 * @return
	 */
	private static List<String> getAllXmlFiles() {
		List<String> list = new ArrayList<>();
		try {
			File file = new File(dirPath);
			File[] files = file.listFiles();
			for (final File xmlFile : files) {
				String xmlFileName = xmlFile.getAbsolutePath();
				if (xmlFileName.endsWith(".xml")) {
					list.add(xmlFileName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@Test
	public void execute() {
		try {
			if (null != xmlFileList) {
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema schema = schemaFactory.newSchema(new File(xsdPath));
				Validator validator = schema.newValidator();
				for (final String xmlPath : xmlFileList) {
					System.out.println("Processing xmlFile:" + xmlPath);
					boolean status = validateXmlAgainstXSDUtil(validator, xmlPath);
					assertEquals(true, status);
					System.out.println("Validation Status:" + status + " , xml:" + xmlPath);
					System.out.println();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method validates the all files against xsd.
	 * 
	 * @param validator
	 * @param xmlPath
	 * @return
	 */
	private static boolean validateXmlAgainstXSDUtil(final Validator validator, final String xmlPath) {
		boolean validationStatus = false;
		try {
			validator.validate(new StreamSource(new File(xmlPath)));
			validationStatus = true;
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("<<Fail Message:" + e.getMessage() + ", Cause:" + e.getCause() + ">>");
		}
		return validationStatus;
	}

}
