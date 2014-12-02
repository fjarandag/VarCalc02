package varcalc02.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import varcalc02.FunctionVariable;
import varcalc02.VarCalc02;

public class CoreVariableBuilder extends SimpleBuilder<FunctionVariable> {

	private static CoreVariableBuilder s_instance = new CoreVariableBuilder();
	public static CoreVariableBuilder instance(String builderName) {
		// TT-LOW
		return s_instance;
	}
	
	static {
		XmlUtil.registerBuilder(FunctionVariable.class, "default", instance("default"));
	}
	
	private static Set<String> s_required = XmlUtil.unmodifiableSet("name");
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
	protected FunctionVariable createItem(Element ele,
			Map<String, String> properties, BuilderContext context) {
		// caption will follow name value if not specified
		// type is optional. TT-LOW surrogate properties might warn if type not specified. Check might be done at Function completion
		// Properties by default
		HashMap<String, Object> fullProperties = new HashMap<>();
		// Fill default properties
		fullProperties.put("caption", properties.get("name"));
		// TT-LOW Unable to convert varcalc02.FunctionVariable#calculable(boolean)=true(java.lang.Boolean)
		fullProperties.put("calculable", "true");
		
		fullProperties.putAll(properties);
		FunctionVariable variable = new FunctionVariable();
		this.setPropertiesInto(fullProperties, variable);
		
		return variable;
	}

	@Override
	protected void handleChild(Element child, BuilderContext childrenContext) {
		// Unexpected
		VarCalc02.log("CoraVariableBuilder.handleChild");
	}

	@Override
	protected void handleElementDone(Element ele, BuilderContext childrenContext) {
		// Nothing to do
		
	}

}
