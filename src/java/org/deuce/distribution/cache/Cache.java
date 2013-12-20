package org.deuce.distribution.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.UniqueObject;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class Cache
{
	private final ConcurrentHashMap<ObjectMetadata, UniqueObject> cache = new ConcurrentHashMap<ObjectMetadata, UniqueObject>(
			50000);

	public void put(ObjectMetadata metadata, UniqueObject obj)
	{
		cache.put(metadata, obj);
	}

	public UniqueObject get(ObjectMetadata metadata)
	{
		return cache.get(metadata);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Map.Entry<ObjectMetadata, UniqueObject> entry : cache.entrySet())
		{
			sb.append(String.format("%s=%s, ", entry.getKey(), entry.getValue()));
		}
		sb.append("}");
		return sb.toString();
	}

}
