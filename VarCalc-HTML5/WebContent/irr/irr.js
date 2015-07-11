/*
IRR Calculator: Calculates Rate of Return, or any other variable, for an arbitrary number of flows.
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
// Depends on jQuery 1.7 or compatible
/* ToC
- addSinglePayment()
- addPeriodicPayment()
- resetPayments()

- readField(aForm, aFormula, aName)
- writeField(aForm, aFormula, aName, aFormulaValue)
- doCalculate(aFormId, aFormula)

- expand(parentId)
- collapse(parentId)

-clone(obj)

*/
// Math.log1p and Math.expm1 allow better accuracy. Specially for values near to 0. But are not available in older JavaScript engines. Not sure if the extra accuracy is worth the pain.
var HAS_LOG1P = Math.log1p !== undefined;
// TODO Might be possible to invoke a function (in read method) to validate (e.g. negative frequency or duration)
var VARSPEC_CURRENCY = {read:'parseFloat(%1)',write:'%1.toFixed(2)'};
var VARSPEC_INTEREST = HAS_LOG1P ?
		{read:'Math.log1p(parseFloat(%1)/100.0)', write:'( Math.expm1(%1) * 100 ).toFixed(4)'}:
		{read:'Math.log(1.0 + parseFloat(%1)/100.0)', write:'( (Math.exp(%1) - 1.0) * 100 ).toFixed(4)'};
var VARSPEC_TIME = {read:'parseFloat(%1)',write:'(%1).toFixed(2)'};
var VARSPEC_FREQUENCY = {read:'parseFloat(%1)',write:'(%1).toFixed(2)'};
var FORMAT_PP_SUBEXPRESSION = HAS_LOG1P ? 
		' + (r == 0.0?pp%1*dpp%1*fpp%1:pp%1* -Math.expm1(-dpp%1*r)/ Math.expm1(r/fpp%1) * Math.exp(-tpp%1*r) )':
		' + (r == 0.0?pp%1*dpp%1*fpp%1:pp%1*(1.0-Math.exp(-dpp%1*r))/(Math.exp(r/fpp%1)-1.0) * Math.exp(-tpp%1*r) )';

var DEFAULT_IRR_FORMULA = {
	expression:'-P + sp0 * Math.exp(- tsp0 * r)'+ FORMAT_PP_SUBEXPRESSION.replace(/%1/g, 0),
	variables:{
            // TODO Maybe cash flow elements should be nested within an extra scope (e.g. sp0.t instead of tsp0)
            //  * might not be a good idea. An HTML form field name might contain a period, but the related javascript might be more complex.
		P:VARSPEC_CURRENCY, r:VARSPEC_INTEREST,
		sp0:VARSPEC_CURRENCY, tsp0:VARSPEC_TIME, 
		pp0:VARSPEC_CURRENCY, tpp0:VARSPEC_TIME, fpp0:VARSPEC_FREQUENCY, dpp0:VARSPEC_TIME
	},
	countSP:1,countPP:1
};


/**
 * Check that an IRR form has the formula property. Create it if needed
 * @param formName Id of the form
 * @param forceReset (optional) Re-create the formula. Useful on reset.
 */
