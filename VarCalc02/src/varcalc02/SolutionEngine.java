package varcalc02;
/**
 * Algorithm to solve the zero of function problem.
 * Finding the value of a variable such that the function will yield zero as result.
 * @author Javier Aranda (javier-aranda.com)
 * CC SA BY
 */
public interface SolutionEngine {
	/**
	 * Given the function, initial variables values, and selected target variable,
	 *  update the selected target variable value so the function will have zero as result.
	 * @param function
	 * @param varValues
	 * @param targetVat
	 * @throws ArithmeticException
	 */
	// TODO Should be able to tell apart if an execution error ocurred (as a scripting or error),
	// or if the algorithm could not find a solution.
	public void calculateZeroOfFunction(Function function, double[] varValues, int targetVat) throws ArithmeticException;
}
