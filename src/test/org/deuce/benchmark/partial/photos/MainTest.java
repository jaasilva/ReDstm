package org.deuce.benchmark.partial.photos;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MainTest
{

	public static void main(String[] args) throws InterruptedException
	{
		int size = 10000;
		BufferedImage[] images = new BufferedImage[size];
		// ArrayList<byte[]> images = new ArrayList<byte[]>();

		for (int i = 0; i < size; i++)
		{
			images[i] = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
			// images.add(new byte[65536]);

			if (i % 100 == 0)
			{
				memStats();
				Thread.sleep(2000);
			}
		}
	}

	static void memStats()
	{
		int mb = 1024 * 1024;

		// Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();

		System.out.println();
		System.out.println("##### Heap utilization statistics [MB] #####");

		// Print used memory
		System.out.println("Used Memory:"
				+ (runtime.totalMemory() - runtime.freeMemory()) / mb);

		// Print free memory
		System.out.println("Free Memory:" + runtime.freeMemory() / mb);

		// Print total available memory
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);

		// Print Maximum available memory
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);
		System.out.println();
	}
}
