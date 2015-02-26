package varcalc02;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Panel for GUI for Function.
 * Displays basic information about the function, and current value and selected units for variables.
 * Allow selecting a variable to be calculated, and infer its value from the values entered in other variables.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
@SuppressWarnings("serial")
public class FunctionPanel extends JPanel implements ActionListener, FocusListener {
	//
	// Data elements
	//
	/** Function object displayed in panel. */
	private Function function;
	/** Index (as in variables array within function) of the variable being calculated. */
	private int selectedVariable;
	/**
	 *  Current values of the variables. Indices correlate to variables array within Function.
	 * Notice the stored values are in the units used by Function#calculate. Therefore must convert from and to displayed units as needed.
	 */
	private double[] valuesVariables;
	
	/** Variable units currently selected. Indices correlate to variables array within Function. */
	private VariableTypeUnit[] unitsSelected;
	
	//
	// Interface elements
	//
	/** Radio buttons for selecting calculated variable (and displaying variable's label). Indices correlate to variables array within Function. */
	private JRadioButton[] rbVariables;
	/** Group for #rbVariables. */
	private ButtonGroup groupVariables;
	// TT-LOW labels might be useful if/where radioButtons not used (but in that case radioButtons might be disabled).
//	private JLabel[] labelsVariables;
	/** Text boxes containing values for variables. Indices correlate to Function#variables. */
	private JTextField[] fieldsVariables;
	/** Optional selection boxes for available variable units (meters, feet). As provided by VariableType#units from each variable's type. */
	private JComboBox<String>[] combosUnits;
	/** Position in the layout of the first row containing variables. */
	private int gridy_firstVariable = 1;
	
	//
	// Initialization
	//
	public FunctionPanel() {
		super(new GridBagLayout());
		
		// No further initialization until formula provided
	}
	
	/**
	 * Set the function object into the panel.
	 * If the panel was configured for a previous function, previous components are cleared and the panel set up again.
	 * The caller might need to invoke #revalidate() in order to make the content property displayed.
	 * @param newFunction
	 */
	// Not reentrant as fields are altered within
	public synchronized void setFunction(Function newFunction) {
		this.function = newFunction;
		
		// Just in case there was a previous deployment
		removeAll();
		
		// Display name and description
		setBorder(BorderFactory.createTitledBorder(function.getCaption()));

		if (function.getDescription() != null && !function.getDescription().trim().isEmpty()) {
			customAdd(new JLabel(function.getDescription()), -1, 0, "gridWidth=0 fill=Horizontal");
		}
		
		// Deploy the variables' components
		
		FunctionVariable[] variables = newFunction.getVariables();
		rbVariables = new JRadioButton[variables.length];
		groupVariables = new ButtonGroup();
//		labelsVariables = new JLabel[variables.length];
		fieldsVariables = new JTextField[variables.length];
		// Hack casting to allow create array of generic type http://stackoverflow.com/questions/14917375/cannot-create-generic-array-of-how-to-create-an-array-of-mapstring-obje
		combosUnits = (JComboBox<String>[]) new JComboBox[variables.length];
		valuesVariables = new double[variables.length];
		// current units applied to variables, if no type specified a neutral unit is used.
		unitsSelected = new VariableTypeUnit[variables.length];
//		unitsCalculate = new VariableTypeUnit[variables.length];
		
		selectedVariable = 0;
		
		final int yOffset = 1; // gridbag layout y coordinate for first variable
		for (int i = 0; i < variables.length; i++) { // foreach variable. Need the index
			// Init data
			FunctionVariable atVariable = variables[i];
			valuesVariables[i] = atVariable.getInitialDisplayValue();
			VariableType varType = null; // optional, null if no type specified -> No combo
			if (atVariable.getType() != null) {
				varType = function.getVariableType(atVariable.getType());
				unitsSelected[i] = varType.getUnit(atVariable.getInitialDisplayUnit());
//				unitsCalculate[i] = varType.getUnit(atVariable.getFunctionUnit());
			} else { // set unit(parse/format; value conversion)
				unitsSelected[i] = new VariableTypeUnit.Proportional("default","", // name, caption
						NumberFormat.getNumberInstance(), 1.0, 0.0); // NumberFormat, factor, offset
			}
			
			
			JRadioButton rb = createRadioButtonForVariable(atVariable);
			rbVariables[i] = rb;
			customAdd(rb, i, 0, "");
			// selected variable will have radio button selected after loop
			
			JTextField field = createTextFieldForVariable(i);
			fieldsVariables[i] = field;
			customAdd(field, i, 1, "weightx=1");
			// selected textfield will be disabled after loop
			
			// available units (e.g. meters-feet) if a type is specified for the variable
			if (varType != null) {
				JComboBox<String> comboUnits = createUnitsComboForVariable(atVariable, varType);
				combosUnits[i] = comboUnits;
				customAdd(comboUnits, i, 2, "");
			}
		} // foreach variable
		// Apply custom behavior to selected variable
		rbVariables[selectedVariable].setSelected(true);
		fieldsVariables[selectedVariable].setEditable(false);

		// TT-LOW variable values textfields could have consecutive tab indices, so they would be navigated more conveniently with keyboard
	}

