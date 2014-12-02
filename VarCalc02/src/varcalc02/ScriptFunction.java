package varcalc02;

import java.util.HashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class ScriptFunction extends Function {
	private String scriptLanguage;
	private String scriptCode;
	
	
	public ScriptFunction() {
		super();
	}

	public String getScriptLanguage() {
		return scriptLanguage;
	}


	public void setScriptLanguage(String language) {
		this.scriptLanguage = language;
	}


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
			throw new ArithmeticException(e.getMessage());
		}
		return result;
	}

}
