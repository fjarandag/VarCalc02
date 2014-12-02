package varcalc02;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Unit for a variable type. For example grams for a mass variable type.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
public abstract class VariableTypeUnit {
	private NumberFormat delegateFormat;

	// name
	/** Name for identifying unit within variable type. */
	private String name;
	/** Caption for describing unit to user. */
	private String caption;
	// TT-REDESIGN not implementing description.

	public VariableTypeUnit() {
		super();
	}
	
	
	public VariableTypeUnit(String name, String caption, NumberFormat delegateFormat) {
		super();
		this.name = name;
		this.caption = caption;
		this.delegateFormat = delegateFormat;
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
	
	protected NumberFormat getDelegateFormat() {
		return delegateFormat;
	}
	
	protected void setDelegateFormat(NumberFormat delegateFormat) {
		this.delegateFormat = delegateFormat;
	}

	
	public final String format(double number) {
		return delegateFormat.format(number);
	}
	public Number parse(String source) throws ParseException {
		return delegateFormat.parse(source);
	}
	
	public abstract double valueToCore(double unitValue);
	
	public abstract double valueFromCore(double coreValue);
	
	public static class Proportional extends VariableTypeUnit {
		private double factor;
		private double offset;
		
		public Proportional(String name, String caption, NumberFormat delegateFormat, double factor, double offset) {
			super(name, caption, delegateFormat);
			this.factor = factor;
			this.offset = offset;
		}
		@Override
		public double valueToCore(double unitValue) {
			return (unitValue - offset) / factor;
		}
		@Override
		public double valueFromCore(double coreValue) {
			return coreValue * factor + offset;
		}
	}
	
	// TT-LOW Check if the naming (Exponential, factor) etc is correct
	public static class Exponential extends VariableTypeUnit {
		private double factor;
		
		public Exponential(String name, String caption, NumberFormat delegateFormat, double factor) {
			super(name, caption, delegateFormat);
			this.factor = factor;
		}
		@Override
		public double valueToCore(double unitValue) {
			// log(1 +unitValue) / factor
			return Math.log1p(unitValue) / factor;
		}
		@Override
		public double valueFromCore(double coreValue) {
			// exp(coreValue * factor) -  1
			return Math.expm1(coreValue * factor);
		}
	}
	
}
