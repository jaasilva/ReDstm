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
public class ByteArrayContainer extends ArrayContainer
{
	public static final String NAME = Type
			.getInternalName(ByteArrayContainer.class);
	public static final String DESC = Type
			.getDescriptor(ByteArrayContainer.class);

	public static final String ARRAY_FIELD_DESC = Type
			.getDescriptor(byte[].class);
	public byte[] array;

	public TxField[] metadata;

	public static final String CTOR_DESC = String.format("(%s)%s",
			Type.getDescriptor(byte[].class) // arr
			, Type.VOID_TYPE.getDescriptor());

	public ByteArrayContainer(byte[] arr)
	{
		super();
		array = arr;

		final int base = AddressUtil.arrayBaseOffset(byte[].class);
		final int scale = AddressUtil.arrayIndexScale(byte[].class);
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
				{ // XXX check arrays stuff partial rep.
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
				{ // XXX check arrays stuff partial rep.
					VBoxField vbox = (VBoxField) field;
					vbox.setType(Type.BYTE);
				}
				if (field instanceof org.deuce.transaction.mvstm.field.VBoxField)
				{
					org.deuce.transaction.mvstm.field.VBoxField vbox = (org.deuce.transaction.mvstm.field.VBoxField) field;
					vbox.setType(Type.BYTE);
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
