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

package explicit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import common.IterableStateSet;
import explicit.rewards.MCRewards;
import parser.ast.ModulesFile;
import parser.ast.Agent;
import parser.ast.Observation;
import parser.ast.ProbTraceList;
import parser.ast.ProbTransLabel;
import prism.ModelType;
import prism.Pair;
import prism.PrismException;
import prism.PrismLog;
import prism.PrismUtils;

/**
 * Simple explicit-state representation of an pomas.
 * The implementation is far from optimal, both in terms of memory usage and speed of access.
 * The model is, however, easy to manipulate. For a static model (i.e. one that does not change
 * after creation), consider MDPSparse, which is more efficient. 
 */
public class POMASSimple extends DTMCSimple implements POMASExplicit
{

	/** the ModulesFile associated**/
	protected ModulesFile modulesFile;

	// Labeled transition matrix (labeled distribution list) 
	protected List<LDistribution> trans;
		
	/** information about the agents, transition labels and their observations of this model */
	//protected List<Observation> observationList;
	protected List<Agent> agentList;
	protected static String observer;

	//protected int numObservations;
	protected int numAgents;

	// Constructors

	/**
	 * Constructor: empty pomas.
	 */
	public POMASSimple()
	{
		initialise(0);
	}

	/**
	 * Constructor: new pomas with fixed number of states.
	 */
	public POMASSimple(int numStates)
	{
		initialise(numStates);
	}

	/**
	 * Copy constructor.
	 */
	public POMASSimple(POMASSimple pomas)
	{
		this(pomas.numStates);
		copyFrom(pomas);
		
		for (int i = 0; i < numStates; i++) {
			trans.set(i, new LDistribution(pomas.trans.get(i)));
		}
		
		modulesFile = pomas.modulesFile;
		//observationList = pomas.observationList;
		agentList = pomas.agentList;
		observer = pomas.observer;
		
		//numObservations = pomas.numObservations;
		numAgents = pomas.numAgents;
		numTransitions = pomas.numTransitions;
		numStates = pomas.numStates;
	}

	/**
	 * Construct an pomas from an existing one and a state index permutation,
	 * i.e. in which state index i becomes index permut[i].
	 * Note: have to build new labeled Distributions from scratch anyway to do this,
	 * so may as well provide this functionality as a constructor.
	 */
	public POMASSimple(POMASSimple pomas, int permut[])
	{
		
		this(pomas.numStates);
		copyFrom(pomas, permut);
		for (int i = 0; i < numStates; i++) {
			trans.set(permut[i], new LDistribution(pomas.trans.get(i), permut));
		}

		modulesFile = pomas.modulesFile;
		//observationList = pomas.observationList;
		agentList = pomas.agentList;
		observer = pomas.observer;
		//numObservations = pomas.numObservations;
		numAgents = pomas.numAgents;
		numTransitions = pomas.numTransitions;

	}

	public POMASSimple(DTMCSimple dtmc)
	{
		this(dtmc.numStates);
		copyFrom(dtmc);

		for (int i = 0; i < numStates; i++) {
			trans.set(i, new LDistribution(dtmc.trans.get(i)));
		}
		modulesFile = null;
		//observationList = null;
		agentList = null;
		observer = null;
		numTransitions = dtmc.numTransitions;
		numStates = dtmc.numStates;
	}

	/**
	 * set the associated modulesFile.
	 */
	public void setModulesFile(ModulesFile modulesFile)
	{
		this.modulesFile = modulesFile;
	}

	/**
	 * Set the associated (read-only) observation list.
	 */
	//public void setObservationsList(List<Observation> observationsList)
	//{
	//	this.observationList = observationsList;
	//}
	
	/**
	 * Set the associated (read-only) agent list.
	 */
	public void setAgentsList(List<Agent> agents)
	{
		this.agentList = agents;
	}
	
	public void setObserver(String observer)
	{
		this.observer = observer;
	}


	/*************************** Mutators (for ModelSimple)********************************/

	@Override
	public void initialise(int numStates)
	{
		super.initialise(numStates);
		trans = new ArrayList<LDistribution>(numStates);
		for (int i = 0; i < numStates; i++) {
			trans.add(new LDistribution());
		}
	}
	
