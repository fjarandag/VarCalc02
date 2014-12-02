import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Test {

	private static void testScripts() {
//		javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
//		java.util.List<javax.script.ScriptEngineFactory> factories = manager.getEngineFactories();
//		for (javax.script.ScriptEngineFactory factory : factories) {
//			System.out.println(factory.getEngineName());
//		}

		try {
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
			engine.eval("function calculate() { return 1.0; }");
			Object res = engine.eval("calculate();");
			System.out.println(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private static void testParseXml() {
		try {
			DocumentBuilder builder =  DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse("test/mortgage-01-simplest.xml");
			Element functionNode = (Element)doc.getElementsByTagName("function").item(0);
			System.out.println(functionNode.getAttributes().getNamedItem("builder"));
			Element scriptCodeProperty = (Element)functionNode.getElementsByTagName("property").item(0);
			System.out.println(scriptCodeProperty.getTextContent());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// testScripts();
		testParseXml();
		
	}


}
