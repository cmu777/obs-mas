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

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import prism.PrismUtils;
import parser.ast.ProbTransLabel;

/**
 * Explicit representation of a probability distribution.
 * Basically, a mapping from (integer-valued) indices to (non-zero, double-valued) probabilities. 
 */
public class LDistribution implements Iterable<Entry<Integer, ProbTransLabel>>
{
	private HashMap<Integer, ProbTransLabel> map;

	/**
	 * Create an empty distribution.
	 */
	public LDistribution()
	{
		clear();
	}

	/**
	 * Copy constructor.
	 */
	public LDistribution(LDistribution distr)
	{
		this();
		Iterator<Entry<Integer, ProbTransLabel>> i = distr.iterator();
		while (i.hasNext()) {
			Map.Entry<Integer, ProbTransLabel> e = i.next();
			add(e.getKey(), e.getValue());
		}
	}
	
	public LDistribution(Distribution distr)
	{
		this();
		Iterator<Entry<Integer, Double>> i = distr.iterator();
		while (i.hasNext()) {
			Map.Entry<Integer, Double> e = i.next();
			ProbTransLabel l = new ProbTransLabel(null, null, null, null, e.getValue());
			add(e.getKey(), l);
		}
	}

	/**
	 * Construct a distribution from an existing one and an index permutation,
	 * i.e. in which index i becomes index permut[i].
	 * Note: have to build the new distributions from scratch anyway to do this,
	 * so may as well provide this functionality as a constructor.
	 */
	public LDistribution(LDistribution distr, int permut[])
	{
		this();
		Iterator<Entry<Integer, ProbTransLabel>> i = distr.iterator();
		while (i.hasNext()) {
			Map.Entry<Integer, ProbTransLabel> e = i.next();
			add(permut[e.getKey()], e.getValue());
		}
	}

	/**
	 * Clear all entries of the distribution.
	 */
	public void clear()
	{
		map = new HashMap<Integer, ProbTransLabel>();
	}

	/**
	 * Add 'prob transition label' to the labeled distribution for index 'j'.
	 * Return boolean indicating whether or not there was already
	 * non-zero labeled probability for this index (i.e. false denotes new transition).
	 */
	public boolean add(int j, ProbTransLabel probLabel)
	{
		ProbTransLabel d = (ProbTransLabel) map.get(j);
		if (d == null) {
			map.put(j, probLabel);
			return false;
		} else {
			d.setValue(d.getValue()+probLabel.getValue());
			set(j, d);
			return true;
		}
	}
	
	public boolean add(int j, Double prob, String agent, String label)
	{
		ProbTransLabel probLabel = (ProbTransLabel) map.get(j);
		if (probLabel == null) {
			probLabel = new ProbTransLabel(agent, label, prob);
			map.put(j, probLabel);
			return false;
		} else {
			probLabel.setValue(prob+probLabel.getValue());
			set(j,probLabel);
			return true;
		}
	}
	

	/**
	 * Set the labeled probability for index 'j' to 'probLabel'.
	 */
	public void set(int j, ProbTransLabel probLabel)
	{
		if (probLabel.getValue() == 0.0)
			map.remove(j);
		else
			map.put(j, probLabel);
	}
	/**
	 * Set the probability for index 'j' to 'prob'.
	 */
	public void set(int j, double prob)
	{
		if (prob == 0.0)
			map.remove(j);
		else 
		{
			ProbTransLabel probLabel = 
					new ProbTransLabel(map.get(j).getAgent(), map.get(j).getObserver(), 
							map.get(j).getAction(), map.get(j).getObservation(), prob);
			map.put(j, probLabel);
		}
	}
	
	/**
	 * Get the probability for index j. 
	 */
	public double get(int j)
	{
		ProbTransLabel d;
		d = (ProbTransLabel) map.get(j);
		return d == null ? 0.0 : d.getValue();
	}
	
