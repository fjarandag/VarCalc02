package varcalc02;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Unit for a variable type. For example grams for a mass variable type.
 * Units define a conversion fore and aft the function internal scale (as calculating in grams and showing ounces).
 * Such conversions might be more complicated than a simple proportional and offset conversion (as in Fahrenheit to Celsius).
 * In some cases such as the interest rates in mortgages we might need exponential/logarithmic conversions.
 * Therefore conversion is defined as abstract so actual implementations define it.
 * Also units might determine in how a magnitude is formatted (many units might be defined even with the same conversion, just to provide diferent formats).
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
public abstract class VariableTypeUnit {

	// name
	private String name;
	private String caption;
	private NumberFormat delegateFormat;
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


	/** Name for identifying unit within variable type. */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/** Caption for describing unit to user. */
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	
	/**
	 *  NumberFormats instance used to convert the magnitude from scalar to string and vice-versa.
	 *  The scalar will have to be converted separately between function scalar and unit scalar.
	 */
	protected NumberFormat getDelegateFormat() {
		return delegateFormat;
	}
	
	protected void setDelegateFormat(NumberFormat delegateFormat) {
		this.delegateFormat = delegateFormat;
	}

// TODO Redundant. both providing numberformat. are both modes used?
// Alternatively:
// - Might use a generic format, so formatting might be done for generic objects. Might as well provide awt/swing displaying/editing components.
// Not sure if it might be the best to impose implementing a custom Format class for custom magnitudes (% examples?)
	public final String format(double number) {
		return delegateFormat.format(number);
	}
	public Number parse(String source) throws ParseException {
		return delegateFormat.parse(source);
	}
	
	/** Convert the unit scalar into function scalar. */
	public abstract double valueToCore(double unitValue);
	
	/** Convert the function scalar into unit scalar. */
	public abstract double valueFromCore(double coreValue);
	
	/**
	 * Type Unit which only requires a proportional and offset conversion (as in Fahrenheit to Celsius).
	 * @author Javier Aranda (javier-aranda.com)
	 * CC SA BY
	 */
	// TODO example
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
	
	/**
	 * Type unit which requires an exponential/logaritmic conversion (as in interest rates from a period based rate to an exponential rate).
	 * @author Javier Aranda (javier-aranda.com)
	 * CC SA BY
	 */
	// TT-LOW Check if the naming (Exponential, factor) is correct.
	// TODO example
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
