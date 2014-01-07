package org.deuce.distribution.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.transaction.score.field.Version;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class Cache
{
	private final ConcurrentHashMap<ObjectMetadata, Version> cache = new ConcurrentHashMap<ObjectMetadata, Version>(
			50000);

	public void put(ObjectMetadata metadata, Version obj)
	{
		cache.put(metadata, obj);
	}

	public Version get(ObjectMetadata metadata)
	{
		return cache.get(metadata);
	}

	public boolean contains(ObjectMetadata metadata)
	{
		return cache.containsKey(metadata);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Map.Entry<ObjectMetadata, Version> entry : cache.entrySet())
		{
			sb.append(String.format("%s=%s\n", entry.getKey(), entry.getValue()));
		}
		sb.append("}");
		return sb.toString();
	}
}
