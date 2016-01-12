package varcalc02;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

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
	// 2016-01-16 Cache script engines for the same thread.
	// TT-LOW Tested for single-thread. Multi-thread testing out of scope.
	private static Map<String,ThreadLocal<SoftReference<ScriptEngine>>> scriptEngineCache = new HashMap<>();
	public static ScriptEngine getScriptEngine(String engineName) {
		// Might be the first thread to look for the engine
		ThreadLocal<SoftReference<ScriptEngine>> enginePerThread = scriptEngineCache.get(engineName);
		if (enginePerThread == null) {
			enginePerThread = new ThreadLocal<>();
			scriptEngineCache.put(engineName, enginePerThread);
		}
		// It might be the first time this thread requests the engine, or the GC might had disposed it.
		SoftReference<ScriptEngine> ref = enginePerThread.get();
		ScriptEngine engine = ref == null ? null : ref.get();
		if (engine == null) {
			engine = new ScriptEngineManager().getEngineByName(engineName);
			enginePerThread.set(new SoftReference<ScriptEngine>(engine));
		}
		return engine;
	}
	
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
		// - As of jdk1.8_31 actually it takes significant time (in the order of tenths of a second, according to JUnit and JVisualVM profiling). Therefore
		//		| actually the scriptengine instance should be shared/reused between calculations.
		// - However there might be multi-threading concerns. See ScriptEngineFactory#getParameter. A global engine would not be suitable then.
		//		| Caching depending on threading might be over-complicating
		// Therefore as of 2016-01-12, ScriptEngine is cached per thread and not per context.
		ScriptEngine engine = getScriptEngine(scriptLanguage);

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
