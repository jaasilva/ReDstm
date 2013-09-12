package org.deuce.benchmark.intset;

import java.io.Serializable;

public class MyObject implements Serializable
{
	private static final long serialVersionUID = 1L;
	final MyObjectBackend backend;

	public MyObject()
	{
		backend = new MyObjectBackend();
	}
}
