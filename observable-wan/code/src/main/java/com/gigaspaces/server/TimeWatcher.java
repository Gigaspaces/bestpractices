package com.gigaspaces.server;

import java.util.logging.Logger;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.transaction.manager.LocalJiniTxManagerConfigurer;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.PlatformTransactionManager;

import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.core.client.SQLQuery;
import com.j_spaces.core.client.UpdateModifiers;

/**
 * The purpose of this class is to detect the arrival
 * of TimeStamp objects on the replication stream,
 * and update the related TimeRecord object in the space.
 * Each remote site has it's own TimeRecord object.
 * 
 * @author dfilppi
 *
 */
public class TimeWatcher {
	private static final Logger log=Logger.getLogger(TimeWatcher.class.getName());
	private static final SQLQuery query=new SQLQuery(TimeStamp.class,"location <> ?");
	private GigaSpace space;
	private SimplePollingEventListenerContainer pec=null;
	private String location;

	//init-method
	public void init() throws Exception{
		query.setParameters(getLocation());
		PlatformTransactionManager tm=new LocalJiniTxManagerConfigurer(space.getSpace()).transactionManager();
		pec=new SimplePollingContainerConfigurer(space)
			.template(query)
			.transactionManager(tm)
			.eventListenerAnnotation(new Object(){
				@SpaceDataEvent
				public TimeStamp eventListener(TimeStamp event){
					TimeRecord lt=getSpace().readById(TimeRecord.class,event.getLocation(),1,100,ReadModifiers.EXCLUSIVE_READ_LOCK);
					if(lt==null){
						lt=new TimeRecord(event.getLocation());
					}
					lt.setTime(event);
					getSpace().write(lt,LeaseContext.FOREVER,1000L,UpdateModifiers.UPDATE_OR_WRITE|UpdateModifiers.NO_RETURN_VALUE);
					return null;
				}
			}).pollingContainer();
	}
	
	//destroy-method
	public void finished(){
		pec.destroy();
	}

	@Required
	public void setSpace(GigaSpace space) {
		this.space = space;
	}

	public GigaSpace getSpace() {
		return space;
	}

	@Required
	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}
}
