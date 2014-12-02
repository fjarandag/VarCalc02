package varcalc02.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import varcalc02.VarCalc02;

public class XmlUtil {

	/** Context property for varcalc02 namespace.
	 * Value of property (null or any) follows function element,
	 *  rest of elements and attributes should follow. */
	public static final String VC02_NS = "xmlns:vc02";
	
	private static Map<Class<?>, Map<String, SimpleBuilder<?>>> registeredBuilders = new HashMap<>();

	static {
		// Do core builders initialization/registration
		try {
			Class.forName(CoreFunctionBuilder.class.getName());
			Class.forName(CoreVariableBuilder.class.getName());
			Class.forName(CoreVariableTypeBuilder.class.getName());
			Class.forName(CoreVariableTypeUnitBuilder.class.getName());
		} catch(ClassNotFoundException unconceibable) {
			// Vizzini (Princess Bride villain) dixit
			VarCalc02.handle(unconceibable);
		}
	}

	public static <T2> SimpleBuilder<T2> registerBuilder (Class<T2> targetClass, String name, SimpleBuilder<T2> builder) {
		Map<String, SimpleBuilder<?>> buildersForClass = registeredBuilders.get(targetClass);
		if (buildersForClass == null) { // Need to create when inserting first
			buildersForClass = new HashMap<>();
			registeredBuilders.put(targetClass, buildersForClass);
		}
		return (SimpleBuilder<T2>)buildersForClass.put(name, builder);
	}

	public static <T2> SimpleBuilder<T2> getRegisteredBuilder(Class<T2> targetClass, String name) {
		SimpleBuilder<T2> builder = null;
		Map<String, SimpleBuilder<?>> buildersForClass = registeredBuilders.get(targetClass);
		if (buildersForClass != null) {
			builder = (SimpleBuilder<T2>)buildersForClass.get(name);
		}
		if (builder == null) {
			VarCalc02.log("No builder found for %s %s", targetClass.getName(), name);
		}
		return builder;
	}

	public static <T2> SimpleBuilder<T2> getRegisteredBuilder(Class<T2> targetClass, Element element, BuilderContext context) {
		String builderName = element.getAttributeNS(context.getNamespace(), "builder");
		if (builderName.isEmpty()) builderName = "default";
		return getRegisteredBuilder(targetClass, builderName);
	}

	public static <T2> Set<T2> unmodifiableSet (T2 ... items) {
		return Collections.unmodifiableSet(new HashSet<T2>(Arrays.asList(items)));
	}

}
