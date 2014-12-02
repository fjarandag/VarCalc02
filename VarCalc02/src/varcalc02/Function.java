package varcalc02;

import java.util.HashMap;


/**
 * Base definition for a function handled in variable calculator.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
public abstract class Function {
	
	/**
	 *  function type where the result is always ascending or descending with variables.
	 *  Can calculate the value of any variable from the others (which will make the function result zero)
	 *    using conventional aproximation methods (Such as Newton-Raphson (NR) ).
	 *  Can state a variable calculable attribute to false if not feasible or unwanted.
	 */
	public static final String MONOTONIC = "monotonic";
	/**
	 * Function type where it is fixed which variables are input (not calculable) or output (calculable).
	 * Calculator behavior is altered, so will disable target var selection, and SolutionEngine wont be needed.
	 */
	public static final String ONE_WAY = "oneway";

	// TT-FUTURE will need more knowledge about other cases. So far will treat as if it was monotonic
	// Might specify an specific SolutionEngine (specific Function class might implement that interface).
	/**
	 * Function type for functions where result is not monotonic.
	 * ** Simple aproximation methods might fail, and will yield just one result if there was many. **
	 */
	public static final String OTHER = "other";
	
	// TT-REDESIGN might use caption as name. However, name should be a valid file name.
	// However caption would be i18n-able. But that is a bit far forward.
	/** Internal name (used as key). Should be an adequate file name. */
	private String name;
	// caption
	/** Text shown at the calculator panel. Might be more descriptive than name. */
	private String caption;
	/** Description for function shown at the calculator panel. */
	private String description;
	/** Longer and richer (html) description for the function. Optional */
	private String longDescription;
	/** Type of function (monotonic, oneway, etc?). Handling will depend on type. */
	private String behavior;
	/** Which variable will be selected as target initially. */
	private String defaultTargetVariable;

	/** Variable types (for information, or unit conversion combos) */
	private VariableType[] variableTypes;

	/** Variables used in function. */
	private FunctionVariable[] variables;

	public Function() {
		super();
	}
	// TT-LOW Define some factory, etc for filling in data

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public String getBehavior() {
		return behavior;
	}

	public void setBehavior(String type) {
		this.behavior = type;
	}

	public String getDefaultTargetVariable() {
		return defaultTargetVariable;
	}

	public void setDefaultTargetVariable(String defaultTarget) {
		this.defaultTargetVariable = defaultTarget;
	}

	public VariableType[] getVariableTypes() {
		return variableTypes;
	}

	public void setVariableTypes(VariableType[] variableTypes) {
		this.variableTypes = variableTypes;
	}
	
	public VariableType getVariableType(String typeName) {
		// Note: Not using a hashmap. how many types to be worth ?
		for (VariableType atVarType : variableTypes) {
			if (atVarType.getName().equalsIgnoreCase(typeName)) return atVarType;
		}
		// Not found
		//return null;
		throw new IllegalArgumentException("not found " + typeName);
	}

	public FunctionVariable[] getVariables() {
		return variables;
	}
	
	public void setVariables(FunctionVariable[] variables) {
		this.variables = variables;
	}

	public FunctionVariable getVariable(String varName) {
		// Note: Not using a hashmap. how many types to be worth ?
		for (FunctionVariable atVar : variables) {
			if (atVar.getName().equalsIgnoreCase(varName)) return atVar;
		}
		// Not found
		//return null;
		throw new IllegalArgumentException("not found " + varName);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Function [name=" + name + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Function other = (Function) obj;
		if (name == null) {
			if (other.caption != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	// Function evaluation. Abstract at this level
	// Calculate function. The zero aproximation to 1-N values should be carried by an engine supplied by calculator.
	public abstract double calculate(double[] varValues, HashMap<String, Object> context) throws ArithmeticException;

	
}
