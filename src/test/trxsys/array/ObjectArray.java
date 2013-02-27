package trxsys.array;

import org.deuce.reflection.UnsafeHolder;
import org.deuce.transform.localmetadata.array.ArrayContainer;

public class ObjectArray
{
	public static void main(String[] args)
	{
		Object[] a = new Object[2];
		a[0] = "string";
		a[1] = new int[2];
	}
}

abstract class TxField1<T>
{
	Object ref;
	long address;

	public TxField1(Object ref, long address)
	{
		this.ref = ref;
		this.address = address;
	}

	public abstract void write(T value);
}

interface TxField
{
	public void write(ArrayContainer value);

	public <T> void write(T value);

	public void write(boolean value);

	public void write(byte value);

	public void write(char value);

	public void write(double value);

	public void write(float value);

	public void write(int value);

	public void write(long value);

	public void write(short value);
}

class TxField2 implements TxField
{
	Object ref;
	long address;
	Object[] backend;
	int index;

	public TxField2(Object ref, long address)
	{
		this.ref = ref;
		this.address = address;
	}

	public void write(ArrayContainer value)
	{
		UnsafeHolder.getUnsafe().putObject(ref, address, value);
		backend[index] = value.getArray();
	}

	public <T> void write(T value)
	{
		UnsafeHolder.getUnsafe().putObject(ref, address, value);
	}

	public void write(boolean value)
	{
		UnsafeHolder.getUnsafe().putBoolean(ref, address, value);
	}

	public void write(byte value)
	{
		UnsafeHolder.getUnsafe().putByte(ref, address, value);
	}

	public void write(char value)
	{
		UnsafeHolder.getUnsafe().putChar(ref, address, value);
	}

	public void write(double value)
	{
		UnsafeHolder.getUnsafe().putDouble(ref, address, value);
	}

	public void write(float value)
	{
		UnsafeHolder.getUnsafe().putFloat(ref, address, value);
	}

	public void write(int value)
	{
		UnsafeHolder.getUnsafe().putInt(ref, address, value);
	}

	public void write(long value)
	{
		UnsafeHolder.getUnsafe().putLong(ref, address, value);
	}

	public void write(short value)
	{
		UnsafeHolder.getUnsafe().putShort(ref, address, value);
	}
}
