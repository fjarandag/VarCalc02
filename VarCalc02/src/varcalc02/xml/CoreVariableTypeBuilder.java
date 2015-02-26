package varcalc02.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import varcalc02.VarCalc02;
import varcalc02.VariableType;
import varcalc02.VariableTypeUnit;

/**
 * Builder for the VariableType.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
// TT-LOW Specification for properties and child elements.
public class CoreVariableTypeBuilder extends SimpleBuilder<VariableType> {

	private static CoreVariableTypeBuilder s_instance = new CoreVariableTypeBuilder();
	public static CoreVariableTypeBuilder instance(String builderName) {
		// TT-LOW
		return s_instance;
	}
	
	static { // Initialization. Need Class.forName( ) done before look up. Done in XmlUtil.
		XmlUtil.registerBuilder(VariableType.class, "default", instance("default"));
	}
	
	// TT-LOW
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
	protected VariableType createItem(Element ele, Map<String, String> properties, BuilderContext context) {
		// Properties by default. None
//		HashMap<String, Object> fullProperties = new HashMap<>();
		
//		fullProperties.putAll(properties);
		Map<String, ?> fullProperties = properties;

		VariableType vtype = new VariableType();
		this.setPropertiesInto(fullProperties, vtype);
		
		// add structures in context for embedded items
		context.put("typeUnits", new ArrayList<>());
		
		return vtype;
	}

	@Override
	protected void handleChild(Element child, BuilderContext childrenContext) {
		if (child.getLocalName().equals("type-unit") && childrenContext.equalsNamespace(child.getNamespaceURI())) {
			List<VariableTypeUnit> units = (List<VariableTypeUnit>)childrenContext.get("typeUnits");
			SimpleBuilder<VariableTypeUnit> builder = XmlUtil.getRegisteredBuilder(VariableTypeUnit.class, child, childrenContext);
			VariableTypeUnit newUnit = builder.buildFromDom(child,childrenContext);
			units.add(newUnit);
		} else {
			VarCalc02.log("unexpected element %s", child.getLocalName());
		}
	}

	@Override
	protected void handleElementDone(Element ele, BuilderContext childrenContext) {
		VariableType vtype = (VariableType)childrenContext.get("parentItem");

		List<VariableTypeUnit> typeUnits = (List<VariableTypeUnit>)childrenContext.get("typeUnits");
		vtype.setUnits(typeUnits.toArray(new VariableTypeUnit[typeUnits.size()]));
		
	}

}
