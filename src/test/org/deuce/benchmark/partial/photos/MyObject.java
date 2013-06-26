package org.deuce.benchmark.partial.photos;

import java.io.Serializable;

public class MyObject implements Serializable
{
	private static final long serialVersionUID = 1L;
	final byte[] array;

	public MyObject(int width, int height)
	{
		array = new byte[width * height * 3];
	}
}
