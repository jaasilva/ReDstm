package org.deuce.benchmark.partial.photos;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class MyObjectBackend implements Serializable
{
	private static final long serialVersionUID = 1L;
	final byte[] array;

	public MyObjectBackend(int width, int height)
	{
		array = new byte[width * height * 3];
	}
}
