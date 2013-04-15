package org.deuce.transaction.score.field;

import org.deuce.transaction.score.SCOReContext;
import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>, jaasilva
 */
@ExcludeTM
public class Version
{
	public volatile int version;
	public Version next;
	public int size;
	public Object value;

	public Version(int version, Object value, Version next)
	{
		this.version = version;
		this.next = next;
		this.value = value;
		this.size = next != null ? next.size + 1 : 1;
	}

	public Version get(int maxVersion)
	{
		Version res = this;
		while (res.version > maxVersion)
		{
			res = res.next;
			if (res == null)
			{ // com tamanho ilimitado isto acontece?
				throw SCOReContext.VERSION_UNAVAILABLE_EXCEPTION;
			}
		}
		return res;
	}
}
