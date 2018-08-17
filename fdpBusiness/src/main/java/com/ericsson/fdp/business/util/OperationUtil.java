package com.ericsson.fdp.business.util;

public class OperationUtil {

	public static double convertToPrecision(double input, int precision) {
		double prec = Math.pow(10, precision);
		return ((int) (input * (prec)) / prec);
	}

}
