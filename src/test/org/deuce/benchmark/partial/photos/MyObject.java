package org.deuce.benchmark.partial.photos;

import java.io.Serializable;

public class MyObject implements Serializable
{
	private static final long serialVersionUID = 1L;
	final MyObjectBackend backend;

	public MyObject(int width, int height)
	{
		backend = new MyObjectBackend(width, height);
	}
}
