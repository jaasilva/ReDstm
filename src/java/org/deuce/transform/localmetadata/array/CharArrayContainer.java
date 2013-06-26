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
public class CharArrayContainer extends ArrayContainer
{
	public static final String NAME = Type
			.getInternalName(CharArrayContainer.class);
	public static final String DESC = Type
			.getDescriptor(CharArrayContainer.class);

	public static final String ARRAY_FIELD_DESC = Type
			.getDescriptor(char[].class);
	public char[] array;

	public TxField[] metadata;

	public static final String CTOR_DESC = String.format("(%s)%s",
			Type.getDescriptor(char[].class) // arr
			, Type.VOID_TYPE.getDescriptor());

	public CharArrayContainer(char[] arr)
	{
		super();
		array = arr;

		final int base = AddressUtil.arrayBaseOffset(char[].class);
		final int scale = AddressUtil.arrayIndexScale(char[].class);
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
					vbox.setType(Type.CHAR);
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
