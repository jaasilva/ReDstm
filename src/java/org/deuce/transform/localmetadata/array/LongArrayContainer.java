package org.deuce.transform.localmetadata.array;

import org.deuce.objectweb.asm.Type;
import org.deuce.reflection.AddressUtil;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.IContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class LongArrayContainer extends ArrayContainer
{
	public static final String NAME = Type
			.getInternalName(LongArrayContainer.class);
	public static final String DESC = Type
			.getDescriptor(LongArrayContainer.class);

	public static final String ARRAY_FIELD_DESC = Type
			.getDescriptor(long[].class);
	public long[] array;

	public TxField[] metadata;

	public static final String CTOR_DESC = String.format("(%s)%s",
			Type.getDescriptor(long[].class) // arr
			, Type.VOID_TYPE.getDescriptor());

	public LongArrayContainer(long[] arr)
	{
		array = arr;

		final int base = AddressUtil.arrayBaseOffset(long[].class);
		final int scale = AddressUtil.arrayIndexScale(long[].class);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				// TODO Auto-generated catch block
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
