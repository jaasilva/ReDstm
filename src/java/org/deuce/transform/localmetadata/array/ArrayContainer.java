package org.deuce.transform.localmetadata.array;

import java.io.ObjectStreamException;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.partial.PartialReplicationSerializer;
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

	public ArrayContainer()
	{
		if (TribuDSTM.PARTIAL)
		{ // XXX check arrays stuff partial rep.
			final PartialReplicationSerializer s = (PartialReplicationSerializer) TribuDSTM
					.getObjectSerializer();
			s.createFullReplicationMetadata(this);
		}
	}

	public abstract Object getArray();

	public abstract Object getArray(IContext ctx);

	private ObjectMetadata metadata;

	@Override
	public ObjectMetadata getMetadata()
	{
		return metadata;
	}

	@Override
	public void setMetadata(ObjectMetadata metadata)
	{
		this.metadata = metadata;
	}

	@Override
	public Object writeReplace() throws ObjectStreamException
	{
		return TribuDSTM.getObjectSerializer().writeReplaceHook(this);
	}

	@Override
	public Object readResolve() throws ObjectStreamException
	{
		return TribuDSTM.getObjectSerializer().readResolveHook(this);
	}
}
