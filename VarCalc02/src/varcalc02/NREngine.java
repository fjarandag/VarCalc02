package varcalc02;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Engine using Newton-Raphson method for zero of function approximation.
 * https://en.wikipedia.org/wiki/Newton's_method
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
public class NREngine implements SolutionEngine {

	@Override
	public void calculateZeroOfFunction(Function function, double[] varValues,
			int targetVar) throws ArithmeticException {
		double targetValue = function.getVariables()[targetVar].getInitialCalcValue();
		varValues[targetVar] = targetValue;
		
		HashMap<String,Object> context = new HashMap<>();
		
		// TT-REDESIGN epsilon-issues
		// - Might have different optimal values for each variable and function (even depend on rest of variables)
		// - Might have different optimal values for result check and slope calculation.
		// (and non-optimal might even lead to error).
		int stepCount = 0;
		boolean precisionAchieved = false;
		
		// approximate until required precision is achieved, or trying for too long
		while (!precisionAchieved) {
			double resAtTarget = function.calculate(varValues, context);
			// TT-LOW Might detect if converging or running in circles (or into infinity).
			if (stepCount >= 20) {
				throw new ArithmeticException("Not reaching zero x=" + targetValue + ";f(x)=" + resAtTarget);
			}

			// Extrapolate next target value
			// slope : f'(x) calculated as ( f(x+ε) - f(x) ) / ε
			double epsilon = VarCalc02.epsilon(function, varValues, targetVar);
			varValues[targetVar] = targetValue + epsilon;
			double resAtTarget2 = function.calculate(varValues, context);
			double slope = (resAtTarget2 - resAtTarget) / epsilon;
			
			// refine x value (targetValue) for next iteration
			double nextValue = targetValue - resAtTarget / slope;
			
			if (VarCalc02.DEBUG) {
				VarCalc02.log("NR-aproximation;step %d;x %f;f %e;e %e;f' %e;next %g", 
						stepCount, targetValue, resAtTarget, epsilon, slope, nextValue);
			}
			// Update values
			precisionAchieved = Math.abs(nextValue - targetValue) < epsilon; 

			varValues[targetVar] = targetValue = nextValue;
			stepCount++;
		}
		if (VarCalc02.DEBUG) {
			VarCalc02.log("NR-aproximation result: " + Arrays.toString(varValues));
		}
	}

}
