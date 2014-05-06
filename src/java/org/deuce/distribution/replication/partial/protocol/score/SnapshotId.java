package org.deuce.distribution.replication.partial.protocol.score;

/**
 * @author jaasilva
 */
public class SnapshotId implements Comparable<SnapshotId>
{
	public int version;
	public int nodeId;

	public SnapshotId(int version, int nodeId)
	{
		this.version = version;
		this.nodeId = nodeId;
	}

	@Override
	public int compareTo(SnapshotId other)
	{
		if (other == null || getClass() != other.getClass())
			return 1;

		if (this.version == other.version)
		{
			if (this.nodeId == other.nodeId)
				return 0;
			else if (this.nodeId < other.nodeId)
				return -1;
			else
				return 1;
		}
		else if (this.version < other.version)
			return -1;
		else
			return 1;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		SnapshotId other = (SnapshotId) obj;
		if (nodeId != other.nodeId)
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "[ver:" + version + ",node:" + nodeId + "]";
	}
}
