
package parser.ast;

import parser.visitor.*;
import prism.PrismLangException;

/**
 * Variable declaration details
 */
public class ProbTransLabel extends ASTElement
{
	// Probability of the transition label
	protected double value = 0.0;
	// agent of the transition label (action)
	protected String agent = "";
	protected String observer = "";
	// name of the transition label (action)
	protected String action = "";
	protected String observation = "";
	
	public ProbTransLabel(String a, String l, double v)
	{
		setAgent(a);
		setAction(l);
		setValue(v);
	}
	
	public ProbTransLabel(String a, String observer, String l, String o, double v)
	{
		setAgent(a);
		setObserver(observer);
		setAction(l);
		setValue(v);
		setObservation(o);
	}
	
	// Set methods
	
	public void setAgent(String a)
	{
		this.agent = a;
	}	
	
	public void setAction(String l)
	{
		this.action = l;
	}	
	
	public void setValue(double v)
	{
		this.value = v;
	}	

	public void setObserver(String o)
	{
		this.observer = o;
	}
	
	public void setObservation(String o)
	{
		this.observation = o;
	}

	// Get methods

	public String getAgent()
	{
		return agent;
	}
	
	public String getAction()
	{
		return action;
	}

	public double getValue()
	{
		return value;
	}	
	
	public String getObserver()
	{
		return observer;
	}
	
	public String getObservation()
	{
		return observation;
	}

	@Override
    public boolean equals(Object obj) {

        try {
        		ProbTransLabel ptl  = (ProbTransLabel) obj;
            return (agent.equals(ptl.getAgent()) && action.equals(ptl.getAction()));
        }
        catch (Exception e)
        {
            return false;
        }

    }

	
	// Methods required for ASTElement:
	
	/**
	 * Visitor method.
	 */
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}

	/**
	 * Convert to string.
	 */
	@Override
	public String toString()
	{
		String s  = "";
		s += value + ":";
		s += agent + ".";
		s += observer;
		s += action;
		s += "->" + observation;
		return s;
	}

	/**
	 * Perform a deep copy.
	 */
	@Override
	public ASTElement deepCopy()
	{
		ProbTransLabel ret = new ProbTransLabel(getAgent(), getObserver(), getAction(), getObservation(), getValue());
		ret.setPosition(this);
		return ret;
	}
}

// ------------------------------------------------------------------------------
