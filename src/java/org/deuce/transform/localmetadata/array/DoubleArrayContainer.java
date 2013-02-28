package org.deuce.transform.localmetadata.array;

import org.deuce.objectweb.asm.Type;
import org.deuce.reflection.AddressUtil;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.IContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class DoubleArrayContainer extends ArrayContainer
{
	public static final String NAME = Type
			.getInternalName(DoubleArrayContainer.class);
	public static final String DESC = Type
			.getDescriptor(DoubleArrayContainer.class);

	public static final String ARRAY_FIELD_DESC = Type
			.getDescriptor(double[].class);
	public double[] array;

	public TxField[] metadata;

	public static final String CTOR_DESC = String.format("(%s)%s",
			Type.getDescriptor(double[].class) // arr
			, Type.VOID_TYPE.getDescriptor());

	public DoubleArrayContainer(double[] arr)
	{
		array = arr;

		final int base = AddressUtil.arrayBaseOffset(double[].class);
		final int scale = AddressUtil.arrayIndexScale(double[].class);
		int length = arr.length;
		metadata = new TxField[length];
		for (int i = 0; i < length; i++)
		{
			Object obj;
			try
			{
				obj = ContextDelegator.getMetadataClass().newInstance();
				TxField field = (TxField) obj;
				field.init(arr, base + scale * i);
				metadata[i] = field;
			}
			catch (InstantiationException e)
			{
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

	public Object getArray()
	{
		return array;
	}

	public Object getArray(IContext ctx)
	{
		return array;
	}
}