	private JRadioButton createRadioButtonForVariable(
			FunctionVariable variable) {
		// Radio button (includes label) for #1 Displaying the variable name and #2 Allowing to select the calculated variable
		// TT-FUTURE disable for oneway functions
		JRadioButton rb = new JRadioButton(variable.getCaption());
		rb.setEnabled(variable.isCalculable());
		groupVariables.add(rb);
		rb.setFocusable(false);
		rb.addActionListener(this);
		return rb;
	}
	
	private JTextField createTextFieldForVariable(int variableIndex) {
		JTextField field = new JTextField();
		double fieldValue = unitsSelected[variableIndex].valueFromCore(valuesVariables[variableIndex]);
		String fieldText = unitsSelected[variableIndex].format(fieldValue);
		field.setText(fieldText);

		field.addActionListener(this);
		field.addFocusListener(this);
		field.setHorizontalAlignment(JTextField.RIGHT);
		return field;
	}

	private JComboBox<String> createUnitsComboForVariable(
			FunctionVariable atVariable, VariableType varType) {
		// TT-LOW behavior for no type, no units, single unit, disabled units ... [52]
		// Fill in combo choices, and find out the initially selected index.
		String[] unitChoices = new String[varType.getUnits().length];
		int iUnitSelected = -1;
		String unitSelectedName = atVariable.getInitialDisplayUnit();
		for (int iunit = 0; iunit < unitChoices.length; iunit++) {
			unitChoices[iunit] = varType.getUnits()[iunit].getCaption();
			if (varType.getUnits()[iunit].getName().equals(unitSelectedName)) { iUnitSelected = iunit; }
		}
		assert iUnitSelected > -1;
		JComboBox<String> comboUnits = new JComboBox<String>(unitChoices);
		comboUnits.setSelectedIndex(iUnitSelected);
		comboUnits.setEditable(false);
		comboUnits.addActionListener(this);
		return comboUnits;
	}

	private Insets custom_insets = new Insets(2,2,2,2);
	/**
	 * customization for #add(component, constraints) with the repeated and automatizable settings. 
	 * @param comp
	 */
	private void customAdd(Component comp, int iVar, int gridx, String custom) {
		int l_gridWidth = 1;
		int l_gridHeight = 1;
		double l_weightx = 0.0;
		double l_weighty = 0.0;
		int l_anchor = GridBagConstraints.WEST;
		int l_fill = GridBagConstraints.HORIZONTAL;
		Insets l_insets = custom_insets;
		int l_padx = 2;
		int l_pady = 2;

		if (custom.contains("weightx=1")) {
			l_weightx = 1.0;
		}
		
		if (custom.contains("gridWidth=0") ) {
			l_gridWidth = 0;
			l_anchor = GridBagConstraints.NORTHWEST;
		}
		
		add(comp, new GridBagConstraints(gridx, iVar + gridy_firstVariable,
				l_gridWidth, l_gridHeight, l_weightx, l_weighty,
				l_anchor, l_fill,
				l_insets, l_padx, l_pady));

	}
	
	/**
	 * When a text field value is changed, the value is parsed and copied into the array of values.
	 * @param index Index of the variable/field with new value entered.
	 * @throws ParseException
	 */
	protected void updateCalcValueFromFieldText(int index) throws ParseException {
		String fieldText = fieldsVariables[index].getText();
		double fieldValue = ((Number)unitsSelected[index].parse(fieldText)).doubleValue();
		double coreValue = unitsSelected[index].valueToCore(fieldValue);
//		double calcValue = unitsCalculate[index].valueFromCore(coreValue);
//		valuesVariables[index] = calcValue;
		valuesVariables[index] = coreValue;
	}

