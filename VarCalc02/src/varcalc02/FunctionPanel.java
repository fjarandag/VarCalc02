package varcalc02;

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

@SuppressWarnings("serial")
public class FunctionPanel extends JPanel implements ActionListener, FocusListener {

	private Function function;
	private JRadioButton[] rbVariables;
	private ButtonGroup groupVariables;
//	private JLabel[] labelsVariables;
	private JTextField[] fieldsVariables;
	private JComboBox<String>[] combosUnits;
	
	private int selectedVariable;
	// Note: Values are converted into calculate units
	private double[] valuesVariables;
	
	/** Variable units currently selected. */
	private VariableTypeUnit[] unitsSelected;
	/** Units used in actual calculation. */
//	private VariableTypeUnit[] unitsCalculate;
	
	public FunctionPanel() {
		super(new GridBagLayout());
		
		// No further initialization until formula provided
	}
	
	/**
	 * Set the function object into the panel.
	 * The panel components are re-deployed.
	 * @param newFunction
	 */
	public void setFunction(Function newFunction) {
		this.function = newFunction;
		
		// Just in case there was a previous deployment
		removeAll();
		
		// Display name and description
		setBorder(BorderFactory.createTitledBorder(function.getCaption()));

		if (function.getDescription() != null && !function.getDescription().trim().isEmpty()) {
			add(new JLabel(function.getDescription()), new GridBagConstraints(0, 0, 0, 1, 1.0, 0.0, // gridx, gridy, gridwidth, gridheight, weightx, weighty
					GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, // anchor, fill
					new Insets(2, 2, 2, 2), 2, 2)); // insets, ipadx, ipady
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
			
			// Radio button (includes label) for #1 Displaying the variable name and #2 Allowing to select the calculated variable
			// TT-FUTURE disable for oneway functions
			JRadioButton rb = new JRadioButton(atVariable.getCaption());
			rbVariables[i] = rb;
			rb.setSelected(i == selectedVariable);
			rb.setEnabled(atVariable.isCalculable());
			groupVariables.add(rb);
			rb.setFocusable(false);
			rb.addActionListener(this);
			add(rb, new GridBagConstraints(0, i + yOffset, 1, 1, 0.0, 0.0, // gridx, gridy, gridwidth, gridheight, weightx, weighty
					GridBagConstraints.WEST, GridBagConstraints.NONE, // anchor, fill
					new Insets(2, 2, 2, 2), 2, 2)); // insets, ipadx, ipady
			
			// TextField for displaying/entering variable value
			JTextField field = new JTextField();
			fieldsVariables[i] = field;
			updateFieldTextFromCalcValue(i);
			field.addActionListener(this);
			field.addFocusListener(this);
			field.setHorizontalAlignment(JTextField.RIGHT);
			field.setEditable(i != selectedVariable);
			// TT-LOW might not want to modify it, but need to be able to focus if want to copy value
//			field.setFocusable(i != selectedVariable);
			add(field, new GridBagConstraints(1, i + yOffset, 1, 1, 1.0, 0.0, // gridx, gridy, gridwidth, gridheight, weightx, weighty
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, // anchor, fill
					new Insets(2, 2, 2, 2), 2, 2)); // insets, ipadx, ipady
			
			// available units (e.g. meters-feet) if a type is specified for the variable
			if (varType != null) {
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
				combosUnits[i] = comboUnits;
				comboUnits.setSelectedIndex(iUnitSelected);
				comboUnits.setEditable(false);
				comboUnits.addActionListener(this);
				add(comboUnits, new GridBagConstraints(2, i + yOffset, 1, 1, 0.0, 0.0, // gridx, gridy, gridwidth, gridheight, weightx, weighty
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, // anchor, fill
						new Insets(2, 2, 2, 2), 2, 2)); // insets, ipadx, ipady
			}
		} // foreach variable
		// TT-LOW variable values textfields could have consecutive tab indices, so they would be navigated more conveniently with keyboard
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
