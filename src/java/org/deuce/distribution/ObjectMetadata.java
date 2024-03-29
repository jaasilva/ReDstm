package org.deuce.distribution;

import java.io.Serializable;

import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

/**
 * Every distribution metadata class must implement this interface.
 * 
 * @author tvale
 */
@ExcludeTM
public interface ObjectMetadata extends Serializable
{
	public static final String NAME = Type
			.getInternalName(ObjectMetadata.class);
	public static final String DESC = Type.getDescriptor(ObjectMetadata.class);
}
