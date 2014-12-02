package varcalc02;

import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import varcalc02.xml.BuilderContext;
import varcalc02.xml.SimpleBuilder;
import varcalc02.xml.XmlUtil;

/**
 * Main class for the Variable Calculator. Contains some static/global facilities for the other classes.
 * VarClass02 main and related methods are intended to be a simple working example. Might be extended or used for testing related issues.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
public class VarCalc02 {
	public static final NumberFormat NUMFORMAT = DecimalFormat.getNumberInstance();
//	public static final NumberFormat PERCENTFORMAT = DecimalFormat.getPercentInstance();
	public static final NumberFormat PERCENTFORMAT = new DecimalFormat("0.00%");
//	public static final NumberFormat CURRENCYFORMAT = DecimalFormat.getCurrencyInstance();
	public static final NumberFormat CURRENCYFORMAT = new DecimalFormat("#,###,##0.##");
	public static final NumberFormat PRECISIONFORMAT = new DecimalFormat("0.00000000");

	public static void main(String[] args) {
		try {
			// Accept arguments, such as an url to load
			String strUrl = null;
			if (args.length > 0) {
				// TT-LOW what if there are more parameters, spec seems loose.
				// TT-LOW might have a collection of builtin functions
				for (String s : args) {
					if (s.equalsIgnoreCase("debug")) {
						VarCalc02.DEBUG = true;
					} else {
						strUrl = s;
					}
				}
			}
			// Load the function instance
			// final here does NOT mean constant
			final Function l_function;
			if (strUrl == null) {
				l_function = createMortgageFunction();
			} else {
				// load function from URL
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

				l_function = functionBuilder.buildFromDom(eleFunction, funcContext);
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					//Create and set up the window.
					JFrame frame = new JFrame("VarCalc02");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					
					FunctionPanel panel = new FunctionPanel();
					panel.setFunction(l_function);
					
					panel.setOpaque(true);
					frame.setContentPane(panel);
					//Display the window.
					//frame.setPreferredSize(new Dimension(200,150));
					frame.pack();
					frame.setVisible(true);
				}
			});
		} catch(Throwable e) {
			handle(e);
		}
	}
	
	public static Function createMortgageFunction() {
		// From http://en.wikipedia.org/wiki/Mortgage_calculator
		// c = r P / (1 - (1+r)^-N)
		// TT-REDESIGN might enter actual currencies (e.g. exchange from EUR to legacy currencies with fixed rates), but I see it better as a single unit type sample
		VariableTypeUnit[] unitsCurrency = {new VariableTypeUnit.Proportional("currency","currency",CURRENCYFORMAT, 1.0, 0.0)};
		unitsCurrency[0].setCaption("¤");
		VariableType typeCurrency = new VariableType("currency", unitsCurrency);

		VariableTypeUnit[] unitsTime = {
				new VariableTypeUnit.Proportional("months", "months", NUMFORMAT, 1.0, 0.0),
				new VariableTypeUnit.Proportional("periods", "periods", NUMFORMAT, 1.0, 0.0),
				new VariableTypeUnit.Proportional("years", "years", NUMFORMAT, 1.0 / 12.0, 0.0)
		};
		VariableType typeTime = new VariableType("time", unitsTime);

		VariableTypeUnit[] unitsInterest = {
				new VariableTypeUnit.Proportional("expRate", "exp-rate", PRECISIONFORMAT, 1.0, 0.0),
				new VariableTypeUnit.Exponential("periodRate", "period rate", PRECISIONFORMAT, 1.0),
				new VariableTypeUnit.Exponential("apr", "APR(%)", PERCENTFORMAT, 12.0),
		};
		VariableType typeInterest = new VariableType("interest", unitsInterest);
		
		FunctionVariable varPayment = FunctionVariable.createVariable("c", "periodic payment", 1000.0, "currency", "currency");
		FunctionVariable varPrincipal = FunctionVariable.createVariable("P", "Mortgage principal", 200000.0, "currency", "currency");
		FunctionVariable varDuration = FunctionVariable.createVariable("N", "Duration", 240, "time", "years");
		FunctionVariable varInterest = FunctionVariable.createVariable("r", "Interest rate", 0.0001, "interest", "apr");
		
		ScriptFunction function = new ScriptFunction();
		function.setName("Mortgage");
		function.setCaption("Mortgage/Fixed rate interest");
		function.setDescription("Mortgage formula calculator c = P r / (1 - (1+r)^-N) ; if r=0 then c=P/N");
		function.setBehavior(Function.MONOTONIC);
		function.setVariableTypes(new VariableType[]{typeCurrency,typeTime,typeInterest});
		function.setVariables(new FunctionVariable[]{varPayment,varPrincipal,varDuration,varInterest});
		function.setDefaultTargetVariable("c");
		function.setScriptLanguage("JavaScript");
		// formula converted for exponential ratio = Ln(1 + r)
		// Using expm1 for improved precision
		// Need to access java Math, as of Nov 2014 expm1 is experimental in javascript
		//    (https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math/expm1)
		function.setScriptCode("r==0.0 ? P / N - c : (P * java.lang.Math.expm1(r) / - java.lang.Math.expm1(r * -N)) - c");
		// scriptCode also works with multiple statements
		//function.setScriptCode("if(r==0.0) { P / N - c } else { (P * java.lang.Math.expm1(r) / - java.lang.Math.expm1(r * -N)) - c }");

		return function;
	}
	
	// Logging. Not using any particular library for very simple needs
	public static boolean DEBUG = true;
	
	public static void log(String msg, Object ... args) {
		String formatted = String.format(msg, args);
		System.err.println(formatted);
	}
	
	public static void handle(Throwable e) {
		e.printStackTrace(System.err);
	}


	// Calculating epsilon for a given variable and values
	// epsilon variable is used for zero-of-function aproximation
	// epsilon must be as small as posible for greater precision, but too small an epsilon might fail
	//   (because of floating point precision limits in initial and intermediate values and roundings). 
	// Different variables in a function might have different range of good epsilon values.
	// 	At this point this specific setting wont be introduced.
	// Good epsilon values will depend on the magnitude of a variable. We might need a lower epsilon for values very near zero.
	//	At this point epsilon will be max(10^-12,x*10^-12). Which is good for 12 decimals precision.
	//	Double precision allows 52 mantissa bits (around 17 decimals)
	// TT-REDESIGN If result precision attribute is introduced in variables setting, might need to rethink implementation
	public static double epsilon(Function f, double[] varValues, int targetVar) {
		double x = varValues[targetVar];
		double e = Math.max(1e-12, x * 1e-12);
		return e;
	}
	
}
