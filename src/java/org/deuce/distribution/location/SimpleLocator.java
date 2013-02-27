package org.deuce.distribution.location;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.deuce.distribution.Locator;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.UniqueObject;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class SimpleLocator implements Locator
{
	private Map<ObjectMetadata, Reference<UniqueObject>> map = Collections
			.synchronizedMap(new WeakHashMap<ObjectMetadata, Reference<UniqueObject>>(
					50000));

	public void put(ObjectMetadata metadata, UniqueObject obj)
	{
		map.put(metadata, new SoftReference<UniqueObject>(obj));
	}

	public UniqueObject get(ObjectMetadata metadata)
	{
		Reference<UniqueObject> ref = map.get(metadata);
		return (ref != null ? ref.get() : null);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Map.Entry<ObjectMetadata, Reference<UniqueObject>> entry : map
				.entrySet())
		{
			sb.append(String.format("%s=%s, ", entry.getKey(), entry.getValue()
					.get()));
		}
		sb.append("}");
		return sb.toString();
	}
}
