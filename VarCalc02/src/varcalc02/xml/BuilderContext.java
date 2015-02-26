package varcalc02.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * Data context used by {@link SimpleBuilder} implementations during xml function file "unmarshalling".
 * The context allows for nesting:
 *   A builder processing an inner part will be able to access through the context
 *   to the "parent" builder, and her context.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
@SuppressWarnings("serial")
// TT-REDESIGN context currently is by set of siblings, not by item, which is weird.
public class BuilderContext extends HashMap<String, Object> {
	// TT-LOW set as fields
	public static final String PARENT_CONTEXT = "parentContext";
	public static final String PARENT_ITEM = "parentItem";

	private String namespace;

	public BuilderContext() {
		super();
	}

	/** namespace for the element under construction. (used frequently) */
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
	// TT-REDESIGN equalsNS(Node)
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

	
	/**
	 * Get property contained in the context.
	 * If the property is not found at current level, will look up recursively in the parent's context.
	 * If need to check only properties defined at current level, use {@link #get(String, boolean)}
	 * @param pName property name
	 * @return
	 */
	@Override
	public Object get(Object pName) {
		Object result;
		BuilderContext ancestor = this;
		do {
			result = ancestor.get((String)pName, false);
			ancestor = (BuilderContext)ancestor.get(PARENT_CONTEXT, false);
		} while (result == null && ancestor != null);
		// TODO Test simpler Alternative ? (but produces recursivity? % better crash than infinite loop)
		// result = super.get(pName);
		// if (result == null) { result = ((BuilderContext)super.get(PARENT_CONTEXT)).get(pName); }
		// TT-LOW Use some counter, just in case one context is set as an ancestor of itself.
		return result;
	}
	
	/**
	 * Get property contained in the context, indicating whether the look up should be recursive.
	 * @param pName Name of the property
	 * @param nested true if must continue look up in ancestor context while not found.
	 * @return
	 */
	public Object get(String pName, boolean nested) {
		return nested ? get(pName) : super.get(pName);
	}

	/**
	 * Create child context (to be used by child builder)
	 * @param builtItem item currently being built, so child builder (which will use the context) can reference it
	 * @return new Context, with the namespace, the parent context and parent item references.
	 */
	// TT-LOW Naming. is it really embedding? createChildContext?
	public BuilderContext embedOnBuiltItem(Object builtItem) {
		BuilderContext embeddedContext = new BuilderContext();
		embeddedContext.namespace = this.namespace;
		embeddedContext.put(PARENT_CONTEXT, this);
		embeddedContext.put(PARENT_ITEM, builtItem);
		return embeddedContext;
	}


}
