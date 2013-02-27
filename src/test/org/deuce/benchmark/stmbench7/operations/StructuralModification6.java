package org.deuce.benchmark.stmbench7.operations;

import org.deuce.benchmark.stmbench7.OperationId;
import org.deuce.benchmark.stmbench7.Parameters;
import org.deuce.benchmark.stmbench7.Setup;
import org.deuce.benchmark.stmbench7.ThreadRandom;
import org.deuce.benchmark.stmbench7.annotations.Transactional;
import org.deuce.benchmark.stmbench7.annotations.Update;
import org.deuce.benchmark.stmbench7.backend.Index;
import org.deuce.benchmark.stmbench7.core.BaseAssembly;
import org.deuce.benchmark.stmbench7.core.OperationFailedException;

/**
 * Structural modification SM6 (see the specification).
 */
public class StructuralModification6 extends StructuralModification5
{

	protected Index<Integer, BaseAssembly> baseAssemblyIdIndex;

	public StructuralModification6(Setup oo7setup)
	{
		super(oo7setup);
		this.baseAssemblyIdIndex = oo7setup.getBaseAssemblyIdIndex();
	}

	@Override
	@Transactional
	@Update
	public int performOperation() throws OperationFailedException
	{
		int baseAssemblyToRemoveId = ThreadRandom
				.nextInt(Parameters.MaxBaseAssemblies) + 1;
		BaseAssembly baseAssemblyToRemove = baseAssemblyIdIndex
				.get(baseAssemblyToRemoveId);
		if (baseAssemblyToRemove == null)
			throw new OperationFailedException();

		// We want the tree of BAs/CAs to keep its form
		// so that each CA has always at least one child sub-assembly
		if (baseAssemblyToRemove.getSuperAssembly().getSubAssemblies().size() == 1)
			throw new OperationFailedException();

		assemblyBuilder.unregisterAndRecycleBaseAssembly(baseAssemblyToRemove);

		return 1;
	}

	@Override
	public OperationId getOperationId()
	{
		return OperationId.SM6;
	}
}
