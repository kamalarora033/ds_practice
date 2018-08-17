package com.ericsson.fdp.AirConfig.parser;

import java.io.File;

/**
 * Context to execute any parsing strategy
 * */
public class ParseContext {

	private ParseStrategy _parseStrategy;
	
	public ParseContext(ParseStrategy parsestrategy)
	{
		_parseStrategy=parsestrategy;
	}
	
	public Object executeStrategy(File file)
	{
		return _parseStrategy.parse(file);
	}
	
	public Object executeStrategy(File file,String expression)
	{
		return _parseStrategy.parse(file, expression);
	
	}
	
	public Object executeStrategy(File file,Object... additionalinformation)
	{
		return _parseStrategy.parse(file, additionalinformation);
	
	}
	
	
	
}
