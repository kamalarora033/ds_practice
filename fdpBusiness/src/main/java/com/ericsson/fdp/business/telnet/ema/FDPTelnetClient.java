package com.ericsson.fdp.business.telnet.ema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;

import javax.net.SocketFactory;

import org.apache.commons.net.SocketClient;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.config.utils.PropertyUtils;

/**
 * This class will used to create Telnet session.
 * @author GUR21122
 *
 */
public class FDPTelnetClient extends SocketClient{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FDPTelnetClient.class);

	private boolean isLogedIn;

	public FDPTelnetClient() { }
	
	public FDPTelnetClient(boolean isLogedIn) {
		this.isLogedIn = isLogedIn;
	}
	
	public boolean isLogedIn() {
		return isLogedIn;
	}

	public void setLogedIn(boolean isLogedIn) {
		this.isLogedIn = isLogedIn;
	}
	
	/**
	 * Get the default Socket Factory
	 * @return
	 */
	protected SocketFactory obtainSocketFactory() { 
        return SocketFactory.getDefault(); 
    }
	
	/**
	 * Initialize the Socket property
	 * @param socketDiameterBean
	 */
	public void initializeSocket(TelnetBean telnetBean) {
		try {
			this.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setDefaultTimeout(telnetBean.getTimeout());
		this.setConnectTimeout(telnetBean.getTimeout());
		this.setSocketFactory(obtainSocketFactory());
		this.connectSocket(telnetBean);
	}
	
	/**
     * Connect the socket to destination IP and port
     * @param socketDiameterBean
     */
    public void connectSocket(TelnetBean telnetBean) {
    	try {
    		if (telnetBean != null) {
    			this.connect(telnetBean.getIpaddress(), telnetBean.getPort());
    			this.setKeepAlive(true);
    			this.setSoTimeout(telnetBean.getSotimeout());
    			this.readConnectResponse();
    		}
		} catch (SocketException e) {
			LOGGER.error("Exception occurs during creating socket connection: "+ e);
		} catch (IOException e) {
			LOGGER.error("Exception occurs during creating I/O operation: "+ e);
		}
    }
    
    /***
     * Returns the socket connection output stream.  You should not close the
     * stream when you finish with it.  Rather, you should call
     * {@link #disconnect  disconnect }.
     * <p>
     * @return The connection output stream.
     ***/
    public OutputStream getOutputStream()
    {
        return _output_;
    }

    /***
     * Returns the socket connection input stream.  You should not close the
     * stream when you finish with it.  Rather, you should call
     * {@link #disconnect  disconnect }.
     * <p>
     * @return The connection input stream.
     ***/
    public InputStream getInputStream()
    {
        return _input_;
    }
    
    /**
     * This method will read the telent connect response
     * @throws IOException
     */
    private void readConnectResponse() throws IOException{
    	LOGGER.debug("Inside FDPTelnetClient::readConnectResponse method");
    	InputStream inputstream;
    	inputstream = this.getInputStream();
    	BufferedReader br;
		String line;
		String connectResponse = PropertyUtils.getProperty("EMA_CONNECT_RESPONSE");
		
		if (connectResponse == null)
			connectResponse = "CONNECTED";
		else
			connectResponse = connectResponse.trim();
		
	    br = new BufferedReader(new InputStreamReader(inputstream));
	    while ((line = br.readLine()) != null) {
	    	LOGGER.debug("Line:"+line);
	    	
			if (line.contains(connectResponse)) {
				break;
			}
		}
    }
	
}
