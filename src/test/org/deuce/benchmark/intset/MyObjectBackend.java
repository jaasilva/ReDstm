package org.deuce.benchmark.intset;

import java.io.Serializable;
import java.util.Random;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class MyObjectBackend implements Serializable
{
	private static final long serialVersionUID = 1L;
	final byte[] array;
	private final static int max_range = 5242880; // 5 MB
	private final static int min_range = 2097152; // 2 MB

	public MyObjectBackend()
	{
		Random random = new Random();
		array = new byte[random.nextInt(max_range + 1) + min_range];
	}
}
