package varcalc02;

/**
 * Variable in a calculated function.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
public class FunctionVariable {
	/** Internal name (used as key). Should be an adequate file name. */
	private String name;
	// caption
	/** Text shown at the calculator panel. Might be more descriptive than name. */
	private String caption;
	/** Description for function shown at the calculator panel. */
	private String description;
	/**
	 * Variable type.
	 * E.g. a weight variable, which might be specified in grams, Kg, pounds, ounces, etc.
	 * Is optional, if not specified the type combo will be empty and disabled.
	 */
	private String type;
	/** Which variable type unit is initially set. */
	private String initialDisplayUnit;
	/** Which value is initially assigned. Specified in function units. */
	// TT-REDESIGN at some stage might other data types than numbers.
	private double initialDisplayValue;
	/** Initial value for approximation (Root-finding) methods. */
	private double initialCalcValue = 1.0;
	private double maxValue = Double.MAX_VALUE;
	private double minValue = Double.MIN_VALUE;
	
	// TT-FUTURE 
//	private String[] favoriteValueCaptions;
//	private double[] favoriteValues;
	
	/** By default variables might be calculable, but in some cases might want to disable.
	 * In oneway functions will be used to tell apart input variables (not calculable) and output variables.  */
	private boolean calculable;

	public FunctionVariable() {
		super();
	}


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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getInitialDisplayUnit() {
		return initialDisplayUnit;
	}

	public void setInitialDisplayUnit(String initialUnit) {
		this.initialDisplayUnit = initialUnit;
	}

	public double getInitialDisplayValue() {
		return initialDisplayValue;
	}

	public void setInitialDisplayValue(double initialValue) {
		this.initialDisplayValue = initialValue;
	}

	
	public double getInitialCalcValue() {
		return initialCalcValue;
	}


	public void setInitialCalcValue(double initialCalcValue) {
		this.initialCalcValue = initialCalcValue;
	}


	public double getMaxValue() {
		return maxValue;
	}


	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}


	public double getMinValue() {
		return minValue;
	}


	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}


	// TT-FUTURE 
/*	public String[] getFavoriteValueCaptions() {
		return favoriteValueCaptions;
	}

	public void setFavoriteValueCaptions(String[] favouriteValueCaptions) {
		this.favoriteValueCaptions = favouriteValueCaptions;
	}

	public double[] getFavoriteValues() {
		return favoriteValues;
	}

	public void setFavoriteValues(double[] favouriteValues) {
		this.favoriteValues = favouriteValues;
	}
*/
	public boolean isCalculable() {
		return calculable;
	}

	public void setCalculable(boolean calculable) {
		this.calculable = calculable;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FunctionVariable [name=" + name + "]";
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
		FunctionVariable other = (FunctionVariable) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public static FunctionVariable createVariable(String name, String description, double defValue, String varType, String defUnit) {
			FunctionVariable newVariable = new FunctionVariable();
			newVariable.setName(name);
			newVariable.setCaption(name);
			newVariable.setDescription(description);
			newVariable.setInitialDisplayValue(defValue);
			newVariable.setInitialCalcValue(defValue);
			newVariable.setType(varType);
			newVariable.setInitialDisplayUnit(defUnit);
			newVariable.setCalculable(true);
			// TT-FUTURE 
	//		newVariable.setFavoriteValues(new double[]{});
	//		newVariable.setFavoriteValueCaptions(new String[]{});
			return newVariable;
		}

	
}
