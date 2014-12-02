package varcalc02;

/**
 * Nature of the magnitude (weight, speed, interest rate, etc) contained by a function variable.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
public class VariableType {
	// name
	private String name;
	
	// TT-REDESIGN Might need a decodedUnit. But that unit might not be shown, so at this point I see better skip it.
	// unitDecoded
	
	// units
	private VariableTypeUnit[] units;

	public VariableType() {
		super();
	}
	

	public VariableType(String name, VariableTypeUnit[] units) {
		super();
		this.name = name;
		this.units = units;
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public VariableTypeUnit[] getUnits() {
		return units;
	}

	public void setUnits(VariableTypeUnit[] units) {
		this.units = units;
	}

	public VariableTypeUnit getUnit(String unitName) {
		// Note: Not using a hashmap. how many types to be worth ?
		for (VariableTypeUnit atUnit : units) {
			if (atUnit.getName().equalsIgnoreCase(unitName)) return atUnit;
		}
		// Not found
		//return null;
		throw new IllegalArgumentException("not found " + unitName);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "VariableType [name=" + name + "]";
	}

	// TT-REDESIGN Using same equals() and hashcode() as Object.
	// Might not need specific methods, and name field might be not enough

	
}
