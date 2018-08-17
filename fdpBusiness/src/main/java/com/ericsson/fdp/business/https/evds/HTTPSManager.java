package com.ericsson.fdp.business.https.evds;

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

/**
 * This class defines https connection methods 
 *
 * @author Ericsson
 *
 */
public class HTTPSManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPSManager.class);
	
	List<HttpsURLConnection> httpsurlconnectionlst;
	List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst;
	String[] urlstoconnect ;
	

	public HTTPSManager(List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst) {
		this.httpsserverdetailsdtolst=httpsserverdetailsdtolst;
		urlstoconnect=generateHTTPSURL(httpsserverdetailsdtolst);
	}
	
	/*
	 * this method is used for creating connection with https
	 */
	
public HttpsURLConnection getConnection() throws IOException
	{
		int urlid=getHTTPSURL();
		
		String urlfirst=null;
		String urlcandidate=null;
		HttpsURLConnection httpsURLConnection = null;
		Iterator<HTTPSServerDetailsDTO> it= httpsserverdetailsdtolst.iterator();
		urlfirst=urlstoconnect[urlid];
		HTTPSServerDetailsDTO httpsserverdetaildto=httpsserverdetailsdtolst.get(urlid);
		
		if(httpsserverdetaildto.isIsenabled()==false) {
			while(it.hasNext()){
				HTTPSServerDetailsDTO httpsserverdetaildtotmp  = (HTTPSServerDetailsDTO) it.next();
				if(httpsserverdetaildtotmp.isIsenabled()){
					urlcandidate=urlstoconnect[urlid];
					 break;
				}
				
			}
			if(urlfirst.equals(urlcandidate)) {
				throw new IOException("All Evds IP are close or Inactive");
			}
			
		} else {
			URL httpsurl = new URL(urlfirst);
			httpsURLConnection = (HttpsURLConnection) httpsurl
					.openConnection();
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
	
	private int getHTTPSURL() {
		
		int idx = new Random().nextInt(urlstoconnect.length);
	return idx;
	}

//this method need to be removed 
	public void startnewConnection(
			HTTPSServerDetailsDTO httpserverstartnewconnection)
	{
		List<HTTPSServerDetailsDTO> httpsserverdtolist=new ArrayList<>();
		httpsserverdtolist.add(httpserverstartnewconnection);
		String[] urlstoconnect = generateHTTPSURL(httpsserverdtolist);
		
		int i = 0;
		for (String url : urlstoconnect) {
			connect(url, httpsserverdtolist.get(i));
			
			i++;
		}
	}
	
	//this method need to be removed
	public boolean removeConnection(HTTPSServerDetailsDTO httpserverstartnewconnection)
	{
		HttpsURLConnection httpsurlconnection=searchHTTPConnection(httpserverstartnewconnection);
		
		if(httpsurlconnection!=null)
		{
			httpsurlconnection.disconnect();
			return true;
		}
			
	return false;
	};
	
	//need to be removed 
	private HttpsURLConnection searchHTTPConnection(
			HTTPSServerDetailsDTO httpserverstopconnection) {
	
		for(HttpsURLConnection httpsurlconnection:httpsurlconnectionlst)
		{
			
			if(httpsurlconnection.getURL().getHost().equals(httpserverstopconnection.getIp()) 
					&& httpsurlconnection.getURL().getPort()==httpserverstopconnection.getPort())
			{
				httpsurlconnection.disconnect();
				return httpsurlconnection;
			}
			
		}
		return null;
	}

	/**
	 * 
	 * @param url2
	 * @param httpsServerDetailsDTO
	 */
	private void connect(String url2,
			final HTTPSServerDetailsDTO httpsServerDetailsDTO) {

		//for (int i = 0; i <= httpsServerDetailsDTO.getRetry(); i++) {
			try {
				URL httpsurl = new URL(url2);
				HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
					
					@Override
					public boolean verify(String hostname, SSLSession arg1) {
			            if (hostname.equals(httpsServerDetailsDTO.getIp())) {
			                return true;
			            }
			            return false;
			        
					}
				});
				HttpsURLConnection httpsURLConnection = (HttpsURLConnection) httpsurl
						.openConnection();
				verify(httpsServerDetailsDTO);
				httpsURLConnection.setConnectTimeout(0);
				httpsURLConnection.setDoOutput(true);
				httpsURLConnection.setRequestMethod("POST");
				
				httpsURLConnection.setRequestProperty("User-Agent", httpsServerDetailsDTO.getUseragent());
				httpsURLConnection.setRequestProperty("Accept-Language", httpsServerDetailsDTO.getAcceptlanguage());
				httpsURLConnection.setConnectTimeout(httpsServerDetailsDTO.getTimeout());
				connect(httpsURLConnection);
				httpsurlconnectionlst.add(0,httpsURLConnection);
				
				LOGGER.debug("https url connection list"+httpsurlconnectionlst.size());
			} catch (IOException ioexception) {
				LOGGER.debug("HTTPS CONNECTION FAIL :-"
						+ httpsServerDetailsDTO.getIp());
				ioexception.printStackTrace();
			}
		

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


	private void connect(HttpsURLConnection httpsURLConnection)
			throws IOException {

		httpsURLConnection.connect();

	}

	/**
	 * this method is used for creating https URL
	 * @param httpsserverdetailsdtolst
	 *                  
	 * @return
	 */
	private String[] generateHTTPSURL(
			List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst) {
		
		List<String> httpsurls = new ArrayList<String>();
		for (HTTPSServerDetailsDTO httpsserverdetailsdto : httpsserverdetailsdtolst) {
			httpsurls.add("https://"+httpsserverdetailsdto.getIp() + ":"
					+ httpsserverdetailsdto.getPort()
					+BusinessConstants.FORWARD_SLASH
					+ httpsserverdetailsdto.getContext());
			LOGGER.debug("EVDS Url:" + httpsurls);
		}
		Object[] arr=httpsurls.toArray();
		return Arrays.copyOf(arr, arr.length, String[].class);
	}

	/**
	 * 
	 * @param input
	 * @return this method is used to read content from https server
	 * @throws IOException
	 */
	public String httpsHit(String input) throws IOException {
		DataOutputStream wr = null;
		StringBuffer stringbuffer=new StringBuffer();
		BufferedReader in=null;
		String inputLine;
		HttpsURLConnection httpsurlconnection;
		
		try
		{
			httpsurlconnection = getConnection();
		    wr= new DataOutputStream(httpsurlconnection.getOutputStream());
		    wr.writeBytes(input);
		    wr.flush();
			wr.close();
			
			int responseCode = httpsurlconnection.getResponseCode();
				
		    in = new BufferedReader(
		        new InputStreamReader(httpsurlconnection.getInputStream()));
	
			while ((inputLine = in.readLine()) != null) {
				stringbuffer.append(inputLine).append("\n");
			}
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(in!=null)
			{
				in.close();
			}
		}
		return stringbuffer.toString();
	}

}
