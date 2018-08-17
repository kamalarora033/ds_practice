package com.ericsson.fdp.business.adapter.impl;

import java.util.Map;

import com.ericsson.fdp.business.adapter.Adapter;

// TODO: Auto-generated Javadoc
/**
 * This class implements the adapter for http interface.
 *
 * @param <T> The parameter type which the adapter uses.
 * @author Ericsson
 */
public abstract class AbstractTelnetAdapter<T> implements Adapter {
	
	/** The user name. */
	private String userName;
	
	/** The password. */
	private String password;
	
	/** The command. */
	private T command;
	
	/** The response map. */
	private Map<String, Object> responseMap;
	

	/**
	 * Instantiates a new abstract telnet adapter.
	 *
	 * @param userName the user name
	 * @param password the password
	 * @param command the command
	 */
	public AbstractTelnetAdapter(String userName, String password, T command) {
		super();
		this.userName = userName;
		this.password = password;
		this.command = command;
	}

	
}
