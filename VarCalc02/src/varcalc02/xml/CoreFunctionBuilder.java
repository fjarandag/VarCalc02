package varcalc02.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import varcalc02.Function;
import varcalc02.FunctionVariable;
import varcalc02.ScriptFunction;
import varcalc02.VarCalc02;
import varcalc02.VariableType;

public class CoreFunctionBuilder extends SimpleBuilder<Function> {
	// PARADIGM Builder for a complex type with nested types.
	private static CoreFunctionBuilder s_instance = new CoreFunctionBuilder();
	public static CoreFunctionBuilder instance(String builderName) {
		// TT-LOW
		return s_instance;
	}

	// TT-LOW Modify for telling specific function class
	private static Set<String> s_required = XmlUtil.unmodifiableSet("name","scriptCode");
	
	private static Set<String> s_ommitable = XmlUtil.unmodifiableSet("builder");
	
	static {
		XmlUtil.registerBuilder(Function.class, "default", instance("default"));
		XmlUtil.registerBuilder(Function.class, "jsMonotonic", instance("jsMonotonic"));
	}
	
	@Override
	protected Function createItem(Element ele, Map<String, String> properties, BuilderContext context) {
		
		// Properties by default
		HashMap<String, Object> fullProperties = new HashMap<>();
		// Fill properties
		Class<? extends Function> functionClass = ScriptFunction.class;
		if (properties.get("builder").equals("jsMonotonic")) {
			// properties that need a default value.
			fullProperties.put("scriptLanguage", "JavaScript");
			fullProperties.put("behavior", "monotonic");
			// class and behavior as is
		}
		// TT-LOW add more cases
		
		Function func = null;
		fullProperties.putAll(properties);
/* legacy simple hardcoded mapping
		if (functionClass == ScriptFunction.class) {
			// TT-LOW reflection auto-filling
			func = new ScriptFunction();
			func.setName(fullProperties.get("name"));
			func.setCaption(fullProperties.get("caption"));
			func.setDescription(fullProperties.get("description"));
			//func.setLongDescription(fullProperties.get("longDescription"));
			func.setType(functionBehavior);
			func.setDefaultTarget(fullProperties.get("target")); // optional
			((ScriptFunction)func).setScriptLanguage("JavaScript");
			((ScriptFunction)func).setScriptCode(fullProperties.get("scriptCode"));
		}
*/
		try {
			func = functionClass.newInstance();
			this.setPropertiesInto(fullProperties, func);
			if (func.getCaption() == null) { func.setCaption(func.getName()); }
		} catch(Exception e) {
			// major build failure. returning null, might throw exception as well
			VarCalc02.handle(e);
		}
		
		// add structures in context for embedded items
		context.put("variableTypes", new ArrayList<>());
		context.put("variables", new ArrayList<>());
		
		return func;
	}

	@Override
	protected void handleChild(Element child, BuilderContext childrenContext) {
		if (child.getLocalName().equals("variable-type") && childrenContext.equalsNamespace(child.getNamespaceURI())) {
			List<VariableType> varTypes = (List<VariableType>)childrenContext.get("variableTypes");
			SimpleBuilder<VariableType> builder = XmlUtil.getRegisteredBuilder(VariableType.class, child, childrenContext);
			VariableType varType = builder.buildFromDom(child,childrenContext);
			varTypes.add(varType);
		} else if (child.getLocalName().equals("variable") && childrenContext.equalsNamespace(child.getNamespaceURI())) {
			List<FunctionVariable> variables = (List<FunctionVariable>)childrenContext.get("variables");
			SimpleBuilder<FunctionVariable> builder = XmlUtil.getRegisteredBuilder(FunctionVariable.class, child, childrenContext);
			FunctionVariable variable = builder.buildFromDom(child,childrenContext);
			variables.add(variable);
		} else {
			VarCalc02.log("unexpected element %s", child.getLocalName());
		}

	}

	@Override
	protected void handleElementDone(Element ele, BuilderContext childrenContext) {
		Function function = (Function)childrenContext.get("parentItem");

		List<VariableType> varTypes = (List<VariableType>)childrenContext.get("variableTypes");
		function.setVariableTypes(varTypes.toArray(new VariableType[varTypes.size()]));
		
		List<FunctionVariable> variables = (List<FunctionVariable>)childrenContext.get("variables");
		function.setVariables(variables.toArray(new FunctionVariable[variables.size()]));
		
		// TT-LOW Check function, items x-refs
	}


	@Override
	protected Set<String> requiredProperties() {
		return s_required;
	}

	@Override
	protected Set<String> ommitableProperties() {
		return s_ommitable;
	}

}
