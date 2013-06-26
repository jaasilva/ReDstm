package org.deuce.benchmark.partial.photos;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class MyObject
{
	final byte[] array;

	public MyObject(byte[] a)
	{
		array = a;
	}
}
