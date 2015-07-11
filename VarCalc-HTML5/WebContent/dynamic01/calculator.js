/*
Mortgage Calculator. Prototype of dynamic multi-directional calculator by Javier Aranda.
Dynamic: Formula/Equation specified at runtime. Multi-directional: Many of the variables are selectable as target.
Copyright (c) 2015 Javier Aranda (http://javier-aranda.com/).
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

// ?? Depend on jQuery 1.7 or compatible loaded

// Replace the content of the form with the HTML for a calculator for the formula
// <form id="calculator01" class="calculator" action="javascript:doCalculate('calculator01','mortgageFormula')">
function generateCalculator(formId, formulaName) {
	
	var formElement = document.getElementById(formId);
	
	var forFormula = eval(formulaName);	
	var rowClass = ['row0','row1'];
	var htmlPerRow = [];
	var rowNumber = 0;
	// 	for each variable insert a row;
	// 	<div class="row0">
	//		<input type="radio" name="target" value="@varName" />
	//		<label for="@varName">@var.caption:</label>
	//		<input name="@varName" type="number" class="fieldNumber" @var.htmlAttributes />
	//	</div>
	for (var varName in forFormula.variables) {
		if (forFormula.variables.hasOwnProperty(varName)) {
			var forVar = forFormula.variables[varName];
			var htmlForVar = 
					'<div class="@var.class">\n' +
					'	<input type="radio" name="target" value="@var.name" @var.checked />\n' +
					'	<label for="@var.name">@var.caption:</label>\n' +
					'	<input name="@var.name" type="number" class="fieldNumber" value="@var.defValue" @var.htmlAttributes />\n' +
					'</div>';
			
			htmlForVar = htmlForVar
					.replace('@var.class', rowClass[rowNumber++ % 2])
					.replace(/@var.name/g, varName) // appears many times
					.replace('@var.checked', forFormula.checked == varName ? 'checked':'' )
					.replace('@var.caption', forVar.caption)
					.replace('@var.defValue', forVar.defValue)
					.replace('@var.htmlAttributes', forVar.htmlAttributes);
					
			htmlPerRow.push(htmlForVar);
			
		}
	} // for each variable

	htmlPerRow.push(
		'<div class="'+ rowClass[rowNumber % 2] + '">\n'+
		'	<button type="submit" style="margin-left:20pt;">Calculate</button>\n'+
		'	<button type="reset" style="margin-left:20pt;">Reset</button>\n'+
		'</div>');

	formElement.innerHTML = htmlPerRow.join('\n\n');

};

// Read value from field. Might have to convert into formula value.
// Same as awkward eval(aFormula.variables[aName].read.replace('%1','aForm[aName].value'));
function readField(aForm, aFormula, aName) {
	var formFieldValue = aForm[aName].value;
	var readConversion = aFormula.variables[aName].read;
	var formulaFieldValue = eval(readConversion.replace('%1','formFieldValue'));
	return formulaFieldValue;
}

// Write value in form field. Might have to convert value from formula
// Same as aForm[aName].value = eval(aFormula.variables[aName].write.replace('%1','aFormulaValue'));
function writeField(aForm, aFormula, aName, aFormulaValue) {
	var writeConversion = aFormula.variables[aName].write;
	var formFieldValue = eval(writeConversion.replace('%1','aFormulaValue'));
	aForm[aName].value = formFieldValue;
}

// Does the formula calculation: Approximates the target variable to a value that complies with the rest of variables.
// @arg aFormId Id for the form
// @arg aFormulaName Name of the formula being used.
function doCalculate(aFormId, aFormulaName) {	
	// Get target variable from radio buttons
	// var targetVar = l_form.target.value; // works for Chrome, not for IE
	var atForm = document.getElementById(aFormId);
	var targetVar = $(atForm).find('input[name=target]:checked').val();
//	var targetRadios = aForm.elements;
//	for (i = 0; i < targetRadios.length; i++) {
//		if (targetRadios[i].name=='target' && targetRadios[i].checked) {
//			var targetVar = targetRadios[i].value;
//			break;
//		}
//	}

	// load the variables
	// Will load the target variable as well. (will be used as default value, although stuff like that should be given by the spec)
	var atFormula = eval(aFormulaName);
	for (var varName in atFormula.variables) {
		if (atFormula.variables.hasOwnProperty(varName)) {
			eval ('var %1 = readField(atForm,atFormula,varName)'.replace('%1',varName));
		}
	}
		
	// Approximate target value
	// Code follows Mortgate Formula and Newton-Raphson method (look-up if need further information)
	
	var formula_expression = atFormula.expression;
	// Loosely based on varcalc02.NREngine

	var precisionAchieved = false;
	var stepCount = 0;
	while (!precisionAchieved) {
		var resAtTarget = eval(formula_expression);
		var targetValue = eval(targetVar);
		// TT-LOW Might detect if converging or running in circles (or into infinity).
		// @@@ Should assimilate js exception handling and implement a better solution
		if (stepCount >= 20) {
			// @@@
//				throw new ArithmeticException("Not reaching zero x=" + targetValue + ";f(x)=" + resAtTarget);
			console.log("Not reaching zero x=" + targetValue + ";f(x)=" + resAtTarget);
			break;
		}
		// Extrapolate next target value
		// slope : f'(x) calculated as ( f(x+ε) - f(x) ) / ε
		var epsilon = 1e-9;
		eval(targetVar+'+= epsilon');
		var resAtTarget2 = eval(formula_expression);
		var slope = (resAtTarget2 - resAtTarget) / epsilon;

		// refine x value (targetValue) for next iteration
		var nextValue = targetValue - resAtTarget / slope;
			
		// Update values
		precisionAchieved = Math.abs(nextValue - targetValue) < 1e-7; // using a slightly different precision constraint @@@
		
		eval(targetVar +' = nextValue');
		stepCount++;
	}

	if (!precisionAchieved) {
		window.alert('could not aproximate rate within 20 attempts');
	} else {
		// update target field
		writeField(atForm, atFormula, targetVar, eval(targetVar));
	}
}
	
	// Expand - Collapse
function expand(parentId) {
	$('#'+parentId+' .expandable').show();
	$('#'+parentId+' .expand-link').html('<a href="javascript:collapse(\''+ parentId + '\')">[collapse]</a>');
}

function collapse(parentId) {
	$('#'+parentId+' .expandable').hide();
	$('#'+parentId+' .expand-link').html('<a href="javascript:expand(\''+ parentId + '\')">[expand]</a>');
}

