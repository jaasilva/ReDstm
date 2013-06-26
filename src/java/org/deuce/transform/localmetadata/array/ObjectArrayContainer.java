package org.deuce.transform.localmetadata.array;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationSerializer;
import org.deuce.distribution.replication.partial.oid.PartialReplicationOID;
import org.deuce.objectweb.asm.Type;
import org.deuce.reflection.AddressUtil;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.IContext;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class ObjectArrayContainer extends ArrayContainer
{
	public static final String NAME = Type
			.getInternalName(ObjectArrayContainer.class);
	public static final String DESC = Type
			.getDescriptor(ObjectArrayContainer.class);

	public static final String ARRAY_FIELD_DESC = Type
			.getDescriptor(Object[].class);
	public Object[] array;

	public TxField[] metadata;

	public static final String CTOR_DESC = String.format("(%s)%s",
			Type.getDescriptor(Object[].class) // arr
			, Type.VOID_TYPE.getDescriptor());

	public ObjectArrayContainer(Object[] arr)
	{
		super();
		array = arr;

		final int base = AddressUtil.arrayBaseOffset(Object[].class);
		final int scale = AddressUtil.arrayIndexScale(Object[].class);
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

				// XXX
				if (TribuDSTM.PARTIAL) {
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
				/*
				 * XXX t.vale: A instrumentação tem que passar mais informação
				 * ao init, nomeadamente o tipo! O João implementou este pedaço
				 * de código na instrumentação mas os metadados das células dos
				 * arrays são inicializados nesta classe. Se o init do TxField
				 * receber o tipo fica gerar para todos os algoritmos.
				 */
				if (TribuDSTM.PARTIAL && field instanceof VBoxField) {
					VBoxField vbox = (VBoxField) field;
					vbox.setType(Type.OBJECT);
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
