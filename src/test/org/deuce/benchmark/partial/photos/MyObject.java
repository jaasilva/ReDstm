package org.deuce.benchmark.partial.photos;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class MyObject implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final byte[] array;

	public MyObject()
	{
		array = new byte[65255];
	}
}
