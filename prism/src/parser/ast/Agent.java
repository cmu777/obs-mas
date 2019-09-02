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

import parser.visitor.*;
import prism.PrismLangException;

public class Agent extends ASTElement
{
	// Agent name
	private String name;
	// Actions and observations
	private ArrayList<Observation> actions;

	// Constructor
	
	public Agent(String n)
	{
		name = n;
		actions = new ArrayList<Observation>();
	}

	// Set methods
	
	public void setName(String n)
	{
		name = n;
	}
	
	public void addAction(Observation a)
	{
		actions.add(a);
	}
	
	public void removeAction(Observation a)
	{
		actions.remove(a);
	}
	
	public void setAction(int i, Observation a)
	{
		actions.set(i, a);
	}
	
	// Get methods
	
	public String getName()
	{
		return name;
	}

	
	public int getNumActions()
	{
		return actions.size();
	}
	
	public Observation getAction(int i)
	{
		return actions.get(i);
	}

	public List<Observation> getObservationsList() {
		// TODO Auto-generated method stub
		return actions;
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
		
		s = s + "agent " + name + "\n\n";
		n = getNumActions();
		for (i = 0; i < n; i++) {
			s = s + "\t" + getAction(i) + ";\n";
		}
		s = s + "\nendagent";
		
		return s;
	}
	
	/**
	 * Perform a deep copy.
	 */
	public ASTElement deepCopy()
	{
		int i, n;
		Agent ret = new Agent(name);
		n = getNumActions();
		for (i = 0; i < n; i++) {
			ret.addAction((Observation)getAction(i).deepCopy());
		}
		ret.setPosition(this);
		return ret;
	}

}

//------------------------------------------------------------------------------
