package com.ericsson.fdp.business.telnet.ema;

public class TelnetBean {
	
		private int port;
		private int timeout;
		private String ipaddress;
		private int sotimeout;
		private int numberofsessions;
		private String userName;
		private String password;
		private String login;
		private String logout;

		public TelnetBean() {}
		
		public TelnetBean( String ipaddress,int port, int timeout, int sotimeout, int numberofsessions, String userName, String password, String login, String logout) {
			this.port = port;
			this.timeout = timeout;
			this.ipaddress = ipaddress;
			this.sotimeout = sotimeout;
			this.numberofsessions = numberofsessions;
			this.userName = userName;
			this.password = password;
			this.login = login;
			this.logout = logout;
		}
		
		public String getIpaddress() {
			return ipaddress;
		}
		
		
		public void setIpaddress(String ipaddress) {
			this.ipaddress = ipaddress;
		}
		
		public int getTimeout() {
			return timeout;
		}


		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}

		public int getPort() {
			return port;
		}


		public void setPort(int port) {
			this.port = port;
		}


		public int getSotimeout() {
			return sotimeout;
		}


		public void setSotimeout(int sotimeout) {
			this.sotimeout = sotimeout;
		}


		public int getNumberofsessions() {
			return numberofsessions;
		}


		public void setNumberofsessions(int numberofsessions) {
			this.numberofsessions = numberofsessions;
		}


		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
		
		public String getLogin() {
			return login;
		}

		public void setLogin(String login) {
			this.login = login;
		}
		
		public String getLogout() {
			return logout;
		}

		public void setLogout(String logout) {
			this.logout = logout;
		}
	
}
