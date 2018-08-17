package com.ericsson.fdp.business.builder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ThreadConfigurationBean {

	private int min;
	private int max;
	private int ttl;
	private TimeUnit timeoutUnit;
	private LinkedBlockingQueue lbq;
	public int getMin() {
		return min;
	}
	public void setMin(int min) {
		this.min = min;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	public int getTtl() {
		return ttl;
	}
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	public TimeUnit getTimeoutUnit() {
		return timeoutUnit;
	}
	public void setTimeoutUnit(TimeUnit timeoutUnit) {
		this.timeoutUnit = timeoutUnit;
	}
	public LinkedBlockingQueue getLbq() {
		return lbq;
	}
	public void setLbq(LinkedBlockingQueue lbq) {
		this.lbq = lbq;
	}
	
	
	
}
