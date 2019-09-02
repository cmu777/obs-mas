package param;


public class Solution {
	private Object solution;
	private String variable;
	private double real;
	private double imaginary;
	private boolean isReal;
	/**
	 * Construct an empty Solution object.
	 */
	public Solution()
	{
		this.variable = null;
		this.real = 0.0;
		this.imaginary = 0.0;
		this.isReal = false;
	}	
	
	/**
	 * Create a Result object based on a result.
	 */
	public Solution(Object soln)
	{
		this();
		setSolution(soln);
	}
	
	/**
	 * Set the result.
	 */
	public void setSolution(Object soln)
	{
		this.solution = soln;
	}
	
	/**
	 * Set the variable name (null denotes absent).
	 */
	public void setVariable(String variable)
	{
		this.variable = variable;
	}

	/**
	 * Set the real part value.
	 */
	public void setReal(double r)
	{
		this.real = r;
	}

	
	/**
	 * Set the imaginary part value.
	 */
	public void setImaginary(double i)
	{
		this.imaginary = i;
	}	
	
	
	public void setIsReal(boolean is) 
	{
		this.isReal = is;
		
	}
	
	/**
	 * Get the solution.
	 */
	public Object getSolution()
	{
		return solution;
	}
	
	/**
	 * Get the variable name (null denotes absent).
	 */
	public String getVariable()
	{
		return this.variable;
	}

	/**
	 * Get the real part value.
	 */
	public double getReal()
	{
		return this.real;
	}
	
	/**
	 * Get the imaginary part value.
	 */
	public double getImaginary()
	{
		return this.imaginary;
	}
	
	public boolean getIsReal() 
	{
		return this.isReal;
		
	}

}
