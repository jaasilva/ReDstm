package org.deuce.transform.localmetadata.type;

import java.io.ObjectStreamException;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.objectweb.asm.Type;
import org.deuce.reflection.UnsafeHolder;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;

/**
 * This is the base class for transactional metadata.
 * 
 * @author Tiago Vale
 */
@ExcludeTM
public class TxField implements UniqueObject
{
	private static final long serialVersionUID = -4736700297466670012L;

	public static final String DESC = Type.getDescriptor(TxField.class);
	public static final String NAME = Type.getInternalName(TxField.class);

	// Associated memory cell
	public Object ref;
	public long address;

	// Only if in a multiarray context
	public Object[] backend;
	public int index;

	private ObjectMetadata metadata;

	public TxField()
	{
	}

	public static final String CTOR_DESC = "(Ljava/lang/Object;J)V";

	public TxField(Object ref, long address)
	{
		init(ref, address);
	}

	public static final String CTOR_MULTIARRAY_DESC = String.format(
			"(%s%s%s)%s", Type.getType(Object[].class).getDescriptor(),
			Type.INT_TYPE.getDescriptor(), Type.getType(Object[].class)
					.getDescriptor(), Type.VOID_TYPE.getDescriptor());

	public TxField(Object[] containersRef, int idx, Object[] backend)
	{
		init(containersRef, idx, backend);
	}

	public void init(Object ref, long address)
	{
		this.ref = ref;
		this.address = address;
		backend = null;
		index = -1;
	}

	public void init(Object[] containersRef, int idx, Object[] backend)
	{
		init(containersRef, ContextDelegator.OBJECT_ARR_BASE
				+ ContextDelegator.OBJECT_ARR_SCALE * idx);
		this.backend = backend;
		index = idx;
	}

	public void writeBoolean(boolean value)
	{
		UnsafeHolder.getUnsafe().putBoolean(ref, address, value);
	}

	public boolean readBoolean()
	{
		return UnsafeHolder.getUnsafe().getBoolean(ref, address);
	}

	public void writeByte(byte value)
	{
		UnsafeHolder.getUnsafe().putByte(ref, address, value);
	}

	public byte readByte()
	{
		return UnsafeHolder.getUnsafe().getByte(ref, address);
	}

	public void writeChar(char value)
	{
		UnsafeHolder.getUnsafe().putChar(ref, address, value);
	}

	public char readChar()
	{
		return UnsafeHolder.getUnsafe().getChar(ref, address);
	}

	public void writeDouble(double value)
	{
		UnsafeHolder.getUnsafe().putDouble(ref, address, value);
	}

	public double readDouble()
	{
		return UnsafeHolder.getUnsafe().getDouble(ref, address);
	}

	public void writeFloat(float value)
	{
		UnsafeHolder.getUnsafe().putFloat(ref, address, value);
	}

	public float readFloat()
	{
		return UnsafeHolder.getUnsafe().getFloat(ref, address);
	}

	public void writeInt(int value)
	{
		UnsafeHolder.getUnsafe().putInt(ref, address, value);
	}

	public int readInt()
	{
		return UnsafeHolder.getUnsafe().getInt(ref, address);
	}

	public void writeLong(long value)
	{
		UnsafeHolder.getUnsafe().putLong(ref, address, value);
	}

	public long readLong()
	{
		return UnsafeHolder.getUnsafe().getLong(ref, address);
	}

	public void writeShort(short value)
	{
		UnsafeHolder.getUnsafe().putShort(ref, address, value);
	}

	public short readShort()
	{
		return UnsafeHolder.getUnsafe().getShort(ref, address);
	}

	public void writeObject(Object value)
	{
		UnsafeHolder.getUnsafe().putObject(ref, address, value);
	}

	public Object readObject()
	{
		return UnsafeHolder.getUnsafe().getObject(ref, address);
	}

	public void writeArray(ArrayContainer value)
	{
		UnsafeHolder.getUnsafe().putObject(ref, address, value);
		if (backend != null)
		{
			// If backend is not null, this is being used in a multiarray
			// context. Therefore, value is an instance of a sub-type of
			// ArrayContainer.
			backend[index] = value.getArray();
		}
	}

	public ArrayContainer readArray()
	{
		return (ArrayContainer) readObject();
	}

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
