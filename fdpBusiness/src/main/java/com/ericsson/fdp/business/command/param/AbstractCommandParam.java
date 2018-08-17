package com.ericsson.fdp.business.command.param;

import java.util.List;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This class defines the command parameters.
 * 
 * @author Ericsson
 * 
 */
public abstract class AbstractCommandParam implements CommandParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7596750315581409467L;
	/** The parameter name. */
	private String name;
	/** The parameter type. */
	private CommandParameterType type;
	/** The parameter type in case of primitives. */
	private Primitives primitiveValue;
	/** The parameter value. */
	private Object value;
	/** The children of the parameter if any. */
	private List<CommandParam> childeren;
	/** The command to which this parameter is linked. */
	private FDPCommand command;
	/** The parent of the parameter if any. */
	private CommandParam parent;
	/** The value of the flattened parameter. */
	private String fullyQualifiedPathOfParameter;

	@Override
	public String flattenParam() {
		// If the parameter has been flattened earlier, no need to flatten it
		// again.
		if (fullyQualifiedPathOfParameter == null) {
			StringBuilder flattenedString = new StringBuilder();
			if (parent != null) {
				flattenedString.append(parent.flattenParam());
				flattenedString.append(FDPConstant.PARAMETER_SEPARATOR).append(name);
			} else {
				flattenedString.append(name);
			}
			fullyQualifiedPathOfParameter = flattenedString.toString();
		}
		return fullyQualifiedPathOfParameter;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param nameToSet
	 *            the name to set
	 */
	public void setName(final String nameToSet) {
		this.name = nameToSet;
	}

	@Override
	public CommandParameterType getType() {
		return type;
	}

	/**
	 * @param typeToSet
	 *            the type to set
	 */
	public void setType(final CommandParameterType typeToSet) {
		this.type = typeToSet;
	}

	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * @param valueToSet
	 *            the value to set
	 */
	public void setValue(final Object valueToSet) {
		this.value = valueToSet;
	}

	/**
	 * @return the childeren
	 */
	public List<CommandParam> getChilderen() {
		return childeren;
	}

	/**
	 * @param childerenToSet
	 *            the childeren to set
	 */
	public void setChilderen(final List<CommandParam> childerenToSet) {
		this.childeren = childerenToSet;
	}

	@Override
	public FDPCommand getCommand() {
		return command;
	}

	/**
	 * @param commandToSet
	 *            the command to set
	 */
	public void setCommand(final FDPCommand commandToSet) {
		this.command = commandToSet;
	}

	/**
	 * @return the parent
	 */
	public CommandParam getParent() {
		return parent;
	}

	/**
	 * @param parentToSet
	 *            the parent to set
	 */
	public void setParent(final CommandParam parentToSet) {
		this.parent = parentToSet;
	}

	/**
	 * @param primitiveValueToSet
	 *            the primitive value to set.
	 */
	public void setPrimitiveValue(final Primitives primitiveValueToSet) {
		this.primitiveValue = primitiveValueToSet;
	}

	@Override
	public Primitives getPrimitiveValue() {
		return primitiveValue;
	}

	/**
	 * @param fullyQualifiedPathOfParameterToSet
	 *            fully qualified path of parameter.
	 */
	protected void setFullyQualifiedPathOfParameter(final String fullyQualifiedPathOfParameterToSet) {
		this.fullyQualifiedPathOfParameter = fullyQualifiedPathOfParameterToSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((flattenParam() == null) ? 0 : flattenParam().hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractCommandParam other = (AbstractCommandParam) obj;
		if (flattenParam() == null) {
			if (other.flattenParam() != null) {
				return false;
			}
		} else if (!flattenParam().equals(other.flattenParam())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return " command parameter fully qualified path :- " + flattenParam() + " value :- " + (value == null ? "null"
				: value.toString());
	}

}