function checkIRRForm(formName, forceReset) {
	if (forceReset === undefined) {forceReset = false;}
	var lForm = document.getElementById(formName);
	if ((lForm.formula === undefined) || forceReset) {
		lForm.formula = clone(DEFAULT_IRR_FORMULA);
	}
}
function addSinglePayment(formName) {
		checkIRRForm(formName, false);
		var lForm = document.getElementById(formName);
		
        // UI elements
        var jqAddRow = $('#'+formName + ' .row-addSP');
        var rowTypePrev = 'row-sp1'; var rowTypeNext = 'row-sp0';
        if (lForm.formula.countSP % 2 == 0){
            rowTypePrev = 'row-sp0'; rowTypeNext = 'row-sp1';
        }
        var htmlNewRow = '<div class="@rowType" >\n' +
		'<div class="item-fields">\n' +
		'<div class="field0">\n' +
		'<input type="radio" name="target" value="sp@spNum" />\n' +
		'<label for="sp@spNum">Quantity:</label>\n' +
		'<input name="sp@spNum" type="number" class="fieldNumber" value="0" pattern="(-)?\d+(\.\d*)?" step="any"/>\n' +
		'</div>\n' +
		'<div class="field1">\n' +
		'<input type="radio" name="target" value="tsp@spNum" />\n' +
		'<label for="tsp@spNum">At time (years):</label>\n' +
		'<input name="tsp@spNum" type="number" class="fieldNumber" value="0" pattern="(-)?\d+(\.\d*)?" step="any" />\n' +
		'</div>\n' +
		'</div>\n' +
		'</div>\n';
        htmlNewRow = htmlNewRow.replace(/@rowType/g, rowTypePrev);
        htmlNewRow = htmlNewRow.replace(/@spNum/g, lForm.formula.countSP);
        jqAddRow.before(htmlNewRow);
        jqAddRow.removeClass(rowTypePrev);
        jqAddRow.addClass(rowTypeNext);
        
        // Formula Elements
        var newFormulaElement = ' + sp%1 * Math.exp(- tsp%1 * r)';
        newFormulaElement = newFormulaElement.replace(/%1/g, lForm.formula.countSP);
        lForm.formula.expression += newFormulaElement;
        lForm.formula.variables['sp'+lForm.formula.countSP] = VARSPEC_CURRENCY;
        lForm.formula.variables['tsp'+lForm.formula.countSP] = VARSPEC_TIME;
 
	// Done
	lForm.formula.countSP++;
}

function addPeriodicPayment(formName) {
		checkIRRForm(formName, false);
		var lForm = document.getElementById(formName);

        // UI elements
        var jqAddRow = $('#'+formName + ' .row-addPP');
        var rowTypePrev = 'row-pp1'; var rowTypeNext = 'row-pp0';
        if (lForm.formula.countPP % 2 == 0){
            rowTypePrev = 'row-pp0'; rowTypeNext = 'row-pp1';
        }
        var htmlNewRow = '<div class="@rowType row-height2" >\n' +
		'   <div class="item-title">Periodic&nbsp;Payment</div>\n' +
		'       <div class="item-fields">\n' +
		'<div class="field0">\n' +
		'   <input type="radio" name="target" value="pp@spNum" />\n' +
		'   <label for="pp@spNum">Periodic quantity:</label>\n' +
		'   <input name="pp@spNum" type="number" class="fieldNumber" value="0" pattern="(-)?\d+(\.\d*)?" step="any"/>\n' +
		'</div>\n' +
		'<div class="field1">\n' +
		'   <input type="radio" name="target" value="tpp@spNum" />\n' +
		'   <label for="tpp@spNum">Start time (years):</label>\n' +
		'   <input name="tpp@spNum" type="number" class="fieldNumber" value="0" pattern="(-)?\d+(\.\d*)?" step="any" />\n' +
		'</div>\n' +
		'<div class="field2">\n' +
		'   <input type="radio" name="target" value="fpp@spNum" />\n' +
		'   <label for="fpp@spNum">Payments per year:</label>\n' +
		'   <input name="fpp@spNum" type="number" class="fieldNumber" value="12" pattern="\d+(\.\d*)?" step="any" />\n' +
		'</div>\n' +
		'<div class="field3">\n' +
		'   <input type="radio" name="target" value="dpp@spNum" />\n' +
		'   <label for="dpp@spNum">Duration (years):</label>\n' +
		'   <input name="dpp@spNum" type="number" class="fieldNumber" value="1" pattern="\d+(\.\d*)?" step="any" />\n' +
		'</div>\n' +
		'       </div>\n' +
		'   </div>\n' +
		'</div>';
        htmlNewRow = htmlNewRow.replace(/@rowType/g, rowTypePrev);
        htmlNewRow = htmlNewRow.replace(/@spNum/g, lForm.formula.countPP);
        jqAddRow.before(htmlNewRow);
        jqAddRow.removeClass(rowTypePrev);
        jqAddRow.addClass(rowTypeNext);
        
        // Formula Elements
        var newFormulaElement = FORMAT_PP_SUBEXPRESSION.replace(/%1/g, lForm.formula.countPP);
        lForm.formula.expression += newFormulaElement;
        lForm.formula.variables['pp'+lForm.formula.countPP] = VARSPEC_CURRENCY;
        lForm.formula.variables['tpp'+lForm.formula.countPP] = VARSPEC_TIME;
        lForm.formula.variables['fpp'+lForm.formula.countPP] = VARSPEC_FREQUENCY;
        lForm.formula.variables['dpp'+lForm.formula.countPP] = VARSPEC_TIME;

	// Done
	lForm.formula.countPP++;


}

