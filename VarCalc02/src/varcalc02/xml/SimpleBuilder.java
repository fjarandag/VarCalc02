package varcalc02.xml;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import varcalc02.VarCalc02;

/**
 * Builds an object from a DOM element.
 * This basic implementation provides support for some automatic property setting into the new object's fields.
 * Use actual builders (like {@link CoreFunctionBuilder} or {@link CoreVariableTypeUnitBuilder} as examples.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 * @param <T> Class of the object being built
 */
// TT-REDESIGN Issues about item building
// - context handling is weird
// - #createItem should be allowed to not create the actual item
// - properties: Using "disabled" concept instead of ommitable. Allow properties after children.
// - provide some automation for building children (as a mapping from child name to target class).
public abstract class SimpleBuilder <T> {
	/**
	 * Builds the target item from the DOM element.
	 * The building process can be divided in these steps:
	 * - read the DOM attributes and core property elements
	 * - Create the actual item with this core properties ({@link #createItem(Element, Map, BuilderContext)})
	 * - Handle the children (create internal items) if any ( {@link #handleChild(Element, BuilderContext)})
	 * - Complete the created item setup ({@link #handleElementDone(Element, BuilderContext)}).
	 * @param ele
	 * @param context
	 * @return
	 */
	public T buildFromDom(Element ele, BuilderContext context) {
		// - load properties (attributes and property elements at start)
		// - before first embedded element, or end of element, create target instance
		// - handle each embedded element.
		HashMap<String,String> properties = new HashMap<>();
		// TT-LOW Consider if might consider namespaces in some way.
		NamedNodeMap attributes = ele.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Attr attrAtI = (Attr)attributes.item(i);
			if (context.equalsNamespace(attrAtI.getNamespaceURI())) {
				properties.put(attrAtI.getLocalName(), attrAtI.getValue());
			}
		}
		Node child = ele.getFirstChild();
		// process property elements (until end of children, or other tag found)
		// TT-LOW Allow recognizing required properties as elements
		String vc02NS = context.getNamespace();
		while (child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
//				if (child.getNodeName().equals("property")) {
				if (child.getLocalName().equals("property") &&
						context.equalsNamespace(child.getNamespaceURI())) {
				 				
//					properties.put(child.getAttributes().getNamedItem("name").getTextContent(), child.getTextContent());
					// TT-LOW undocumented NullPointer if name not specified
					String propertyName = child.getAttributes().getNamedItemNS(vc02NS,"name").getTextContent();
					properties.put(propertyName, child.getTextContent());
				} else {
					break;
				}
			}
			child = child.getNextSibling();
		}
		
		// Check if all required properties are collected
		Set<String> required = requiredProperties();
		if (! properties.keySet().containsAll(required)) {
			Set<String> missingProperties = new HashSet<String>(required);
			missingProperties.removeAll(properties.keySet());
			VarCalc02.log("Error: %s missing properties: %s", ele.getTagName(), missingProperties);
		}

		T builtItem = createItem(ele, properties, context);
		
		BuilderContext embeddedContext = context.embedOnBuiltItem(builtItem);
		if (child != null) {
			
			
			while (child != null) {
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					// TT-LOW catch individual exceptions allow finding more errors
					handleChild((Element)child, embeddedContext);
				}
				child = child.getNextSibling();
			}
		}
		handleElementDone(ele, embeddedContext);
		return (T)embeddedContext.get("parentItem");
	}

	/**
	 *  Handle element creation once properties are known and before handling children.
	 *  {@link #setPropertiesInto(Map, Object)} is provided to automatically setting up the new object fields, if naming and conversion is straight-forward.
	 * */
	protected abstract T createItem(Element ele, Map<String, String> properties, BuilderContext context);

	/** Handle child creation. */
	protected abstract void handleChild(Element child, BuilderContext childrenContext);

	/** Handle final setup (as final linking with children elements) */
	protected abstract void handleElementDone(Element ele, BuilderContext childrenContext);

	/** Simple properties that must be present for the element to be built.
	 * Specific implementations will specify the set so the base code checks it.
	 */
	protected abstract Set<String> requiredProperties();
	/** Properties that are not required to be set in the built item.
	 * All found properties might be attempted to be set in the target (if you don't want them to be set, remove them before invoking #setPropertiesInto).
	 * If attempt to set property fails, will only complain if property is not ommitable.
	 * Intended for properties like "builder", which might be defined (for looking up the builder), but not assigned to the built item itself.
	 */
	// TT-REDESIGN ommitable is weird. Alternative: "disabledProperties" which might be declared, but not automatically set.
	protected abstract Set<String> ommitableProperties();

	/**
	 * Sets some properties as a new object attributes, provided that naming and type conversion of these is straight forward.
	 * This method is supplied to automatize the built item setup (No need to write boiler-plate code for each attribute).
	 * Depends on a list of required and ommitable properties for automation:
	 * - Will complain if a property that should be isn't
	 * - Will complain if a property which is not ommitable fails to be set.
	 * @param properties
	 * @param target
	 */
	protected void setPropertiesInto(Map<String, ?> properties, Object target) {
		// TT-LOW Should the item construction fail if there are problems with not ommitable properties?
		// TT-LOW Should we track if all required properties are actually set? (maybe easier from #createItem)		
		for (Map.Entry<String, ?> entry : properties.entrySet()) {
			Method setter = null;
			String propertyName = entry.getKey();
			// Ommitable: can try to set, but wont take seriously if cant
			boolean ommitable = ommitableProperties().contains(propertyName);
			try {
				// TT-LOW Handling. Need 2 points for handling? Need to know IntrospectionException message?
				setter = new PropertyDescriptor(propertyName, target.getClass()).getWriteMethod();
			} catch (IntrospectionException e) {
				if (VarCalc02.DEBUG && ! ommitable){
					VarCalc02.log("Getting setter for %s#%s: %s", target.getClass().getName(), propertyName, e);
				}
			}
			if (setter == null) {
				if (! ommitable) {
					VarCalc02.log("No setter for %s#%s", target.getClass().getName(), propertyName);
				} // else (ommitable) nothing to do
			} else {
				// Assign the property. Different cases:
				// - The property type is assignable to the setter type -> invoke
				// - The property type is String (very probable coming from xml file) and the setter type has an String constructor -> Create object to set
				// - Otherwise -> incompatible assignment.
				// TT-LOW What if we actually want to assign null value
				Object entryValue = entry.getValue();
				Object setValue = null;
				Class<?> setterClass = setter.getParameterTypes()[0];
				if (setterClass.isAssignableFrom(entryValue.getClass())) {
					setValue = entryValue;
				} else {
					if (entryValue instanceof String) {
						try {
							// http://stackoverflow.com/a/13949291/1117429 (how-to-convert-from-string-to-a-primitive-type-or-standard-java-wrapper-types)
							PropertyEditor editor = PropertyEditorManager.findEditor(setterClass);
						    editor.setAsText((String)entryValue);
						    setValue = editor.getValue();
//						    Constructor<?> setterConstructor = setterClass.getConstructor(String.class);
//							setValue = setterConstructor.newInstance(setValue);
						} catch (Exception e) {
							if (VarCalc02.DEBUG) { // Will log again later anyway
								VarCalc02.log("Building setter argument for %s#%s (%s): %s", target.getClass().getName(), propertyName, setterClass.getName(), e);
							}
						}
					}
				}
				if (setValue == null) { 
					VarCalc02.log("Unable to convert %s#%s(%s)=%s(%s)",
							target.getClass().getName(), propertyName, setterClass, entryValue, entryValue.getClass().getName());
				} else {
					try {
						setter.invoke(target, setValue);
					} catch (Exception e) {
						VarCalc02.log("Setting %s#%s=%s: %s", target.getClass().getName(), propertyName, setValue, e);
					}
					
				}
			}
		}
	}
	
}
