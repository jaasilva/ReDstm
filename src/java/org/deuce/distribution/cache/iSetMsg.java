package org.deuce.distribution.cache;

import java.util.Set;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.replication.msgs.ControlMessage;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class iSetMsg extends ControlMessage
{
	private static final long serialVersionUID = 1L;
	public Set<ObjectMetadata> iSet;
	public int mostRecentSid;
	public int group;

	public iSetMsg(Set<ObjectMetadata> iSet, int mostRecentSid, int group)
	{
		this.iSet = iSet;
		this.mostRecentSid = mostRecentSid;
		this.group = group;
	}
}
