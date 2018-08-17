package com.ericsson.fdp.business.enums;

import com.ericsson.fdp.business.command.activation.FDPActivationCommand;
import com.ericsson.fdp.business.command.activation.impl.FDPAPNIDMMLActivationCommandImpl;
import com.ericsson.fdp.business.command.activation.impl.FDPCAIThreeGActivationCommandImpl;
import com.ericsson.fdp.business.command.activation.impl.FDPNAMMMLActivationCommandImpl;
import com.ericsson.fdp.business.command.activation.impl.FDPQueryMMLActivationCommandImpl;
import com.ericsson.fdp.business.command.activation.impl.FDPServiceActivationCommandImpl;
import com.ericsson.fdp.business.command.activation.impl.FDPServiceDeactivationCommandImpl;
import com.ericsson.fdp.business.command.activation.impl.FDPThreeGMMLActivationCommandImpl;
import com.ericsson.fdp.business.command.activation.impl.FDPVideoCallMMLActivationCommandImpl;

/**
 * This enum defines the modes for threeG activation.
 * 
 * @author Ericsson
 * 
 */
public enum ActivationCommands {

	/**
	 * Query for service activate CAI Blackberry
	 * */
	SERVICE_ACTIVATE_CAI("SERVICE_ACTIVATE_CAI",FDPServiceActivationCommandImpl.class),
	

	/**
	 * Query for service deactivate CAI Blackberry
	 * */
	SERVICE_DEACTIVATE_CAI("SERVICE_DEACTIVATE_CAI",FDPServiceDeactivationCommandImpl.class),
	
	/**
	 * The query command.
	 */
	QUERY_MML("QueryMML", FDPQueryMMLActivationCommandImpl.class),
	/**
	 * The threeG activation command.
	 */
	THREEG_ACTIVATIONMML("3G ActivationMML", FDPThreeGMMLActivationCommandImpl.class),
	/**
	 * The video call activation command.
	 */
	VIDEO_CALL_ACTIVATION("Video call ActivationMML", FDPVideoCallMMLActivationCommandImpl.class),
	/**
	 * The update CAI command.
	 */
	UPDATE_CAI_THREEG("UPDATE3G CAI", FDPCAIThreeGActivationCommandImpl.class),
	/**
	 * The attach nam command.
	 */
	ATTACH_NAM_MML("Attach NAM MML", FDPNAMMMLActivationCommandImpl.class),
	/**
	 * The attach apnid mml command.
	 */
	ATTACH_APNID_MML("Attach APNID MML", FDPAPNIDMMLActivationCommandImpl.class),
	/**
	 * The update CAI twoG command.
	 */
	UPDATE_CAI_TWOG("UPDATE2G CAI", FDPCAIThreeGActivationCommandImpl.class),
	/**
	 * The query and update command CAI.
	 */
	QUERY_N_UPDATE_THREEG("QUERYUPDATE3G CAI", FDPCAIThreeGActivationCommandImpl.class),
	/**
	 * The query and update twoG for CAI.
	 */
	QUERY_N_UPDATE_TWOG("QUERYUPDATE2G CAI", FDPCAIThreeGActivationCommandImpl.class);

	/**
	 * The command name.
	 */
	private String commandName;
	/**
	 * The interceptor class for the command
	 */
	private Class<? extends FDPActivationCommand> clazz;

	/**
	 * The constructor.
	 * 
	 * @param commandName
	 *            the command name.
	 * @param clazz
	 *            the class name.
	 */
	private ActivationCommands(final String commandName, final Class<? extends FDPActivationCommand> clazz) {
		this.commandName = commandName;
		this.setClazz(clazz);
	}

	/**
	 * @return the commandName
	 */
	public String getCommandName() {
		return commandName;
	}

	/**
	 * @param commandName
	 *            the commandName to set
	 */
	public void setCommandName(final String commandName) {
		this.commandName = commandName;
	}

	/**
	 * 
	 * @return the class
	 */
	public Class<? extends FDPActivationCommand> getClazz() {
		return clazz;
	}

	/**
	 * 
	 * @param clazz
	 *            the class to set.
	 */
	public void setClazz(final Class<? extends FDPActivationCommand> clazz) {
		this.clazz = clazz;
	}

	/**
	 * This method is used to get the activation command value from command
	 * name.
	 * 
	 * @param commandName
	 *            the command name for which value is required.
	 * @return the activation command corresponding to the command.
	 */
	public static ActivationCommands getValueFromCommandName(final String commandName) {
		for (ActivationCommands activationCommands : ActivationCommands.values()) {
			if (activationCommands.getCommandName().equalsIgnoreCase(commandName)) {
				return activationCommands;
			}
		}
		return null;
	}

}
