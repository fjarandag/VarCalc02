package varcalc02.xml;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import varcalc02.FunctionVariable;
import varcalc02.VarCalc02;
import varcalc02.VariableType;
import varcalc02.VariableTypeUnit;

/**
 * Builder for {@link VariableTypeUnit}.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
// TT-LOW Specification for properties
public class CoreVariableTypeUnitBuilder extends SimpleBuilder<VariableTypeUnit> {

	private static CoreVariableTypeUnitBuilder s_instance = new CoreVariableTypeUnitBuilder();
	public static CoreVariableTypeUnitBuilder instance(String builderName) {
		// TT-LOW
		return s_instance;
	}
	
	static { // Initialization. Class.forName() needed before look up. Done in XmlUtil.
		XmlUtil.registerBuilder(VariableTypeUnit.class, "default", instance("proportional"));
		XmlUtil.registerBuilder(VariableTypeUnit.class, "proportional", instance("proportional"));
		XmlUtil.registerBuilder(VariableTypeUnit.class, "exponential", instance("exponential"));
	}
	
	// TT-LOW
	private static Set<String> s_required = XmlUtil.unmodifiableSet("name", "factor");
	private static Set<String> s_ommitable = XmlUtil.unmodifiableSet("builder");
	

	@Override
	protected Set<String> requiredProperties() {
		return s_required;
	}

	@Override
	protected Set<String> ommitableProperties() {
		return s_ommitable;
	}


	@Override
	protected VariableTypeUnit createItem(Element ele,
			Map<String, String> properties, BuilderContext context) {
		// PARADIGM create will full Constructor
		// caption will follow name value if not specified
		// delegateFormat will be DecimalFormat
		// factor defaults to 1.0
		// offset defaults to 0.0
		
		HashMap<String, String> l_propertiesCopy = new HashMap(properties);
		// removing properties, leave just the unreclaimed
		String l_name = l_propertiesCopy.remove("name");
		String l_caption = l_propertiesCopy.remove("caption");
		if (l_caption == null) l_caption = l_name;
		String l_formatPattern = l_propertiesCopy.remove("format");
		NumberFormat l_format = NumberFormat.getNumberInstance();
		try {
			l_format = new DecimalFormat(l_formatPattern);
		} catch (NullPointerException npe) { /* format not specified. will be default */
		} catch (Exception e) { /* Bad format or else. warn and will be default */
			VarCalc02.log("@varTypeUnitBuilder.create.format: %s", e);
		}
		String l_strFactor = l_propertiesCopy.remove("factor");
		double l_factor = 1.0;
		try {
			l_factor = Double.parseDouble(l_strFactor);
//		} catch (NullPointerException npe) { /* Not specified. warn. */
		} catch (Exception e) { /* Bad format or else. warn and will be default */
			VarCalc02.log("@varTypeUnitBuilder.create.factor: %s", e);
		}
		String l_strOffset = l_propertiesCopy.remove("factor");
		double l_offset = 0.0;
		try {
			l_offset = Double.parseDouble(l_strOffset);
		} catch (NullPointerException npe) { /* Not specified. optional property. */
		} catch (Exception e) { /* Bad format or else. warn and will be default */
			VarCalc02.log("@varTypeUnitBuilder.create.offset: %s", e);
		}
		
		// Tell about extra properties
		l_propertiesCopy.keySet().removeAll(ommitableProperties()); // items removed from keySet are removed from map.
		if (VarCalc02.DEBUG && ! l_propertiesCopy.isEmpty()) {
			VarCalc02.log("@varTypeUnitBuilder.create.unreclaimed: %s", l_propertiesCopy);
		}
		
		// Which specific unit type to build
		VariableTypeUnit l_unit = null;
		String l_builderName = properties.get("builder");
		if (l_builderName != null && (
				l_builderName.equalsIgnoreCase("exponential") ||
				l_builderName.equalsIgnoreCase("e"))) {
			l_unit = new VariableTypeUnit.Exponential(l_name, l_caption, l_format, l_factor);
		} else { // default
			l_unit = new VariableTypeUnit.Proportional(l_name, l_caption, l_format, l_factor, l_offset);			
		}
		
		return l_unit;
	}

	@Override
	protected void handleChild(Element child,
			BuilderContext childrenContext) {
		// Unexpected
		VarCalc02.log("CoraVariableBuilder.handleChild");
		
	}

	@Override
	protected void handleElementDone(Element ele,
			BuilderContext childrenContext) {
		// Nothing to do
		
	}

}
