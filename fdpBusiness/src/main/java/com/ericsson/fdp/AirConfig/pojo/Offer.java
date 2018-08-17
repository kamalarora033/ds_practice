package com.ericsson.fdp.AirConfig.pojo;

import com.ericsson.fdp.FDPCacheable;

/**
 * Pojo class for offer
 * */
public class Offer implements FDPCacheable{

	private Refill _refill;
	private String _offerID;
	private String _offerName;
	private String _accountDivisionModifierID;
	private String _daID;

	public String get_daID() {
		return _daID;
	}

	public void set_daID(String _daID) {
		this._daID = _daID;
	}

	public Refill get_refill() {
		return _refill;
	}

	public void set_refill(Refill _refill) {
		this._refill = _refill;
	}

	public String get_offerID() {
		return _offerID;
	}

	public void set_offerID(String _offerID) {
		this._offerID = _offerID;
	}

	public String get_offerName() {
		return _offerName;
	}

	public void set_offerName(String _offerName) {
		this._offerName = _offerName;
	}

	public String get_accountDivisionModifierID() {
		return _accountDivisionModifierID;
	}

	public void set_accountDivisionModifierID(String _accountDivisionModifierID) {
		this._accountDivisionModifierID = _accountDivisionModifierID;
	}

}
