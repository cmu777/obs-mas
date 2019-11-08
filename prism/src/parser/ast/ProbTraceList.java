
package parser.ast;

import java.util.ArrayList;

import parser.visitor.*;
import prism.PrismLangException;

/**
 * Variable declaration details
 */
public class ProbTraceList extends ASTElement
{
	// Probability of the trace list
	protected double prob;
	// trace list
	protected ArrayList<ProbTransLabel> traceList;
	// all the states in the trace
	protected ArrayList<Integer> states;
	// if the state is in a cycle
	protected boolean[] withCycle;
	
	public ProbTraceList(int n)
	{
		prob = 0.0;
		traceList = new ArrayList<ProbTransLabel>();
		states = new ArrayList<Integer>();
		withCycle = new boolean[n];
	}
		
	public ProbTraceList(ArrayList<ProbTransLabel> traceList, double prob)
	{
		setProb(prob);
		setTraceList(traceList);
	}
	
	public ProbTraceList(ArrayList<ProbTransLabel> traceList)
	{
		double p = 0;
		for (int i=0; i<traceList.size(); i++) {
			p += traceList.get(i).getValue();
		}
		setTraceList(traceList);
		setProb(p);
	}

	/*public ProbTraceList(ArrayList<ProbTransLabel> traceList, ArrayList<ProbTransLabel> observationList, double prob)
	{
		setProb(prob);
		setTraceList(traceList);
		setObservationList(observationList);
	}*/
	
	public void addStates(int s)
	{
		this.states.add(s);
	}
	
	public ArrayList<Integer> getStates()
	{
		return this.states;
	}
	
	public void addTransition(double prob, String agent, String observer, String label, String obs)
	{
		ProbTransLabel trace = new ProbTransLabel(agent, observer, label, obs, prob);
		this.traceList.add(trace);
	}
	
	public void removeAll()
	{
		prob = 0.0;
		for (int i=0; i<traceList.size(); i++)
			this.traceList.remove(i);
		for (int i=0; i<states.size(); i++)
			this.states.remove(i);
		for (int i=0; i<withCycle.length; i++) 
			this.withCycle[i] = false;
	}
	
	public void removeTransition(int i)
	{
		this.traceList.remove(i);
	}
	
	public ProbTransLabel getTransition(int i)
	{
		return this.traceList.get(i);
	}
	
	public void setTransition(ProbTransLabel ptl, int i)
	{
		this.traceList.set(i, ptl);
	}
	

	public boolean getWithCycle(int s)
	{
		return withCycle[s];
	}
	
	public void setCycle(int s)
	{
		this.withCycle[s] = true;
	}
	
	// Set methods
	
	public void setTraceList(ArrayList<ProbTransLabel> tl)
	{
		this.traceList = tl;
	}	
	
	public void addTraceList(ArrayList<ProbTransLabel> tl)
	{
		traceList.addAll(tl);
	}	

	public void removeTraceList(ArrayList<ProbTransLabel> tl)
	{
		traceList.removeAll(tl);
	}
	
	public void setTraceList(int i, ProbTransLabel l)
	{
		this.traceList.set(i, l);
	}

	public void setProb(double p)
	{
		this.prob = p;
	}
	
	public void computeProb()
	{
		for (int i=0; i<traceList.size(); i++) {
			this.prob += traceList.get(i).getValue();
		}
	}


	// Get methods

	public ArrayList<ProbTransLabel> getTraceList()
	{
		return traceList;
	}

	public double getProb()
	{
		return prob;
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
		s += prob + ".{\n";
		for (int i=0; i<traceList.size(); i++) {
			s += traceList.get(i).getValue() + ":";
			//s += traceList.get(i).getAgent();
			s += traceList.get(i).getAction();
			s += "->" + traceList.get(i).getObservation();
			if (i < traceList.size()-1) s += ",\n";
		}
		s += "\n}";
		return s;
	}

	/**
	 * Perform a deep copy.
	 */
	@Override
	public ASTElement deepCopy()
	{
		ProbTraceList ret = new ProbTraceList(getTraceList(), getProb());
		ret.setPosition(this);
		return ret;
	}
}

// ------------------------------------------------------------------------------
