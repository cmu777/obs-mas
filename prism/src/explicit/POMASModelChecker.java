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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import parser.VarList;
import parser.ast.Declaration;
import parser.ast.DeclarationIntUnbounded;
import parser.ast.Expression;
import parser.ast.ProbTraceList;
import parser.ast.ProbTransLabel;
import prism.Prism;
import prism.PrismComponent;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismNotSupportedException;
import prism.PrismUtils;
import acceptance.AcceptanceReach;
import acceptance.AcceptanceType;
import common.IterableBitSet;
import common.IterableStateSet;
import explicit.rewards.MCRewards;
import explicit.rewards.Rewards;

/**
 * Explicit-state model checker for labeled discrete-time Markov chains (POMASs).
 */
public class POMASModelChecker extends ProbModelChecker
{
	/**
	 * Create a new DTMCModelChecker, inherit basic state from parent (unless null).
	 */
	public POMASModelChecker(PrismComponent parent) throws PrismException
	{
		super(parent);
	}

	// Model checking functions

	@Override
	protected StateValues checkProbPathFormulaLTL(Model model, Expression expr, boolean qual, MinMax minMax, BitSet statesOfInterest) throws PrismException
	{
		LTLModelChecker mcLtl;
		StateValues probsProduct, probs;
		LTLModelChecker.LTLProduct<POMAS> product;
		POMASModelChecker mcProduct;

		// For LTL model checking routines
		mcLtl = new LTLModelChecker(this);

		// Build product of Markov chain and automaton
		AcceptanceType[] allowedAcceptance = {
				AcceptanceType.RABIN,
				AcceptanceType.REACH,
				AcceptanceType.GENERIC
		};
		product = mcLtl.constructProductMC(this, (POMAS)model, expr, statesOfInterest, allowedAcceptance);

		// Output product, if required
		if (getExportProductTrans()) {
				mainLog.println("\nExporting product transition matrix to file \"" + getExportProductTransFilename() + "\"...");
				product.getProductModel().exportToPrismExplicitTra(getExportProductTransFilename());
		}
		if (getExportProductStates()) {
			mainLog.println("\nExporting product state space to file \"" + getExportProductStatesFilename() + "\"...");
			PrismFileLog out = new PrismFileLog(getExportProductStatesFilename());
			VarList newVarList = (VarList) modulesFile.createVarList().clone();
			String daVar = "_da";
			while (newVarList.getIndex(daVar) != -1) {
				daVar = "_" + daVar;
			}
			newVarList.addVar(0, new Declaration(daVar, new DeclarationIntUnbounded()), 1, null);
			product.getProductModel().exportStates(Prism.EXPORT_PLAIN, newVarList, out);
			out.close();
		}
		
		// Find accepting states + compute reachability probabilities
		BitSet acc;
		if (product.getAcceptance() instanceof AcceptanceReach) {
			mainLog.println("\nSkipping BSCC computation since acceptance is defined via goal states...");
			acc = ((AcceptanceReach)product.getAcceptance()).getGoalStates();
		} else {
			mainLog.println("\nFinding accepting BSCCs...");
			acc = mcLtl.findAcceptingBSCCs(product.getProductModel(), product.getAcceptance());
		}
		mainLog.println("\nComputing reachability probabilities...");
		mcProduct = new POMASModelChecker(this);
		mcProduct.inheritSettings(this);
		probsProduct = StateValues.createFromDoubleArray(mcProduct.computeReachProbs(product.getProductModel(), acc).soln, product.getProductModel());

		// Mapping probabilities in the original model
		probs = product.projectToOriginalModel(probsProduct);
		probsProduct.clear();

		return probs;
	}

	/**
	 * Compute rewards for a co-safe LTL reward operator.
	 */
	protected StateValues checkRewardCoSafeLTL(Model model, Rewards modelRewards, Expression expr, MinMax minMax, BitSet statesOfInterest) throws PrismException
	{
		LTLModelChecker mcLtl;
		MCRewards productRewards;
		StateValues rewardsProduct, rewards;
		POMASModelChecker mcProduct;
		LTLModelChecker.LTLProduct<POMAS> product;

		// For LTL model checking routines
		mcLtl = new LTLModelChecker(this);

		// Build product of Markov chain and automaton
		AcceptanceType[] allowedAcceptance = {
				AcceptanceType.RABIN,
				AcceptanceType.REACH
		};
		product = mcLtl.constructProductMC(this, (POMAS)model, expr, statesOfInterest, allowedAcceptance);
		
		// Adapt reward info to product model
		productRewards = ((MCRewards) modelRewards).liftFromModel(product);
		
		// Output product, if required
		if (getExportProductTrans()) {
				mainLog.println("\nExporting product transition matrix to file \"" + getExportProductTransFilename() + "\"...");
				product.getProductModel().exportToPrismExplicitTra(getExportProductTransFilename());
		}
		if (getExportProductStates()) {
			mainLog.println("\nExporting product state space to file \"" + getExportProductStatesFilename() + "\"...");
			PrismFileLog out = new PrismFileLog(getExportProductStatesFilename());
			VarList newVarList = (VarList) modulesFile.createVarList().clone();
			String daVar = "_da";
			while (newVarList.getIndex(daVar) != -1) {
				daVar = "_" + daVar;
			}
			newVarList.addVar(0, new Declaration(daVar, new DeclarationIntUnbounded()), 1, null);
			product.getProductModel().exportStates(Prism.EXPORT_PLAIN, newVarList, out);
			out.close();
		}
		
		// Find accepting states + compute reachability rewards
		BitSet acc;
		if (product.getAcceptance() instanceof AcceptanceReach) {
			mainLog.println("\nSkipping BSCC computation since acceptance is defined via goal states...");
			acc = ((AcceptanceReach)product.getAcceptance()).getGoalStates();
		} else {
			mainLog.println("\nFinding accepting BSCCs...");
			acc = mcLtl.findAcceptingBSCCs(product.getProductModel(), product.getAcceptance());
		}
		mainLog.println("\nComputing reachability probabilities...");
		mcProduct = new POMASModelChecker(this);
		mcProduct.inheritSettings(this);
		rewardsProduct = StateValues.createFromDoubleArray(mcProduct.computeReachRewards(product.getProductModel(), productRewards, acc).soln, product.getProductModel());
		
		// Mapping rewards in the original model
		rewards = product.projectToOriginalModel(rewardsProduct);
		rewardsProduct.clear();
		
		return rewards;
	}
	
	public ModelCheckerResult computeInstantaneousRewards(POMAS pomas, MCRewards mcRewards, double t) throws PrismException
	{
		ModelCheckerResult res = null;
		int i, n, iters;
		double soln[], soln2[], tmpsoln[];
		long timer;
		int right = (int) t;

		// Store num states
		n = pomas.getNumStates();

		// Start backwards transient computation
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting backwards instantaneous rewards computation...");

		// Create solution vector(s)
		soln = new double[n];
		soln2 = new double[n];

		// Initialise solution vectors.
		for (i = 0; i < n; i++)
			soln[i] = mcRewards.getStateReward(i);

		// Start iterations
		for (iters = 0; iters < right; iters++) {
			// Matrix-vector multiply
			pomas.mvMult(soln, soln2, null, false);
			// Swap vectors for next iter
			tmpsoln = soln;
			soln = soln2;
			soln2 = tmpsoln;
		}

		// Finished backwards transient computation
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Backwards transient instantaneous rewards computation");
		mainLog.println(" took " + iters + " iters and " + timer / 1000.0 + " seconds.");

		// Return results
		res = new ModelCheckerResult();
		res.soln = soln;
		res.lastSoln = soln2;
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		res.timePre = 0.0;
		return res;
	}

