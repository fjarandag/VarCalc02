package varcalc02;

import java.util.HashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
/**
 * Function where the calculation is executing from script code loaded on runtime.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
public class ScriptFunction extends Function {
	private String scriptLanguage;
	private String scriptCode;
	
	
	public ScriptFunction() {
		super();
	}

	/** Scripting language used (e g JavaScript, be careful with uppercase).
	 * Should be supported by the JRE builtin scripting engine (Rhino).
	 */
	public String getScriptLanguage() {
		return scriptLanguage;
	}


	public void setScriptLanguage(String language) {
		this.scriptLanguage = language;
	}


	/**
	 * Scripting code to be executed as the function.
	 * The result of the (last) statement is the result.
	 */
	public String getScriptCode() {
		return scriptCode;
	}


	public void setScriptCode(String scriptCode) {
		this.scriptCode = scriptCode;
	}


	@Override
	public double calculate(double[] varValues, HashMap<String, Object> context)
			throws ArithmeticException {
		// TT-REDESIGN Not clear if worth to store/cache in context (not so hard to create, not so frequent).
		ScriptEngine engine = (ScriptEngine)context.get("engine");
		if (engine == null) {
			engine = new ScriptEngineManager().getEngineByName(scriptLanguage);
			context.put("engine", engine);
		}
		SimpleBindings bindings = new SimpleBindings();
		for (int i = 0; i < varValues.length; i++) {
			bindings.put(this.getVariables()[i].getName(), varValues[i]);
		}
		double result = 0.0;
		try {
			result = ((Number) engine.eval(this.scriptCode, bindings))
					.doubleValue();
		} catch (ScriptException e) {
			// Stack traces from within the script interpreter might not be too significative.
			// Hope you wont mind too much if they are lost
			throw new ArithmeticException(e.getMessage() + ":" + e.getLineNumber());
		}
		return result;
	}

}
