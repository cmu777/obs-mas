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

import parser.visitor.*;
import prism.PrismLangException;

/**
 * Variable declaration details
 */
public class Observation extends ASTElement
{
	// Transition label Name
	protected String name;
	// observation of the label
	protected String observedLbl;
		
	public Observation(String name, String observedLbl)
	{
		setName(name);
		if (observedLbl.compareTo("null") == 0)
			observedLbl = "";
		setObservedLbl(observedLbl);
	}
	
	// Set methods
	
	public void setName(String name)
	{
		this.name = name;
	}	

	public void setObservedLbl(String observedLbl)
	{
		if (observedLbl.compareTo("null") == 0)
			observedLbl = "";
		this.observedLbl = observedLbl;
	}	

	// Get methods

	public String getName()
	{
		return name;
	}

	public String getObservedLbl()
	{
		return observedLbl;
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
		s += name + " -> ";
		s += observedLbl;
		return s;
	}

	/**
	 * Perform a deep copy.
	 */
	@Override
	public ASTElement deepCopy()
	{
		Observation ret = new Observation(getName(), getObservedLbl());
		ret.setPosition(this);
		return ret;
	}
}

// ------------------------------------------------------------------------------
