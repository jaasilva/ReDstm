package org.deuce.distribution.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class Cache
{
	private final ConcurrentHashMap<ObjectMetadata, Object> cache = new ConcurrentHashMap<ObjectMetadata, Object>(
			50000);

	public void put(ObjectMetadata metadata, Object obj)
	{
		cache.put(metadata, obj);
	}

	public Object get(ObjectMetadata metadata)
	{
		return cache.get(metadata);
	}

	public boolean contains(ObjectMetadata metadata)
	{
		return cache.contains(metadata);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Map.Entry<ObjectMetadata, Object> entry : cache.entrySet())
		{
			sb.append(String.format("%s=%s\n", entry.getKey(), entry.getValue()));
		}
		sb.append("}");
		return sb.toString();
	}
}