	/**
	 * Update the value displayed in a field (when calculated/loaded in the variables array).
	 * @param index
	 */
	protected void updateFieldTextFromCalcValue(int index) {
//		double coreValue = unitsCalculate[index].valueToCore(valuesVariables[index]);
//		double fieldValue = unitsSelected[index].valueFromCore(coreValue);
		double fieldValue = unitsSelected[index].valueFromCore(valuesVariables[index]);
		String fieldText = unitsSelected[index].format(fieldValue);
		fieldsVariables[index].setText(fieldText);
	}
	
	/**
	 * Set which variable should be calculated when the other variables are entered.
	 * @param newVariableIndex
	 */
	public void setSelectedVariable(int newVariableIndex) {
		if (selectedVariable != newVariableIndex) {
			fieldsVariables[selectedVariable].setEditable(true);
			fieldsVariables[newVariableIndex].setEditable(false);

			// update field for previously selected variable (ERR text might be currently displayed)
			updateFieldTextFromCalcValue(selectedVariable);

			// formatted value might not be exactly calculated value
			int oldIndex = selectedVariable;
			selectedVariable = newVariableIndex;
			onFieldUpdated(oldIndex); // Will force recalculation
		}
	}
	
	/**
	 * Calculate the target variable and display its textfield.
	 * Executed once the variables array is updated.
	 */
	public void calculateVariable() {
		try {
			// TT-LOW fetch engine
			new NREngine().calculateZeroOfFunction(function, valuesVariables, selectedVariable);
			updateFieldTextFromCalcValue(selectedVariable);
		} catch (ArithmeticException e) {
			VarCalc02.handle(e);
			fieldsVariables[selectedVariable].setText("ERR");
		}
	}
	
	/**
	 * Invoked when a text field is updated
	 * @param fieldIndex
	 */
	private void onFieldUpdated(int fieldIndex) {
		// TT-LOW Might check if value is actually changed (although re-calculating is not so expensive)
		try {
			updateCalcValueFromFieldText(fieldIndex);
		} catch (ParseException e) {
			getToolkit().beep();
			JTextField atField = fieldsVariables[fieldIndex];
			atField.selectAll();
			atField.grabFocus();
			return;
		}

		calculateVariable();

	}
	
	//
	// Event Handlers
	//
	
	@Override
	public void focusGained(FocusEvent e) {
		// when field gains focus, select text to edit/copy faster
		Object oSrc = e.getSource();
		if (oSrc instanceof JTextField) {
			((JTextField)oSrc).selectAll();
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		// when field loses focus, check field's value
		Object oSrc = e.getSource();
		if (oSrc instanceof JTextField) {
			for (int i = fieldsVariables.length - 1; i >= 0; i--) {
				if (fieldsVariables[i] == oSrc && fieldsVariables[i].isEditable()) {
					onFieldUpdated(i);
					break;
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object oSrc = e.getSource();
		// When radio button is activated, that variable is selected as target variable
		if (oSrc instanceof JRadioButton) {
			for (int i = rbVariables.length - 1; i >= 0; i--) {
				if (rbVariables[i] == oSrc) {					
					setSelectedVariable(i);
					break;
				}
			}
		// When a new value is entered in a field
		} else if (oSrc instanceof JTextField) {
			for (int i = fieldsVariables.length - 1; i >= 0; i--) {
				if (fieldsVariables[i] == oSrc) {
					onFieldUpdated(i);
					break;
				}
			}
		// When a unit is selected for a variable
		} else if (oSrc instanceof JComboBox) {
			for (int i = combosUnits.length - 1; i >= 0; i--) {
				if (combosUnits[i] == oSrc) {
					// update selected unit. refresh field value
					FunctionVariable atVariable = function.getVariables()[i];
					VariableType atType = function.getVariableType(atVariable.getType());
					VariableTypeUnit newSelectedUnit = atType.getUnits()[combosUnits[i].getSelectedIndex()];
					unitsSelected[i] = newSelectedUnit;
					updateFieldTextFromCalcValue(i);
				}
			}
		}
		
	}

}