	public ModelCheckerResult computeCumulativeRewards(POMAS pomas, MCRewards mcRewards, double t) throws PrismException
	{
		ModelCheckerResult res = null;
		int i, n, iters;
		double soln[], soln2[], tmpsoln[];
		long timer;
		int right = (int) t;

		// Store num states
		n = pomas.getNumStates();

		// Start backwards transient computation
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting backwards cumulative rewards computation...");

		// Create solution vector(s)
		soln = new double[n];
		soln2 = new double[n];

		// Start iterations
		for (iters = 0; iters < right; iters++) {
			// Matrix-vector multiply plus adding rewards
			pomas.mvMult(soln, soln2, null, false);
			for (i = 0; i < n; i++) {
				soln2[i] += mcRewards.getStateReward(i);
			}
			// Swap vectors for next iter
			tmpsoln = soln;
			soln = soln2;
			soln2 = tmpsoln;
		}

		// Finished backwards transient computation
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Backwards cumulative rewards computation");
		mainLog.println(" took " + iters + " iters and " + timer / 1000.0 + " seconds.");

		// Return results
		res = new ModelCheckerResult();
		res.soln = soln;
		res.lastSoln = soln2;
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		res.timePre = 0.0;
		return res;
	}

	public ModelCheckerResult computeTotalRewards(POMAS pomas, MCRewards mcRewards) throws PrismException
	{
		ModelCheckerResult res = null;
		int n, numBSCCs = 0;
		long timer;

		// Switch to a supported method, if necessary
		if (!(linEqMethod == LinEqMethod.POWER)) {
			linEqMethod = LinEqMethod.POWER;
			mainLog.printWarning("Switching to linear equation solution method \"" + linEqMethod.fullName() + "\"");
		}

		// Store num states
		n = pomas.getNumStates();

		// Start total rewards computation
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting total reward computation...");

		// Compute bottom strongly connected components (BSCCs)
		SCCComputer sccComputer = SCCComputer.createSCCComputer(this, pomas);
		sccComputer.computeBSCCs();
		List<BitSet> bsccs = sccComputer.getBSCCs();
		numBSCCs = bsccs.size();

		// Find BSCCs with non-zero reward
		BitSet bsccsNonZero = new BitSet();
		for (int b = 0; b < numBSCCs; b++) {
			BitSet bscc = bsccs.get(b);
			for (int i = bscc.nextSetBit(0); i >= 0; i = bscc.nextSetBit(i + 1)) {
				if (mcRewards.getStateReward(i) > 0) {
					bsccsNonZero.or(bscc);
					break;
				}
			}
		}
		mainLog.print("States in non-zero reward BSCCs: " + bsccsNonZero.cardinality());
		
		// Find states with infinite reward (those reach a non-zero reward BSCC with prob > 0)
		BitSet inf = prob0(pomas, null, bsccsNonZero);
		inf.flip(0, n);
		int numInf = inf.cardinality();
		mainLog.println(", inf=" + numInf + ", maybe=" + (n - numInf));
		
		// Compute rewards
		// (do this using the functions for "reward reachability" properties but with no targets)
		switch (linEqMethod) {
		case POWER:
			res = computeReachRewardsValIter(pomas, mcRewards, new BitSet(), inf, null, null);
			break;
		default:
			throw new PrismException("Unknown linear equation solution method " + linEqMethod.fullName());
		}

		// Finished total reward computation
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Total reward computation");
		mainLog.println(" took " + timer / 1000.0 + " seconds.");

		// Return results
		return res;
	}

	// Steady-state/transient probability computation

	/**
	 * Compute steady-state probability distribution (forwards).
	 * Start from initial state (or uniform distribution over multiple initial states).
	 */
	public StateValues doSteadyState(POMAS pomas) throws PrismException
	{
		return doSteadyState(pomas, (StateValues) null);
	}

	/**
	 * Compute steady-state probability distribution (forwards).
	 * Optionally, use the passed in file initDistFile to give the initial probability distribution (time 0).
	 * If null, start from initial state (or uniform distribution over multiple initial states).
	 */
	public StateValues doSteadyState(POMAS pomas, File initDistFile) throws PrismException
	{
		StateValues initDist = readDistributionFromFile(initDistFile, pomas);
		return doSteadyState(pomas, initDist);
	}

	/**
	 * Compute steady-state probability distribution (forwards).
	 * Optionally, use the passed in vector initDist as the initial probability distribution (time 0).
	 * If null, start from initial state (or uniform distribution over multiple initial states).
	 * For reasons of efficiency, when a vector is passed in, it will be trampled over,
	 * so if you wanted it, take a copy. 
	 * @param POMAS The POMAS
	 * @param initDist Initial distribution (will be overwritten)
	 */
	public StateValues doSteadyState(POMAS pomas, StateValues initDist) throws PrismException
	{
		StateValues initDistNew = (initDist == null) ? buildInitialDistribution(pomas) : initDist;
		ModelCheckerResult res = computeSteadyStateProbs(pomas, initDistNew.getDoubleArray());
		return StateValues.createFromDoubleArray(res.soln, pomas);
	}

	/**
	 * Compute transient probability distribution (forwards).
	 * Optionally, use the passed in vector initDist as the initial probability distribution (time step 0).
	 * If null, start from initial state (or uniform distribution over multiple initial states).
	 * For reasons of efficiency, when a vector is passed in, it will be trampled over,
	 * so if you wanted it, take a copy. 
	 * @param POMAS The POMAS
	 * @param k Time step
	 * @param initDist Initial distribution (will be overwritten)
	 */
	public StateValues doTransient(POMAS pomas, int k, double initDist[]) throws PrismException
	{
		throw new PrismNotSupportedException("Not implemented yet");
	}

	// Numerical computation functions

	/**
	 * Compute next=state probabilities.
	 * i.e. compute the probability of being in a state in {@code target} in the next step.
	 * @param POMAS The POMAS
	 * @param target Target states
	 */
	public ModelCheckerResult computeNextProbs(POMAS pomas, BitSet target) throws PrismException
	{
		ModelCheckerResult res = null;
		int n;
		double soln[], soln2[];
		long timer;
		PredecessorRelation pre = null;

		timer = System.currentTimeMillis();

		// Store num states
		n = pomas.getNumStates();

		// Create/initialise solution vector(s)
		soln = Utils.bitsetToDoubleArray(target, n);
		soln2 = new double[n];

		// Next-step probabilities 
		pomas.mvMult(soln, soln2, null, false);
		
		// Return results
		res = new ModelCheckerResult();
		res.soln = soln2;
		res.createLabeledSolnFromDoubleSoln();
		res.numIters = 1;
		
		/////////////////////////////////////////////////
		// Determine set of states actually need to compute traces for
		BitSet unknown = new BitSet();
		unknown.set(0, n);
		pre = pomas.getPredecessorRelation(this, true);

		//System.out.println("Unknown :: " + unknown.toString());
		// Compute probabilistic labeled traces for satisfying states
		computeTraces(pomas, unknown, unknown, target, pre, n, res.solnWithLabels);

		// Finished probabilistic reachability
		timer = System.currentTimeMillis() - timer;
		res.timeTaken = timer / 1000.0;
		
		//System.out.println("!!!!!!!!!!!! + res.soln = " + res.soln[0] 
		//		+ ", res.solnWithLabels = " + res.solnWithLabels[0].toString());
		
		
		////////////////////////////////////////////////////
		// Compute probabilistic labeled traces for deadlocks (all terminating traces)
		// for the purpose of computing negation of the traces
		unknown.set(0, n);
		BitSet deadlocks = pomas.getDeadlockStatesList().valuesB;
		res.solnDeadlocks = new ProbTraceList[n];
		// Compute probabilistic labeled traces for all terminating states
		computeTraces(pomas, unknown, unknown, deadlocks, pre, n, res.solnDeadlocks);

		//System.out.println("!!!!!!!!!!!! +  res.solnDeadlocks = " + res.solnDeadlocks[0].toString());
		/////
		
		return res;
	}
	

