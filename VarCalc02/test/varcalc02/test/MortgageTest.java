package varcalc02.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import varcalc02.Function;
import varcalc02.NREngine;
import varcalc02.SolutionEngine;
import varcalc02.VarCalc02;

@RunWith(Parameterized.class)
public class MortgageTest {
	private static Function scriptFunction = VarCalc02.createMortgageScriptFunction();
	private static Function inlineFunction = VarCalc02.createMortgageInlineFunction();
	
	private static double[] TEST_200K_RLOW = new double[]{843.42, 200000, 240, 0.0001};// APR 0.12%
	private static double[] TEST_200K_RZERO = new double[]{833.333, 200000, 240, 0.0};
	private static double[] TEST_200K_RNEG = new double[]{792.09, 200000, 240, -4.177584E-4};// -0.5%
	private static double[] TEST_200K_APR5 = new double[]{1307.67, 200000, 240, 0.004065847};// 5%
	
	private static SolutionEngine ENGINE_NR = new NREngine();
	

	@Parameters (name="{index}-{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        	{"script-200k-0.12%", scriptFunction, TEST_200K_RLOW}, 
        	{"script-200k-0%", scriptFunction, TEST_200K_RZERO},
        	{"script-200k--0.5%", scriptFunction, TEST_200K_RNEG}, 
        	{"script-200k-5%", scriptFunction, TEST_200K_APR5}, 
        	{"inline-200k-0.12%", inlineFunction, TEST_200K_RLOW}, 
        	{"inline-200k-0%", inlineFunction, TEST_200K_RZERO},
        	{"inline-200k--0.5%", inlineFunction, TEST_200K_RNEG}, 
        	{"inline-200k-5%", inlineFunction, TEST_200K_APR5} 
           });
    }

    @Parameter
    public String testName;
    @Parameter (value=1)
    public Function function;
    @Parameter (value=2)
    public double[] variablesValues;
    
	@Test
	public void testValues () {
		double[] calculateValues = variablesValues.clone();
		// 4 variables. Check every direction
		for (int i = 0; i < 4; i++) {
			ENGINE_NR.calculateZeroOfFunction(function, calculateValues, i);
			double targetValue = variablesValues[i];
			// Allow .1% error. Note targetValue might be negative or zero.
			// Correct test cases seen with an error of about .5e-9
			double delta = Math.max(Math.abs(targetValue*.001), 1e-7);
			Assert.assertEquals("at " + i, targetValue, calculateValues[i], delta);
			calculateValues[i] = targetValue; // Even if calculated value is close, might not be same
		}
	}
	
}
