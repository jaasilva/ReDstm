package org.deuce.benchmark.intset;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class MyObjectBackend implements Serializable
{
	private static final long serialVersionUID = 1L;
	final byte[] array;

	public MyObjectBackend()
	{
		array = new byte[3145728]; // 3MB
	}
}
