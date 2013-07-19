package org.deuce.transaction;

public class GroupsViolationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public GroupsViolationException(String string)
	{
		super(string);
	}
}
