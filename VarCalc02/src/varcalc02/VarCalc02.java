package varcalc02;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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
				l_function = createMortgageScriptFunction();
			} else {
				l_function = loadFunctionFromUrl(strUrl);
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					//Create and set up the window.
					JFrame frame = new JFrame("VarCalc02");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					
					FunctionPanel panel = new FunctionPanel();
					panel.setFunction(l_function);
					
					createAndSetMenuBar(frame);
					
					setIcon(frame);
					
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

	// Main method delegates
	//
	
	/** Create a function object from an URL (which content is the function's xml). */
	private static Function loadFunctionFromUrl(String strUrl)
			throws MalformedURLException, ParserConfigurationException,
			IOException, SAXException {
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

		Function l_function = functionBuilder.buildFromDom(eleFunction, funcContext);
		return l_function;
	}
	
	/** Handles the menu items events, such as actions for loading URL or exiting application. */
	private static class MenuListener implements ActionListener {
		private JFrame boundFrame;
		public MenuListener(JFrame boundFrame) { this.boundFrame = boundFrame; }
		@Override
		public void actionPerformed(ActionEvent evt) {
			String action = evt.getActionCommand();
			if (action.equals("exit")) {
				boundFrame.dispose();
				// (application terminated) as dispose() triggers close Window event.
			} else if (action.equals("load")) {
				String loadUrl = (String)JOptionPane.showInputDialog(boundFrame, "Enter URL for function file");
				try {
					Function function = loadFunctionFromUrl(loadUrl);
					FunctionPanel panel = (FunctionPanel)boundFrame.getContentPane();
					panel.setFunction(function);
					// Force repaint
					// - repaint by itself does not seem to work, display does not update until layout is recalculated (when changing window size)
					// - Alternatively might replace the function panel (http://stackoverflow.com/questions/1097366/java-swing-revalidate-vs-repaint)
					//panel.repaint();
					panel.revalidate();
				} catch (Exception e) {
					JOptionPane.showInternalMessageDialog(boundFrame, 
							"could not load function from URL: " + e.getMessage(), "VarCalc02 error",
							JOptionPane.ERROR_MESSAGE);
					handle(e);
				}
			}
			
		}
		
	}
	
	/** Create menubar for the frame/window. */
	// Creates separate instances to allow multiple windows in the future
	private static void createAndSetMenuBar(JFrame varcalc02frame) {
		MenuListener menuListener = new MenuListener(varcalc02frame);
		JMenuBar menuBar = new JMenuBar();
		JMenuItem itemLoad = new JMenuItem("Load...", 'l');
		itemLoad.setActionCommand("load");
		itemLoad.addActionListener(menuListener);
		menuBar.add(itemLoad);
		
		JMenuItem itemExit = new JMenuItem("Exit", 'x');
		itemExit.setActionCommand("exit");
		itemExit.addActionListener(menuListener);
		menuBar.add(itemExit);
		
		varcalc02frame.setJMenuBar(menuBar);
	}
	
	/** Loads and sets the icon for the window/frame. */
	private static void setIcon(JFrame frame) {
		try {
			InputStream iconStream = VarCalc02.class.getResourceAsStream("/varcalc02/func-32.png");
			BufferedImage image = ImageIO.read(iconStream);
			frame.setIconImage(image);
		} catch(Exception e) {
			handle(e);
		}
	}
	

	/**
	 * Creates the default mortgage function, used for demonstration.
	 */
	public static Function createMortgageScriptFunction() {
		// From http://en.wikipedia.org/wiki/Mortgage_calculator
		// c = r P / (1 - (1+r)^-N)
		ScriptFunction function = new ScriptFunction();
		createMortgageFunction_fillCommon(function);
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
	
	public static Function createMortgageInlineFunction() {
		Function function = new Function() {			
			@Override
			public double calculate(double[] varValues, HashMap<String, Object> context) throws ArithmeticException {
				double c = varValues[0];
				double P = varValues[1];
				double N = varValues[2];
				double r = varValues[3];
				if (r == 0.0) { // Exception at r==0. would cause DivBy0
					return  P / N - c;
				} else { // General formula
					return (P * Math.expm1(r) / - Math.expm1(r * -N)) - c;
				}
			}
		};
		createMortgageFunction_fillCommon(function);
		return function;
	}

	private static void createMortgageFunction_fillCommon(Function function) {
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
		
		function.setName("Mortgage");
		function.setCaption("Mortgage/Fixed rate interest");
		function.setDescription("Mortgage formula calculator c = P r / (1 - (1+r)^-N) ; if r=0 then c=P/N");
		function.setBehavior(Function.MONOTONIC);
		function.setVariableTypes(new VariableType[]{typeCurrency,typeTime,typeInterest});
		function.setVariables(new FunctionVariable[]{varPayment,varPrincipal,varDuration,varInterest});
		function.setDefaultTargetVariable("c");
	}

	
	// Functions intended to be used elsewhere
	//
	
	// Logging. Not using any particular library for very simple needs
	public static boolean DEBUG = true;
	
	/**
	 * Handles simple login within application. Message formatting as in String#format(String, Object...).
	 */
	public static void log(String msg, Object ... args) {
		String formatted = String.format(msg, args);
		System.err.println(formatted);
	}
	
	/**
	 * Handles simple exception logging within application.
	 */
	public static void handle(Throwable e) {
		e.printStackTrace(System.err);
	}


	/**
	 * Calculate the epsilon value used in the approximation algorithm.
	 * epsilon is an small increment in a variable, used to obtain the function's slope.
	 * An inappropriate epsilon value might produce errors if not small enough (the slope changes within the gap) or if too small
	 * (precision truncation within function might induce too small a difference, or "jitter" might become an issue).
	 * Currently a combination of an absolute value and a relative value is used ( max(10^-12,x*10^-12) ),
	 * but further refination-customization might be appropriate.
	 * @param f Function being evaluated
	 * @param varValues values of the variables in the current approximation.
	 * @param targetVar index of the variable being approximated.
	 * @return recommended epsilon value.
	 */
	// TT-REDESIGN [customizable-epsilon] Might be interesting to allow telling in each variable
	//    how small a value is tolerable, although that might depend on other variables.
	//    Maybe epsilon should be provided by function implementation. 
	public static double epsilon(Function f, double[] varValues, int targetVar) {
		double x = varValues[targetVar];
		double e = Math.max(1e-12, x * 1e-12);
		return e;
	}
	

}
