package com.ericsson.fdp.AirConfig.parser;

import java.io.File;

public interface ParseStrategy {

	/**
	 * Parse file
	 * */
	public Object parse(File file);

	/**
	 * Parse file with expression
	 * */
	public Object parse(File file, String Expression);

	public Object parse(File file, String Expression, Object... additionalparam);

	public Object parse(File file, Object... additionalparam);
}