	/**
	 * Get the probability label for index j. 
	 */
	public ProbTransLabel getProbTrans(int j)
	{
		ProbTransLabel d;
		d = (ProbTransLabel) map.get(j);
		return d == null ? new ProbTransLabel(null, null, null, null, 0.0) : d;
	}

	/**
	 * Returns true if index j is in the support of the distribution. 
	 */
	public boolean contains(int j)
	{
		return map.get(j) != null;
	}

	/**
	 * Returns true if all indices in the support of the distribution are in the set. 
	 */
	public boolean isSubsetOf(BitSet set)
	{
		Iterator<Entry<Integer, ProbTransLabel>> i = iterator();
		while (i.hasNext()) {
			Map.Entry<Integer, ProbTransLabel> e = i.next();
			if (!set.get((Integer) e.getKey()))
				return false;
		}
		return true;
	}

	/**
	 * Returns true if at least one index in the support of the distribution is in the set. 
	 */
	public boolean containsOneOf(BitSet set)
	{
		Iterator<Entry<Integer, ProbTransLabel>> i = iterator();
		while (i.hasNext()) {
			Map.Entry<Integer, ProbTransLabel> e = i.next();
			if (set.get((Integer) e.getKey()))
				return true;
		}
		return false;
	}

	/**
	 * Get the support of the distribution.
	 */
	public Set<Integer> getSupport()
	{
		return map.keySet();
	}
	
	/**
	 * Get an iterator over the entries of the map defining the distribution.
	 */
	public Iterator<Entry<Integer, ProbTransLabel>> iterator()
	{
		return map.entrySet().iterator();
	}

	/**
	 * Returns true if the distribution is empty.
	 */
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	/**
	 * Get the size of the support of the distribution.
	 */
	public int size()
	{
		return map.size();
	}

	/**
	 * Get the sum of the probabilities in the distribution.
	 */
	public ProbTransLabel sum()
	{
		ProbTransLabel d = new ProbTransLabel(null, null, null, null, 0);
		Iterator<Entry<Integer, ProbTransLabel>> i = iterator();
		if (i.hasNext()) d = i.next().getValue();
		while (i.hasNext()) {
			Map.Entry<Integer, ProbTransLabel> e = i.next();
			d.setValue(d.getValue() + e.getValue().getValue());
		}
		return d;
	}

	/**
	 * Get the sum of all the probabilities in the distribution except for index j.
	 */
	public ProbTransLabel sumAllBut(int j)
	{
		ProbTransLabel d = new ProbTransLabel(null, null, null, null, 0);
		Iterator<Entry<Integer, ProbTransLabel>> i = iterator();
		if (i.hasNext()) d = i.next().getValue();
		while (i.hasNext()) {
			Map.Entry<Integer, ProbTransLabel> e = i.next();
			if (e.getKey() != j)
				d.setValue(d.getValue() + e.getValue().getValue());
		}
		return d;
	}

	/**
	 * Create a new distribution, based on a mapping from the indices
	 * used in this distribution to a different set of indices.
	 */
	public LDistribution map(int map[])
	{
		LDistribution distrNew = new LDistribution();
		Iterator<Entry<Integer, ProbTransLabel>> i = iterator();
		while (i.hasNext()) {
			Map.Entry<Integer, ProbTransLabel> e = i.next();
			distrNew.add(map[e.getKey()], e.getValue());
		}
		return distrNew;
	}

	@Override
	public boolean equals(Object o)
	{
		ProbTransLabel d1, d2;
		LDistribution d = (LDistribution) o;
		if (d.size() != size())
			return false;
		Iterator<Entry<Integer, ProbTransLabel>> i = iterator();
		while (i.hasNext()) {
			Map.Entry<Integer, ProbTransLabel> e = i.next();
			d1 = e.getValue();
			d2 = d.map.get(e.getKey());
			if (d2 == null || !PrismUtils.doublesAreClose(d1.getValue(), d2.getValue(), 1e-12, false))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		// Simple hash code
		return map.size();
	}

	@Override
	public String toString()
	{
		return map.toString();
	}
}
