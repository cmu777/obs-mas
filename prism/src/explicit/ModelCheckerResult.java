//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford)
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

import parser.ast.ProbTraceList;
import parser.ast.ProbTransLabel;
import strat.Strategy;

/**
 * Class storing some info/data from a call to a model checking or
 * numerical computation method in the explicit engine. 
 */
public class ModelCheckerResult
{
	// Solution vector
	public double[] soln = null;
	public ProbTraceList[] solnWithLabels = null;
	// Deadlocks trace vector
	public ProbTraceList[] solnDeadlocks = null;
	// Solution vector from previous iteration
	public double[] lastSoln = null;
	public ProbTraceList[] lastSolnWithLabels = null;
	// Iterations performed
	public int numIters = 0;
	// Total time taken (secs)
	public double timeTaken = 0.0;
	// Time taken for any precomputation (secs)
	public double timePre = 0.0;
	// Time taken for Prob0-type precomputation (secs)
	public double timeProb0 = 0.0;
	// Strategy
	public Strategy strat = null;

	/**
	 * Clear all stored data, including setting of array pointers to null
	 * (which may be helpful for garbage collection purposes).
	 */
	public void clear()
	{
		soln = lastSoln = null;
		solnWithLabels = lastSolnWithLabels = solnDeadlocks = null;
		numIters = 0;
		timeTaken = timePre = timeProb0 = 0.0;
	}
	
	public void createDoubleSolnFromLabeledSoln()
	{
		if (solnWithLabels != null) {
			soln = new double[solnWithLabels.length];
			for (int i=0; i<solnWithLabels.length; i++) {
				if (solnWithLabels[i] != null)
					soln[i] = solnWithLabels[i].getProb();
			}
		}
	}
	
	public void createDoubleLastSolnFromLabeledLastSoln()
	{
		if (lastSolnWithLabels != null) {
			lastSoln = new double[lastSolnWithLabels.length];
			for (int i=0; i<lastSolnWithLabels.length; i++) {
				lastSoln[i] = lastSolnWithLabels[i].getProb();
			}
		}
	}
	
	public void createLabeledSolnFromDoubleSoln()
	{	
		if (soln != null) {
			solnWithLabels = new ProbTraceList[soln.length];
			for (int i=0; i<soln.length; i++) {
				ArrayList<ProbTransLabel> traceList = new ArrayList<ProbTransLabel>();
				solnWithLabels[i] = new ProbTraceList(traceList, soln[i]);

			}
		}
	}
	
	public void createLabeledLastSolnFromDoubleLastSoln()
	{	
		if (lastSoln != null) {
			lastSolnWithLabels = new ProbTraceList[lastSoln.length];
			for (int i=0; i<lastSoln.length; i++) {
				ArrayList<ProbTransLabel> traceList = new ArrayList<ProbTransLabel>();
				lastSolnWithLabels[i] = new ProbTraceList(traceList, lastSoln[i]);
			}
		}
	}
	
}
