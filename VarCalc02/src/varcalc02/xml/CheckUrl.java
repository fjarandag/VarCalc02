package varcalc02.xml;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import varcalc02.Function;
import varcalc02.VarCalc02;

/**
 * Check if an URL contains an xml which is correctly unmarshalled into a function.
 * Similar to VarCalc02, but without loading UI.
 * Used to test the content, and the loading mechanism.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
// TT-LOW Due to similarities, maybe could integrate this testing in VarCalc02 main
public class CheckUrl {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Initialize core xml building infrastructure (register builders).
			Class.forName(XmlUtil.class.getName());

			// Accept arguments, such as a file to load
			String strUrl = null;
			if (args.length > 0) {
				// TT-LOW what if there are more parameters, spec seems loose.
				for (String s : args) {
					if (s.equalsIgnoreCase("debug")) {
						VarCalc02.DEBUG = true;
					} else {
						strUrl = s;
					}
				}
			}
			if (strUrl == null) {
				System.err.println("Must provide URL parameter");
			} else {
				URL l_url = new URL(strUrl);
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				docBuilderFactory.setNamespaceAware(true);
				DocumentBuilder builder =  docBuilderFactory.newDocumentBuilder();
				InputStream stream = l_url.openStream();
				Document doc = builder.parse(stream);
				stream.close();
				
				// might be using namespace. Using the NS found for the function element.
				// Might be weird if there is a different function element found earlier under a different namespace.
				// Element eleFunction = (Element)doc.getElementsByTagName("function").item(0);
				Element eleFunction = (Element)doc.getElementsByTagNameNS("*", "function").item(0);
				String funcNS = eleFunction.getNamespaceURI();
				BuilderContext funcContext = new BuilderContext();
				funcContext.setNamespace(funcNS);
				
				SimpleBuilder<Function> functionBuilder = XmlUtil.getRegisteredBuilder(Function.class, eleFunction, funcContext);
				Function function = functionBuilder.buildFromDom(eleFunction, funcContext);

			}
		} catch(Throwable e) {
			VarCalc02.handle(e);
		}
	}

}