function resetPayments(formName) {
		checkIRRForm(formName, true);

        var formSelector = '#'+formName;
        // remove payment rows, except first of type and add button
        $(formSelector+' .row-sp0, '+formSelector+' .row-sp1').not(':first').not('.row-addSP').remove();
        $(formSelector+' .row-pp0, '+formSelector+' .row-pp1').not(':first').not('.row-addPP').remove();

        // Reset add row. Can be either even or odd row.
        var jqAddRow = $(formSelector + ' .row-addSP');
        // Wont be harmful if unconditionally treated as even row.
        jqAddRow.removeClass('row-sp0');
        jqAddRow.addClass('row-sp1');
        jqAddRow = $(formSelector + ' .row-addPP');
        jqAddRow.removeClass('row-pp0');
        jqAddRow.addClass('row-pp1');
}


/*
Read/Write variables from/into the form.
Notice some conversion might be applied.
*/
// Read value from field. Might have to convert into formula value.
// Same as awkward eval(aFormula.variables[aName].read.replace('%1','aForm[aName].value'));
function readField(aForm, aFormula, aName) {
	var formFieldValue = aForm[aName].value;
	var readConversion = aFormula.variables[aName].read;
	var formulaFieldValue = eval(readConversion.replace(/%1/g, 'formFieldValue'));
	return formulaFieldValue;
}

// Write value in form field. Might have to convert value from formula
// Same as aForm[aName].value = eval(aFormula.variables[aName].write.replace('%1','aFormulaValue'));
function writeField(aForm, aFormula, aName, aFormulaValue) {
	var writeConversion = aFormula.variables[aName].write;
	var formFieldValue = eval(writeConversion.replace(/%1/g, 'aFormulaValue'));
	aForm[aName].value = formFieldValue;
}


/** Does the formula calculation: Approximates the target variable to a value that complies with the rest of variables.
 * @arg aFormId Id for the form
 */
function doCalculate(aFormId) {	
	checkIRRForm(aFormId, false);
	var atForm = document.getElementById(aFormId);
	// Get target variable from radio buttons
	// var targetVar = l_form.target.value; // works for Chrome, not for IE
	var targetVar = $(atForm).find('input[name=target]:checked').val();

	// load the variables (also targetVar)
	for (var varName in atForm.formula.variables) {
		if (atForm.formula.variables.hasOwnProperty(varName)) {
			eval ('var %1 = readField(atForm,atForm.formula,varName)'.replace('%1',varName));
		}
	}
		
	// Approximate target value
	// Uses Newton-Raphson algorithm
	// TODO: Should consider alternatives for non-monotonic functions.
	
	var formula_expression = atForm.formula.expression;
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
		writeField(atForm, atForm.formula, targetVar, eval(targetVar));
	}
}
	
	// Expand - Collapse
function expand(parentId) {
	$('#'+parentId+' .irr-expandable').show();
	$('#'+parentId+' .irr-expand-link').html('<a href="javascript:collapse(\''+ parentId + '\')">[collapse]</a>');
}

function collapse(parentId) {
	$('#'+parentId+' .irr-expandable').hide();
	$('#'+parentId+' .irr-expand-link').html('<a href="javascript:expand(\''+ parentId + '\')">[expand]</a>');
}

// http://stackoverflow.com/a/728694/1117429
function clone(obj) {
    var copy;

    // Handle the 3 simple types, and null or undefined
    if (null == obj || "object" != typeof obj) return obj;

    // Handle Date
    if (obj instanceof Date) {
        copy = new Date();
        copy.setTime(obj.getTime());
        return copy;
    }

    // Handle Array
    if (obj instanceof Array) {
        copy = [];
        for (var i = 0, len = obj.length; i < len; i++) {
            copy[i] = clone(obj[i]);
        }
        return copy;
    }

    // Handle Object
    if (obj instanceof Object) {
        copy = {};
        for (var attr in obj) {
            if (obj.hasOwnProperty(attr)) copy[attr] = clone(obj[attr]);
        }
        return copy;
    }

    throw new Error("Unable to copy obj! Its type isn't supported.");
}
