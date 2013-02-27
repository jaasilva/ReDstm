package org.deuce.benchmark.stmbench7.operations;

import java.util.HashSet;

import org.deuce.benchmark.stmbench7.OperationId;
import org.deuce.benchmark.stmbench7.Setup;
import org.deuce.benchmark.stmbench7.annotations.Transactional;
import org.deuce.benchmark.stmbench7.annotations.Update;
import org.deuce.benchmark.stmbench7.backend.Index;
import org.deuce.benchmark.stmbench7.backend.LargeSet;
import org.deuce.benchmark.stmbench7.core.AtomicPart;

/**
 * Traversal T3, variant (a) (see the specification). Simple update, update on
 * index, long.
 */
public class Traversal3a extends Traversal1
{

	Index<Integer, LargeSet<AtomicPart>> partBuildDateIndex;

	public Traversal3a(Setup oo7setup)
	{
		super(oo7setup);
		this.partBuildDateIndex = oo7setup.getAtomicPartBuildDateIndex();
	}

	@Override
	@Transactional
	@Update
	public int performOperation()
	{
		return super.performOperation();
	}

	@Override
	protected int performOperationInAtomicPart(AtomicPart part,
			HashSet<AtomicPart> setOfVisitedPartIds)
	{
		if (setOfVisitedPartIds.isEmpty())
		{
			updateBuildDate(part);
			return 1;
		}

		part.nullOperation();
		return 0;
	}

	protected void updateBuildDate(AtomicPart part)
	{
		removeAtomicPartFromBuildDateIndex(partBuildDateIndex, part);
		part.updateBuildDate();
		addAtomicPartToBuildDateIndex(partBuildDateIndex, part);
	}

	@Override
	public OperationId getOperationId()
	{
		return OperationId.T3a;
	}
}
