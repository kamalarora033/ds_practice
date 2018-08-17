package com.ericsson.fdp.business.telnet.ema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.config.utils.PropertyUtils;

/**
 * This class will maintain a pool of Telnet connection objects and send the EAM command
 * to EMA server using the telnet object
 * 
 */
public class TelnetClientManager {

	private volatile Map<String, TelnetBean> _ipBasedtelnetmap = new HashMap<String, TelnetBean>();;
	private volatile Map<String, ArrayBlockingQueue<FDPTelnetClient>> _ipBasedtelnetpool;
	private volatile ArrayBlockingQueue<FDPTelnetClient> arrayblockingqueue;
	private AtomicInteger telnetThreadCount = null;
	private final ReentrantLock lock = new ReentrantLock();
	private static final TelnetClientManager telnetClientManager = new TelnetClientManager();
	private static final String EMA_COMMAND_RESPONSE = "RESP:0;";

	private static final Logger LOGGER = LoggerFactory.getLogger(TelnetClientManager.class);

	private static final String IS_EMA_ASYNC = "Y";
	private static final String DAILY = "DAILY";
	private static final String DEFAULT_FILE_PATH = "/tmp/";
	private static final String EMA_COMMAND_LOG = "ema_command_log";
	private static final String EMA_FILE_FORMAT = "EMA_FILE_FORMAT";
	private static final String EMA_FILE_NAME = "EMA_FILE_NAME";
	private static final String SUCCESS_RESPONSE = "RESP:0";

	
	private TelnetClientManager() {
	}

	public static TelnetClientManager getInstance() {
		return telnetClientManager;
	}

	/**
	 * Method to populate the TelnetSessions
	 * 
	 * @param telnetbean
	 * @throws InterruptedException
	 * @throws SocketException
	 * @throws IOException
	 */

	public void populateTelnetClient(TelnetBean telnetbean) {
		_ipBasedtelnetpool = new HashMap<String, ArrayBlockingQueue<FDPTelnetClient>>();
		_ipBasedtelnetmap.put(telnetbean.getIpaddress(), telnetbean);
		telnetThreadCount = new AtomicInteger(0);
		arrayblockingqueue = new ArrayBlockingQueue<>(telnetbean.getNumberofsessions());
		for (int i = 0; i < telnetbean.getNumberofsessions(); i++) {
			populateConnectionQueue(telnetbean);
		}
		_ipBasedtelnetpool.put(telnetbean.getIpaddress(), arrayblockingqueue);
	}
	
	/**
	 * This method will create a telnet session and put telnet session in Blocking Queue
	 * @param telnetbean
	 */
	private synchronized void populateConnectionQueue(TelnetBean telnetbean) {
		
		FDPTelnetClient fdptelnetclient = new FDPTelnetClient(false);
		fdptelnetclient.initializeSocket(telnetbean);
		
		if (fdptelnetclient.isConnected() && fdptelnetclient.isAvailable()) {
			try {
				if (connectEma(fdptelnetclient, telnetbean) && arrayblockingqueue.offer(fdptelnetclient))
					telnetThreadCount.getAndIncrement();
				else
					fdptelnetclient.disconnect();
			} catch (IOException e) {
				LOGGER.error("IOException occurs while disconnecting the telnet session : " + e);
			}
		}
	}

	/**
	 * Method to remove telnet session
	 * 
	 * @param ipaddress
	 * 
	 */

