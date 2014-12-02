package varcalc02.xml;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class BuilderContext extends HashMap<String, Object> {
	// TT-LOW set as fields
	public static final String PARENT_CONTEXT = "parentContext";
	public static final String PARENT_ITEM = "parentItem";


	private String namespace;

	public BuilderContext() {
		super();
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String usedNamespace) {
		this.namespace = usedNamespace;
	}
	
	/**
	 * Specific test for namespaces.
	 * Empty namespaces are allowed to be either null or empty.
	 * Namespaces will be considered different if they differ as little as letter cases or filling spaces.
	 * @param ns2
	 * @return
	 */
	public boolean equalsNamespace(String ns2) {
		boolean ns1Empty = this.namespace == null || this.namespace.isEmpty();
		boolean ns2Empty = ns2 == null || ns2.isEmpty();
		// Handle cases when one or both namespaces are null.
		if (ns1Empty) {
			return ns2Empty; // true or false
		} else {
			return this.namespace.equals(ns2);
		}
	}
	
	public Object get(String pName) {
		Object result;
		BuilderContext ancestor = this;
		do {
			result = ancestor.get(pName, false);
			ancestor = (BuilderContext)ancestor.get(PARENT_CONTEXT, false);
		} while (result == null && ancestor != null);
		return result;
	}
	
	public Object get(String pName, boolean nested) {
		return nested ? get(pName) : super.get(pName);
	}

	public BuilderContext embedOnBuiltItem(Object builtItem) {
		BuilderContext embeddedContext = new BuilderContext();
		embeddedContext.namespace = this.namespace;
		embeddedContext.put(PARENT_CONTEXT, this);
		embeddedContext.put(PARENT_ITEM, builtItem);
		return embeddedContext;
	}


}
