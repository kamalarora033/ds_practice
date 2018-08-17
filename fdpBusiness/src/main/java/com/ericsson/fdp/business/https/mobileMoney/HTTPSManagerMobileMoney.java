package com.ericsson.fdp.business.https.mobileMoney;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.https.evds.HTTPSServerDetailsDTO;

/**
 * This class defines https connection methods 
 *
 * @author Ericsson
 *
 */
public class HTTPSManagerMobileMoney {
		
	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPSManagerMobileMoney.class);
		
	List<HttpsURLConnection> httpsurlconnectionlst;
	List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst;
	String[] urlstoconnect ;
	

	public HTTPSManagerMobileMoney(List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst) {
		this.httpsserverdetailsdtolst=httpsserverdetailsdtolst;
		urlstoconnect=generateHTTPSURL(httpsserverdetailsdtolst);
	}

	/**
	 * 
	 * @param input
	 * @param context
	 * @return this method is used to read content from https server
	 * @throws IOException
	 */
	public String httpsHit(String input, String context) throws IOException {
		DataOutputStream wr = null;
		StringBuffer stringbuffer = new StringBuffer();
		BufferedReader in = null;
		String inputLine;
		HttpsURLConnection httpsurlconnection;

		try {
			httpsurlconnection = getConnection(context);
			wr = new DataOutputStream(httpsurlconnection.getOutputStream());
			wr.writeBytes(input);
			wr.flush();
			wr.close();

			int responseCode = httpsurlconnection.getResponseCode();

			in = new BufferedReader(new InputStreamReader(httpsurlconnection.getInputStream()));

			while ((inputLine = in.readLine()) != null) {
				stringbuffer.append(inputLine).append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return stringbuffer.toString();
	}
		
		
	/*
	 * This method is used for creating connection with https
	 */
	public HttpsURLConnection getConnection(String context) throws IOException {
		int urlid = getHTTPSURL();

		String urlfirst = null;
		String urlcandidate = null;
		HttpsURLConnection httpsURLConnection = null;
		Iterator<HTTPSServerDetailsDTO> it = httpsserverdetailsdtolst.iterator();
		if (null != context) {
			urlstoconnect = generateHTTPSURLForContext(httpsserverdetailsdtolst, context);
		}
		urlfirst = urlstoconnect[urlid];
		HTTPSServerDetailsDTO httpsserverdetaildto = httpsserverdetailsdtolst.get(urlid);

		if (httpsserverdetaildto.isIsenabled() == false) {
			while (it.hasNext()) {
				HTTPSServerDetailsDTO httpsserverdetaildtotmp = (HTTPSServerDetailsDTO) it.next();
				if (httpsserverdetaildtotmp.isIsenabled()) {
					urlcandidate = urlstoconnect[urlid];
					break;
				}
			}
			if (urlfirst.equals(urlcandidate)) {
				throw new IOException("All MM IP are close or Inactive");
			}
		} else {
			URL httpsurl = new URL(urlfirst);
			httpsURLConnection = (HttpsURLConnection) httpsurl.openConnection();
			httpsURLConnection.setConnectTimeout(httpsserverdetaildto.getTimeout());
			httpsURLConnection.setDoOutput(true);
			httpsURLConnection.setRequestMethod("POST");
			httpsURLConnection.setRequestProperty("User-Agent", httpsserverdetaildto.getUseragent());
			httpsURLConnection.setRequestProperty("Accept-Language", httpsserverdetaildto.getAcceptlanguage());
			verify(httpsserverdetaildto);
			connect(httpsURLConnection);
			LOGGER.debug("Connection with https server:" + httpsURLConnection);
		}
		return httpsURLConnection;
	}
	
	/**
	 * This method is used for creating https URL
	 * 
	 * @param httpsserverdetailsdtolst
	 * @return
	 */
	private String[] generateHTTPSURL(List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst) {

		List<String> httpsurls = new ArrayList<String>();
		for (HTTPSServerDetailsDTO httpsserverdetailsdto : httpsserverdetailsdtolst) {
			httpsurls.add("https://" + httpsserverdetailsdto.getIp() + ":" + httpsserverdetailsdto.getPort()
					+ BusinessConstants.FORWARD_SLASH + httpsserverdetailsdto.getContext());
			LOGGER.debug("MM Url:" + httpsurls);
		}
		Object[] arr = httpsurls.toArray();
		return Arrays.copyOf(arr, arr.length, String[].class);
	}
	
	/**
	 * This method creates https url based on the context.
	 * @param httpsserverdetailsdtolst2
	 * @param context
	 * @return
	 */
	private String[] generateHTTPSURLForContext(List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst2, String context) {
		List<String> httpsurls = new ArrayList<String>();
		for (HTTPSServerDetailsDTO httpsserverdetailsdto : httpsserverdetailsdtolst) {
			httpsurls.add("https://" + httpsserverdetailsdto.getIp() + ":" + httpsserverdetailsdto.getPort()
					+ BusinessConstants.FORWARD_SLASH + context);
			LOGGER.debug("Https Url:" + httpsurls);
		}
		Object[] arr = httpsurls.toArray();
		return Arrays.copyOf(arr, arr.length, String[].class);
	}

	/**
	 * 
	 * @param httpsServerDetailsDTO
	 */
	private void verify(final HTTPSServerDetailsDTO httpsServerDetailsDTO) {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession arg1) {
				if (hostname.equals(httpsServerDetailsDTO.getIp())) {
					return true;
				}
				return false;
			}
		});
	}
	
	private void connect(HttpsURLConnection httpsURLConnection) throws IOException {
		httpsURLConnection.connect();
	}

	private int getHTTPSURL() {
		int idx = new Random().nextInt(urlstoconnect.length);
		return idx;
	}
}

