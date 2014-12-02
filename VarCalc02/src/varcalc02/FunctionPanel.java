package varcalc02;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
	
	public void setFunction(Function newFunction) {
		this.function = newFunction;
		
		removeAll();
		
		setBorder(BorderFactory.createTitledBorder(function.getCaption()));

		if (function.getDescription() != null && !function.getDescription().trim().isEmpty()) {
			add(new JLabel(function.getDescription()), new GridBagConstraints(0, 0, 0, 1, 1.0, 0.0, // gridx, gridy, gridwidth, gridheight, weightx, weighty
					GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, // anchor, fill
					new Insets(2, 2, 2, 2), 2, 2)); // insets, ipadx, ipady
		}
		
		FunctionVariable[] variables = newFunction.getVariables();
		rbVariables = new JRadioButton[variables.length];
		groupVariables = new ButtonGroup();
//		labelsVariables = new JLabel[variables.length];
		fieldsVariables = new JTextField[variables.length];
		// Hack casting to allow create array of generic type http://stackoverflow.com/questions/14917375/cannot-create-generic-array-of-how-to-create-an-array-of-mapstring-obje
		combosUnits = (JComboBox<String>[]) new JComboBox[variables.length];
		valuesVariables = new double[variables.length];
		unitsSelected = new VariableTypeUnit[variables.length];
//		unitsCalculate = new VariableTypeUnit[variables.length];
		
		selectedVariable = 0;
		
		final int yOffset = 1;
		for (int i = 0; i < variables.length; i++) { // foreach variable. Need the index
			FunctionVariable atVariable = variables[i];
			valuesVariables[i] = atVariable.getInitialDisplayValue();
			VariableType varType = function.getVariableType(atVariable.getType());
			// TT-LOW handle null properties
			unitsSelected[i] = varType.getUnit(atVariable.getInitialDisplayUnit());
//			unitsCalculate[i] = varType.getUnit(atVariable.getFunctionUnit());
			
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
			
		} // foreach variable
		// TT-LOW variable values textfields could have consecutive tab indices, so they would be navigated more conveniently with keyboard
	}
	
	protected void updateCalcValueFromFieldText(int index) throws ParseException {
		String fieldText = fieldsVariables[index].getText();
		double fieldValue = ((Number)unitsSelected[index].parse(fieldText)).doubleValue();
		double coreValue = unitsSelected[index].valueToCore(fieldValue);
//		double calcValue = unitsCalculate[index].valueFromCore(coreValue);
//		valuesVariables[index] = calcValue;
		valuesVariables[index] = coreValue;
	}
	
	protected void updateFieldTextFromCalcValue(int index) {
//		double coreValue = unitsCalculate[index].valueToCore(valuesVariables[index]);
//		double fieldValue = unitsSelected[index].valueFromCore(coreValue);
		double fieldValue = unitsSelected[index].valueFromCore(valuesVariables[index]);
		String fieldText = unitsSelected[index].format(fieldValue);
		fieldsVariables[index].setText(fieldText);
	}
	
	
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
	
	private void onFieldUpdated(int fieldIndex) {
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
		// when field gains focus, select text to edit faster
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
		if (oSrc instanceof JRadioButton) {
			for (int i = rbVariables.length - 1; i >= 0; i--) {
				if (rbVariables[i] == oSrc) {					
					setSelectedVariable(i);
					break;
				}
			}
		} else if (oSrc instanceof JTextField) {
			for (int i = fieldsVariables.length - 1; i >= 0; i--) {
				if (fieldsVariables[i] == oSrc) {
					onFieldUpdated(i);
					break;
				}
			}
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
