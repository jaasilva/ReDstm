package org.deuce.transform.localmetadata.array;

import java.io.ObjectStreamException;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.objectweb.asm.Type;
import org.deuce.transaction.IContext;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public abstract class ArrayContainer implements UniqueObject
{
	private static final long serialVersionUID = 1L;

	public static final String NAME = Type
			.getInternalName(ArrayContainer.class);
	public static final String ARRAY_FIELD_NAME = "array";

	public static final String GETARRAY_METHOD_NAME = "getArray";
	public static final String GETARRAY_METHOD_DESC = "()"
			+ Type.getDescriptor(Object.class);

	public abstract Object getArray();

	public abstract Object getArray(IContext ctx);

	private ObjectMetadata metadata;

	public ObjectMetadata getMetadata()
	{
		return metadata;
	}

	public void setMetadata(ObjectMetadata metadata)
	{
		this.metadata = metadata;
	}

	public Object writeReplace() throws ObjectStreamException
	{
		return TribuDSTM.getObjectSerializer().writeReplaceHook(this);
	}

	public Object readResolve() throws ObjectStreamException
	{
		return TribuDSTM.getObjectSerializer().readResolveHook(this);
	}
}