	@Override
	public void clearState(int i)
	{
		// Do nothing if state does not exist
		if (i >= numStates || i < 0)
			return;
		// Clear data structures and update stats
		numTransitions -= trans.get(i).size();
		trans.get(i).clear();
	}

	@Override
	public int addState()
	{
		addStates(1);
		return numStates - 1;
	}

	@Override
	public void addStates(int numToAdd)
	{
		for (int i = 0; i < numToAdd; i++) {
			trans.add(new LDistribution());
			numStates++;
		}
	}

	@Override
	public void buildFromPrismExplicit(String filename) throws PrismException
	{
		BufferedReader in;
		String s, ss[];
		int i, j, n, lineNum = 0;
		double prob;

		try {
			// Open file
			in = new BufferedReader(new FileReader(new File(filename)));
			// Parse first line to get num states
			s = in.readLine();
			lineNum = 1;
			if (s == null) {
				in.close();
				throw new PrismException("Missing first line of .tra file");
			}
			ss = s.split(" ");
			n = Integer.parseInt(ss[0]);
			// Initialise
			initialise(n);
			// Go though list of transitions in file
			s = in.readLine();
			lineNum++;
			while (s != null) {
				s = s.trim();
				if (s.length() > 0) {
					ss = s.split(" ");
					i = Integer.parseInt(ss[0]);
					j = Integer.parseInt(ss[1]);
					prob = Double.parseDouble(ss[2]);
					setProbability(i, j, prob);
				}
				s = in.readLine();
				lineNum++;
			}
			// Close file
			in.close();
		} catch (IOException e) {
			System.out.println(e);
			System.exit(1);
		} catch (NumberFormatException e) {
			throw new PrismException("Problem in .tra file (line " + lineNum + ") for " + getModelType());
		}
		// Set initial state (assume 0)
		initialStates.add(0);
	}


	public ModulesFile getModulesFile()
	{
		return modulesFile;
	}

	/*public List<Observation> getObservationsList()
	{
		return observationList;
	}*/
	
	public String getObservationByLabel(String observer, String action)
	{
		List<Agent> myAgents = getAgentsList();
		for (int j=0; j<myAgents.size(); j++) {
			if (myAgents.get(j).getName().equals(observer)) {
				List<Observation> myObs = myAgents.get(j).getObservationsList();
				for (int i=0; i<myObs.size(); i++) {
					if (myObs.get(i).getName().equals(action)){
						return myObs.get(i).getObservedLbl();
					}				
				}
				break;
			}
		}
		return "";
	}
	
	public List<Agent> getAgentsList()
	{
		return agentList;
	}

	public String getObserver()
	{
		return observer;
	}
	
	/**
	 * Get the number of observations in state s.
	*/
	/*public int getNumObservations()
	{
		return numObservations;
	}*/
	
	/**
	 * Get the number of agent.
	*/
	public int getNumAgents()
	{
		return numAgents;
	}

	/**
	 * Set the probability for a transition. 
	 */
	@Override
	public void setProbability(int i, int j, double prob)
	{
		setProbability(i,j,prob,"", "", "","");
	}

	/**
	 * Set the labeled probability for a transition. 
	 */
	@Override
	public void setProbability(int i, int j, ProbTransLabel probLabel)
	{
		LDistribution distr = trans.get(i);
		if (distr.get(j) != 0.0)
			numTransitions--;
		if (probLabel.getValue() != 0.0)
			numTransitions++;
		distr.set(j, probLabel);
	}
	
	public void setProbability(int i, int j, Double prob, String agent, String observer, String label, String obsLbl)
	{
		LDistribution distr = trans.get(i);
		ProbTransLabel probLabel = new ProbTransLabel(agent, observer, label, obsLbl, prob);
		if (distr.get(j) != 0.0)
			numTransitions--;
		if (prob != 0.0)
			numTransitions++;
		distr.set(j, probLabel);
	}
	
