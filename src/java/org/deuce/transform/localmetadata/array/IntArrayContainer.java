package org.deuce.transform.localmetadata.array;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationOID;
import org.deuce.distribution.replication.partial.PartialReplicationSerializer;
import org.deuce.objectweb.asm.Type;
import org.deuce.reflection.AddressUtil;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.IContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class IntArrayContainer extends ArrayContainer
{
	public static final String NAME = Type
			.getInternalName(IntArrayContainer.class);
	public static final String DESC = Type
			.getDescriptor(IntArrayContainer.class);

	public static final String ARRAY_FIELD_DESC = Type
			.getDescriptor(int[].class);
	public int[] array;

	public TxField[] metadata;

	public static final String CTOR_DESC = String.format("(%s)%s",
			Type.getDescriptor(int[].class) // arr
			, Type.VOID_TYPE.getDescriptor());

	public IntArrayContainer(int[] arr)
	{
		super();
		array = arr;

		final int base = AddressUtil.arrayBaseOffset(int[].class);
		final int scale = AddressUtil.arrayIndexScale(int[].class);
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

					if (field instanceof org.deuce.transaction.score.field.VBoxField)
					{
						org.deuce.transaction.score.field.VBoxField vbox = (org.deuce.transaction.score.field.VBoxField) field;
						vbox.setType(Type.INT);
					}
				}

				if (field instanceof org.deuce.transaction.mvstm.field.VBoxField)
				{
					org.deuce.transaction.mvstm.field.VBoxField vbox = (org.deuce.transaction.mvstm.field.VBoxField) field;
					vbox.setType(Type.INT);
				}

				metadata[i] = field;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public Object getArray()
	{
		return array;
	}

	@Override
	public Object getArray(IContext ctx)
	{
		return array;
	}
}
