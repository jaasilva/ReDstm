package org.deuce.benchmark.intset;

import java.io.Serializable;
import java.util.Random;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class MyObjectBackend implements Serializable
{
	private static final long serialVersionUID = 1L;
	final byte[] array;
	private final static int max_range = 2048;
	private final static int min_range = 1024;

	public MyObjectBackend()
	{
		Random random = new Random();
		int width = random.nextInt(max_range + 1) + min_range;
		int height = random.nextInt(max_range + 1) + min_range;
		array = new byte[width * height * 3];
	}
}
