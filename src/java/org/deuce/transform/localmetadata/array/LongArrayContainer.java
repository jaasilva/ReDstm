package org.deuce.transform.localmetadata.array;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationOID;
import org.deuce.distribution.replication.partial.PartialReplicationSerializer;
import org.deuce.objectweb.asm.Type;
import org.deuce.reflection.AddressUtil;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.IContext;
import org.deuce.transaction.score.field.VBoxField;
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
		super();
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

				if (TribuDSTM.PARTIAL)
				{
					final PartialReplicationSerializer s = (PartialReplicationSerializer) TribuDSTM
							.getObjectSerializer();
					s.createFullReplicationMetadata(field);
					final Group g = ((PartialReplicationOID) this.getMetadata())
							.getGroup();
					final PartialReplicationOID field_metadata = (PartialReplicationOID) field
							.getMetadata();
					field_metadata.setGroup(g);
					field_metadata.setPartialGroup(g);
				}
				if (TribuDSTM.PARTIAL && field instanceof VBoxField)
				{
					VBoxField vbox = (VBoxField) field;
					vbox.setType(Type.LONG);
				}
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