	/**
	 * Add to the probability for a transition. 
	 */
	@Override
	public void addToProbability(int i, int j, ProbTransLabel probLabel)
	{
		if (!trans.get(i).add(j, probLabel)) {
			if (probLabel.getValue() != 0.0)
				numTransitions++;
		}
	}
	
	public void addToProbability(int i, int j, Double prob, String agent, String label)
	{
		if (!trans.get(i).add(j, prob, agent, label)) {
			if (prob != 0.0)
				numTransitions++;
		}
	}
	
	@Override
	public void findDeadlocks(boolean fix) throws PrismException
	{
		for (int i = 0; i < numStates; i++) {
			if (trans.get(i).isEmpty()) {
				addDeadlockState(i);
				if (fix)
					setProbability(i, i, 1.0, "", "", "", "");
			}
		}
	}
	
	@Override
	public void checkForDeadlocks(BitSet except) throws PrismException
	{
		for (int i = 0; i < numStates; i++) {
			if (trans.get(i).isEmpty() && (except == null || !except.get(i)))
				throw new PrismException("pomas has a deadlock in state " + i);
		}
	}	
	
	
	// Accessors (for Model)

	@Override
	public int getNumTransitions()
	{
		return numTransitions;
	}

	@Override
	public Iterator<Integer> getSuccessorsIterator(final int s)
	{
		return trans.get(s).getSupport().iterator();
	}
	
	@Override
	public boolean isSuccessor(int s1, int s2)
	{
		return trans.get(s1).contains(s2);
	}

	@Override
	public boolean allSuccessorsInSet(int s, BitSet set)
	{
		return (trans.get(s).isSubsetOf(set));
	}

	@Override
	public boolean someSuccessorsInSet(int s, BitSet set)
	{
		return (trans.get(s).containsOneOf(set));
	}

	// Accessors (for DTMC)

	@Override
	public int getNumTransitions(int s)
	{
		return trans.get(s).size();
	}

	
	/************************ Standard methods*****************************************/

