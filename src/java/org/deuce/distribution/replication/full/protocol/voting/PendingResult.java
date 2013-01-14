package org.deuce.distribution.replication.full.protocol.voting;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class PendingResult {
	public Address src;
	public ResultMessage msg;

	public PendingResult(Address src, ResultMessage msg) {
		this.src = src;
		this.msg = msg;
	}
}
