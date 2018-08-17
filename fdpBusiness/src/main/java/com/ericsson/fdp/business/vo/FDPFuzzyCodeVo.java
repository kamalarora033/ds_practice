package com.ericsson.fdp.business.vo;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.common.vo.FDPCircle;

public class FDPFuzzyCodeVo implements FDPCacheable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int fdp_fuzzy_code_id;
	int fdp_fuzzyCode;
	String fdp_fuzzyCodeCheck_Class;
	String command_display_name;
	String circle_name;
	FDPCircle circlecode;
	
	public int getFdp_fuzzy_code_id() {
		return fdp_fuzzy_code_id;
	}
	public void setFdp_fuzzy_code_id(int fdp_fuzzy_code_id) {
		this.fdp_fuzzy_code_id = fdp_fuzzy_code_id;
	}
	public int getFdp_fuzzyCode() {
		return fdp_fuzzyCode;
	}
	public void setFdp_fuzzyCode(int fdp_fuzzyCode) {
		this.fdp_fuzzyCode = fdp_fuzzyCode;
	}
	public String getFdp_fuzzyCodeCheck_Class() {
		return fdp_fuzzyCodeCheck_Class;
	}
	public void setFdp_fuzzyCodeCheck_Class(String fdp_fuzzyCodeCheck_Class) {
		this.fdp_fuzzyCodeCheck_Class = fdp_fuzzyCodeCheck_Class;
	}
	public String getCommand_display_name() {
		return command_display_name;
	}
	public void setCommand_display_name(String command_display_name) {
		this.command_display_name = command_display_name;
	}
	public String getCircle_name() {
		return circle_name;
	}
	public void setCircle_name(String circle_name) {
		this.circle_name = circle_name;
	}
	public FDPCircle getCirclecode() {
		return circlecode;
	}
	public void setCirclecode(FDPCircle circlecode) {
		this.circlecode = circlecode;
	}


}