	@Override
	public String toString()
	{
		
		int i;
		boolean first;
		String s = "";
		first = true;
		s = "trans: [ ";
		for (i = 0; i < numStates; i++) {
			if (first)
				first = false;
			else
				s += ", ";
			s += i + ": " + trans.get(i).toString(); 
			//TODO: transition labels
		}
		s += " ]";
		return s;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof POMASSimple))
			return false;
		POMASSimple pomas = (POMASSimple) o;
		if (numStates != pomas.numStates)
			return false;
		if (!initialStates.equals(pomas.initialStates))
			return false;
		if (!trans.equals(pomas.trans))
			return false;
		// TODO: compare actions (complicated: null = null,null,null,...)
		return true;
	}

	@Override
	public ModelType getModelType()
	{
		return ModelType.POMAS;
	}

	@Override
	public String infoString()
	{
		String s = "";
		s += numStates + " states (" + getNumInitialStates() + " initial)";
		s += ", " + getNumTransitions() + " transitions";
		//s += ", " + getNumObservations() + " transition labels"; 
		//TODO: transition labels and their observations
		return s;
	}

	@Override
	public String infoStringTable()
	{
		String s = "";
		s += "States:        " + numStates + " (" + getNumInitialStates() + " initial)\n";
		s += "Transitions:   " + getNumTransitions() + "\n";
		return s;
	}
	
	public void exportToPrismExplicitTra(PrismLog out)
	{
		int i;
		TreeMap<Integer, Pair<Double, Object>> sorted;
		
		// Output transitions to .tra file
		out.print(numStates + " " + getNumTransitions() + "\n");
		sorted = new TreeMap<Integer, Pair<Double, Object>>();
		for (i = 0; i < numStates; i++) {
			// Extract transitions and sort by destination state index (to match PRISM-exported files)
			Iterator<Map.Entry<Integer, ProbTransLabel>> iter = getLabeledTransitionsIterator(i);
			while (iter.hasNext()) {
				Map.Entry<Integer, ProbTransLabel> e = iter.next();
				Pair<Double, Object> pair = new Pair<> (e.getValue().getValue(), e.getValue().getAction());
				sorted.put(e.getKey(), pair);
			}
			// Print out (sorted) transitions
			for (Map.Entry<Integer, Pair<Double, Object>> e : sorted.entrySet()) {
				// Note use of PrismUtils.formatDouble to match PRISM-exported files
				out.print(i + " " + e.getKey() + " " + PrismUtils.formatDouble(e.getValue().first));
				Object action = e.getValue().second; 
				if (action != null && !"".equals(action))
					out.print(" " + action);
				out.print("\n");
			}
			sorted.clear();
		}
	}
	
	/*@Override
	public Iterator<Entry<Integer, Pair<Double, Object>>> getLabeledTransitionIterator(int s)
	{
		System.out.println("tran.get(" + s + ") = " + trans.get(s).toString()); 
		LDistribution ldistr = trans.get(s);
		Iterator<Entry<Integer, ProbTransLabel>> i = ldistr.iterator();
		
		// Default implementation: extend iterator, setting all actions to null
		return new AddDefaultActionToTransitionsIterator(getTransitionsIterator(s), i);
	}
	
	@Override
	public Iterator<Entry<Integer, Double>> getTransitionsIterator(int s)
	{
		LDistribution ldistr = trans.get(s);
		Distribution distr = new Distribution(ldistr);
		Iterator<Map.Entry<Integer, Double>> i = distr.iterator();
		return i;
	}*/
	
	private Iterator<Entry<Integer, ProbTransLabel>> getLabeledTransitionsIterator(int s)
	{
		//System.out.println("tran.get(" + s + ") = " + trans.get(s).toString()); 
		LDistribution ldistr = trans.get(s);
		Iterator<Map.Entry<Integer, ProbTransLabel>> i = ldistr.iterator();
		// Default implementation: extend iterator, setting all actions to null
		return i;
	}
	
	@Override
	public void prob0step(BitSet subset, BitSet u, BitSet result)
	{
		LDistribution distr;
		for (int i : new IterableStateSet(subset, numStates)) {
			distr = trans.get(i);
			result.set(i, distr.containsOneOf(u));
		}
	}

	@Override
	public void prob1step(BitSet subset, BitSet u, BitSet v, BitSet result)
	{
		LDistribution distr;
		for (int i : new IterableStateSet(subset, numStates)) {
			distr = trans.get(i);
			result.set(i, distr.containsOneOf(v) && distr.isSubsetOf(u));
		}
	}

	@Override
	public double mvMultSingle(int s, double vect[])
	{
		int k;
		double d, prob;
		LDistribution distr;

		distr = trans.get(s);
		d = 0.0;
		for (Map.Entry<Integer, ProbTransLabel> e : distr) {
			k = (Integer) e.getKey();
			prob = (Double) e.getValue().getValue();
			d += prob * vect[k];
		}

		return d;
	}

	@Override
	public double mvMultJacSingle(int s, double vect[])
	{
		int k;
		double diag, d, prob;
		LDistribution distr;

		distr = trans.get(s);
		diag = 1.0;
		d = 0.0;
		for (Map.Entry<Integer, ProbTransLabel> e : distr) {
			k = (Integer) e.getKey();
			prob = (Double) e.getValue().getValue();
			if (k != s) {
				d += prob * vect[k];
			} else {
				diag -= prob;
			}
		}
		if (diag > 0)
			d /= diag;
		return d;
	}
	
	public double mvMultGS(double vect[], BitSet subset, boolean complement, boolean absolute)
	{
		double d, diff, maxDiff = 0.0;
		for (int s : new IterableStateSet(subset, numStates, complement)) {
			d = mvMultJacSingle(s, vect);
			diff = absolute ? (Math.abs(d - vect[s])) : (Math.abs(d - vect[s]) / d);
			maxDiff = diff > maxDiff ? diff : maxDiff;
			vect[s] = d;
		}
		// Use this code instead for backwards Gauss-Seidel
		/*for (s = numStates - 1; s >= 0; s--) {
			if (subset.get(s)) {
				d = mvMultJacSingle(s, vect);
				diff = absolute ? (Math.abs(d - vect[s])) : (Math.abs(d - vect[s]) / d);
				maxDiff = diff > maxDiff ? diff : maxDiff;
				vect[s] = d;
			}
		}*/
		return maxDiff;
	}
	

	@Override
	public ProbTransLabel computeProbLabeledTrace(int s, ProbTransLabel tl, BitSet subset, int target, 
			ProbTraceList path)
	{
		int k;
		LDistribution distr;
		double prob = 0.0;
		String agent = "", observer = "", label = "", obslabel = "";
		boolean selfloop = false;

		distr = trans.get(s);
		path.addStates(s);
		for (Map.Entry<Integer, ProbTransLabel> e : distr) {
			k = (Integer) e.getKey();
			observer = (String) e.getValue().getObserver();
			if (selfloop) {
				prob *= (Double) e.getValue().getValue();
				agent += (String) e.getValue().getAgent();
				//observer += (String) e.getValue().getObserver();
				label += (String) e.getValue().getAction();
				obslabel += getObservationByLabel((String) e.getValue().getObserver(), (String) e.getValue().getAction());
				path.removeTransition(path.getTraceList().size()-1);
				//System.out.println("1111111111111111 self loop");
				//reset selfloop 
				selfloop = false;
			}
			else if (path.getWithCycle(s)) {
				//prob = path.getTransition(path.getStates().indexOf(s)).getValue() * (Double) e.getValue().getValue();
				prob *= (Double) e.getValue().getValue();
				agent = path.getTransition(path.getStates().indexOf(s)).getAgent() 
						+ (String) e.getValue().getAgent();
				//observer = path.getTransition(path.getStates().indexOf(s)).getObserver() 
				//		+ (String) e.getValue().getObserver();
				label = path.getTransition(path.getStates().indexOf(s)).getAction() 
						+ (String) e.getValue().getAction();
				obslabel = path.getTransition(path.getStates().indexOf(s)).getObservation() 
						+ getObservationByLabel((String) e.getValue().getObserver(), (String) e.getValue().getAction());
				//System.out.println("22222222 prob = " + prob);
			}
			else {
				prob = (Double) e.getValue().getValue();
				agent = (String) e.getValue().getAgent();
				//observer = (String) e.getValue().getObserver();
				label = (String) e.getValue().getAction();
				obslabel = getObservationByLabel(observer,label);
				//System.out.println("33333");
			}
			path.addTransition(prob, agent, observer, label, obslabel);

			System.out.println(">>>> s = " + s + ", target = " + target + ", k = " + k 
					+ ", prob = " + prob + ", agent = " + agent 
					+ ", observer = " + observer + ", label = " + label 
					+ ", observation = " + obslabel + ", subset = " + subset 
					+ ", visited = " + path.toString()
					+ ", visited states = " + path.getStates().toString());
			
			// k is the terminating target 
			if ((k==target) & ((k==s) & label.length()==0 )) {
				System.out.println("targeting ... ");
				tl = new ProbTransLabel(agent, observer, label, obslabel, prob);
				break;
			}
			else if ((!subset.get(k)) | ((k==s) & label.length()==0 )) {
				System.out.println("continue ... ");
				continue;
			} 
			else if ( (k==s) & (label.length()>0) ) {
				if (prob<1) prob = 1/(1-prob);
				obslabel = getObservationByLabel(observer, label);
				label = "(" + label + ")*";
				if ((obslabel != null) & (obslabel.length() > 0)) 
					obslabel = "(" + obslabel + ")*";
				else obslabel = "";
				selfloop = true;
				System.out.println("self looping ..." + observer + " : " + label + "->" + obslabel);
				continue;
			}			
			else if (path.getStates().contains(k)) {
				System.out.println("cycle ... ");
				agent = label = obslabel = ""; prob = 1.0;
				for (int i=path.getStates().indexOf(k);i<path.getStates().size(); i++) {
					agent += path.getTransition(i).getAgent();
					//observer += path.getTransition(i).getObserver();
					label += path.getTransition(i).getAction();
					obslabel += path.getTransition(i).getObservation();
					//prob *= path.getTransition(i).getValue();
					//path.setCycle(path.getStates().get(i));
					ProbTransLabel tmpTrans = new ProbTransLabel("", "", "", "", 1.0);
					path.setTransition(tmpTrans, i);
					System.out.println("cycle[" +path.getStates().get(i) + "] = " + path.getWithCycle(path.getStates().get(i)));
				}
				//if (prob<1) prob = 1/(1-prob);
				if (label.length() > 0)
					label = "(" + label + ")*";
				if ((obslabel != null) & (obslabel.length() > 0)) 
					obslabel = "(" + obslabel + ")*";
				else obslabel = "";
				ProbTransLabel tmpTrans = new ProbTransLabel(agent, observer, label, obslabel, prob);
				path.setTransition(tmpTrans, path.getStates().indexOf(k));
				// set the start state of the cycle as true, for future label replacement
				path.setCycle(path.getStates().indexOf(k));
				System.out.println("cycle :: " + agent + " : " + observer + " : " 
						+ label + " -> " + obslabel + ", PROB ＝ " + prob);
				//System.out.println("cycle[" +k + "] = " + path.getWithCycle(k));
				
			}
			else {
			    System.out.println("recursion ... ");
			    tl = computeProbLabeledTrace(k, tl, subset, target, path);
			    System.out.println(" tl = " + tl.toString());
			    // if s in a cycle (actually the start state of the cycle), 
			    // use the label in the path instead of that of tl, 
			    // set up the prob to be one (rather than the prob of the tranistion in the cycle)
			    if (path.getWithCycle(s)) {
			    		prob = 1.0;
			    		agent = path.getTransition(path.getStates().indexOf(s)).getAgent() 
			    				+ tl.getAgent();
			    		//observer = path.getTransition(path.getStates().indexOf(s)).getAgent() 
			    		//		+ tl.getObserver();
			    		label = path.getTransition(path.getStates().indexOf(s)).getAction()
			    				+ tl.getAction();
			    		obslabel = path.getTransition(path.getStates().indexOf(s)).getObservation() + tl.getObservation();
					System.out.println("+++++ s with cycle:" + s + ":" + path.getWithCycle(s));
					System.out.println("+++++" + agent + " : " + observer + " : "
							+ label + "->" + obslabel + " w.p. " + prob);
			    }
			    else {
				    prob *= tl.getValue();
				    agent += tl.getAgent();
				    //observer += tl.getObserver();
			    	label += tl.getAction();
					obslabel += tl.getObservation();
					System.out.println("==== s without cycle:" + s + ":" + path.getWithCycle(s));
					System.out.println("====" + agent + " : " + observer + " : " 
							+ label + "->" + obslabel + " w.p. " + prob);
			    }
			    tl.setAgent(agent);
			    tl.setObserver(observer);
			    tl.setAction(label);
			    tl.setObservation(obslabel);
			    tl.setValue(prob);
			}
		}
		return tl;		
	}
	
	/*public ProbTransLabel computeProbLabeledTrace(int s, ProbTransLabel tl, BitSet subset, int target)
	{
		int k;
		LDistribution distr;
		double prob = 0.0;
		String label = null, obslabel = null;
		boolean selfloop = false, cycle = false;

		distr = trans.get(s);
		//System.out.println("DISTR :: " + s + "::" + distr.toString());
		for (Map.Entry<Integer, ProbTransLabel> e : distr) {
			k = (Integer) e.getKey();
			if (selfloop) {
				prob *= (Double) e.getValue().getValue();
				label += (String) e.getValue().getName();
				obslabel += getObservationByLabel((String) e.getValue().getName());
			}
			else {
			    prob = (Double) e.getValue().getValue();
			    label = (String) e.getValue().getName();
			    obslabel = getObservationByLabel(label);
			}
			   
			System.out.println(">>>> s = " + s + ", target = " + target + ", k = " + k 
			     + ", prob = " + prob + ", label = " + label 
			     + ", observation = " + obslabel + ", subset = " + subset);
			   
			// k is the terminating target 
			if (k == target & ((k==s) & label.length()==0 )) {
				System.out.println("targeting ... ");
			    tl = new ProbTransLabel(label, obslabel, prob);
			    break;
			}
			else if ((!subset.get(k)) | ((k==s) & label.length()==0 )) {
			    System.out.println("continue ... ");
			    continue;
			} 
			else if ( (k==s) & (label.length()>0) ) {
			    System.out.println("self looping ... ");
			    prob = 1/(1-prob);
			    obslabel = getObservationByLabel(label);
			    label = "(" + label + ")*";
			    if ((obslabel != null) & (obslabel != "")) 
			     obslabel = "(" + obslabel + ")*";
			    else obslabel = "";
			    selfloop = true;
			    continue;
			}
			else {
			    System.out.println("recursion ... ");
			    tl = computeProbLabeledTrace(k, tl, subset, target);
			    //System.out.println(" tl = " + tl.toString());
			    prob *= tl.getValue();
			    label += tl.getName();
			    obslabel += tl.getObservation();
			    tl.setName(label);
			    tl.setObservation(obslabel);
			    tl.setValue(prob);
			}
		}
		return tl;  
	}*/

	@Override
	public double mvMultRewSingle(int s, double vect[], MCRewards mcRewards)
	{
		int k;
		double d, prob;
		LDistribution distr;

		distr = trans.get(s);
		d = mcRewards.getStateReward(s);
		for (Map.Entry<Integer, ProbTransLabel> e : distr) {
			k = (Integer) e.getKey();
			prob = (Double) e.getValue().getValue();
			d += prob * vect[k];
		}

		return d;
	}

	@Override
	public void vmMult(double vect[], double result[])
	{
		int i, j;
		double prob;
		LDistribution distr;

		// Initialise result to 0
		for (j = 0; j < numStates; j++) {
			result[j] = 0;
		}
		// Go through matrix elements (by row)
		for (i = 0; i < numStates; i++) {
			distr = trans.get(i);
			for (Map.Entry<Integer, ProbTransLabel> e : distr) {
				j = (Integer) e.getKey();
				prob = (Double) e.getValue().getValue();
				result[j] += prob * vect[i];
			}

		}
	}
	
	// Accessors (other)
	/**
	 * Get the transitions (a labeled distribution) for state s.
	 */
	public LDistribution getLabeledTransitions(int s)
	{
		return trans.get(s);
	}



	/*public double mvLabeledMultGS(ProbTraceList vect[], BitSet subset, boolean complement, boolean absolute)
	{
		double d, diff, maxDiff = 0.0;
		ProbTraceList ptl = null;
		System.out.println("mvLabeledMultGS goes..... complement = " + complement + ", subset = " + subset.toString() );
		for (int s : new IterableStateSet(subset, numStates, complement)) {
			System.out.println("mvLabeledMultGS ::: s = " + s);
			ptl = mvLabeledMultJacSingle(s, vect);
			d = ptl.getProb(); 
			diff = absolute ? (Math.abs(d - vect[s].getProb())) : (Math.abs(d - vect[s].getProb()) / d);
			maxDiff = diff > maxDiff ? diff : maxDiff;
			vect[s].setProb(d);
			//vect[s].setTraceList(ptl.getTraceList());
			System.out.println("vect[" + s + "] = " + vect[s].getProb() + ", " + vect[s].getTraceList().toString());
		}*/
		// Use this code instead for backwards Gauss-Seidel
		/*for (int s = numStates - 1; s >= 0; s--) {
			System.out.println("mvLabeledMultGS ::: s = " + s + ", subset.get(s) = " + subset.get(s));
			if (subset.get(s)) {
				ptl = mvLabeledMultJacSingle(s, vect);
				d = ptl.getProb(); 
				diff = absolute ? (Math.abs(d - vect[s].getProb())) : (Math.abs(d - vect[s].getProb()) / d);
				maxDiff = diff > maxDiff ? diff : maxDiff;
				vect[s].setProb(d);
				vect[s].setTraceList(ptl.getTraceList());
				System.out.println("vect[" + s + "] = " + vect[s].getProb() + ", " + vect[s].getTraceList().toString());
				
			}
		}*/
		//return maxDiff;
	// d}
	
	/*public ProbTraceList mvLabeledMultJacSingle(int s, ProbTraceList vect[])
	{
		int k, m;
		double diag, d, prob;
		LDistribution distr;
		ArrayList<ProbTransLabel> tl = new ArrayList<ProbTransLabel>();
		ProbTraceList ptl = null;
		String transLbl = null;

		distr = trans.get(s);
		diag = 1.0;
		d = 0.0;
		for (Map.Entry<Integer, ProbTransLabel> e : distr) {
			k = (Integer) e.getKey();
			m = k;
			prob = (Double) e.getValue().getValue();
			transLbl = (String) e.getValue().getName();
			System.out.println("++++ k = " + k + ", transLbl = " + transLbl + ", prob = " + prob 
					+ ", vect[k].getProb() = " + vect[k].getProb());
			System.out.println("####### vect[k] :: " + vect[k].toString());
			if (k != s) {
				d += prob * vect[k].getProb();
			} else
				diag -= prob;

			ProbTransLabel trace = new ProbTransLabel(transLbl,prob,k);
			tl.add(trace);
		}
		if (diag > 0) {
			d /= diag;
		}
		System.out.println("!!!! d = " + d + ", tl = " + tl.toString());
		ptl = new ProbTraceList(tl, d);
		return ptl;
	}

	public ProbTraceList probLabelSingle(int s, ProbTraceList vect[])
	{
		int k;
		double diag, d, prob;
		LDistribution distr;
		ArrayList<ProbTransLabel> tl = new ArrayList<ProbTransLabel>();
		ProbTraceList ptl = null;
		String transLbl = null;

		distr = trans.get(s);
		diag = 1.0;
		d = 0.0;
		for (Map.Entry<Integer, ProbTransLabel> e : distr) {
			k = (Integer) e.getKey();
			prob = (Double) e.getValue().getValue();
			transLbl = (String) e.getValue().getName();
			System.out.println("\n++++ k = " + k + ", transLbl = " + transLbl + ", prob = " + prob 
					+ ", vect[k].getProb() = " + vect[k].getProb());
			System.out.println("####### vect[k] :: " + vect[k].toString());
			if (k != s) {
				d += prob * vect[k].getProb();
				System.out.println("#### d = " + d);
				if (k == vect[k].getTraceList().get(k).getKey()) {
					prob *= vect[k].getTraceList().get(k).getValue();
					if (prob>0.0) {
						transLbl += vect[k].getTraceList().get(k).getName();
						System.out.println("~~~ p = " + prob + ", transLbl = " + transLbl + ", vect[" + k +"," + k +"] = " 
								+ vect[k].getTraceList().get(k).getValue() + ":" 
								+ vect[k].getTraceList().get(k).getName());
					}
				}
			} else
				diag -= prob;

			ProbTransLabel trace = new ProbTransLabel(transLbl,prob,k);
			tl.add(trace);
		}
		if (diag > 0) {
			d /= diag;
		}
		ptl = new ProbTraceList(tl, d);
		return ptl;
	}*/
	
	
	/*public ProbTraceList mvLabeledMultSingle(int s, ProbTraceList vect[])
	{
		int k;
		double d, prob;
		LDistribution distr;
		ArrayList<ProbTransLabel> tl = new ArrayList<ProbTransLabel>();
		ProbTraceList traceList = null;

		distr = trans.get(s);
		d = 0.0;
		for (Map.Entry<Integer, ProbTransLabel> e : distr) {
			k = (Integer) e.getKey();
			prob = (Double) e.getValue().getValue();
			d += prob * vect[k].getProb();
			String transLbl = (String) e.getValue().getName();
			for (int i=0; i<vect[k].getTraceList().size(); i++) {
				double p = prob * vect[k].getProb();
				String l = transLbl+vect[k].getTraceList().get(i).getName();
				ProbTransLabel trace = new ProbTransLabel(l,p);
				tl.add(trace);
			}
		}
		traceList = new ProbTraceList(tl, d);
		return traceList;
	}
	
	public void mvLabeledMult(ProbTraceList vect[], ProbTraceList result[], BitSet subset, boolean complement)
	{
		for (int s : new IterableStateSet(subset, numStates, complement)) {
			result[s] = mvLabeledMultSingle(s, vect);
		}
	}*/
}
