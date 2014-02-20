package org.deuce.transaction.score.field;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 */
@ExcludeTM
public class ReadFieldAccess implements Serializable
{
	private static final long serialVersionUID = 1L;
	public TxField field;

	public ReadFieldAccess()
	{
	}

	public ReadFieldAccess(TxField field)
	{
		init(field);
	}

	public void init(TxField field)
	{
		this.field = field;
	}

	@Override
	public boolean equals(Object obj)
	{
		ReadFieldAccess other = (ReadFieldAccess) obj;
		return field == other.field;
	}

	@Override
	public int hashCode()
	{
		return field.hashCode();
	}

	public void clear()
	{
		field = null;
	}

	@Override
	public String toString()
	{
		return field.getMetadata().toString();
	}

	protected Object readResolve() throws ObjectStreamException
	{ /*
	 * When *de-serializing*, if the field is not group local, it will be null.
	 * When accessing read or write sets received through the network, we have
	 * to be careful with this, and need to do the null check (when traversing
	 * the readSet array or the writeSet hashSet).
	 */
		return field == null ? null : this;
	}

	public ObjectMetadata getDistMetadata()
	{ // Used for cache stuff (invalidation set construction)
		return field.getMetadata();
	}
}