	public void removeTelnetConnection(String ipaddress) {
		
		if (_ipBasedtelnetpool != null) {
			BlockingQueue<FDPTelnetClient> telnetPool = _ipBasedtelnetpool.remove(ipaddress);
			if (telnetPool != null) {
				while (!telnetPool.isEmpty()) {
					try {
						FDPTelnetClient fdpTelnetClient = telnetPool.take();
						fdpTelnetClient.disconnect();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Method to connect with EMA and do login
	 * 
	 * @param fdptelnetclient
	 * @param telnetbean
	 * @return
	 * @throws SocketException
	 * @throws SocketTimeoutException
	 * @throws IOException 
	 */
	private synchronized Boolean connectEma(FDPTelnetClient fdptelnetclient, TelnetBean telnetbean) {
		LOGGER.debug("Inside TelnetClientManager::connectEma method");
        String respone;
        boolean success = false;
        final String loginCommand = telnetbean.getLogin() + ":" + telnetbean.getUserName() + ":" + telnetbean.getPassword() + ";" + BusinessConstants.EMA_COMMAND_END_DELIMITER;
        LOGGER.debug("Login Command : " + loginCommand);
        
        try {
        	if (fdptelnetclient.isConnected() && fdptelnetclient.isAvailable()) {
        		respone = emaResponse(fdptelnetclient, telnetbean, loginCommand);
                
                if (respone != null && respone.contains(SUCCESS_RESPONSE)) {
                	fdptelnetclient.setLogedIn(true);
                	success = true;
                } else {
                	fdptelnetclient.disconnect();
                }
        	}
        	
        } catch (Exception e) {
        	LOGGER.info("Exception occurs while Login : " + e);
        }
		
        return success;
	}
	
	/**
	 * This method will do logout from EAM server
	 * @param fdptelnetclient
	 * @param telnetbean
	 *//*
	private void logoutEma(FDPTelnetClient fdptelnetclient, TelnetBean telnetbean) {
		final String logoutCommand = telnetbean.getLogout() + BusinessConstants.EMA_COMMAND_END_DELIMITER;
		
		OutputStream outputstream;
		if (fdptelnetclient.isAvailable() && fdptelnetclient.isConnected() && fdptelnetclient.isLogedIn()) {
			outputstream = fdptelnetclient.getOutputStream();
			try {
				outputstream.write(logoutCommand.getBytes());
		        outputstream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Exception occurs while executing EMA LOGOUT command");
				e.printStackTrace();
			}
		}
		
        LOGGER.debug("LOGOUT done from EMA successfully");
	}*/
	
	/**
	 * This method will send the command to EMA server
	 * @param fdptelnetclient
	 * @param telnetbean
	 * @param loginCommand
	 * @return
	 * @throws SocketException
	 * @throws IOException
	 */
	private String emaResponse(FDPTelnetClient fdptelnetclient, TelnetBean telnetbean, String loginCommand) throws SocketException, IOException{
		OutputStream outputstream;
        InputStream inputstream;
        String response;
		outputstream = fdptelnetclient.getOutputStream();
        outputstream.write(loginCommand.getBytes());
        outputstream.flush();
        inputstream = fdptelnetclient.getInputStream();
        
        response = getStringFromInputStream(inputstream);
        
        LOGGER.debug("Response received for command " + loginCommand + " is -> " + response);
        return response;
	}

	/**
	 * sends the message only second command is send
	 * 
	 * @param loginCommand
	 * @param commad
	 * @param ipaddress
	 * @param circleCode
	 * @return
	 * @throws Exception
	 */
    public String send(final String command, String ipaddress, String circleCode) throws Exception {
        LOGGER.info("executing EMA Command on :{}", ipaddress);

        String response = new String();
        FDPTelnetClient fdptelnetclient = null;
        TelnetBean telnetBean = null;
        boolean failure = false;
        
        try {
        	
        	telnetBean = _ipBasedtelnetmap.get(ipaddress); 
        	if(telnetBean==null )
        	{
        		failure = true;
                return null;
        	}
        	
        	//Create a new telnet connection if telent session is less than configured telnet session
        	if(_ipBasedtelnetpool.get(ipaddress).isEmpty() && getValue() < telnetBean.getNumberofsessions())
        		populateConnectionQueue(telnetBean);    	
           
            
            if (getValue() == 0) {
            	failure = true;
                return null;
            }
            
            fdptelnetclient = _ipBasedtelnetpool.get(ipaddress).poll(telnetBean.getSotimeout(), TimeUnit.MILLISECONDS);
            if (fdptelnetclient == null) {
            	LOGGER.info("EMA Connection is not available for processing");
            	failure = true;
                return null;
            }
            
            if (!fdptelnetclient.isConnected() || !fdptelnetclient.isAvailable()) {
            	fdptelnetclient.initializeSocket(telnetBean);
            	fdptelnetclient.setLogedIn(false);
            	if (fdptelnetclient.isConnected() && fdptelnetclient.isAvailable()) {
            		if (!connectEma(fdptelnetclient, telnetBean)) {
            			failure = true;
                        return null;
                	}
            	} else {
            		failure = true;
                    return null;
            	}
            	
            }
            
            //If telnet connection is login then send the request to EMA server
            response = emaResponse(fdptelnetclient, telnetBean, command); 
        	if (response == null) {
        		if (!reConnectEMA(fdptelnetclient, telnetBean)) {
        			failure = true;
                    return null;
        		}
        	}
          
        } catch (SocketException e) {
            LOGGER.error("SocketException occurs during command execution: " + e);
            // Special handling has been done to reconnect if connection has been reset by server
            if(fdptelnetclient!=null)
            	fdptelnetclient.initializeSocket(telnetBean);
            if (fdptelnetclient!=null && fdptelnetclient.isConnected() && fdptelnetclient.isAvailable()) {
            	if (!connectEma(fdptelnetclient, telnetBean)) {
            		failure = true;
            		return getAsyncResponse(command);
            	} else {
            		fdptelnetclient.setSoTimeout(telnetBean.getSotimeout());
            		OutputStream outputstream;
                    InputStream inputstream;
            		outputstream = fdptelnetclient.getOutputStream();
                    outputstream.write(command.getBytes());
                    outputstream.flush();
                    inputstream = fdptelnetclient.getInputStream();
                    response = getStringFromInputStream(inputstream);
                    if (response == null) {
                    	LOGGER.debug("Did Not received EMA response for command " +  command + "within response timeout");
                    	if (!reConnectEMA(fdptelnetclient, telnetBean)) {
                    		failure = true;
                            return null;
                    	}
                    } else {
                    	LOGGER.debug("Response received for command " + command + " is -> " + response);
                    }
            	}
                return response;
            } else {
            	failure = true;
                return getAsyncResponse(command);
            }
        	
            
        } catch (Exception e) {
            LOGGER.error("Exception occurs during command execution: " + e);
            failure = true;
            // EMA ASYNC feature
            return getAsyncResponse(command);

        } finally {
            if (!failure) {
            	if (_ipBasedtelnetpool.get(ipaddress) == null) {
            		/*if (fdptelnetclient.isLogedIn())
            			logoutEma(fdptelnetclient, telnetBean);*/
            		fdptelnetclient.disconnect();
            		//decrementSocketCount();
            	} else if (fdptelnetclient.isConnected() && fdptelnetclient.isAvailable()){
            		if (!_ipBasedtelnetpool.get(ipaddress).offer(fdptelnetclient))
            			fdptelnetclient.disconnect();
            	} else {
            		fdptelnetclient.disconnect();
            		decrementSocketCount();
            	}
            	
            } else {
            	if (fdptelnetclient != null) {
            		fdptelnetclient.disconnect();
            		decrementSocketCount();
            	}
            }
        }
        return response;
    }

	private static String getStringFromInputStream(final InputStream is)  throws SocketException{
		LOGGER.debug("Inside TelnetClientManager::getStringFromInputStream method");
		BufferedReader br;
		String line;
	    String response = null;
	    br = new BufferedReader(new InputStreamReader(is));
	    try {
	    	 while ((line = br.readLine()) != null) {
	 	    	LOGGER.debug("Line:"+line);
	 			if (line.endsWith(FDPConstant.LOGGER_KEY_VALUE_VALUE_DELIMITER)) {
	 				response = line;
	 				break;
	 			}
	 		}
	 		if (response != null)
	 			response = response.replaceAll("Enter command:", "").trim();
	 		if (response == null || response.isEmpty()) 
	 			throw new SocketException();
	    } catch (IOException e) {
	    	if (e instanceof SocketTimeoutException) {
	    		LOGGER.debug("Socket Readtimeout Exception occurs:" + e);
	    		response = null;
	    	} else if(e instanceof SocketException) {
	    		LOGGER.debug("Socket Exception occurs:" + e);
	    		throw new SocketException();
	    	}
	    }
	   
	    return response;    
	}

	private void writeCSVFileFromResponseXML(final String emaCommand) {

		try (FileWriter fileWriter = new FileWriter(getFilePathWithName(), true)) {

			fileWriter.append(emaCommand);

			LOGGER.debug(
					"EMA Command Syncup: CSV file was created successfully at Path: ---->>" + getFilePathWithName());
		} catch (Exception e) {
			LOGGER.error("EMA Command Syncup: Error in CsvFileWriter !!!" + e);
		}
	}

	private String getFilePath() {
		final String filePath = PropertyUtils.getProperty("EMA_FILE_PATH").trim();

		if (null == filePath)
			return DEFAULT_FILE_PATH;

		return filePath;
	}

	private String getFileName() {
		final String filename = PropertyUtils.getProperty(EMA_FILE_NAME).trim();

		if (null == filename)
			return EMA_COMMAND_LOG;

		return filename;
	}

	private SimpleDateFormat getFileFormat() {
		final String fileFormat = PropertyUtils.getProperty(EMA_FILE_FORMAT).trim();

		if (DAILY.equals(fileFormat))
			return new SimpleDateFormat("ddMMyyyy");

		return new SimpleDateFormat("ddMMyyyy_HH");
	}

	private String getFilePathWithName() {
		Date date = new Date();

		return getFilePath() + File.separator + getFileName() + FDPConstant.UNDERSCORE + getFileFormat().format(date)
				+ FDPConstant.CSV_FILE_FORMATE_SUFFIX;
	}

	private String getAsyncResponse(final String command) {
		final String nonExecutedCommand = command;

		if (IS_EMA_ASYNC.equalsIgnoreCase(PropertyUtils.getProperty(FDPConstant.IS_EMA_ASYNC))
				&& nonExecutedCommand.startsWith(PropertyUtils.getProperty(FDPConstant.IS_EMA_ASYNC_FOR_UPDATE))) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(new Runnable() {

				@Override
				public void run() {
					writeCSVFileFromResponseXML(nonExecutedCommand);

				}
			});

			return EMA_COMMAND_RESPONSE;
		}
		return null;
	}
	
	/**
	 * This method will reconnect EMA server
	 * @param fdptelnetclient
	 * @param telnetBean
	 */
	private Boolean reConnectEMA(FDPTelnetClient fdptelnetclient, TelnetBean telnetBean) {
		fdptelnetclient.initializeSocket(telnetBean);
		return connectEma(fdptelnetclient, telnetBean);
	}
	
	/**
	 * This method will decrement the value of telnetThreadCount if it is greater than 0
	 */
	private void decrementSocketCount() {
		if (getValue() > 0)
			telnetThreadCount.decrementAndGet();
	}
	
	/**
	 * This method will return the 
	 * @return
	 */
	private int getValue() {
		int threadCount = 0;
		lock.lock();
        try {
        	threadCount = telnetThreadCount.get();
        } finally {
            lock.unlock();
        }
        return threadCount;
	}
	
	
}
