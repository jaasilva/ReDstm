package org.deuce.distribution.location;

import java.lang.ref.*;
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

	@Override
	public void put(ObjectMetadata metadata, UniqueObject obj)
	{ // CHECKME
		map.put(metadata, new SoftReference<UniqueObject>(obj));
		//map.put(metadata, new WeakReference<UniqueObject>(obj));
	}

	@Override
	public UniqueObject get(ObjectMetadata metadata)
	{
		Reference<UniqueObject> ref = map.get(metadata);
		return (ref != null ? ref.get() : null);
	}
}
