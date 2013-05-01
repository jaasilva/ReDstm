package org.deuce.benchmark.stmbench7.core;

import org.deuce.benchmark.stmbench7.annotations.Atomic;
import org.deuce.benchmark.stmbench7.annotations.ReadOnly;
import org.deuce.benchmark.stmbench7.annotations.Update;

/**
 * A class from which most parts of the main benchmark data structure descend.
 * For a default implementation, see stmbench7.impl.core.DesignObjImpl.
 */
@Atomic
public interface DesignObj
{

	@ReadOnly
	int getId();

	@ReadOnly
	int getBuildDate();

	@ReadOnly
	String getType();

	@Update
	void updateBuildDate();

	@ReadOnly
	void nullOperation();
}
