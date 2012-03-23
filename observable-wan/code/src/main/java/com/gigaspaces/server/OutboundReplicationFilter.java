package com.gigaspaces.server;

import java.util.logging.Logger;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.cluster.IReplicationFilter;
import com.j_spaces.core.cluster.IReplicationFilterEntry;
import com.j_spaces.core.cluster.ReplicationPolicy;

/**
 * The purpose of this class ensure that non-domain objects (except TimeStamps) get
 * filtered out.  Other bookkeeping objects are created by the system, including 
 * TimeRecords and ReplicationStatus, but we don't want to replication those.
 * 
 * @author dfilppi
 *
 */
public class OutboundReplicationFilter implements IReplicationFilter{
	private static final Logger log=Logger.getLogger(OutboundReplicationFilter.class.getName());
	private IJSpace space;

	public void close() {
	}

	public void init(IJSpace space, String paramUrl, ReplicationPolicy policy) {
		this.space=space;
	}

	public void process(int direction, IReplicationFilterEntry entry, String targetName) {
		if((!entry.getClassName().equals(TimeStamp.class.getName()))&&(!entry.getClassName().equals(DomainObject.class.getName()))){
			entry.discard();
		}
	}

}
