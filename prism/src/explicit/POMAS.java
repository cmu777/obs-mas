//==============================================================================
//	
//	Copyright (c) 2018-
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

package explicit;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import parser.ast.Observation;
import parser.ast.ModulesFile;
import parser.ast.ProbTraceList;
import parser.ast.ProbTransLabel;

/**
 * Interface for classes that provide (read) access to an explicit-state POMDP.
 */
public interface POMAS extends DTMC
{
	/**
	 * Get the associated modulesFile.
	 */
	public ModulesFile getModulesFile();

	/**
	 * Get access to a list of observations (optionally stored).
	 */
	//public List<Observation> getObservationsList();

	/**
	 * Get the total number of observations over all states.
	 */
	//public int getNumObservations();

	void setProbability(int i, int j, ProbTransLabel probLabel);

	void addToProbability(int i, int j, ProbTransLabel probLabel);

	//public double mvLabeledMultGS(ProbTraceList[] soln, BitSet unknown, boolean b, boolean c);

	//public void mvLabeledMult(ProbTraceList[] soln, ProbTraceList[] soln2, BitSet unknown, boolean b);

	//public ProbTraceList probLabelSingle(int s, ProbTraceList[] soln);

	/**
	 * computeProbLabeledTrace compute the probabilistic trace from a state to a target,
	 * i.e. a probabilistic labeled transition trace,
	 * reach a state {@code target}, from a state {@code s}.
	 * @param s The start state of the trace to compute
	 * @param tl The probabilistic trace (a set of transition labels) to the target
	 * @param subset The set of states can reach the target
	 * @param target The target state
	 * @param myTraces 
	 */
	public ProbTransLabel computeProbLabeledTrace(int s, ProbTransLabel tl, BitSet subset, int target, 
			ProbTraceList path, ArrayList<ProbTransLabel>  myTraces);

}
