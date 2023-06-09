//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford, formerly University of Birmingham)
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

package parser.ast;

import parser.EvaluateContext;
import parser.visitor.ASTVisitor;
import prism.PrismLangException;

public class ExpressionTemporal extends Expression
{
	// Operator constants
	public static final int P_X = 1; // Next (for P operator)
	public static final int P_U = 2; // Until (for P operator)
	public static final int P_F = 3; // Future (for P operator)
	public static final int P_G = 4; // Globally (for P operator)
	public static final int P_W = 5; // Weak until (for P operator)
	public static final int P_R = 6; // Release (for P operator)
	public static final int P_O = 7; // Opacity (for P operator)
	public static final int R_C = 11; // Cumulative (for R operator)
	public static final int R_I = 12; // Instantaneous (for R operator)
	public static final int R_F = 13; // Reachability (for R operator) // DEPRECATED: Use P_F
	public static final int R_S = 14; // Steady-state (for R operator)
	// Operator symbols
	public static final String opSymbols[] = { "", "X", "U", "F", "G", "W", "R", 
		"O", "", "", "", "C", "I", "F", "S" };

	// Operator
	protected int op = 0;
	// Up to two operands (either may be null)
	protected Expression operand1 = null; // LHS of operator
	protected Expression operand2 = null; // RHS of operator
	protected static String observer = null;
	// Optional (time) bounds
	protected Expression lBound = null; // None if null, i.e. zero
	protected Expression uBound = null; // None if null, i.e. infinity
	// Strictness of (time) bounds
	protected boolean lBoundStrict = false; // true: >, false: >= 
	protected boolean uBoundStrict = false; // true: <, false: <=
	// Display as =T rather than [T,T] ?
	protected boolean equals = false;
	protected static boolean isOpacity = false;

	// Constructors

	public ExpressionTemporal()
	{
	}

	public ExpressionTemporal(int op, Expression operand1, Expression operand2)
	{
		this.op = op;
		this.operand1 = operand1;
		this.operand2 = operand2;
	}

	public ExpressionTemporal(int op, Expression operand1, Expression operand2, String obs)
	{
		this.op = op;
		this.operand1 = operand1;
		this.operand2 = operand2;
		this.observer = obs;
	}
	
	// Set methods
	
	public void setOpacity()
	{
		isOpacity = true;
	}
	
	public void setObserver(String obs)
	{
		observer  = obs;
	}

	public void setOperator(int i)
	{
		op = i;
	}

	public void setOperand1(Expression e1)
	{
		operand1 = e1;
	}

	public void setOperand2(Expression e2)
	{
		operand2 = e2;
	}

	/**
	 * Set lower time bound to be of form &gt;= e
	 * (null denotes no lower bound, i.e. zero)
	 */
	public void setLowerBound(Expression e)
	{
		setLowerBound(e, false);
	}

	/**
	 * Set lower time bound to be of form &gt;= e or &gt; e
	 * (null denotes no lower bound, i.e. zero)
	 */
	public void setLowerBound(Expression e, boolean strict)
	{
		lBound = e;
		lBoundStrict = strict;
	}

	/**
	 * Set upper time bound to be of form &lt;= e
	 * (null denotes no upper bound, i.e. infinity)
	 */
	public void setUpperBound(Expression e)
	{
		setUpperBound(e, false);
	}

	/**
	 * Set upper time bound to be of form &lt;= e or &lt; e
	 * (null denotes no upper bound, i.e. infinity)
	 */
	public void setUpperBound(Expression e, boolean strict)
	{
		uBound = e;
		uBoundStrict = strict;
	}

	/**
	 * Set both lower/upper time bound to e, i.e. "=e".
	 */
	public void setEqualBounds(Expression e)
	{
		lBound = e;
		lBoundStrict = false;
		uBound = e;
		uBoundStrict = false;
		equals = true;
	}

	// Get methods

	public int getOperator()
	{
		return op;
	}

	public String getObserver()
	{
		return observer;
	}
	
	public String getOperatorSymbol()
	{
		return opSymbols[op];
	}

	public Expression getOperand1()
	{
		return operand1;
	}

	public Expression getOperand2()
	{
		return operand2;
	}

	public int getNumOperands()
	{
		if (operand1 == null)
			return 0;
		else
			return (operand2 == null) ? 1 : 2;
	}

	public boolean hasBounds()
	{
		return lBound != null || uBound != null;
	}
	
	public boolean isOpacity()
	{
		return isOpacity;
	}

	public Expression getLowerBound()
	{
		return lBound;
	}

	public boolean lowerBoundIsStrict()
	{
		return lBoundStrict;
	}

	public Expression getUpperBound()
	{
		return uBound;
	}

	public boolean upperBoundIsStrict()
	{
		return uBoundStrict;
	}

	/**
	 * Returns true if lower/upper bound are equal and should be displayed as =T 
	 */
	public boolean getEquals()
	{
		return equals;
	}

	// Methods required for Expression:

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@Override
	public boolean isProposition()
	{
		return false;
	}
	
	@Override
	public Object evaluate(EvaluateContext ec) throws PrismLangException
	{
		throw new PrismLangException("Cannot evaluate a temporal operator without a path");
	}

	@Override
	public boolean returnsSingleValue()
	{
		return false;
	}

	// Methods required for ASTElement:

