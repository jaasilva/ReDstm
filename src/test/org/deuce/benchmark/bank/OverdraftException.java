package org.deuce.benchmark.bank;

/**
 * @author Pascal Felber
 * @since 0.1
 */
public class OverdraftException extends Exception
{
	private static final long serialVersionUID = 1L;

	public OverdraftException()
	{
		super();
	}

	public OverdraftException(String reason)
	{
		super(reason);
	}
}
