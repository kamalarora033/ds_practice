package com.ericsson.ms.common.util;

/**
 * The RequestHandlerLogger class
 * 
 * @author Ericsson
 *
 */
public class RequestHandlerLogger {

	/** The request Id field */
	private String requestId;
	/** The msisdn field */
	private String msisdn;

	/** The message field */
	private String message;
	
	/** The Action field */
	private String action;

	/**
	 * The RequestHandlerLoggerBuilder constructor
	 * @param requestHandlerLoggerBuilder
	 */
	public RequestHandlerLogger(RequestHandlerLoggerBuilder requestHandlerLoggerBuilder) {
		this.requestId = requestHandlerLoggerBuilder.requestId;
		this.message = requestHandlerLoggerBuilder.message;
		this.msisdn = requestHandlerLoggerBuilder.msisdn;
		this.action = requestHandlerLoggerBuilder.action;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "requestId:" + requestId + ", msisdn:" + msisdn + ", message:" + message + ", action:" + action;
	}
	

	/**
	 * The RequestHandlerLoggerBuilder class
	 * 
	 * @author Ericsson
	 *
	 */
	public static class RequestHandlerLoggerBuilder {

		/** The request Id field */
		private String requestId;

		/** The msisdn field */
		private String msisdn;

		/** The message field */
		private String message;
		
		/** The Action field */
		private String action;

		/**
		 * @param requestId
		 * 
		 */
		public RequestHandlerLoggerBuilder(String requestId) {
			super();
			this.requestId = requestId;
		}

		/**
		 * 
		 * @return the RequestHandlerLogger object
		 */
		public RequestHandlerLogger build() {
			return new RequestHandlerLogger(this);
		}

		/**
		 * 
		 * @param message
		 * @return
		 */
		public RequestHandlerLoggerBuilder setMessage(String message){
			this.message = message;
			return this;
		}
		
		/**
		 * 
		 * @param msisdn
		 * @return
		 */
		public RequestHandlerLoggerBuilder setMsisdn(String msisdn){
			this.msisdn = msisdn;
			return this;
		}
		
		/**
		 * 
		 * @param action
		 * @return
		 */
		public RequestHandlerLoggerBuilder setAction(String action){
			this.action = action;
			return this;
		}

	}
}
