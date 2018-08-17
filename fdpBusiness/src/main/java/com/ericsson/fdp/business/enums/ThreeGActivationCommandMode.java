package com.ericsson.fdp.business.enums;

import java.util.Arrays;
import java.util.List;

/**
 * This enum defines the modes for threeG activation.
 * 
 * @author Ericsson
 * 
 */
public enum ThreeGActivationCommandMode {

	/**
	 * The query command mode.
	 */
	QUERY("QUERY", new String[] { ActivationCommands.QUERY_MML.getCommandName() }, new String[]{},
			new String[] { ActivationCommands.QUERY_MML.getCommandName() }, new String[]{}),

	/**
	 * The update all mode.
	 */
	UPDATE_ALL("UPDATE_ALL", new String[] { ActivationCommands.THREEG_ACTIVATIONMML.getCommandName(),
			 },
			new String[] { ActivationCommands.UPDATE_CAI_THREEG.getCommandName() }, new String[] {
					ActivationCommands.ATTACH_NAM_MML.getCommandName(),
					ActivationCommands.ATTACH_APNID_MML.getCommandName() },
			new String[] { ActivationCommands.UPDATE_CAI_TWOG.getCommandName() }),

	QUERY_AND_UPDATE_ALL("QUERY_AND_UPDATE", new String[] { ActivationCommands.QUERY_MML.getCommandName(),
			ActivationCommands.THREEG_ACTIVATIONMML.getCommandName(),
			},
			new String[] { ActivationCommands.QUERY_N_UPDATE_THREEG.getCommandName() }, new String[] {
					ActivationCommands.QUERY_MML.getCommandName(), ActivationCommands.ATTACH_NAM_MML.getCommandName(),
					ActivationCommands.ATTACH_APNID_MML.getCommandName() },
			new String[] { ActivationCommands.QUERY_N_UPDATE_TWOG.getCommandName() }),
		
	
			
	BLACKBERRY_SERVICE_ACTIVATE("BLACKBERRY_SERVICE_ACTIVATION",new String[]{ActivationCommands.SERVICE_ACTIVATE_CAI.getCommandName()},new String[]{""},new String[]{""},new String[]{""})		
			;
	

	
	/**
	 * The command mode.
	 */
	private String commandMode;

	/**
	 * The commands for MML.
	 */
	private List<String> commandsForMMLThreeG;

	/**
	 * The commands for CAI.
	 */
	private List<String> commandsForCAIThreeG;

	/**
	 * The commands for MML.
	 */
	private List<String> commandsForMMLTwoG;

	/**
	 * The commands for CAI.
	 */
	private List<String> commandsForCAITwoG;

	/**
	 * The command modes for threeG activation.
	 * 
	 * @param commandMode
	 *            the command mode.
	 * @param commandsForMML
	 *            the commands for MML.
	 * @param commandsForCAI
	 *            the commands for CAI.
	 */
	private ThreeGActivationCommandMode(final String commandMode, final String[] commandsForMMLThreeG,
			final String[] commandsForCAITheeG, final String[] commandsForMMLTwoG, final String[] commandsForCAITwoG) {
		this.commandMode = commandMode;
		this.commandsForMMLThreeG = Arrays.asList(commandsForMMLThreeG);
		this.commandsForCAIThreeG = Arrays.asList(commandsForCAITheeG);
		this.commandsForMMLTwoG = Arrays.asList(commandsForMMLTwoG);
		this.commandsForCAITwoG = Arrays.asList(commandsForCAITwoG);
	}

	/**
	 * @return the command mode.
	 */
	public String getCommandMode() {
		return commandMode;
	}

	/**
	 * Gets the enum value.
	 * 
	 * @param commandMode
	 *            the command mode
	 * @return the enum value
	 */
	public static ThreeGActivationCommandMode getEnumValue(final String commandMode) {
		ThreeGActivationCommandMode result = null;
		for (final ThreeGActivationCommandMode enumVal : values()) {
			if (enumVal.getCommandMode().equalsIgnoreCase(commandMode)) {
				result = enumVal;
				break;
			}
		}
		return result;
	}

	/**
	 * @return the commandsForMMLThreeG
	 */
	public List<String> getCommandsForMMLThreeG() {
		return commandsForMMLThreeG;
	}

	/**
	 * @param commandsForMMLThreeG
	 *            the commandsForMMLThreeG to set
	 */
	public void setCommandsForMMLThreeG(List<String> commandsForMMLThreeG) {
		this.commandsForMMLThreeG = commandsForMMLThreeG;
	}

	/**
	 * @return the commandsForCAIThreeG
	 */
	public List<String> getCommandsForCAIThreeG() {
		return commandsForCAIThreeG;
	}

	/**
	 * @param commandsForCAIThreeG
	 *            the commandsForCAIThreeG to set
	 */
	public void setCommandsForCAIThreeG(List<String> commandsForCAIThreeG) {
		this.commandsForCAIThreeG = commandsForCAIThreeG;
	}

	/**
	 * @return the commandsForMMLTwoG
	 */
	public List<String> getCommandsForMMLTwoG() {
		return commandsForMMLTwoG;
	}

	/**
	 * @param commandsForMMLTwoG
	 *            the commandsForMMLTwoG to set
	 */
	public void setCommandsForMMLTwoG(List<String> commandsForMMLTwoG) {
		this.commandsForMMLTwoG = commandsForMMLTwoG;
	}

	/**
	 * @return the commandsForCAITwoG
	 */
	public List<String> getCommandsForCAITwoG() {
		return commandsForCAITwoG;
	}

	/**
	 * @param commandsForCAITwoG
	 *            the commandsForCAITwoG to set
	 */
	public void setCommandsForCAITwoG(List<String> commandsForCAITwoG) {
		this.commandsForCAITwoG = commandsForCAITwoG;
	}

	/**
	 * @param commandMode
	 *            the commandMode to set
	 */
	public void setCommandMode(String commandMode) {
		this.commandMode = commandMode;
	}

}
