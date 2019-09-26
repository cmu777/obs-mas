//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford, formerly University of Birmingham)
//	* C Mu
//	
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

package parser.type;

import prism.PrismLangException;

public class TypeAgent extends Type 
{
	private static TypeAgent singleton;
	
	
	static
	{
		singleton = new TypeAgent();
	}
	
	public TypeAgent()
	{		
	}	
	
	public boolean equals(Object o)
	{
		return (o instanceof TypeAgent);
	}
	
	@Override
	public String getTypeString()
	{
		return "agent";
	}
	
	@Override
	public Object defaultValue()
	{
		return new TypeAgent();
	}
	
	public static TypeAgent getInstance()
	{
		return singleton;
	}
	
	@Override
	public boolean canAssign(Type type)
	{
		return (type instanceof TypeAgent);
	}
	
	@Override
	public TypeAgent castValueTo(Object value) throws PrismLangException
	{
		if (value instanceof TypeAgent)
			return (TypeAgent) value;
		else
			throw new PrismLangException("Can't convert " + value.getClass() + " to type " + getTypeString());
	}
}
