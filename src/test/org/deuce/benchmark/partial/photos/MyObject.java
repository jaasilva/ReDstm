package org.deuce.benchmark.partial.photos;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class MyObject
{
	final byte[] array;

	public MyObject()
	{
		array = new byte[65255];
	}
}