	/**
	 * Compute opacity probabilities.
	 * i.e. compute the probability of opacity of {@code target},
	 * the probability of trace to target/obs^{-1}(obs(not target)).
	 * @param pomas The POMAS
	 * @param target Target states
	 */
	public ModelCheckerResult computeOpacityProbs(POMAS pomas, StateValues probsProp, StateValues probsNegProp) throws PrismException
	{
		ModelCheckerResult res = null;
		int n;
		ProbTraceList solnWithLabels[], solnNegWithLabels[], solnWithLabels2[];
		long timer;

		timer = System.currentTimeMillis();

		//System.out.println("\n\n\nprobsProp !!! = " + probsProp.valuesL[0].toString());
		//System.out.println("probsNegProp !!! = " + probsNegProp.valuesL[0].toString());

		// Store num states
		n = pomas.getNumStates();
		solnWithLabels = probsProp.valuesL;
		solnNegWithLabels = probsNegProp.valuesL;
		solnWithLabels2 = new ProbTraceList[n];
		for (int i=0; i<n; i++) {
			ArrayList<ProbTransLabel> traces = solnWithLabels[i].getTraceList();
			ArrayList<ProbTransLabel> negTraces = solnNegWithLabels[i].getTraceList();
			ArrayList<ProbTransLabel> opacTraces = new ArrayList<ProbTransLabel>();
			solnWithLabels2[i] = new ProbTraceList(opacTraces);
			//System.out.println(">>>> i : " + i);
			for (int j=0; j<traces.size(); j++) {
				ProbTransLabel t = traces.get(j);
				boolean find = false;
				for (int k=0; k<negTraces.size(); k++) {
					ProbTransLabel t1 = negTraces.get(k);
					//if (t.getValue() > 0.0 & t1.getValue() > 0.0 & t.getObservation().equals(t1.getObservation()) )  {
					if (t.getValue() > 0.0 & t1.getValue() > 0.0 & 
							Pattern.matches(t1.getObservation(), t.getObservation()) )  {
						//System.out.println("finding same observation...." + t.toString());
						find = true;
						break;
					}
				}
				if (!find) opacTraces.add(t);
			}
			
			if (opacTraces != null) {
				//System.out.println("opacTrace...." + opacTraces.toString());
				solnWithLabels2[i].computeProb();
				//System.out.println("solnWithLabels2[i]...." + solnWithLabels2[i].toString());
			}
		}

		//System.out.println("solnWithLabel2[0] :: " + solnWithLabels2[0].getTraceList().toString());
		
		// Return results
		res = new ModelCheckerResult();
		res.solnWithLabels = solnWithLabels2;
		res.createDoubleSolnFromLabeledSoln();
		res.numIters = 1;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Given a value vector x, compute the probability:
	 *   v(s) = Sum_s' P(s,s')*x(s')   for s labeled with a,
	 *   v(s) = 0                      for s not labeled with a.
	 *
	 * @param POMAS the POMAS model
	 * @param a the set of states labeled with a
	 * @param x the value vector
	 */
	protected double[] computeRestrictedNext(POMAS pomas, BitSet a, double[] x)
	{
		double[] soln;
		int n;

		// Store num states
		n = pomas.getNumStates();

		// initialized to 0.0
		soln = new double[n];

		// Next-step probabilities multiplication
		// restricted to a states
		pomas.mvMult(x, soln, a, false);

		return soln;
	}
	
	/**
	 * Compute reachability probabilities.
	 * i.e. compute the probability of reaching a state in {@code target}.
	 * @param POMAS The POMAS
	 * @param target Target states
	 */
	public ModelCheckerResult computeReachProbs(POMAS pomas, BitSet target) throws PrismException
	{
		return computeReachProbs(pomas, null, target, null, null);
	}

	/**
	 * Compute until probabilities.
	 * i.e. compute the probability of reaching a state in {@code target},
	 * while remaining in those in {@code remain}.
	 * @param POMAS The POMAS
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 */
	public ModelCheckerResult computeUntilProbs(POMAS pomas, BitSet remain, BitSet target) throws PrismException
	{
		return computeReachProbs(pomas, remain, target, null, null);
	}

	/**
	 * Compute reachability/until probabilities.
	 * i.e. compute the min/max probability of reaching a state in {@code target},
	 * while remaining in those in {@code remain}.
	 * @param pomas The POMAS
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param init Optionally, an initial solution vector (may be overwritten) 
	 * @param known Optionally, a set of states for which the exact answer is known
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.  
	 */
	public ModelCheckerResult computeReachProbs(POMAS pomas, BitSet remain, BitSet target, double init[], BitSet known) throws PrismException
	{
		ModelCheckerResult res = null;
		BitSet no, yes, unknown;
		int n, numYes, numNo;
		long timer, timerProb0, timerProb1;
		PredecessorRelation pre = null;
		// Local copy of setting
		LinEqMethod linEqMethod = this.linEqMethod;

		// Switch to a supported method, if necessary
		if (!(linEqMethod == LinEqMethod.POWER || linEqMethod == LinEqMethod.GAUSS_SEIDEL)) {
			linEqMethod = LinEqMethod.GAUSS_SEIDEL;
			mainLog.printWarning("Switching to linear equation solution method \"" + linEqMethod.fullName() + "\"");
		}

		// Start probabilistic reachability
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting probabilistic reachability...");
		

		// Check for deadlocks in non-target state (because breaks e.g. prob1)
		pomas.checkForDeadlocks(target);

		// Store num states
		n = pomas.getNumStates();

		// Optimise by enlarging target set (if more info is available)
		if (init != null && known != null && !known.isEmpty()) {
			BitSet targetNew = (BitSet) target.clone();
			for (int i : new IterableBitSet(known)) {
				if (init[i] == 1.0) {
					targetNew.set(i);
				}
			}
			target = targetNew;
		}

		// If required, export info about target states
		if (getExportTarget()) {
			BitSet bsInit = new BitSet(n);
			for (int i = 0; i < n; i++) {
				bsInit.set(i, pomas.isInitialState(i));
			}
			List<BitSet> labels = Arrays.asList(bsInit, target);
			List<String> labelNames = Arrays.asList("init", "target");
			mainLog.println("\nExporting target states info to file \"" + getExportTargetFilename() + "\"...");
			exportLabels(pomas, labels, labelNames, Prism.EXPORT_PLAIN, new PrismFileLog(getExportTargetFilename()));
		}

		if (precomp && (prob0 || prob1) && preRel) {
			pre = pomas.getPredecessorRelation(this, true);
		}

		// Precomputation
		timerProb0 = System.currentTimeMillis();
		if (precomp && prob0) {
			if (preRel) {
				no = prob0(pomas, remain, target, pre);
			} else {
				no = prob0(pomas, remain, target);
			}
		} else {
			no = new BitSet();
		}
		timerProb0 = System.currentTimeMillis() - timerProb0;
		timerProb1 = System.currentTimeMillis();
		if (precomp && prob1) {
			if (preRel) {
				yes = prob1(pomas, remain, target, pre);
			} else {
				yes = prob1(pomas, remain, target);
			}
		} else {
			yes = (BitSet) target.clone();
		}
		timerProb1 = System.currentTimeMillis() - timerProb1;
		
		// Print results of precomputation
		numYes = yes.cardinality();
		numNo = no.cardinality();
		mainLog.println("target=" + target.cardinality() + ", yes=" + numYes + ", no=" + numNo + ", maybe=" + (n - (numYes + numNo)));

		
		// Compute probabilities
		switch (linEqMethod) {
		case POWER:
			res = computeReachProbsValIter(pomas, no, yes, init, known);
			break;
		case GAUSS_SEIDEL:
			res = computeReachProbsGaussSeidel(pomas, no, yes, init, known);
			break;
		default:
			throw new PrismException("Unknown linear equation solution method " + linEqMethod.fullName());
		}
		
		//////////////////////////////
		// Determine set of states actually need to compute traces for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(no);
		if (known != null)
			unknown.andNot(known);

		//System.out.println("£££££££££Target :: " + target.toString());
		// Compute probabilistic labeled traces for satisfying states
		computeTraces(pomas, remain, unknown, target, pre, n, res.solnWithLabels);

		// Finished probabilistic reachability
		timer = System.currentTimeMillis() - timer;
		mainLog.println("Probabilistic reachability took " + timer / 1000.0 + " seconds.");

		// Update time taken
		res.timeTaken = timer / 1000.0;
		res.timeProb0 = timerProb0 / 1000.0;
		res.timePre = (timerProb0 + timerProb1) / 1000.0;
		
		//System.out.println("\n!!!!!!!!!!!! + res.soln = " + res.soln[0]
		//		+ ", res.solnWithLabels = " + res.solnWithLabels[0].getProb() 
		//		+ ":" + res.solnWithLabels[0].getTraceList().toString());
		
		
		////////////////////////////////////////////////////
		// Compute probabilistic labeled traces for deadlocks (all terminating traces)
		// for the purpose of computing negation of the traces
		unknown.set(0, n);
		//unknown.andNot(target);
		//unknown.andNot(yes);	
		BitSet deadlocks = pomas.getDeadlockStatesList().valuesB;
		deadlocks.andNot(target);
		res.solnDeadlocks = new ProbTraceList[n];
		// Compute probabilistic labeled traces for all terminating states
		computeTraces(pomas, remain, unknown, deadlocks, pre, n, res.solnDeadlocks);
		//System.out.println("\n!!!!!!!!!!!! + res.solnDeadlocks = " 
		//			+ res.solnWithLabels[0].getProb() 
		//			+ ":" + res.solnDeadlocks[0].getTraceList().toString());
		
		return res;
	}

	/**
	 * Compute reachability/until traces.
	 * i.e. compute the trace of reaching a state in {@code target},
	 * while remaining in those in {@code remain}.
	 * @param pomas The POMAS
	 * @param remain Remain in these states (optional: null means "all")
	 * @param unknown A set of states actually needed for computing
	 * @param target Target states
	 * @param pre Predecessor relation
	 * @param n The number of the states
	 * @param soln Result trace list 
	 */
	public void computeTraces(POMAS pomas, BitSet remain, BitSet unknown, BitSet target, 
			PredecessorRelation pre, int n, ProbTraceList[] soln)
	{
		String observer =  ((POMASSimple)pomas).getObserver();
		for (int s : new IterableStateSet(unknown, n, false)) {
			ArrayList<ProbTransLabel>  myTraces = new ArrayList<ProbTransLabel>();
			for (int t : new IterableStateSet(target, n, false)) {
				ProbTraceList visited = new ProbTraceList(n);
				ProbTransLabel tl = new ProbTransLabel("", observer, "", "", 0.0);
				BitSet tmpTarget = new BitSet();
				tmpTarget.set(t);
				BitSet canReachTarget = pre.calculatePreStar(remain, tmpTarget, tmpTarget);
				tl = pomas.computeProbLabeledTrace(s, tl, canReachTarget, t, visited, myTraces);
				/*if (tl.getValue() > 0.0) {
					traces.add(tl);
					//if (s==0)  System.out.println("Traces for: [" + s + ":" + t +"]" + tl.toString() + ", visited path = " + visited.toString());
				}*/
			}
			soln[s] = new ProbTraceList(myTraces);
			//if (s==0) System.out.println("\n soln.. my traces = " + soln[0].toString());
		}		
		
	}


	/**
	 * Prob0 precomputation algorithm (using predecessor relation),
	 * i.e. determine the states of a POMAS which, with probability 0,
	 * reach a state in {@code target}, while remaining in those in {@code remain}.
	 * @param pomas The POMAS
	 * @param remain Remain in these states (optional: {@code null} means "all states")
	 * @param target Target states
	 * @param pre The predecessor relation
	 */
	public BitSet prob0(POMAS pomas, BitSet remain, BitSet target, PredecessorRelation pre)
	{
		BitSet canReachTarget, result;
		long timer;

		// Start precomputation
		timer = System.currentTimeMillis();

		// Special case: no target states
		if (target.isEmpty()) {
			BitSet soln = new BitSet(pomas.getNumStates());
			soln.set(0, pomas.getNumStates());
			return soln;
		}

		// calculate all states that can reach 'target'
		// while remaining in 'remain' in the underlying graph,
		// where all the 'target' states are made absorbing
		canReachTarget = pre.calculatePreStar(remain, target, target);

		// prob0 = complement of 'canReachTarget'
		result = new BitSet();
		result.set(0, pomas.getNumStates(), true);
		result.andNot(canReachTarget);

		// Finished precomputation
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Prob0");
		mainLog.println(" took " + timer / 1000.0 + " seconds.");

		return result;
	}

	/**
	 * Prob0 precomputation algorithm (using a fixed-point computation),
	 * i.e. determine the states of a DTMC which, with probability 0,
	 * reach a state in {@code target}, while remaining in those in {@code remain}.
	 * @param pomas The POMAS
	 * @param remain Remain in these states (optional: {@code null} means "all")
	 * @param target Target states
	 */
	public BitSet prob0(POMAS pomas, BitSet remain, BitSet target)
	{
		int n, iters;
		BitSet u, soln, unknown;
		boolean u_done;
		long timer;

		// Start precomputation
		timer = System.currentTimeMillis();
		mainLog.println("Starting Prob0...");

		// Special case: no target states
		if (target.cardinality() == 0) {
			soln = new BitSet(pomas.getNumStates());
			soln.set(0, pomas.getNumStates());
			return soln;
		}

		// Initialise vectors
		n = pomas.getNumStates();
		u = new BitSet(n);
		soln = new BitSet(n);

		// Determine set of states actually need to perform computation for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(target);
		if (remain != null)
			unknown.and(remain);

		// Fixed point loop
		iters = 0;
		u_done = false;
		// Least fixed point - should start from 0 but we optimise by
		// starting from 'target', thus bypassing first iteration
		u.or(target);
		soln.or(target);
		while (!u_done) {
			iters++;
			// Single step of Prob0
			pomas.prob0step(unknown, u, soln);
			// Check termination
			u_done = soln.equals(u);
			// u = soln
			u.clear();
			u.or(soln);
		}

		// Negate
		u.flip(0, n);

		// Finished precomputation
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Prob0");
		mainLog.println(" took " + iters + " iterations and " + timer / 1000.0 + " seconds.");

		return u;
	}

	/**
	 * Prob1 precomputation algorithm (using predecessor relation),
	 * i.e. determine the states of a POMAS which, with probability 1,
	 * reach a state in {@code target}, while remaining in those in {@code remain}.
	 * @param dtmc The POMAS
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param pre The predecessor relation of the DTMC
	 */
	public BitSet prob1(POMAS pomas, BitSet remain, BitSet target, PredecessorRelation pre) {
		// Implements the constrained reachability algorithm from
		// Baier, Katoen: Principles of Model Checking (Corollary 10.31 Qualitative Constrained Reachability)
		long timer;

		// Start precomputation
		timer = System.currentTimeMillis();
		mainLog.println("Starting Prob1...");

		// Special case: no 'target' states
		if (target.isEmpty()) {
			// empty set
			return new BitSet();
		}

		// mark all states in 'target' and all states not in 'remain' as absorbing
		BitSet absorbing = new BitSet();
		if (remain != null) {
			// complement remain
			absorbing.set(0, pomas.getNumStates(), true);
			absorbing.andNot(remain);
		} else {
			// for remain == null, remain consists of all states
			// thus, absorbing = the empty set is already the complementation of remain
		}
		// union with 'target'
		absorbing.or(target);

		// M' = DTMC where all 'absorbing' states are considered to be absorbing

		// the set of states that satisfy E [ F target ] in M'
		// Pre*(target)
		BitSet canReachTarget = pre.calculatePreStar(null, target, absorbing);

		// complement canReachTarget
		// S\Pre*(target)
		BitSet canNotReachTarget = new BitSet();
		canNotReachTarget.set(0, pomas.getNumStates(), true);
		canNotReachTarget.andNot(canReachTarget);

		// the set of states that can reach a canNotReachTarget state in M'
		// Pre*(S\Pre*(target))
		BitSet probTargetNot1 = pre.calculatePreStar(null, canNotReachTarget, absorbing);

		// complement probTargetNot1
		// S\Pre*(S\Pre*(target))
		BitSet result = new BitSet();
		result.set(0, pomas.getNumStates(), true);
		result.andNot(probTargetNot1);

		// Finished precomputation
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Prob1");
		mainLog.println(" took " + timer / 1000.0 + " seconds.");

		return result;
	}

	/**
	 * Prob1 precomputation algorithm (using a fixed-point computation)
	 * i.e. determine the states of a POMAS which, with probability 1,
	 * reach a state in {@code target}, while remaining in those in {@code remain}.
	 * @param pomas The POMAS
	 * @param remain Remain in these states (optional: {@code null} means "all")
	 * @param target Target states
	 */
	public BitSet prob1(POMAS pomas, BitSet remain, BitSet target)
	{
		int n, iters;
		BitSet u, v, soln, unknown;
		boolean u_done, v_done;
		long timer;

		// Start precomputation
		timer = System.currentTimeMillis();
		mainLog.println("Starting Prob1...");

		// Special case: no target states
		if (target.cardinality() == 0) {
			return new BitSet(pomas.getNumStates());
		}

		// Initialise vectors
		n = pomas.getNumStates();
		u = new BitSet(n);
		v = new BitSet(n);
		soln = new BitSet(n);

		// Determine set of states actually need to perform computation for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(target);
		if (remain != null)
			unknown.and(remain);

		// Nested fixed point loop
		iters = 0;
		u_done = false;
		// Greatest fixed point
		u.set(0, n);
		while (!u_done) {
			v_done = false;
			// Least fixed point - should start from 0 but we optimise by
			// starting from 'target', thus bypassing first iteration
			v.clear();
			v.or(target);
			soln.clear();
			soln.or(target);
			while (!v_done) {
				iters++;
				// Single step of Prob1
				pomas.prob1step(unknown, u, v, soln);
				// Check termination (inner)
				v_done = soln.equals(v);
				// v = soln
				v.clear();
				v.or(soln);
			}
			// Check termination (outer)
			u_done = v.equals(u);
			// u = v
			u.clear();
			u.or(v);
		}
		

		// Finished precomputation
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Prob1");
		mainLog.println(" took " + iters + " iterations and " + timer / 1000.0 + " seconds.");

		return u;
	}

	/**
	 * Compute reachability probabilities using value iteration.
	 * @param pomas The POMAS
	 * @param no Probability 0 states
	 * @param yes Probability 1 states
	 * @param init Optionally, an initial solution vector (will be overwritten) 
	 * @param known Optionally, a set of states for which the exact answer is known
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.  
	 */
	protected ModelCheckerResult computeReachProbsValIter(POMAS pomas, BitSet no, BitSet yes, double init[], BitSet known) throws PrismException
	{
		ModelCheckerResult res;
		BitSet unknown;
		int i, n, iters;
		double soln[], soln2[], tmpsoln[], initVal;
		boolean done;
		long timer;

		// Start value iteration
		timer = System.currentTimeMillis();
		mainLog.println("Starting value iteration...");

		// Store num states
		n = pomas.getNumStates();

		// Create solution vector(s)
		soln = new double[n];
		soln2 = (init == null) ? new double[n] : init;

		// Initialise solution vectors. Use (where available) the following in order of preference:
		// (1) exact answer, if already known; (2) 1.0/0.0 if in yes/no; (3) passed in initial value; (4) initVal
		// where initVal is 0.0 or 1.0, depending on whether we converge from below/above. 
		initVal = (valIterDir == ValIterDir.BELOW) ? 0.0 : 1.0;
		if (init != null) {
			if (known != null) {
				for (i = 0; i < n; i++)
					soln[i] = soln2[i] = known.get(i) ? init[i] : yes.get(i) ? 1.0 : no.get(i) ? 0.0 : init[i];
			} else {
				for (i = 0; i < n; i++)
					soln[i] = soln2[i] = yes.get(i) ? 1.0 : no.get(i) ? 0.0 : init[i];
			}
		} else {
			for (i = 0; i < n; i++)
				soln[i] = soln2[i] = yes.get(i) ? 1.0 : no.get(i) ? 0.0 : initVal;
		}
		
		/*if (valIterDir == ValIterDir.BELOW)  
			initVal.setProb(0.0);
		else initVal.setProb(1.0);
		
		if (init != null) {
			if (known != null) {
				for (i = 0; i < n; i++) {
					d = known.get(i) ? init[i].getProb() : yes.get(i) ? 1.0 : no.get(i) ? 0.0 : init[i].getProb();
					soln[i].setProb(d); soln2[i].setProb(d);
				}
			} else {
				for (i = 0; i < n; i++) {
					d = yes.get(i) ? 1.0 : no.get(i) ? 0.0 : init[i].getProb();
					soln[i].setProb(d); soln2[i].setProb(d);
				}
			}
		} else {
			for (i = 0; i < n; i++) {
				d = yes.get(i) ? 1.0 : no.get(i) ? 0.0 : initVal.getProb();
				soln[i].setProb(d); soln2[i].setProb(d);
			}
		}*/

		// Determine set of states actually need to compute values for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(yes);
		unknown.andNot(no);
		if (known != null)
			unknown.andNot(known);

		// Start iterations
		iters = 0;
		done = false;
		while (!done && iters < maxIters) {
			iters++;
			// Matrix-vector multiply
			pomas.mvMult(soln, soln2, unknown, false);
			// Check termination
			done = PrismUtils.doublesAreClose(soln, soln2, termCritParam, termCrit == TermCrit.ABSOLUTE);
			// Swap vectors for next iter
			tmpsoln = soln;
			soln = soln2;
			soln2 = tmpsoln;
		}

		// Finished value iteration
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Value iteration");
		mainLog.println(" took " + iters + " iterations and " + timer / 1000.0 + " seconds.");

		// Non-convergence is an error (usually)
		if (!done && errorOnNonConverge) {
			String msg = "Iterative method did not converge within " + iters + " iterations.";
			msg += "\nConsider using a different numerical method or increasing the maximum number of iterations";
			throw new PrismException(msg);
		}

		// Return results
		res = new ModelCheckerResult();
		//res.solnWithLabels = soln;
		//res.createDoubleSolnFromLabeledSoln();
		res.soln = soln;
		res.createLabeledSolnFromDoubleSoln();
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Compute reachability probabilities using Gauss-Seidel.
	 * @param pomas The POMAS
	 * @param no Probability 0 states
	 * @param yes Probability 1 states
	 * @param init Optionally, an initial solution vector (will be overwritten) 
	 * @param known Optionally, a set of states for which the exact answer is known
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.  
	 */
	protected ModelCheckerResult computeReachProbsGaussSeidel(POMAS pomas, BitSet no, BitSet yes, double init[], BitSet known) throws PrismException
	{
		ModelCheckerResult res;
		BitSet unknown;
		int i, n, iters;
		double soln[], initVal, maxDiff;
		//ProbTraceList soln[], d;
		//ArrayList<ProbTransLabel> traces = new ArrayList<ProbTransLabel>();
		boolean done;
		long timer;

		// Start value iteration
		timer = System.currentTimeMillis();
		mainLog.println("Starting Gauss-Seidel...");

		// Store num states
		n = pomas.getNumStates();

		// Create solution vector
		//soln = (init == null) ? new ProbTraceList[n] : init;
		soln = (init == null) ? new double[n] : init;

		// Initialise solution vector. Use (where available) the following in order of preference:
		// (1) exact answer, if already known; (2) 1.0/0.0 if in yes/no; (3) passed in initial value; (4) initVal
		// where initVal is 0.0 or 1.0, depending on whether we converge from below/above. 
		initVal = (valIterDir == ValIterDir.BELOW) ? 0.0 : 1.0;
		if (init != null) {
			if (known != null) {
				for (i = 0; i < n; i++) 
					soln[i] = known.get(i) ? init[i] : yes.get(i) ? 1.0 : no.get(i) ? 0.0 : init[i];
				/*{
					d = known.get(i) ? init[i].getProb() : yes.get(i) ? 1.0 : no.get(i) ? 0.0 : init[i].getProb();
					if (init[i].getTraceList() != null) 
						traces = init[i].getTraceList();
					else traces.add(new ProbTransLabel("", d));
					soln[i] = new ProbTraceList(traces, d);
				}*/
			} else {
				for (i = 0; i < n; i++) 
					soln[i] = yes.get(i) ? 1.0 : no.get(i) ? 0.0 : init[i];
				/*{
					d = yes.get(i) ? 1.0 : no.get(i) ? 0.0 : init[i].getProb();
					if (init[i].getTraceList() != null) 
						traces = init[i].getTraceList();
					else traces.add(new ProbTransLabel("", d));
					soln[i] = new ProbTraceList(traces, d);
				}*/
			}
		} else {
			for (i = 0; i < n; i++) 
				soln[i] = yes.get(i) ? 1.0 : no.get(i) ? 0.0 : initVal;
			/*{
				d = yes.get(i) ? 1.0 : no.get(i) ? 0.0 : initVal;
				traces.add(new ProbTransLabel("", d, n));
				soln[i] = new ProbTraceList(traces, d);
				System.out.println("soln[" + i + "] = " + soln[i].getProb() + ", d = " + d + ", initVal = " + initVal);
			}*/
			
		}

		// Determine set of states actually need to compute values for
		unknown = new BitSet();
		unknown.set(0, n, true);
		unknown.andNot(yes);
		unknown.andNot(no);
		if (known != null)
			unknown.andNot(known);

		// Start iterations
		iters = 0;
		done = false;
		while (!done && iters < maxIters) {
			iters++;
			// Matrix-vector multiply
			//maxDiff = POMAS.mvLabeledMultGS(soln, unknown, false, termCrit == TermCrit.ABSOLUTE);
			maxDiff = pomas.mvMultGS(soln, unknown, false, termCrit == TermCrit.ABSOLUTE);
			// Check termination
			done = maxDiff < termCritParam;
		}

		// Finished Gauss-Seidel
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Gauss-Seidel");
		mainLog.println(" took " + iters + " iterations and " + timer / 1000.0 + " seconds.");

		// Non-convergence is an error (usually)
		if (!done && errorOnNonConverge) {
			String msg = "Iterative method did not converge within " + iters + " iterations.";
			msg += "\nConsider using a different numerical method or increasing the maximum number of iterations";
			throw new PrismException(msg);
		}
		
		// Return results
		res = new ModelCheckerResult();
		//res.solnWithLabels = soln;
		//res.createDoubleSolnFromLabeledSoln();
		res.soln = soln;
		res.createLabeledSolnFromDoubleSoln();
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		return res;
	}
	

	/**
	 * Compute bounded reachability probabilities.
	 * i.e. compute the probability of reaching a state in {@code target} within k steps.
	 * @param pomas The POMAS
	 * @param target Target states
	 * @param k Bound
	 */
	public ModelCheckerResult computeBoundedReachProbs(POMAS pomas, BitSet target, int k) throws PrismException
	{
		return computeBoundedReachProbs(pomas, null, target, k, null, null);
	}

	/**
	 * Compute bounded until probabilities.
	 * i.e. compute the probability of reaching a state in {@code target},
	 * within k steps, and while remaining in states in {@code remain}.
	 * @param POMAS The POMAS
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param k Bound
	 */
	public ModelCheckerResult computeBoundedUntilProbs(POMAS pomas, BitSet remain, BitSet target, int k) throws PrismException
	{
		return computeBoundedReachProbs(pomas, remain, target, k, null, null);
	}

	/**
	 * Compute bounded reachability/until probabilities.
	 * i.e. compute the probability of reaching a state in {@code target},
	 * within k steps, and while remaining in states in {@code remain}.
	 * @param pomas The POMAS
	 * @param remain Remain in these states (optional: null means "all")
	 * @param target Target states
	 * @param k Bound
	 * @param init Initial solution vector - pass null for default
	 * @param results Optional array of size b+1 to store (init state) results for each step (null if unused)
	 */
	public ModelCheckerResult computeBoundedReachProbs(POMAS pomas, BitSet remain, BitSet target, int k, double init[], double results[]) throws PrismException
	{
		ModelCheckerResult res = null;
		BitSet unknown;
		int i, n, iters;
		double soln[], soln2[], tmpsoln[];
		long timer;

		// Start bounded probabilistic reachability
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting bounded probabilistic reachability...");

		// Store num states
		n = pomas.getNumStates();

		// Create solution vector(s)
		soln = new double[n];
		soln2 = (init == null) ? new double[n] : init;

		// Initialise solution vectors. Use passed in initial vector, if present
		if (init != null) {
			for (i = 0; i < n; i++)
				soln[i] = soln2[i] = target.get(i) ? 1.0 : init[i];
		} else {
			for (i = 0; i < n; i++)
				soln[i] = soln2[i] = target.get(i) ? 1.0 : 0.0;
		}
		// Store intermediate results if required
		// (compute min/max value over initial states for first step)
		if (results != null) {
			// TODO: whether this is min or max should be specified somehow
			results[0] = Utils.minMaxOverArraySubset(soln2, pomas.getInitialStates(), true);
		}

		// Determine set of states actually need to perform computation for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(target);
		if (remain != null)
			unknown.and(remain);

		// Start iterations
		iters = 0;
		while (iters < k) {

			iters++;
			// Matrix-vector multiply
			pomas.mvMult(soln, soln2, unknown, false);
			// Store intermediate results if required
			// (compute min/max value over initial states for this step)
			if (results != null) {
				// TODO: whether this is min or max should be specified somehow
				results[iters] = Utils.minMaxOverArraySubset(soln2, pomas.getInitialStates(), true);
			}
			// Swap vectors for next iter
			tmpsoln = soln;
			soln = soln2;
			soln2 = tmpsoln;
		}

		// Finished bounded probabilistic reachability
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Bounded probabilistic reachability");
		mainLog.println(" took " + iters + " iterations and " + timer / 1000.0 + " seconds.");

		// Return results
		res = new ModelCheckerResult();
		res.soln = soln;
		res.lastSoln = soln2;
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		res.timePre = 0.0;
		return res;
	}

	/**
	 * Compute expected reachability rewards.
	 * @param POMAS The POMAS
	 * @param mcRewards The rewards
	 * @param target Target states
	 */
	public ModelCheckerResult computeReachRewards(POMAS pomas, MCRewards mcRewards, BitSet target) throws PrismException
	{
		return computeReachRewards(pomas, mcRewards, target, null, null);
	}

	/**
	 * Compute expected reachability rewards.
	 * @param dtmc The DTMC
	 * @param mcRewards The rewards
	 * @param target Target states
	 * @param init Optionally, an initial solution vector (may be overwritten) 
	 * @param known Optionally, a set of states for which the exact answer is known
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.  
	 */
	public ModelCheckerResult computeReachRewards(POMAS pomas, MCRewards mcRewards, BitSet target, double init[], BitSet known) throws PrismException
	{
		ModelCheckerResult res = null;
		BitSet inf;
		int n, numTarget, numInf;
		long timer, timerProb1;
		// Local copy of setting
		LinEqMethod linEqMethod = this.linEqMethod;

		// Switch to a supported method, if necessary
		if (!(linEqMethod == LinEqMethod.POWER)) {
			linEqMethod = LinEqMethod.POWER;
			mainLog.printWarning("Switching to linear equation solution method \"" + linEqMethod.fullName() + "\"");
		}

		// Start expected reachability
		timer = System.currentTimeMillis();
		mainLog.println("\nStarting expected reachability...");

		// Check for deadlocks in non-target state (because breaks e.g. prob1)
		pomas.checkForDeadlocks(target);

		// Store num states
		n = pomas.getNumStates();

		// Optimise by enlarging target set (if more info is available)
		if (init != null && known != null && !known.isEmpty()) {
			BitSet targetNew = (BitSet) target.clone();
			for (int i : new IterableBitSet(known)) {
				if (init[i] == 1.0) {
					targetNew.set(i);
				}
			}
			target = targetNew;
		}

		// Precomputation (not optional)
		timerProb1 = System.currentTimeMillis();
		inf = prob1(pomas, null, target);
		inf.flip(0, n);
		timerProb1 = System.currentTimeMillis() - timerProb1;

		// Print results of precomputation
		numTarget = target.cardinality();
		numInf = inf.cardinality();
		mainLog.println("target=" + numTarget + ", inf=" + numInf + ", rest=" + (n - (numTarget + numInf)));

		// Compute rewards
		switch (linEqMethod) {
		case POWER:
			res = computeReachRewardsValIter(pomas, mcRewards, target, inf, init, known);
			break;
		default:
			throw new PrismException("Unknown linear equation solution method " + linEqMethod.fullName());
		}

		// Finished expected reachability
		timer = System.currentTimeMillis() - timer;
		mainLog.println("Expected reachability took " + timer / 1000.0 + " seconds.");

		// Update time taken
		res.timeTaken = timer / 1000.0;
		res.timePre = timerProb1 / 1000.0;

		return res;
	}

	/**
	 * Compute expected reachability rewards using value iteration.
	 * @param pomas The POMAS
	 * @param mcRewards The rewards
	 * @param target Target states
	 * @param inf States for which reward is infinite
	 * @param init Optionally, an initial solution vector (will be overwritten) 
	 * @param known Optionally, a set of states for which the exact answer is known
	 * Note: if 'known' is specified (i.e. is non-null, 'init' must also be given and is used for the exact values.
	 */
	protected ModelCheckerResult computeReachRewardsValIter(POMAS pomas, MCRewards mcRewards, BitSet target, BitSet inf, double init[], BitSet known)
			throws PrismException
	{
		ModelCheckerResult res;
		BitSet unknown;
		int i, n, iters;
		double soln[], soln2[], tmpsoln[];
		boolean done;
		long timer;

		// Start value iteration
		timer = System.currentTimeMillis();
		mainLog.println("Starting value iteration...");

		// Store num states
		n = pomas.getNumStates();

		// Create solution vector(s)
		soln = new double[n];
		soln2 = (init == null) ? new double[n] : init;

		// Initialise solution vectors. Use (where available) the following in order of preference:
		// (1) exact answer, if already known; (2) 0.0/infinity if in target/inf; (3) passed in initial value; (4) 0.0
		if (init != null) {
			if (known != null) {
				for (i = 0; i < n; i++)
					soln[i] = soln2[i] = known.get(i) ? init[i] : target.get(i) ? 0.0 : inf.get(i) ? Double.POSITIVE_INFINITY : init[i];
			} else {
				for (i = 0; i < n; i++)
					soln[i] = soln2[i] = target.get(i) ? 0.0 : inf.get(i) ? Double.POSITIVE_INFINITY : init[i];
			}
		} else {
			for (i = 0; i < n; i++)
				soln[i] = soln2[i] = target.get(i) ? 0.0 : inf.get(i) ? Double.POSITIVE_INFINITY : 0.0;
		}

		// Determine set of states actually need to compute values for
		unknown = new BitSet();
		unknown.set(0, n);
		unknown.andNot(target);
		unknown.andNot(inf);
		if (known != null)
			unknown.andNot(known);

		// Start iterations
		iters = 0;
		done = false;
		while (!done && iters < maxIters) {
			//mainLog.println(soln);
			iters++;
			// Matrix-vector multiply
			pomas.mvMultRew(soln, mcRewards, soln2, unknown, false);
			// Check termination
			done = PrismUtils.doublesAreClose(soln, soln2, termCritParam, termCrit == TermCrit.ABSOLUTE);
			// Swap vectors for next iter
			tmpsoln = soln;
			soln = soln2;
			soln2 = tmpsoln;
		}

		// Finished value iteration
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Value iteration");
		mainLog.println(" took " + iters + " iterations and " + timer / 1000.0 + " seconds.");

		// Non-convergence is an error (usually)
		if (!done && errorOnNonConverge) {
			String msg = "Iterative method did not converge within " + iters + " iterations.";
			msg += "\nConsider using a different numerical method or increasing the maximum number of iterations";
			throw new PrismException(msg);
		}

		// Return results
		res = new ModelCheckerResult();
		res.soln = soln;
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Compute (forwards) steady-state probabilities
	 * i.e. compute the long-run probability of being in each state,
	 * assuming the initial distribution {@code initDist}. 
	 * For space efficiency, the initial distribution vector will be modified and values over-written,  
	 * so if you wanted it, take a copy. 
	 * @param POMAS The POMAS
	 * @param initDist Initial distribution (will be overwritten)
	 */
	public ModelCheckerResult computeSteadyStateProbs(POMAS pomas, double initDist[]) throws PrismException
	{
		ModelCheckerResult res;
		BitSet startNot, bscc;
		double probBSCCs[], solnProbs[], reachProbs[];
		int n, numBSCCs = 0, allInOneBSCC;
		long timer;

		timer = System.currentTimeMillis();

		// Store num states
		n = pomas.getNumStates();
		// Create results vector
		solnProbs = new double[n];

		// Compute bottom strongly connected components (BSCCs)
		SCCComputer sccComputer = SCCComputer.createSCCComputer(this, pomas);
		sccComputer.computeBSCCs();
		List<BitSet> bsccs = sccComputer.getBSCCs();
		BitSet notInBSCCs = sccComputer.getNotInBSCCs();
		numBSCCs = bsccs.size();

		// See which states in the initial distribution do *not* have non-zero prob
		startNot = new BitSet();
		for (int i = 0; i < n; i++) {
			if (initDist[i] == 0)
				startNot.set(i);
		}
		// Determine whether initial states are all in a single BSCC 
		allInOneBSCC = -1;
		for (int b = 0; b < numBSCCs; b++) {
			if (!bsccs.get(b).intersects(startNot)) {
				allInOneBSCC = b;
				break;
			}
		}

		// If all initial states are in a single BSCC, it's easy...
		// Just compute steady-state probabilities for the BSCC
		if (allInOneBSCC != -1) {
			mainLog.println("\nInitial states all in one BSCC (so no reachability probabilities computed)");
			bscc = bsccs.get(allInOneBSCC);
			computeSteadyStateProbsForBSCC(pomas, bscc, solnProbs);
		}

		// Otherwise, have to consider all the BSCCs
		else {

			// Compute probability of reaching each BSCC from initial distribution 
			probBSCCs = new double[numBSCCs];
			for (int b = 0; b < numBSCCs; b++) {
				mainLog.println("\nComputing probability of reaching BSCC " + (b + 1));
				bscc = bsccs.get(b);
				// Compute probabilities
				reachProbs = computeUntilProbs(pomas, notInBSCCs, bscc).soln;
				// Compute probability of reaching BSCC, which is dot product of
				// vectors for initial distribution and probabilities of reaching it
				probBSCCs[b] = 0.0;
				for (int i = 0; i < n; i++) {
					probBSCCs[b] += initDist[i] * reachProbs[i];
				}
				mainLog.print("\nProbability of reaching BSCC " + (b + 1) + ": " + probBSCCs[b] + "\n");
			}

			// Compute steady-state probabilities for each BSCC 
			for (int b = 0; b < numBSCCs; b++) {
				mainLog.println("\nComputing steady-state probabilities for BSCC " + (b + 1));
				bscc = bsccs.get(b);
				// Compute steady-state probabilities for the BSCC
				computeSteadyStateProbsForBSCC(pomas, bscc, solnProbs);
				// Multiply by BSCC reach prob
				for (int i = bscc.nextSetBit(0); i >= 0; i = bscc.nextSetBit(i + 1))
					solnProbs[i] *= probBSCCs[b];
			}
		}

		// Return results
		res = new ModelCheckerResult();
		res.soln = solnProbs;
		timer = System.currentTimeMillis() - timer;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Perform (backwards) steady-state probabilities, as required for (e.g. CSL) model checking.
	 * Compute, for each initial state s, the sum over all states s'
	 * of the steady-state probability of being in s'
	 * multiplied by the corresponding probability in the vector {@code multProbs}.
	 * If {@code multProbs} is null, it is assumed to be all 1s.
	 * @param POMAS The POMAS
	 * @param multProbs Multiplication vector (optional: null means all 1s)
	 */
	public ModelCheckerResult computeSteadyStateBackwardsProbs(POMAS pomas, double multProbs[]) throws PrismException
	{
		ModelCheckerResult res;
		BitSet bscc;
		double probBSCCs[], ssProbs[], reachProbs[], soln[];
		int n, numBSCCs = 0;
		long timer;

		timer = System.currentTimeMillis();

		// Store num states
		n = pomas.getNumStates();

		// Compute bottom strongly connected components (BSCCs)
		SCCComputer sccComputer = SCCComputer.createSCCComputer(this, pomas);
		sccComputer.computeBSCCs();
		List<BitSet> bsccs = sccComputer.getBSCCs();
		BitSet notInBSCCs = sccComputer.getNotInBSCCs();
		numBSCCs = bsccs.size();

		// Compute steady-state probability for each BSCC...
		probBSCCs = new double[numBSCCs];
		ssProbs = new double[n];
		for (int b = 0; b < numBSCCs; b++) {
			mainLog.println("\nComputing steady state probabilities for BSCC " + (b + 1));
			bscc = bsccs.get(b);
			// Compute steady-state probabilities for the BSCC
			computeSteadyStateProbsForBSCC(pomas, bscc, ssProbs);
			// Compute weighted sum of probabilities with multProbs
			probBSCCs[b] = 0.0;
			if (multProbs == null) {
				for (int i = bscc.nextSetBit(0); i >= 0; i = bscc.nextSetBit(i + 1)) {
					probBSCCs[b] += ssProbs[i];
				}
			} else {
				for (int i = bscc.nextSetBit(0); i >= 0; i = bscc.nextSetBit(i + 1)) {
					probBSCCs[b] += multProbs[i] * ssProbs[i];
				}
			}
			mainLog.print("\nValue for BSCC " + (b + 1) + ": " + probBSCCs[b] + "\n");
		}

		// Create/initialise prob vector
		soln = new double[n];
		for (int i = 0; i < n; i++) {
			soln[i] = 0.0;
		}

		// If every state is in a BSCC, it's much easier...
		if (notInBSCCs.isEmpty()) {
			mainLog.println("\nAll states are in BSCCs (so no reachability probabilities computed)");
			for (int b = 0; b < numBSCCs; b++) {
				bscc = bsccs.get(b);
				for (int i = bscc.nextSetBit(0); i >= 0; i = bscc.nextSetBit(i + 1))
					soln[i] += probBSCCs[b];
			}
		}

		// Otherwise we have to do more work...
		else {
			// Compute probabilities of reaching each BSCC...
			for (int b = 0; b < numBSCCs; b++) {
				// Skip BSCCs with zero probability
				if (probBSCCs[b] == 0.0)
					continue;
				mainLog.println("\nComputing probabilities of reaching BSCC " + (b + 1));
				bscc = bsccs.get(b);
				// Compute probabilities
				reachProbs = computeUntilProbs(pomas, notInBSCCs, bscc).soln;
				// Multiply by value for BSCC, add to total
				for (int i = 0; i < n; i++) {
					soln[i] += reachProbs[i] * probBSCCs[b];
				}
			}
		}

		// Return results
		res = new ModelCheckerResult();
		res.soln = soln;
		timer = System.currentTimeMillis() - timer;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Compute steady-state probabilities for a BSCC
	 * i.e. compute the long-run probability of being in each state of the BSCC.
	 * No initial distribution is specified since it does not affect the result.
	 * The result will be stored in the relevant portion of a full vector,
	 * whose size equals the number of states in the DTMC.
	 * Optionally, pass in an existing vector to be used for this purpose.
	 * @param POMAS The POMAS
	 * @param bscc The BSCC to be analysed
	 * @param result Storage for result (ignored if null)
	 */
	public ModelCheckerResult computeSteadyStateProbsForBSCC(POMAS pomas, BitSet bscc, double result[]) throws PrismException
	{
		ModelCheckerResult res;
		int n, iters;
		double soln[], soln2[], tmpsoln[];
		boolean done;
		long timer;

		// Start value iteration
		timer = System.currentTimeMillis();
		mainLog.println("Starting value iteration...");

		// Store num states
		n = pomas.getNumStates();

		// Create solution vector(s)
		// Use the passed in vector, if present
		soln = result == null ? new double[n] : result;
		soln2 = new double[n];

		// Initialise solution vectors. Equiprobable for BSCC states.
		double equiprob = 1.0 / bscc.cardinality();
		for (int i = bscc.nextSetBit(0); i >= 0; i = bscc.nextSetBit(i + 1))
			soln[i] = soln2[i] = equiprob;

		// Start iterations
		iters = 0;
		done = false;
		while (!done && iters < maxIters) {
			iters++;
			// Matrix-vector multiply
			pomas.vmMult(soln, soln2);
			// Check termination
			done = PrismUtils.doublesAreClose(soln, soln2, termCritParam, termCrit == TermCrit.ABSOLUTE);
			// Swap vectors for next iter
			tmpsoln = soln;
			soln = soln2;
			soln2 = tmpsoln;
		}

		// Finished value iteration
		timer = System.currentTimeMillis() - timer;
		mainLog.print("Value iteration");
		mainLog.println(" took " + iters + " iterations and " + timer / 1000.0 + " seconds.");

		// Non-convergence is an error (usually)
		if (!done && errorOnNonConverge) {
			String msg = "Iterative method did not converge within " + iters + " iterations.";
			msg += "\nConsider using a different numerical method or increasing the maximum number of iterations";
			throw new PrismException(msg);
		}

		// Return results
		res = new ModelCheckerResult();
		res.soln = soln;
		res.numIters = iters;
		res.timeTaken = timer / 1000.0;
		return res;
	}

	/**
	 * Compute transient probabilities
	 * i.e. compute the probability of being in each state at time step {@code k},
	 * assuming the initial distribution {@code initDist}. 
	 * For space efficiency, the initial distribution vector will be modified and values over-written,  
	 * so if you wanted it, take a copy. 
	 * @param POMAS The POMAS
	 * @param k Time step
	 * @param initDist Initial distribution (will be overwritten)
	 */
	public ModelCheckerResult computeTransientProbs(POMAS pomas, int k, double initDist[]) throws PrismException
	{
		throw new PrismNotSupportedException("Not implemented yet");
	}

	/**
	 * Simple test program.
	 */
	public static void main(String args[])
	{
		POMASModelChecker mc;
		POMASSimple pomas;
		ModelCheckerResult res;
		try {
			// Two examples of building and solving a DTMC

			int version = 2;
			if (version == 1) {

				// 1. Read in from .tra and .lab files
				//    Run as: PRISM_MAINCLASS=explicit.DTMCModelChecker bin/prism dtmc.tra dtmc.lab target_label
				mc = new POMASModelChecker(null);
				pomas = new POMASSimple();
				pomas.buildFromPrismExplicit(args[0]);
				//System.out.println(dtmc);
				Map<String, BitSet> labels = mc.loadLabelsFile(args[1]);
				//System.out.println(labels);
				BitSet target = labels.get(args[2]);
				if (target == null)
					throw new PrismException("Unknown label \"" + args[2] + "\"");
				for (int i = 3; i < args.length; i++) {
					if (args[i].equals("-nopre"))
						mc.setPrecomp(false);
				}
				res = mc.computeReachProbs(pomas, target);
				System.out.println(res.solnWithLabels[0].toString());

			} else {

				// 2. Build DTMC directly
				//    Run as: PRISM_MAINCLASS=explicit.DTMCModelChecker bin/prism
				//    (example taken from p.14 of Lec 5 of http://www.prismmodelchecker.org/lectures/pmc/) 
				mc = new POMASModelChecker(null);
				pomas = new POMASSimple(6);
				pomas.setProbability(0, 1, 0.1, "x0", "x3", "d", "d");
				pomas.setProbability(0, 2, 0.9, "x0", "x3", "a", "");
				pomas.setProbability(1, 0, 0.4, "x1", "x3", "b", "");
				pomas.setProbability(1, 3, 0.6, "x1", "x3", "c", "c");
				pomas.setProbability(2, 2, 0.1, "x2","x3", "c", "c");
				pomas.setProbability(2, 4, 0.5, "x2", "x3", "b", "");
				pomas.setProbability(2, 5, 0.3, "x2", "x3", "d", "d");
				pomas.setProbability(3, 3, 1.0, "x2", "x3", "d", "d");
				pomas.setProbability(4, 4, 1.0, "x3", "x3", "d", "d");
				pomas.setProbability(5, 5, 0.3, "x3", "x3", "d", "d");
				pomas.setProbability(5, 4, 0.7, "x3", "x3", "b", "");
				System.out.println(pomas);
				BitSet target = new BitSet();
				target.set(4);
				BitSet remain = new BitSet();
				remain.set(1);
				remain.flip(0, 6);
				System.out.println(target);
				System.out.println(remain);
				res = mc.computeUntilProbs(pomas, remain, target);
				System.out.println(res.solnWithLabels[0].toString());
			}
		} catch (PrismException e) {
			System.out.println(e);
		}
	}
}