	@Override
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}

	@Override
	public Expression deepCopy()
	{
		ExpressionTemporal expr = new ExpressionTemporal();
		expr.setOperator(op);
		if (observer != null)
			expr.setObserver(observer);
		if (operand1 != null)
			expr.setOperand1(operand1.deepCopy());
		if (operand2 != null)
			expr.setOperand2(operand2.deepCopy());
		expr.setLowerBound(lBound == null ? null : lBound.deepCopy(), lBoundStrict);
		expr.setUpperBound(uBound == null ? null : uBound.deepCopy(), uBoundStrict);
		expr.equals = equals;
		expr.setType(type);
		expr.setPosition(this);
		return expr;
	}

	// Standard methods

	@Override
	public String toString()
	{
		String s = "";
		//if ((observer !=null) & (op==P_O))
		//	s += "<" + observer + "> ";
		if (operand1 != null)
			s += operand1 + " ";
		s += opSymbols[op];
		if (lBound == null) {
			if (uBound != null) {
				if (op != R_I)
					s += "<" + (uBoundStrict ? "" : "=") + uBound;
				else
					s += "=" + uBound;
			}
		} else {
			if (uBound == null) {
				s += ">" + (lBoundStrict ? "" : "=") + lBound;
			} else {
				if (equals)
					s += "=" + lBound;
				else
					s += "[" + lBound + "," + uBound + "]";
			}
		}
		if (operand2 != null)
			s += " " + operand2;
		return s;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (equals ? 1231 : 1237);
		result = prime * result + ((lBound == null) ? 0 : lBound.hashCode());
		result = prime * result + (lBoundStrict ? 1231 : 1237);
		result = prime * result + op;
		result = prime * result + ((operand1 == null) ? 0 : operand1.hashCode());
		result = prime * result + ((operand2 == null) ? 0 : operand2.hashCode());
		result = prime * result + ((uBound == null) ? 0 : uBound.hashCode());
		result = prime * result + (uBoundStrict ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExpressionTemporal other = (ExpressionTemporal) obj;
		if (equals != other.equals)
			return false;
		if (lBound == null) {
			if (other.lBound != null)
				return false;
		} else if (!lBound.equals(other.lBound))
			return false;
		if (lBoundStrict != other.lBoundStrict)
			return false;
		if (op != other.op)
			return false;
		if (observer == null) {
			if (other.observer != null)
				return false;
		} else if (!observer.equals(other.observer))
			return false;
		if (operand1 == null) {
			if (other.operand1 != null)
				return false;
		} else if (!operand1.equals(other.operand1))
			return false;
		if (operand2 == null) {
			if (other.operand2 != null)
				return false;
		} else if (!operand2.equals(other.operand2))
			return false;
		if (uBound == null) {
			if (other.uBound != null)
				return false;
		} else if (!uBound.equals(other.uBound))
			return false;
		if (uBoundStrict != other.uBoundStrict)
			return false;
		return true;
	}

	// Other useful methods

	/**
	 * Convert (P operator) path formula to untils, using standard equivalences.
	 */
	public Expression convertToUntilForm() throws PrismLangException
	{
		Expression op1, op2;
		String observer;
		ExpressionTemporal exprTemp = null;
		switch (op) {
		case P_X:
			return this;
		case P_U:
			return this;
		case P_O:
			// O ag \psi
			int op = ((ExpressionTemporal) operand2).getOperator();
			op1 = operand1.deepCopy();
			observer = op1.toString();
			observer = observer.substring(1, observer.length()-1);
			op2 = ((ExpressionTemporal) operand2).getOperand2();
			exprTemp = new ExpressionTemporal(op, op1, op2, observer);
			exprTemp.setOpacity();
			//System.out.println("+++++ convertToUntilForm P_O ++++ " + exprTemp.getObserver()
			//		+ " +++ opacity is "  + exprTemp.isOpacity());
			return exprTemp.convertToUntilForm();
		case P_F:
			// F a == true U a
			op1 = Expression.True();
			exprTemp = new ExpressionTemporal(P_U, op1, operand2);
			exprTemp.setLowerBound(lBound, lBoundStrict);
			exprTemp.setUpperBound(uBound, uBoundStrict);
			exprTemp.equals = equals;
			return exprTemp;
		case P_G:
			// G a == !(true U !a)
			op1 = Expression.True();
			op2 = Expression.Not(operand2);
			exprTemp = new ExpressionTemporal(P_U, op1, op2);
			exprTemp.setLowerBound(lBound, lBoundStrict);
			exprTemp.setUpperBound(uBound, uBoundStrict);
			exprTemp.equals = equals;
			return Expression.Not(exprTemp);
		case P_W:
			// a W b == !(a&!b U !a&!b)
			op1 = Expression.And(operand1, Expression.Not(operand2));
			op2 = Expression.And(Expression.Not(operand1), Expression.Not(operand2));
			exprTemp = new ExpressionTemporal(P_U, op1, op2);
			exprTemp.setLowerBound(lBound, lBoundStrict);
			exprTemp.setUpperBound(uBound, uBoundStrict);
			exprTemp.equals = equals;
			return Expression.Not(exprTemp);
		case P_R:
			// a R b == !(!a U !b)
			op1 = Expression.Not(operand1);
			op2 = Expression.Not(operand2);
			exprTemp = new ExpressionTemporal(P_U, op1, op2);
			exprTemp.setLowerBound(lBound, lBoundStrict);
			exprTemp.setUpperBound(uBound, uBoundStrict);
			exprTemp.equals = equals;
			return Expression.Not(exprTemp);
		}
		throw new PrismLangException("Cannot convert " + getOperatorSymbol() + " to until form");
	}
}

//------------------------------------------------------------------------------
