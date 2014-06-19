package org.deuce.transaction;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class GroupsViolationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public GroupsViolationException(String string)
	{
		super(string);
	}
}
