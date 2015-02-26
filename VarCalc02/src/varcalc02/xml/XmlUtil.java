package varcalc02.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import varcalc02.VarCalc02;

/**
 * Utility methods used within xml processing
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
public class XmlUtil {

	/** Context property for varcalc02 namespace.
	 * Value of property (null or any) follows function element,
	 *  rest of elements and attributes should follow. */
	public static final String VC02_NS = "xmlns:vc02";
	
	private static Map<Class<?>, Map<String, SimpleBuilder<?>>> registeredBuilders = new HashMap<>();

	static {
		// Do core builders initialization/registration of core builders
		// Class.forName() forces initialization if not done before.
		// This class initialization will probably occur when #getRegisteredBuilder is invoked first time. 
		try {
			Class.forName(CoreFunctionBuilder.class.getName());
			Class.forName(CoreVariableBuilder.class.getName());
			Class.forName(CoreVariableTypeBuilder.class.getName());
			Class.forName(CoreVariableTypeUnitBuilder.class.getName());
		} catch(ClassNotFoundException unconceivable) {
			// Vizzini (Princess Bride villain) dixit
			VarCalc02.handle(unconceivable);
		}
	}

	/**
	 * Register one builder for loading an item from an xml element.
	 * As different custom implementations are possible for a given element
	 *  (e.g. a function which uses an incompatible parametrization)
	 *  a builder attribute in the element gives clue of builder implementation to use.
	 * Registration of a builder to some names might be done in an static initializer in the builder class, and that initialization forced with
	 *  a Class.forName for that particular class before the xml loading is performed.
	 * @param targetClass
	 * @param name
	 * @param builder
	 * @return
	 */
	public static <T2> SimpleBuilder<T2> registerBuilder (Class<T2> targetClass, String name, SimpleBuilder<T2> builder) {
		Map<String, SimpleBuilder<?>> buildersForClass = registeredBuilders.get(targetClass);
		if (buildersForClass == null) { // Need to create when inserting first
			buildersForClass = new HashMap<>();
			registeredBuilders.put(targetClass, buildersForClass);
		}
		return (SimpleBuilder<T2>)buildersForClass.put(name, builder);
	}

	/**
	 * Returns the builder implementation for target class and builder name.
	 * @param targetClass Class of the item to be built.
	 * Expected to be determined by element name.
	 * Use the proper class (e.g. Function), not a subclass or superclass.
	 * @param name Builder name. The name might be specified in the builder attribute in the xml element.
	 * If a builder attribute is not present, then look up for the builder registered as "default".
	 * That is register and invoke default with "default" name: entering empty string or null for that purpose is a mistake.
	 * @return
	 */
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

	/**
	 * Returns the builder implementation to be used for an xml element.
	 * @param targetClass class of the item to be built.
	 * Use proper class (e.g. Function), subclass or superclass is wrong.
	 * Though class might be deduced from the element's name in simple cases, that's the caller's duty (if we want to keep it simple and generic then wont do here).
	 * @param element xml element containing information for building
	 * @param context data context to be used in the element's construction.
	 * @return
	 */
	public static <T2> SimpleBuilder<T2> getRegisteredBuilder(Class<T2> targetClass, Element element, BuilderContext context) {
		String builderName = element.getAttributeNS(context.getNamespace(), "builder");
		if (builderName.isEmpty()) builderName = "default";
		return getRegisteredBuilder(targetClass, builderName);
	}

	/** Shortcut for creating a Set collection. Used for the list of required and unbound parameters. */
	public static <T2> Set<T2> unmodifiableSet (T2 ... items) {
		return Collections.unmodifiableSet(new HashSet<T2>(Arrays.asList(items)));
	}

}
