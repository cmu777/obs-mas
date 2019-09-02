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

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import prism.Pair;

/**
 * Base class for explicit-state representations of an LDTMC.
 */
public interface POMASExplicit extends POMAS
{
	
	public class AddDefaultLabelToTransitionsIterator implements Iterator<Map.Entry<Integer, Pair<Double, Object>>>
	{
		private Iterator<Entry<Integer, Double>> transIter;
		private Object defaultAction;
		private Entry<Integer, Double> next;
		
		public AddDefaultLabelToTransitionsIterator(Iterator<Entry<Integer, Double>> transIter, Object defaultAction)
		{
			this.transIter = transIter;
			this.defaultAction = defaultAction;
		}

		@Override
		public Entry<Integer, Pair<Double, Object>> next()
		{
			next = transIter.next();
			final Integer state = next.getKey();
			final Double probability = next.getValue();
			return new AbstractMap.SimpleImmutableEntry<>(state, new Pair<>(probability, defaultAction));
		}

		@Override
		public boolean hasNext()
		{
			return transIter.hasNext();
		}

		@Override
		public void remove()
		{
			// Do nothing: read-only
		}
	}

}
