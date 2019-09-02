//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford, formerly University of Birmingham)
//	* C Mu
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package parser.ast;

import java.util.*;

import parser.*;
import parser.visitor.*;
import prism.PrismLangException;

/**
 * Class to store a list of updates with associated probabilities (or rates) and transition labels.
 * e.g. 0.5:(s'=1)&amp;(x'=x+1) + 0.5:(s'=2)&amp;(x'=x-1)
 * e.g. 0.5:a:(s'=1)&amp;(x'=x+1) + 0.5:b:(s'=2)&amp;(x'=x-1)
 * e.g. 0.5:ag1:a:(s'=1)&amp;(x'=x+1) + 0.5:ag1:b:(s'=2)&amp;(x'=x-1)
 */
public class Updates extends ASTElement
{
	// Pairs of probabilities/updates
	private ArrayList<Expression> probs;
	private ArrayList<String> tranLbls;
	private ArrayList<String> agents;
	private ArrayList<Update> updates;
	// Parent command
	private Command parent;

	/**
	 * Construct an empty Updates object.
	 */
	public Updates()
	{
		probs = new ArrayList<Expression>();
		tranLbls = new ArrayList<String>();
		agents  = new ArrayList<String>();
		updates = new ArrayList<Update>();
		parent = null;
	}

	// Set methods

	/**
	 * Add a probability (or rate) and update pair (probability/rate can be null, which equates to 1.0).
	 */
	public void addUpdate(Expression p, Update u)
	{
		probs.add(p);
		agents.add(null);
		tranLbls.add(null);
		updates.add(u);
		u.setParent(this);
	}
	
	/**
	 * Add a probability (or rate), agent, transition label and update pair (probability/rate can be null, which equates to 1.0).
	 */
	public void addUpdate(Expression p, String a, String l, Update u)
	{
		probs.add(p);
		agents.add(a);
		tranLbls.add(l);
		updates.add(u);
		u.setParent(this);
	}

	/**
	 * Set the ith update.
	 */
	public void setUpdate(int i, Update u)
	{
		updates.set(i, u);
		u.setParent(this);
	}

	/**
	 * Set the probability (or rate) of the ith update (can be null; denotes default of 1.0)
	 */
	public void setProbability(int i, Expression p)
	{
		// Note: probability can be null, which equates to 1.0
		probs.set(i, p);
	}
	
	/**
	 * Set the transition label of the ith update (can be null; denotes default of 0)
	 */
	public void setTransitionLbl(int i, String l)
	{
		// Note: transition label can be null, which equates to 0
		tranLbls.set(i, l);
	}
	
	/**
	 * Set the agent of the ith update (can be null; denotes default of 0)
	 */
	public void setAgent(int i, String a)
	{
		// Note: agent can be null, which equates to 0
		agents.set(i, a);
	}
	

	/**
	 * Set the parent Command.
	 */
	public void setParent(Command c)
	{
		parent = c;
	}

	// Get methods

	/**
	 * Get the number of updates.
	 */
	public int getNumUpdates()
	{
		return updates.size();
	}

	/**
	 * Get the ith update.
	 */
	public Update getUpdate(int i)
	{
		return updates.get(i);
	}

	/**
	 * Get access to the whole list of the updates
	 */
	public List<Update> getUpdates()
	{
		return updates;
	}

	/**
	 *  Get the probability (or rate) of the ith update (may be null, which should be interpreted as constant 1.0)
	 */
	public Expression getProbability(int i)
	{
		return probs.get(i);
	}
	
	/**
	 *  Get the transition label of the ith update (may be null, which should be interpreted as 0)
	 */
	public String getTransitionLbl(int i)
	{
		return tranLbls.get(i);
	}
	
	/**
	 *  Get the transition labels
	 */
	public ArrayList<String> getTransitionLbls()
	{
		return tranLbls;
	}

	/**
	 *  Get the agent of the ith update (may be null, which should be interpreted as 0)
	 */
	public String getAgent(int i)
	{
		return agents.get(i);
	}
	
	/**
	 *  Get the transition labels
	 */
	public ArrayList<String> getAgents()
	{
		return agents;
	}
	
	/**
	 * Evaluate the probability (or rate) of the ith update, in the context of a state
	 */
	public double getProbabilityInState(int i, State state) throws PrismLangException
	{
		Expression p = probs.get(i);
		return (p == null) ? 1.0 : p.evaluateDouble(state);
	}

	/**
	 * Evaluate the translate label of the ith update, in the context of a state
	 */
	public String getTransLabelInState(int i, State state) throws PrismLangException
	{
		String l = tranLbls.get(i);
		return (l == null) ? "" : l;
	}
	
	public String getAgentInState(int i, State state) {
		String a = agents.get(i);
		return (a == null) ? "" : a;
	}

	
	/**
	 * Get the Command to which this Updates object belongs.
	 */
	public Command getParent()
	{
		return parent;
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
	public String toString()
	{
		String s = "";
		int i, n;

		n = getNumUpdates();
		for (i = 0; i < n - 1; i++) {
			if (getProbability(i) != null)
				s += getProbability(i) + ".";
			if (getAgent(i) != null)
				s += getAgent(i) + ".";
			if (getTransitionLbl(i) != null)
				s += getTransitionLbl(i) + ":";
			s += getUpdate(i) + " + ";
		}
		if (getProbability(n - 1) != null)
			s += getProbability(n - 1) + ".";

		if (getAgent(n-1) != null)
			s += getAgent(n-1) + ".";
		
		if (getTransitionLbl(n-1) != null)
			s += getTransitionLbl(n-1) + ":";
		
		s += getUpdate(n - 1);

		return s;
	}

	/**
	 * Perform a deep copy.
	 */
	public ASTElement deepCopy()
	{
		int i, n;
		Expression p;
		String a, l;
		Updates ret = new Updates();
		n = getNumUpdates();
		for (i = 0; i < n; i++) {
			p = getProbability(i);
			if (p != null)
				p = p.deepCopy();
			l = getTransitionLbl(i);
			a = getAgent(i);
			ret.addUpdate(p, a, l, (Update) getUpdate(i).deepCopy());
		}
		ret.setPosition(this);
		return ret;
	}

}

//------------------------------------------------------------------------------
