//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* 
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

import parser.ast.ProbTraceList;
import prism.PrismLangException;

public class TypeProbTraceList extends Type 
{
	private static TypeProbTraceList singleton;
	
	static
	{
		singleton = new TypeProbTraceList();
	}
	
	private TypeProbTraceList()
	{		
	}	
	
	public boolean equals(Object o)
	{
		return (o instanceof TypeDouble);
	}
	
	@Override
	public String getTypeString()
	{
		return "probTraceList";
	}
	
	@Override
	public Object defaultValue()
	{
		return new TypeProbTraceList();
	}
	
	public static TypeProbTraceList getInstance()
	{
		return singleton;
	}
	
	@Override
	public boolean canAssign(Type type)
	{
		return (type instanceof TypeProbTraceList);
	}
	
	@Override
	public ProbTraceList castValueTo(Object value) throws PrismLangException
	{
		if (value instanceof TypeProbTraceList)
			return (ProbTraceList) value;
		else
			throw new PrismLangException("Can't convert " + value.getClass() + " to type " + getTypeString());
	}
}
